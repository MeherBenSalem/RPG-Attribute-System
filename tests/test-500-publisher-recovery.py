#!/usr/bin/env python3
"""Offline provenance, journal and workflow tests for the RAS 5.0.0 CurseForge recovery helper."""
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import yaml

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('helper500', ROOT / 'scripts/validate-500-publisher-recovery.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
helper = module.recovery
LOCK = helper.load_lock()
HELPER = 'b' * 40
CURRENT = 40000000000
SOURCE = LOCK['source_run_id']
RECEIPT = LOCK['minimum_receipt_run_id']
MANIFEST = ROOT / 'tests/fixtures/verified-release-500.json'
REJECTED = {'schema_version': 1, 'stage': 'http-status', 'method': 'POST', 'http_status': 400,
            'response_content_type': 'application/json', 'response_bytes': 308, 'response_json_type': 'object',
            'response_id_type': 'absent', 'response_has_error': False, 'response_has_message': False,
            'error_type': 'Error', 'transport_code': None, 'transmission_outcome': 'unknown'}


class Recovery500Tests(unittest.TestCase):
    def run_payload(self, run_id, *, recovery=False, current=False):
        return {'id': run_id, 'repository': {'full_name': LOCK['repository'], 'id': LOCK['repository_id']},
                'head_repository': {'id': LOCK['repository_id']}, 'actor': {'id': LOCK['owner_id']},
                'head_sha': HELPER if recovery else LOCK['source_commit'],
                'path': helper.HELPER_WORKFLOW if recovery else helper.original.WORKFLOW,
                'name': helper.HELPER_NAME if recovery else 'Publish', 'head_branch': 'main' if recovery else 'v5.0.0',
                'event': 'workflow_dispatch' if recovery else 'push', 'status': 'in_progress' if current else 'completed',
                'run_attempt': 1, 'created_at': f'2026-10-22T07:{17 if run_id == SOURCE else 50}:00Z'}

    def job(self, job_id, *, recovery=False, wrote=True):
        return {'id': job_id, 'name': 'recover' if recovery else 'publish', 'status': 'completed',
                'steps': [{'name': helper.HELPER_STEP if recovery else 'Publish/resume verified binaries with per-loader dependencies',
                           'conclusion': 'failure' if wrote else 'skipped'}]}

    def artifact(self, expected, run):
        return {**{k: v for k, v in expected.items() if k != 'run_id'}, 'expired': False,
                'workflow_run': {'id': run['id'], 'head_sha': run['head_sha'],
                                 'repository_id': LOCK['repository_id'], 'head_repository_id': LOCK['repository_id']}}

    def payloads(self):
        current, source = self.run_payload(CURRENT, recovery=True, current=True), self.run_payload(SOURCE)
        artifacts = [self.artifact(row, source) for row in LOCK['source_artifacts'] + [LOCK['source_provenance']]]
        payloads = {
            '/actions/runs/' + str(CURRENT): current,
            '/actions/runs/' + str(SOURCE): source,
            '/git/ref/tags/v5.0.0': {'ref': 'refs/tags/v5.0.0', 'object': {'sha': LOCK['source_commit'], 'type': 'commit'}},
            '/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100': {'total_count': len(artifacts), 'artifacts': artifacts},
            '/actions/workflows/publish.yml/runs?head_sha=' + LOCK['source_commit'] + '&per_page=100': {'total_count': 1, 'workflow_runs': [source]},
            '/actions/workflows/recover-500-publication.yml/runs?per_page=100': {'total_count': 1, 'workflow_runs': [current]},
        }
        jobs = {str(SOURCE): {'jobs': [self.job(1)]}}
        builds = {'jobs': [{'name': 'Build ' + workspace, 'status': 'completed', 'conclusion': 'success',
                            'steps': [{'name': 'Build both loaders and common checks at exact source commit', 'conclusion': 'success'}]}
                           for workspace in helper.original.WORKSPACES]}
        def read_jobs(run_id, attempt=None):
            return builds if attempt == 1 else jobs[str(run_id)]
        return payloads, read_jobs

    def validate(self, payloads, read_jobs, receipt=RECEIPT):
        with patch.object(helper, 'request', side_effect=lambda path: payloads[path]), \
                patch.object(helper.original, 'read_jobs', side_effect=read_jobs):
            return helper.validate(HELPER, str(receipt), str(CURRENT), LOCK)

    def test_lock_pins_the_tagged_run_artifacts_and_manifest(self):
        self.assertEqual(LOCK['source_commit'], 'c8e06b8412c7e4ee6c7985411c14a73f032549af')
        self.assertEqual(LOCK['tag'], 'v5.0.0')
        self.assertEqual(len(LOCK['source_artifacts']), 5)
        self.assertEqual({row['name'].rsplit('-', 1)[1] for row in LOCK['source_artifacts']}, set(helper.original.WORKSPACES))
        self.assertEqual(hashlib.sha256(MANIFEST.read_bytes()).hexdigest(), LOCK['original_manifest_sha256'])
        self.assertEqual(helper.HELPER_WORKFLOW, '.github/workflows/recover-500-publication.yml')
        with patch.object(Path, 'read_text', return_value=json.dumps({**LOCK, 'release_version': '5.0.1'})):
            with self.assertRaises(ValueError): module.load_lock()

    def test_valid_tagged_run_is_the_latest_journal(self):
        payloads, read_jobs = self.payloads()
        result = self.validate(payloads, read_jobs)
        self.assertEqual(result['release_version'], '5.0.0')
        self.assertEqual(result['receipt_artifact']['id'], LOCK['source_provenance']['id'])
        self.assertIsNone(result['receipt_helper_commit'])

    def test_changed_tag_source_actor_digest_or_stale_journal_fail_closed(self):
        mutations = {
            'tag': lambda p: p['/git/ref/tags/v5.0.0']['object'].update(sha='f' * 40),
            'annotated tag': lambda p: p['/git/ref/tags/v5.0.0']['object'].update(type='tag'),
            'helper branch': lambda p: p['/actions/runs/' + str(CURRENT)].update(head_branch='feature'),
            'helper rerun': lambda p: p['/actions/runs/' + str(CURRENT)].update(run_attempt=2),
            'source': lambda p: p['/actions/runs/' + str(SOURCE)].update(head_sha='f' * 40),
            'source actor': lambda p: p['/actions/runs/' + str(SOURCE)]['actor'].update(id=1),
            'source digest': lambda p: p['/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100']['artifacts'][0].update(digest='sha256:' + 'f' * 64),
            'newer helper journal': lambda p: p['/actions/workflows/recover-500-publication.yml/runs?per_page=100'].update(
                total_count=2, workflow_runs=[p['/actions/runs/' + str(CURRENT)], self.run_payload(CURRENT - 1, recovery=True)]),
        }
        for name, mutate in mutations.items():
            with self.subTest(name=name):
                payloads, read_jobs = self.payloads(); mutate(payloads)
                if name == 'newer helper journal':
                    base = read_jobs
                    read_jobs = lambda run_id, attempt=None: {'jobs': [self.job(9, recovery=True)]} \
                        if str(run_id) == str(CURRENT - 1) and attempt is None else base(run_id, attempt)
                with self.assertRaises(ValueError): self.validate(payloads, read_jobs)

    def directories(self):
        temp = tempfile.TemporaryDirectory(); self.addCleanup(temp.cleanup)
        root = Path(temp.name); old, prior = root / 'original', root / 'prior'; old.mkdir(); prior.mkdir()
        manifest = MANIFEST.read_bytes()
        (old / 'verified-release.json').write_bytes(manifest); (prior / 'verified-release.json').write_bytes(manifest)
        files = json.loads(manifest)['files']
        journal = {'schema_version': 1, 'source_commit': LOCK['source_commit'], 'version': '5.0.0', 'publicly_verified': False,
                   'uploads': [{'platform': 'modrinth', 'file': row['file_name'], 'sha256': row['sha256'], 'status': 'verified', 'id': 'offline'}
                               for row in files]
                   + [{'platform': 'curseforge', 'file': files[0]['file_name'], 'sha256': files[0]['sha256'],
                       'status': 'uncertain', 'request_diagnostic': REJECTED}]}
        return root, old, prior, journal

    def test_prepare_restores_the_exact_manifest_and_journal(self):
        root, old, prior, journal = self.directories()
        text = json.dumps(journal, indent=2) + '\n'; (prior / 'publication-receipts.json').write_text(text)
        provenance = {'receipt_helper_commit': None, 'receipt_run_id': RECEIPT}
        cwd = os.getcwd()
        try:
            os.chdir(root); helper.prepare(old, prior, provenance, LOCK)
            self.assertEqual(Path('publication-receipts.json').read_text(), text)
            self.assertEqual(Path('verified-release.json').read_bytes(), MANIFEST.read_bytes())
            self.assertTrue(provenance['receipt_input_verified'])
        finally: os.chdir(cwd)

    def test_changed_manifest_or_missing_modrinth_rows_fail_closed(self):
        for mutation in ['changed manifest', 'missing MR', 'unverified MR', 'wrong version']:
            with self.subTest(mutation=mutation):
                root, old, prior, journal = self.directories()
                if mutation == 'changed manifest': (old / 'verified-release.json').write_text('{}')
                elif mutation == 'missing MR': journal['uploads'].pop(0)
                elif mutation == 'unverified MR': journal['uploads'][0]['status'] = 'pending-publication'
                else: journal['version'] = '4.3.0'
                (prior / 'publication-receipts.json').write_text(json.dumps(journal))
                with self.assertRaises(ValueError):
                    helper.prepare(old, prior, {'receipt_helper_commit': None, 'receipt_run_id': RECEIPT}, LOCK)

    def test_workflow_is_owner_only_read_only_by_default_and_posts_curseforge_only(self):
        source = (ROOT / helper.HELPER_WORKFLOW).read_text(); workflow = yaml.safe_load(source)
        inputs = (workflow.get('on') or workflow[True])['workflow_dispatch']['inputs']
        self.assertIs(inputs['dry_run']['default'], True)
        self.assertEqual(inputs['receipt_run_id']['default'], str(RECEIPT))
        self.assertEqual(workflow['concurrency']['group'], 'ras-release-publication')
        self.assertEqual(workflow['permissions'], {'contents': 'read', 'actions': 'read'})
        self.assertIn("github.actor_id == '74470806'", workflow['jobs']['recover']['if'])
        self.assertNotIn('gradlew', source)
        job = workflow['jobs']['recover']; steps = job['steps']; names = [row.get('name') for row in steps]
        self.assertEqual(job['env']['SOURCE_COMMIT'], LOCK['source_commit'])
        self.assertEqual(job['env']['SOURCE_RUN_ID'], str(SOURCE))
        for earlier in ['Reject unreviewed helper identity and every job rerun', 'Restore exact original manifest and mandatory latest journal',
                        'Reverify unchanged complete original c8e06b8 binaries and metadata',
                        'Preflight both exact store inventories and strict real catalog IDs']:
            self.assertLess(names.index(earlier), names.index(helper.HELPER_STEP))
        preflight = steps[names.index('Preflight both exact store inventories and strict real catalog IDs')]
        self.assertIn('--require-public-modrinth', preflight['run'])
        self.assertIn("before.equals(fs.readFileSync('publication-receipts.json'))", preflight['run'])
        self.assertIn('--retry-rejected-curseforge', preflight['run'])
        live = steps[names.index(helper.HELPER_STEP)]
        self.assertEqual(live['if'], '${{ inputs.dry_run == false }}')
        self.assertNotIn('MODRINTH_TOKEN', live['env']); self.assertIn('--platforms curseforge', live['run'])
        self.assertIn('--retry-rejected-curseforge', live['run'])
        self.assertIn('RPG-Attribute-System-5.0.0-PatchNotes.md', live['run'])
        downloads = [row for row in steps if row.get('uses') == 'actions/download-artifact@v4']
        self.assertEqual({int(value) for value in downloads[0]['with']['artifact-ids'].split(',')}, {row['id'] for row in LOCK['source_artifacts']})
        self.assertEqual(int(downloads[1]['with']['artifact-ids']), LOCK['source_provenance']['id'])
        self.assertTrue(all(str(row['with']['run-id']) in [str(SOURCE), '${{ inputs.receipt_run_id }}'] for row in downloads))
        checkout = [row for row in steps if row.get('uses') == 'actions/checkout@v4']
        self.assertEqual(checkout[1]['with']['ref'], LOCK['source_commit'])
        self.assertIn('publication-receipts.json', steps[-1]['with']['path'])


if __name__ == '__main__':
    unittest.main()
