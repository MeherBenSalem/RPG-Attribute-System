#!/usr/bin/env python3
"""Read-only provenance for publisher-only recovery of the immutable09a release.

Separate helper code identity from source/JAR identity. Reuse original binaries
and manifest; require the latest attempted original-or-helper publication journal.
Existing workflow token only, fixed GitHub GETs, no credential/response logging.
"""
import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import urllib.error
import urllib.request

spec = importlib.util.spec_from_file_location('original_recovery', Path(__file__).with_name('validate-release-recovery.py'))
original = importlib.util.module_from_spec(spec)
spec.loader.exec_module(original)
HELPER_WORKFLOW = '.github/workflows/recover-430-publication.yml'
HELPER_NAME = 'Recover RAS 4.3.0 publication'
HELPER_STEP = 'Publish exact verified original binaries'
MAX_BYTES = 12 * 1024 * 1024


def request(path):
    token = os.environ.get('GH_TOKEN')
    if not token:
        raise ValueError('GitHub read token required')
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, headers, newurl):
            return None
    req = urllib.request.Request('https://api.github.com/repos/' + original.REPOSITORY + path,
        method='GET', headers={'Authorization': 'Bearer ' + token, 'Accept': 'application/vnd.github+json',
                              'X-GitHub-Api-Version': '2022-11-28', 'User-Agent': 'NightBeam-RAS-publisher-recovery'})
    try:
        with urllib.request.build_opener(NoRedirect).open(req, timeout=30) as response:
            body = response.read(MAX_BYTES + 1)
            if len(body) > MAX_BYTES:
                raise ValueError('GitHub provenance response exceeds safe bound')
            return json.loads(body)
    except Exception:
        raise ValueError('GitHub provenance GET failed or returned malformed/oversized data') from None


original.request = request


def load_lock():
    lock = json.loads(Path(__file__).with_name('release-430-recovery-lock.json').read_text())
    if lock.get('schema_version') != 1 or lock.get('repository') != original.REPOSITORY \
            or lock.get('source_commit') != '09a559f500daa21ce6dbc60d80ea0b490a605915' \
            or lock.get('release_version') != '4.3.0' or lock.get('source_run_id') != 38053545236 \
            or lock.get('minimum_receipt_run_id') != 38054427839:
        raise ValueError('Recovery lock identity differs')
    return lock


def validate_identity(run, lock, *, current_helper=None):
    if not isinstance(run, dict) or run.get('repository', {}).get('full_name') != lock['repository'] \
            or run.get('repository', {}).get('id') != lock['repository_id'] \
            or run.get('head_repository', {}).get('id') != lock['repository_id'] \
            or run.get('actor', {}).get('id') != lock['owner_id']:
        raise ValueError('Foreign/fork/non-owner recovery run')
    if run.get('path') == original.WORKFLOW:
        original.validate_run(run, lock['source_commit'])
    elif run.get('path') == HELPER_WORKFLOW:
        if run.get('name') != HELPER_NAME or run.get('event') != 'workflow_dispatch' \
                or run.get('head_branch') != 'main' or not re.fullmatch(r'[0-9a-f]{40}', run.get('head_sha', '')):
            raise ValueError('Publisher helper workflow/identity differs')
        if current_helper:
            if run.get('head_sha') != current_helper:
                raise ValueError('Current helper is not exact reviewed commit')
        elif run.get('status') != 'completed':
            raise ValueError('Receipt helper run must be completed')
    else:
        raise ValueError('Unsupported recovery workflow')


def artifact_list(payload):
    if not isinstance(payload, dict) or not isinstance(payload.get('artifacts'), list) \
            or type(payload.get('total_count')) is not int or payload['total_count'] != len(payload['artifacts']) \
            or payload['total_count'] > 100:
        raise ValueError('Incomplete/malformed recovery artifact list')
    return payload['artifacts']


def validate_artifact(artifacts, run, expected, lock):
    matches = [row for row in artifacts if isinstance(row, dict) and row.get('name') == expected['name']]
    if len(matches) != 1:
        raise ValueError('Missing/ambiguous recovery artifact')
    row = matches[0]
    workflow = row.get('workflow_run', {})
    if type(row.get('id')) is not int or row['id'] <= 0 or row.get('expired') is not False \
            or workflow.get('id') != run['id'] or workflow.get('head_sha') != run['head_sha'] \
            or workflow.get('repository_id') != lock['repository_id'] or workflow.get('head_repository_id') != lock['repository_id'] \
            or not re.fullmatch(r'sha256:[0-9a-f]{64}', row.get('digest', '')) \
            or ('id' in expected and row['id'] != expected['id']) \
            or ('digest' in expected and row['digest'] != expected['digest']):
        raise ValueError('Recovery artifact immutable identity/digest differs')
    return {key: row[key] for key in ['id', 'name', 'digest']}


