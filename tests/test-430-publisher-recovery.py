#!/usr/bin/env python3
"""Offline immutable provenance, journal ancestry and workflow regression tests."""
import copy
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import urllib.request
import yaml

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('helper', ROOT / 'scripts/validate-430-publisher-recovery.py')
helper = importlib.util.module_from_spec(spec)
spec.loader.exec_module(helper)
LOCK = helper.load_lock()
HELPER = 'b' * 40
CURRENT = 40000000000
SOURCE = LOCK['source_run_id']
RECEIPT = LOCK['minimum_receipt_run_id']


class RecoveryTests(unittest.TestCase):
    def run_payload(self, run_id, *, recovery=False, current=False):
        return {'id': run_id, 'repository': {'full_name': LOCK['repository'], 'id': LOCK['repository_id']},
                'head_repository': {'id': LOCK['repository_id']}, 'actor': {'id': LOCK['owner_id']},
                'head_sha': HELPER if recovery else LOCK['source_commit'],
                'path': helper.HELPER_WORKFLOW if recovery else helper.original.WORKFLOW,
                'name': helper.HELPER_NAME if recovery else 'Publish', 'head_branch': 'main' if recovery else 'v4.3.0',
                'event': 'workflow_dispatch', 'status': 'in_progress' if current else 'completed', 'run_attempt': 1,
                'created_at': f'2026-10-10T13:{0 if run_id == SOURCE else 10 if run_id == RECEIPT else 20}:00Z'}

    def job(self, job_id, *, recovery=False, wrote=True, running=False):
        return {'id': job_id, 'name': 'recover' if recovery else 'publish', 'status': 'in_progress' if running else 'completed',
                'steps': [{'name': helper.HELPER_STEP if recovery else 'Publish/resume verified binaries with per-loader dependencies',
                           'conclusion': 'failure' if wrote else 'skipped'}]}

    def artifact(self, expected, run):
        return {**{k: v for k, v in expected.items() if k != 'run_id'}, 'expired': False,
                'workflow_run': {'id': run['id'], 'head_sha': run['head_sha'],
                                 'repository_id': LOCK['repository_id'], 'head_repository_id': LOCK['repository_id']}}

    def payloads(self):
        current, source, receipt = self.run_payload(CURRENT, recovery=True, current=True), self.run_payload(SOURCE), self.run_payload(RECEIPT)
        artifacts = [self.artifact(row, source) for row in LOCK['source_artifacts'] + [LOCK['source_provenance']]]
        payloads = {
            '/actions/runs/' + str(CURRENT): current,
            '/actions/runs/' + str(SOURCE): source,
            '/actions/runs/' + str(RECEIPT): receipt,
            '/git/ref/tags/v4.3.0': {'ref': 'refs/tags/v4.3.0', 'object': {'sha': LOCK['source_commit'], 'type': 'commit'}},
            '/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100': {'total_count': len(artifacts), 'artifacts': artifacts},
            '/actions/runs/' + str(RECEIPT) + '/artifacts?per_page=100': {'total_count': 1, 'artifacts': [self.artifact(LOCK['known_receipts'][0], receipt)]},
            '/actions/workflows/publish.yml/runs?head_sha=' + LOCK['source_commit'] + '&per_page=100': {'total_count': 2, 'workflow_runs': [source, receipt]},
            '/actions/workflows/recover-430-publication.yml/runs?per_page=100': {'total_count': 1, 'workflow_runs': [current]},
        }
        jobs = {str(SOURCE): {'jobs': [self.job(1)]}, str(RECEIPT): {'jobs': [self.job(2)]}}
        builds = {'jobs': [{'name': 'Build ' + workspace, 'status': 'completed', 'conclusion': 'success',
                           'steps': [{'name': 'Build both loaders and common checks at exact source commit', 'conclusion': 'success'}]}
                          for workspace in helper.original.WORKSPACES]}
        def read_jobs(run_id, attempt=None):
            return builds if attempt == 1 else jobs[str(run_id)]
        return payloads, jobs, read_jobs

    def validate(self, payloads, read_jobs):
        with patch.object(helper, 'request', side_effect=lambda path: payloads[path]), patch.object(helper.original, 'read_jobs', side_effect=read_jobs):
            return helper.validate(HELPER, str(RECEIPT), str(CURRENT), LOCK)

    def test_valid_separate_helper_original_source_and_latest_mr_receipt(self):
        payloads, _, jobs = self.payloads()
        result = self.validate(payloads, jobs)
        self.assertEqual(result['source_commit'], LOCK['source_commit'])
        self.assertEqual(result['helper_commit'], HELPER)
        self.assertEqual(result['receipt_run_id'], RECEIPT)
        self.assertIsNone(result['receipt_helper_commit'])
        self.assertEqual(len(result['source_artifacts']), 5)
        self.assertFalse(result['receipt_input_verified'])

    def test_changed_tag_helper_source_actor_artifact_and_truncated_history_fail_closed(self):
        mutations = {
            'tag': lambda p: p['/git/ref/tags/v4.3.0']['object'].update(sha='f' * 40),
            'helper': lambda p: p['/actions/runs/' + str(CURRENT)].update(head_sha='f' * 40),
            'helper branch': lambda p: p['/actions/runs/' + str(CURRENT)].update(head_branch='unreviewed'),
            'helper rerun': lambda p: p['/actions/runs/' + str(CURRENT)].update(run_attempt=2),
            'source': lambda p: p['/actions/runs/' + str(SOURCE)].update(head_sha='f' * 40),
            'source actor': lambda p: p['/actions/runs/' + str(SOURCE)]['actor'].update(id=1),
            'receipt actor': lambda p: p['/actions/runs/' + str(RECEIPT)]['actor'].update(id=1),
            'source digest': lambda p: p['/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100']['artifacts'][0].update(digest='sha256:' + 'f' * 64),
            'source artifact ID': lambda p: p['/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100']['artifacts'][0].update(id=1),
            'source artifact namespace': lambda p: p['/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100']['artifacts'][0]['workflow_run'].update(head_sha='f' * 40),
            'receipt digest': lambda p: p['/actions/runs/' + str(RECEIPT) + '/artifacts?per_page=100']['artifacts'][0].update(digest='sha256:' + 'f' * 64),
            'truncated artifacts': lambda p: p['/actions/runs/' + str(SOURCE) + '/artifacts?per_page=100'].update(total_count=100),
            'truncated history': lambda p: p['/actions/workflows/recover-430-publication.yml/runs?per_page=100'].update(total_count=2),
        }
        for name, mutate in mutations.items():
            with self.subTest(name=name):
                payloads, _, jobs = self.payloads(); mutate(payloads)
                with self.assertRaises(ValueError): self.validate(payloads, jobs)

    def test_latest_journal_tracks_helpers_all_attempts_and_ignores_read_only_preflight(self):
        old, mr, newer = self.run_payload(SOURCE), self.run_payload(RECEIPT), self.run_payload(CURRENT - 1, recovery=True)
        runs = [old, mr, newer]
        jobs = {str(SOURCE): {'jobs': [self.job(1)]}, str(RECEIPT): {'jobs': [self.job(2)]},
                str(CURRENT - 1): {'jobs': [self.job(3, recovery=True, wrote=False)]}}
        helper.latest_receipt(runs, jobs, CURRENT, RECEIPT)
        jobs[str(CURRENT - 1)]['jobs'].insert(0, self.job(4, recovery=True, wrote=True))
        with self.assertRaises(ValueError): helper.latest_receipt(runs, jobs, CURRENT, RECEIPT)
        helper.latest_receipt(runs, jobs, CURRENT, CURRENT - 1)
        newer['status'] = 'in_progress'
        jobs[str(CURRENT - 1)] = {'jobs': [self.job(5, recovery=True, running=True)]}
        with self.assertRaises(ValueError): helper.latest_receipt(runs, jobs, CURRENT, CURRENT - 1)

    def test_malformed_helper_jobs_and_bounded_histories_fail_closed(self):
        for payload in [None, {}, {'jobs': [None]}, {'jobs': [{'name': 'recover', 'status': 'completed', 'steps': None}]},
                        {'jobs': [{'name': 'recover', 'status': 'completed', 'steps': [None]}]}]:
            with self.subTest(payload=payload), self.assertRaises(ValueError): helper.helper_may_have_published(payload)
        for payload in [{}, {'total_count': 100, 'workflow_runs': []}, {'total_count': 2, 'workflow_runs': [self.run_payload(SOURCE)]},
                        {'total_count': 2, 'workflow_runs': [self.run_payload(SOURCE), self.run_payload(SOURCE)]}]:
            with patch.object(helper, 'request', return_value=payload), self.assertRaises(ValueError): helper.history('/fixed')

    def directories(self):
        temp = tempfile.TemporaryDirectory(); self.addCleanup(temp.cleanup)
        root = Path(temp.name); old, prior = root / 'original', root / 'prior'; old.mkdir(); prior.mkdir()
        manifest = (ROOT / 'tests/fixtures/verified-release-430.json').read_bytes()
        (old / 'verified-release.json').write_bytes(manifest); (prior / 'verified-release.json').write_bytes(manifest)
        parsed = json.loads(manifest)
        journal = {'schema_version': 1, 'source_commit': LOCK['source_commit'], 'version': '4.3.0', 'publicly_verified': True,
                   'uploads': [{'platform': 'modrinth', 'file': row['file_name'], 'sha256': row['sha256'], 'status': 'verified', 'id': 'offline'}
                               for row in parsed['files']]}
        (prior / 'publication-receipts.json').write_text(json.dumps(journal, indent=2) + '\n')
        provenance = {'receipt_helper_commit': None, 'receipt_run_id': RECEIPT}
        return root, old, prior, journal, provenance

    def test_prepare_preserves_manifest_journal_bytes_all_mr_rows_and_unresolved_cf_state(self):
        root, old, prior, journal, provenance = self.directories()
        journal['uploads'].append({'platform': 'curseforge', 'file': journal['uploads'][0]['file'], 'sha256': journal['uploads'][0]['sha256'], 'status': 'uncertain'})
        original = json.dumps(journal, indent=2) + '\n'; (prior / 'publication-receipts.json').write_text(original)
        cwd = os.getcwd()
        try:
            os.chdir(root); helper.prepare(old, prior, provenance, LOCK)
            self.assertEqual(Path('publication-receipts.json').read_text(), original)
            self.assertEqual(Path('verified-release.json').read_bytes(), (old / 'verified-release.json').read_bytes())
            self.assertTrue(provenance['receipt_input_verified'])
        finally: os.chdir(cwd)

    def test_empty_stale_changed_manifest_journal_and_helper_provenance_fail_closed(self):
        for mutation in ['empty', 'missing MR', 'unknown MR file', 'changed MR hash', 'wrong source', 'changed manifest', 'wrong helper']:
            with self.subTest(mutation=mutation):
                root, old, prior, journal, provenance = self.directories()
                if mutation == 'empty': journal['uploads'] = []
                elif mutation == 'missing MR': journal['uploads'].pop()
                elif mutation == 'unknown MR file': journal['uploads'][0].update(file='foreign.jar', sha256=None)
                elif mutation == 'changed MR hash': journal['uploads'][0]['sha256'] = 'f' * 64
                elif mutation == 'wrong source': journal['source_commit'] = HELPER
                elif mutation == 'changed manifest': (prior / 'verified-release.json').write_text('{}')
                else:
                    provenance['receipt_helper_commit'] = HELPER
                    (prior / 'recovery-provenance.json').write_text(json.dumps({'source_commit': LOCK['source_commit'], 'helper_commit': 'c' * 40}))
                (prior / 'publication-receipts.json').write_text(json.dumps(journal))
                with self.assertRaises(ValueError): helper.prepare(old, prior, provenance, LOCK)

    def test_new_workflow_pins_source_no_build_same_concurrency_preflight_before_live_and_retains_journal(self):
        source = (ROOT / helper.HELPER_WORKFLOW).read_text(); workflow = yaml.safe_load(source)
        self.assertEqual(workflow['concurrency']['group'], 'ras-release-publication')
        self.assertEqual(workflow['permissions'], {'contents': 'read', 'actions': 'read'})
        self.assertIn("github.actor_id == '74470806'", workflow['jobs']['recover']['if'])
        self.assertNotIn('gradlew', source)
        steps = workflow['jobs']['recover']['steps']; names = [row.get('name') for row in steps]
        self.assertLess(names.index('Reject unreviewed helper identity and every job rerun'), names.index(helper.HELPER_STEP))
        self.assertLess(names.index('Restore exact original manifest and mandatory latest journal'), names.index(helper.HELPER_STEP))
        self.assertLess(names.index('Preflight both exact store inventories and strict real catalog IDs'), names.index(helper.HELPER_STEP))
        preflight = next(row for row in steps if row.get('name') == 'Preflight both exact store inventories and strict real catalog IDs')
        self.assertIn('--require-public-modrinth', preflight['run'])
        live = next(row for row in steps if row.get('name') == helper.HELPER_STEP)
        self.assertEqual(live['if'], '${{ inputs.dry_run == false }}')
        self.assertNotIn('MODRINTH_TOKEN', live['env']); self.assertIn('--platforms curseforge', live['run'])
        downloads = [row for row in steps if row.get('uses') == 'actions/download-artifact@v4']
        self.assertEqual({int(value) for value in downloads[0]['with']['artifact-ids'].split(',')}, {row['id'] for row in LOCK['source_artifacts']})
        self.assertTrue(all(row['with']['merge-multiple'] is True for row in downloads))
        self.assertEqual(str(downloads[0]['with']['run-id']), str(SOURCE))
        self.assertIn('publication-receipts.json', steps[-1]['with']['path'])

    def test_provenance_requests_are_get_only_bounded_and_reject_redirects(self):
        class Response:
            def __init__(self, body): self.body = body
            def __enter__(self): return self
            def __exit__(self, *args): pass
            def read(self, count): return self.body[:count]
        captured = []
        def opener(handler):
            self.assertTrue(issubclass(handler, urllib.request.HTTPRedirectHandler))
            self.assertIsNone(handler().redirect_request(None, None, 302, None, None, 'https://unintended.invalid'))
            class Opener:
                def open(self, req, timeout):
                    captured.append(req); self.timeout = timeout
                    return Response(b'{"ok":true}')
            return Opener()
        with patch.dict(os.environ, {'GH_TOKEN': 'offline-not-a-token'}), patch.object(helper.urllib.request, 'build_opener', side_effect=opener):
            self.assertEqual(helper.request('/fixed'), {'ok': True})
        self.assertEqual(captured[0].get_method(), 'GET')
        self.assertEqual(captured[0].full_url, 'https://api.github.com/repos/' + LOCK['repository'] + '/fixed')
        for body in [b'not json', b'x' * 21]:
            class BadOpener:
                def open(self, *args, **kwargs): return Response(body)
            with patch.dict(os.environ, {'GH_TOKEN': 'offline-not-a-token'}), patch.object(helper, 'MAX_BYTES', 20), patch.object(helper.urllib.request, 'build_opener', return_value=BadOpener()):
                with self.assertRaisesRegex(ValueError, 'GitHub provenance GET failed'): helper.request('/fixed')


if __name__ == '__main__':
    unittest.main()