def helper_may_have_published(payload):
    jobs = payload.get('jobs') if isinstance(payload, dict) else None
    if not isinstance(jobs, list):
        raise ValueError('Malformed helper job history')
    for job in jobs:
        if not isinstance(job, dict) or not isinstance(job.get('name'), str) or not isinstance(job.get('status'), str):
            raise ValueError('Malformed helper job')
        if job['name'] != 'recover':
            continue
        if job['status'] != 'completed':
            return True
        if not isinstance(job.get('steps'), list):
            raise ValueError('Malformed helper steps')
        for step in job['steps']:
            if not isinstance(step, dict) or not isinstance(step.get('name'), str):
                raise ValueError('Malformed helper step')
            if step['name'] == HELPER_STEP and step.get('conclusion') != 'skipped':
                return True
    return False


def latest_receipt(runs, jobs, current_id, receipt_id):
    writers = []
    for run in runs:
        if str(run.get('id')) == str(current_id):
            continue
        if run.get('path') not in [original.WORKFLOW, HELPER_WORKFLOW]:
            raise ValueError('Unexpected publication history workflow')
        wrote = original.may_have_published(jobs.get(str(run.get('id')))) if run['path'] == original.WORKFLOW \
            else helper_may_have_published(jobs.get(str(run.get('id'))))
        if wrote:
            if run.get('status') != 'completed':
                raise ValueError('Parallel/incomplete publication needs reconciliation')
            writers.append(run)
    if not writers:
        raise ValueError('No attempted publication journal found')
    latest = max(writers, key=lambda run: (run.get('created_at', ''), int(run.get('id', 0))))
    if str(latest['id']) != str(receipt_id):
        raise ValueError('Stale journal; restore latest original-or-helper publication receipt')


def history(path):
    payload = request(path)
    runs = payload.get('workflow_runs') if isinstance(payload, dict) else None
    count = payload.get('total_count') if isinstance(payload, dict) else None
    if not isinstance(runs, list) or type(count) is not int or count != len(runs) or count >= 100:
        raise ValueError('Cannot prove complete bounded publication history')
    ids = [run.get('id') for run in runs if isinstance(run, dict)]
    if len(ids) != len(runs) or any(type(value) is not int or value <= 0 for value in ids) or len(ids) != len(set(ids)):
        raise ValueError('Malformed/duplicate publication run IDs')
    return runs


def validate(helper_commit, receipt_run_id, current_run_id, lock):
    current = request('/actions/runs/' + current_run_id)
    validate_identity(current, lock, current_helper=helper_commit)
    if str(current.get('id')) != current_run_id or current.get('path') != HELPER_WORKFLOW or current.get('run_attempt') != 1:
        raise ValueError('Only fresh exact publisher recovery dispatches are permitted')
    tag = request('/git/ref/tags/' + lock['tag'])
    if tag.get('ref') != 'refs/tags/' + lock['tag'] or tag.get('object', {}).get('sha') != lock['source_commit'] \
            or tag.get('object', {}).get('type') != 'commit':
        raise ValueError('Immutable release tag differs')
    source = request('/actions/runs/' + str(lock['source_run_id']))
    validate_identity(source, lock)
    if source.get('id') != lock['source_run_id']:
        raise ValueError('Original source run differs')
    original.validate_build_jobs(original.read_jobs(lock['source_run_id'], attempt=1))
    artifacts = artifact_list(request('/actions/runs/' + str(lock['source_run_id']) + '/artifacts?per_page=100'))
    sources = [validate_artifact(artifacts, source, expected, lock) for expected in lock['source_artifacts']]
    source_provenance = validate_artifact(artifacts, source, lock['source_provenance'], lock)
    old = history('/actions/workflows/publish.yml/runs?head_sha=' + lock['source_commit'] + '&per_page=100')
    helpers = history('/actions/workflows/recover-430-publication.yml/runs?per_page=100')
    runs = old + helpers
    jobs = {str(run['id']): original.read_jobs(run['id']) for run in runs if str(run['id']) != current_run_id}
    latest_receipt(runs, jobs, current_run_id, receipt_run_id)
    receipt = request('/actions/runs/' + receipt_run_id)
    validate_identity(receipt, lock)
    if str(receipt.get('id')) != receipt_run_id or int(receipt_run_id) < lock['minimum_receipt_run_id']:
        raise ValueError('Cannot restore original empty or pre-Modrinth journal')
    expected = next((row for row in lock['known_receipts'] if str(row['run_id']) == receipt_run_id), None)
    if expected is None:
        expected = {'name': ('release-provenance-' if receipt['path'] == original.WORKFLOW else 'publisher-recovery-provenance-') + lock['source_commit']}
    prior = validate_artifact(artifact_list(request('/actions/runs/' + receipt_run_id + '/artifacts?per_page=100')), receipt, expected, lock)
    return {'schema_version': 1, 'repository': lock['repository'], 'source_commit': lock['source_commit'],
            'release_version': lock['release_version'], 'helper_commit': helper_commit, 'current_run_id': int(current_run_id),
            'source_run_id': lock['source_run_id'], 'source_artifacts': sources, 'source_provenance': source_provenance,
            'receipt_run_id': int(receipt_run_id), 'receipt_helper_commit': receipt['head_sha'] if receipt['path'] == HELPER_WORKFLOW else None,
            'receipt_artifact': prior, 'receipt_input_verified': False}


def prepare(original_directory, prior_directory, provenance, lock):
    original_manifest = Path(original_directory, 'verified-release.json').read_bytes()
    if hashlib.sha256(original_manifest).hexdigest() != lock['original_manifest_sha256']:
        raise ValueError('Original09a manifest immutable digest differs')
    manifest = json.loads(original_manifest)
    prior_manifest = json.loads(Path(prior_directory, 'verified-release.json').read_text())
    if prior_manifest != manifest:
        raise ValueError('Latest receipt manifest differs from original09a inventory')
    journal_bytes = Path(prior_directory, 'publication-receipts.json').read_bytes()
    journal = json.loads(journal_bytes)
    if journal.get('source_commit') != lock['source_commit'] or journal.get('version') != lock['release_version'] \
            or journal.get('schema_version') != 1 or not isinstance(journal.get('uploads'), list):
        raise ValueError('Latest journal source/version/schema differs')
    mr = [row for row in journal['uploads'] if isinstance(row, dict) and row.get('platform') == 'modrinth']
    expected = {row['file_name']: row['sha256'] for row in manifest['files']}
    if len(mr) != 10 or {row.get('file') for row in mr} != set(expected) or any(
            row.get('sha256') != expected.get(row.get('file')) or row.get('status') not in ['verified', 'verified-existing'] for row in mr):
        raise ValueError('Latest journal must preserve all ten verified Modrinth entries')
    if provenance['receipt_helper_commit'] is not None:
        prior_provenance = json.loads(Path(prior_directory, 'recovery-provenance.json').read_text())
        if prior_provenance.get('source_commit') != lock['source_commit'] \
                or prior_provenance.get('helper_commit') != provenance['receipt_helper_commit'] \
                or prior_provenance.get('current_run_id') != provenance['receipt_run_id'] \
                or prior_provenance.get('receipt_input_verified') is not True:
            raise ValueError('Prior helper/source/journal provenance differs')
    provenance['receipt_input_sha256'] = hashlib.sha256(journal_bytes).hexdigest()
    provenance['receipt_input_verified'] = True
    Path('verified-release.json').write_bytes(original_manifest)
    Path('publication-receipts.json').write_bytes(journal_bytes)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--helper-commit', required=True)
    parser.add_argument('--receipt-run-id', required=True)
    parser.add_argument('--current-run-id', required=True)
    parser.add_argument('--prepare', action='store_true')
    parser.add_argument('--original-directory', default='original-release-provenance')
    parser.add_argument('--prior-directory', default='prior-release-provenance')
    args = parser.parse_args()
    if not re.fullmatch(r'[0-9a-f]{40}', args.helper_commit) or any(
            not re.fullmatch(r'[1-9]\d*', value) for value in [args.receipt_run_id, args.current_run_id]):
        raise ValueError('Invalid recovery helper or run identity')
    lock = load_lock()
    if args.prepare:
        provenance = json.loads(Path('recovery-provenance.json').read_text())
        if provenance.get('helper_commit') != args.helper_commit or provenance.get('current_run_id') != int(args.current_run_id) \
                or provenance.get('receipt_run_id') != int(args.receipt_run_id):
            raise ValueError('Current recovery provenance differs')
        prepare(args.original_directory, args.prior_directory, provenance, lock)
    else:
        provenance = validate(args.helper_commit, args.receipt_run_id, args.current_run_id, lock)
        output = os.environ.get('GITHUB_OUTPUT')
        if output:
            with open(output, 'a') as handle:
                print('receipt_artifact_id=' + str(provenance['receipt_artifact']['id']), file=handle)
    Path('recovery-provenance.json').write_text(json.dumps(provenance, indent=2) + '\n')
    print('PASS immutable09a source/tag/artifacts and exact helper/latest journal provenance')


if __name__ == '__main__':
    try:
        main()
    except Exception:
        print('Publisher recovery provenance failed; no store write is permitted.')
        raise SystemExit(1)
