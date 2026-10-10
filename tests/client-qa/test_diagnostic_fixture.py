"""Diagnostic trust-boundary regressions only. No Minecraft or native evidence."""
import copy
import contextlib
import hashlib
import importlib.util
import io
import json
import os
from pathlib import Path
import stat
import sys
import subprocess
import tempfile
import unittest
from unittest import mock
import urllib.error
import zipfile

ROOT = Path(__file__).resolve().parents[2]

def load(name, relative):
    spec = importlib.util.spec_from_file_location(name, ROOT / relative)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

qa = load('diagnostic_test_qa', 'scripts/client-qa/run.py')
diag = load('diagnostic_test_helper', 'scripts/client-qa/diagnostic_fixture.py')


def metadata():
    repository = {'id': diag.REPOSITORY_ID, 'full_name': diag.REPOSITORY, 'private': False}
    head = {'id': diag.RUN_ID, 'repository_id': diag.REPOSITORY_ID, 'head_repository_id': diag.REPOSITORY_ID,
            'head_sha': diag.PRODUCER_SHA, 'head_branch': diag.BRANCH}
    return {'repository': repository,
        'workflow': {'id': diag.WORKFLOW_ID, 'path': diag.WORKFLOW_PATH, 'name': diag.WORKFLOW_NAME, 'state': 'active'},
        'run': {'id': diag.RUN_ID, 'workflow_id': diag.WORKFLOW_ID, 'path': diag.WORKFLOW_PATH,
            'head_sha': diag.PRODUCER_SHA, 'head_branch': diag.BRANCH, 'event': 'pull_request',
            'status': 'completed', 'run_attempt': 1, 'repository': repository, 'head_repository': repository,
            'pull_requests': [{'id': diag.PULL_REQUEST_ID, 'number': 48,
                               'head': {'ref': diag.BRANCH, 'sha': diag.PRODUCER_SHA, 'repo': {'id': diag.REPOSITORY_ID}},
                               'base': {'repo': {'id': diag.REPOSITORY_ID}}}]},
        'job': {'id': diag.JOB_ID, 'run_id': diag.RUN_ID, 'run_attempt': 1, 'head_sha': diag.PRODUCER_SHA,
            'head_branch': diag.BRANCH, 'workflow_name': diag.WORKFLOW_NAME, 'name': 'client-1211',
            'status': 'completed', 'conclusion': 'success', 'started_at': '2026-10-10T09:53:35Z',
            'completed_at': '2026-10-10T10:05:40Z', 'steps': [
                {'name': 'Generate genuine demo world and exercise scales 2 through 4', 'status': 'completed', 'conclusion': 'success'},
                {'name': 'Run actions/upload-artifact@v4', 'number': 9, 'status': 'completed', 'conclusion': 'success',
                 'started_at': '2026-10-10T10:05:37Z', 'completed_at': '2026-10-10T10:05:38Z'}]},
        'artifact': {'id': diag.ARTIFACT_ID, 'name': diag.ARTIFACT_NAME, 'size_in_bytes': diag.ARCHIVE_BYTES,
            'expired': False, 'digest': 'sha256:' + diag.ARCHIVE_SHA256, 'workflow_run': head,
            'created_at': '2026-10-10T10:05:38Z', 'archive_download_url': f'{diag.API}/actions/artifacts/{diag.ARTIFACT_ID}/zip'}}


class DiagnosticMetadataTests(unittest.TestCase):
    def test_exact_successful_producer_is_allowed_despite_failed_later_263_job(self):
        data = metadata(); data['run']['conclusion'] = 'failure'
        diag.validate_metadata(data)

    def test_historical_run_accepts_changing_linked_pr_head_without_rewriting_it(self):
        for linked_sha in (diag.PRODUCER_SHA, '17f60af725073fe6b49b5585f2802381f31b3f1b', 'c'*40):
            with self.subTest(linked_sha=linked_sha):
                data = metadata(); data['run']['pull_requests'][0]['head']['sha'] = linked_sha
                original = copy.deepcopy(data)
                diag.validate_metadata(data)
                self.assertEqual(data, original)
                self.assertEqual(data['run']['head_sha'], diag.PRODUCER_SHA)
                self.assertEqual(data['job']['head_sha'], diag.PRODUCER_SHA)
                self.assertEqual(data['artifact']['workflow_run']['head_sha'], diag.PRODUCER_SHA)
                self.assertEqual(data['run']['pull_requests'][0]['head']['sha'], linked_sha)

    def test_mutable_linked_head_never_replaces_authoritative_producer_identity(self):
        for group, container in [('run', None), ('job', None), ('artifact', 'workflow_run')]:
            with self.subTest(group=group):
                data = metadata(); data['run']['pull_requests'][0]['head']['sha'] = 'c'*40
                source = data[group] if container is None else data[group][container]
                source['head_sha'] = 'c'*40
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)

    def test_linked_pr_still_requires_exact_id_number_repositories_and_branch(self):
        for container, field, value in [(None, 'id', 1), (None, 'number', 49), ('head', 'ref', 'main'),
                                         ('head_repo', 'id', 1), ('base_repo', 'id', 1)]:
            with self.subTest(container=container, field=field):
                data = copy.deepcopy(metadata()); pr = data['run']['pull_requests'][0]
                pr['head']['sha'] = '17f60af725073fe6b49b5585f2802381f31b3f1b'
                if container == 'head_repo': target = pr['head']['repo']
                elif container == 'base_repo': target = pr['base']['repo']
                else: target = pr if container is None else pr[container]
                target[field] = value
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)
                del target[field]
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)

    def test_rejects_changed_missing_or_expired_metadata(self):
        mutations = [('repository', 'id', 1), ('repository', 'full_name', 'other/repo'),
            ('repository', 'private', True), ('workflow', 'id', 1), ('workflow', 'path', 'other.yml'),
            ('workflow', 'name', 'other'), ('workflow', 'state', 'disabled'),
            ('run', 'id', 1), ('run', 'workflow_id', 1), ('run', 'head_sha', 'b'*40),
            ('run', 'head_branch', 'main'), ('run', 'event', 'workflow_dispatch'), ('run', 'status', 'in_progress'),
            ('run', 'run_attempt', 2), ('run', 'pull_requests', []),
            ('job', 'id', 1), ('job', 'name', 'client-263'), ('job', 'run_id', 1), ('job', 'run_attempt', 2),
            ('job', 'head_sha', 'b'*40), ('job', 'conclusion', 'failure'), ('job', 'steps', []),
            ('artifact', 'id', 1), ('artifact', 'name', 'other'), ('artifact', 'size_in_bytes', 1),
            ('artifact', 'digest', 'sha256:'+'b'*64), ('artifact', 'expired', True),
            ('artifact', 'archive_download_url', 'https://elsewhere.invalid/archive'),
            ('artifact', 'created_at', '2026-10-10T10:05:39Z')]
        for group, key, value in mutations:
            with self.subTest(group=group, key=key):
                data = copy.deepcopy(metadata()); data[group][key] = value
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)
                data = copy.deepcopy(metadata()); del data[group][key]
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)

    def test_rejects_cross_repository_head_and_artifact_workflow_identity(self):
        for group, key in [('run', 'head_repository'), ('run', 'repository')]:
            data = copy.deepcopy(metadata()); data[group][key]['id'] = 1
            with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)
        for key, value in [('id', 1), ('repository_id', 1), ('head_repository_id', 1), ('head_sha', 'b'*40), ('head_branch', 'main')]:
            with self.subTest(key=key):
                data = copy.deepcopy(metadata()); data['artifact']['workflow_run'][key] = value
                with self.assertRaises(diag.DiagnosticError): diag.validate_metadata(data)

    def test_runtime_context_rejects_dispatch_fork_wrong_pr_branch_job_and_sha(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'event.json'
            repo = {'full_name': diag.REPOSITORY, 'id': diag.REPOSITORY_ID}
            event = {'number': 48, 'pull_request': {'id': diag.PULL_REQUEST_ID, 'number': 48,
                'head': {'ref': diag.BRANCH, 'sha': 'a'*40, 'repo': repo}, 'base': {'repo': repo}}}
            path.write_text(json.dumps(event))
            env = {'GITHUB_ACTIONS': 'true', 'GITHUB_REPOSITORY': diag.REPOSITORY,
                   'GITHUB_EVENT_NAME': 'pull_request', 'GITHUB_JOB': 'client-263-diagnostic', 'GITHUB_EVENT_PATH': str(path)}
            diag.check_pr48_context(env, 'a'*40)
            for key, value in [('GITHUB_ACTIONS', 'false'), ('GITHUB_REPOSITORY', 'other/repo'),
                               ('GITHUB_EVENT_NAME', 'workflow_dispatch'), ('GITHUB_JOB', 'client-263')]:
                bad = dict(env); bad[key] = value
                with self.assertRaises(diag.DiagnosticError): diag.check_pr48_context(bad, 'a'*40)
            for mutate in [lambda e: e.update(number=49),
                           lambda e: e['pull_request'].update(id=1),
                           lambda e: e['pull_request']['head'].update(ref='main'),
                           lambda e: e['pull_request']['head'].update(sha='b'*40),
                           lambda e: e['pull_request']['head']['repo'].update(full_name='fork/repo')]:
                bad = copy.deepcopy(event); mutate(bad); path.write_text(json.dumps(bad))
                with self.assertRaises(diag.DiagnosticError): diag.check_pr48_context(env, 'a'*40)


class Response(io.BytesIO):
    def __init__(self, body, status=200, headers=None):
        super().__init__(body); self.status = status; self.headers = headers or {}


class DiagnosticNetworkTests(unittest.TestCase):
    def test_only_github_api_gets_token_and_blob_gets_no_authorization(self):
        token = 'unit-test-fake-token'
        reader = diag.GitHubReader(token)
        archive = b'unit-test-archive'
        signed = 'https://unit-test.blob.core.windows.net/fixture?sig=unit-test-only'
        responses = [Response(b'', 302, {'Location': signed}), Response(archive)]
        with tempfile.TemporaryDirectory() as temporary, mock.patch.object(reader.opener, 'open', side_effect=responses) as opened, \
                mock.patch.object(diag, 'ARCHIVE_BYTES', len(archive)), \
                mock.patch.object(diag, 'ARCHIVE_SHA256', hashlib.sha256(archive).hexdigest()):
            reader.archive(Path(temporary) / 'fixture.zip')
            api_request, storage_request = (call.args[0] for call in opened.call_args_list)
            self.assertEqual(api_request.get_header('Authorization'), 'Bearer ' + token)
            self.assertEqual(storage_request.header_items(), [])

    def test_http_redirect_is_manual_and_never_copies_headers(self):
        reader = diag.GitHubReader('unit-test-fake-token')
        redirect = urllib.error.HTTPError(f'{diag.API}/actions/artifacts/{diag.ARTIFACT_ID}/zip', 302, 'Found',
                                         {'Location': 'https://unit-test.blob.core.windows.net/fixture'}, io.BytesIO())
        with mock.patch.object(reader.opener, 'open', side_effect=redirect):
            response = reader.request(redirect.url)
            self.assertEqual(response.status, 302)
        self.assertIsNone(diag.NoRedirect().redirect_request(None, None, 302, '', {}, 'https://other.invalid/'))

    def test_denied_read_reports_exact_public_endpoint_without_token(self):
        reader = diag.GitHubReader('unit-test-fake-token')
        endpoint = f'{diag.API}/actions/artifacts/{diag.ARTIFACT_ID}/zip'
        with mock.patch.object(reader.opener, 'open', side_effect=urllib.error.HTTPError(endpoint, 403, 'Forbidden', {}, None)) as opened:
            with self.assertRaises(diag.DiagnosticError) as caught: reader.archive(Path('/unused'))
            self.assertIn(endpoint, str(caught.exception)); self.assertIn('HTTP 403', str(caught.exception))
            self.assertNotIn('unit-test-fake-token', str(caught.exception)); opened.assert_called_once()

    def test_unexpected_insecure_foreign_storage_host_or_redirect_never_followed(self):
        reader = diag.GitHubReader('unit-test-fake-token')
        for location in ['http://unit-test.blob.core.windows.net/a', 'https://other.invalid/a',
                         'https://user@unit-test.blob.core.windows.net/a', 'https://unit-test.blob.core.windows.net:444/a']:
            with self.subTest(location=location), mock.patch.object(reader.opener, 'open') as opened:
                with self.assertRaises(diag.DiagnosticError): reader.request(location, api=False)
                opened.assert_not_called()
        error = urllib.error.HTTPError('https://unit-test.blob.core.windows.net/a?sig=private', 302, 'Found', {}, None)
        with mock.patch.object(reader.opener, 'open', side_effect=error):
            with self.assertRaises(diag.DiagnosticError) as caught:
                reader.request(error.url, api=False)
            self.assertNotIn('sig=private', str(caught.exception))

    def test_short_oversized_or_corrupt_download_is_rejected_before_write(self):
        reader = diag.GitHubReader('unit-test-fake-token')
        for body in [b'', b'long', b'bad']:
            with tempfile.TemporaryDirectory() as temporary, mock.patch.object(reader.opener, 'open',
                    side_effect=[Response(b'', 302, {'Location': 'https://unit-test.blob.core.windows.net/a'}), Response(body)]), \
                    mock.patch.object(diag, 'ARCHIVE_BYTES', 3), mock.patch.object(diag, 'ARCHIVE_SHA256', hashlib.sha256(b'yes').hexdigest()):
                destination = Path(temporary) / 'fixture.zip'
                with self.assertRaises(diag.DiagnosticError): reader.archive(destination)
                self.assertFalse(destination.exists())


class DiagnosticArchiveTests(unittest.TestCase):
    def extract_test_zip(self, entries):
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, 'w') as bundle:
            for name, content, mode in entries:
                info = zipfile.ZipInfo(name); info.external_attr = mode << 16
                bundle.writestr(info, content)
        body = buffer.getvalue()
        with tempfile.TemporaryDirectory() as temporary:
            archive = Path(temporary) / 'fixture.zip'; archive.write_bytes(body)
            with mock.patch.object(diag, 'ARCHIVE_BYTES', len(body)), \
                    mock.patch.object(diag, 'ARCHIVE_SHA256', hashlib.sha256(body).hexdigest()):
                diag.extract_archive(archive, Path(temporary) / 'out')
                return (Path(temporary) / 'out/provenance.json').read_bytes()

    def test_safe_archive_preserves_original_provenance_bytes(self):
        self.assertEqual(self.extract_test_zip([('provenance.json', b'original unit-test provenance\n', stat.S_IFREG),
                                                ('world/level.dat', b'x'*100, stat.S_IFREG)]), b'original unit-test provenance\n')

    def test_rejects_traversal_absolute_backslash_extra_symlink_device_and_duplicate(self):
        for name, mode in [('../outside', stat.S_IFREG), ('/outside', stat.S_IFREG), ('world/../outside', stat.S_IFREG),
                           ('world\\outside', stat.S_IFREG), ('unrelated.json', stat.S_IFREG),
                           ('world/link', stat.S_IFLNK), ('world/device', stat.S_IFCHR), ('world//double', stat.S_IFREG)]:
            with self.subTest(name=name), self.assertRaises(diag.DiagnosticError):
                self.extract_test_zip([('provenance.json', b'{}', stat.S_IFREG), (name, b'unit-test', mode)])
        with self.assertRaises(diag.DiagnosticError):
            self.extract_test_zip([('provenance.json', b'{}', stat.S_IFREG), ('provenance.json', b'{}', stat.S_IFREG)])

    def test_normal_fixture_validation_never_accepts_producer_sha_as_current(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary); (root/'world').mkdir(); (root/'world/level.dat').write_bytes(b'unit-test'*20)
            original = {'schema_version': 1, 'source_sha': diag.PRODUCER_SHA,
                        'fixture_kind': 'live-client-generated-disposable-demo', 'producer_mc': '1.21.1',
                        'world_files': qa.world_inventory(root/'world')}
            (root/'provenance.json').write_text(json.dumps(original))
            qa.validate_fixture(root, diag.PRODUCER_SHA)
            with self.assertRaises(qa.QaError): qa.validate_fixture(root, 'b'*40)
            (root/'world/level.dat').write_bytes(b'changed unit-test'*20)
            with self.assertRaises(qa.QaError): qa.validate_fixture(root, diag.PRODUCER_SHA)

    def test_manifest_binds_all_files_to_current_sha_and_original_producer(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary); (root/'failure-screen.png').write_bytes(b'unit-test pixels only')
            (root/'driver-failure.txt').write_text('unit-test failure')
            proof = {'source_sha': 'b'*40, 'producer_source_sha': diag.PRODUCER_SHA,
                     'original_fixture_provenance': {'source_sha': diag.PRODUCER_SHA}}
            diag.write_evidence_manifest(root, 'b'*40, 'unit-test-run', proof)
            manifest = json.loads((root/'diagnostic-evidence-manifest.json').read_text())
            self.assertEqual(manifest['source_sha'], 'b'*40); self.assertFalse(manifest['native_acceptance'])
            self.assertEqual(manifest['fixture_producer'], proof)
            self.assertEqual(set(manifest['evidence_files']), {'failure-screen.png', 'driver-failure.txt'})
            self.assertEqual(diag.evidence_context(None), {})


class DiagnosticSourceTests(unittest.TestCase):
    def test_checks_entire_tree_ancestry_checkout_sha_and_dirty_files(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            def git(*args):
                result = subprocess.run(['git', '-c', 'user.name=Unit Test', '-c', 'user.email=unit-test@example.invalid', *args],
                                        cwd=root, text=True, capture_output=True, check=True)
                return result.stdout.strip()
            git('init', '-q'); (root/'1.21.1').mkdir(); (root/'1.21.1/source').write_text('unit-test producer')
            git('add', '.'); git('commit', '-qm', 'unit-test producer'); producer = git('rev-parse', 'HEAD')
            tree = git('rev-parse', 'HEAD:1.21.1')
            (root/'26.3').mkdir(); (root/'26.3/source').write_text('unit-test target')
            git('add', '.'); git('commit', '-qm', 'unit-test target'); target = git('rev-parse', 'HEAD')
            with mock.patch.object(diag, 'PRODUCER_SHA', producer), mock.patch.object(diag, 'PRODUCER_TREE', tree):
                self.assertTrue(diag.check_source(root, target)['producer_is_ancestor'])
                with self.assertRaises(diag.DiagnosticError): diag.check_source(root, 'f'*40)
                (root/'1.21.1/untracked').write_text('unit-test dirty')
                with self.assertRaises(diag.DiagnosticError): diag.check_source(root, target)
                (root/'1.21.1/untracked').unlink(); (root/'1.21.1/source').write_text('unit-test changed subtree')
                with self.assertRaises(diag.DiagnosticError): diag.check_source(root, target)
                git('add', '.'); git('commit', '-qm', 'unit-test changed subtree')
                with self.assertRaises(diag.DiagnosticError): diag.check_source(root, git('rev-parse', 'HEAD'))
                git('checkout', '-q', '--orphan', 'unit-test-unrelated'); git('rm', '-rf', '.')
                (root/'1.21.1').mkdir(); (root/'1.21.1/source').write_text('unit-test producer')
                git('add', '.'); git('commit', '-qm', 'unit-test unrelated')
                with self.assertRaises(diag.DiagnosticError): diag.check_source(root, git('rev-parse', 'HEAD'))

    def test_workflow_keeps_original_permissions_normal_dependency_and_separate_artifact(self):
        source = (ROOT/'.github/workflows/client-qa.yml').read_text()
        self.assertIn('permissions:\n  contents: read\n', source)
        self.assertEqual(source.count('actions: read'), 1)
        normal = source.split('  client-263:\n')[1].split('  # Auxiliary PR48 diagnosis only.')[0]
        self.assertIn('needs: client-1211', normal); self.assertNotIn('diagnostic', normal)
        self.assertNotIn('permissions:', normal)
        diagnostic = source.split('  client-263-diagnostic:\n')[1]
        self.assertNotIn('needs:', diagnostic); self.assertIn('continue-on-error: true', diagnostic)
        self.assertIn('permissions:\n      contents: read\n      actions: read', diagnostic)
        self.assertNotIn(': write', diagnostic)
        self.assertIn('github.event.pull_request.number == 48', diagnostic)
        self.assertIn('github.event.pull_request.head.repo.full_name == github.repository', diagnostic)
        self.assertIn('diagnostic-only-client-evidence-26.3-pr48', diagnostic)
        self.assertIn('persist-credentials: false', diagnostic)


class DiagnosticCliTests(unittest.TestCase):
    def test_explicit_mode_removes_token_before_every_subprocess_and_labels_all_cases(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / 'evidence'
            proof = {'source_sha': 'b'*40, 'producer_source_sha': diag.PRODUCER_SHA,
                     'original_fixture_provenance': {'source_sha': diag.PRODUCER_SHA}}
            seen = []
            class Driver:
                def __init__(self, repo, mc, output, sha, fixture, environment):
                    self.output = output; self.run_id = 'unit-test-run'; self.case = None
                    self.fixture_evidence_context = {}
                    seen.append(('driver', dict(environment)))
                def launch(self, scale):
                    self.case = self.output / ('unit-test-scale' + str(scale)); self.case.mkdir()
                    (self.case/'unit-test-only.txt').write_text('no native evidence')
                    seen.append(('launch', dict(os.environ)))
                def cleanup(self): pass
            def checked(*args, **kwargs):
                self.assertNotIn('GITHUB_TOKEN', os.environ)
                return type('Result', (), {'stdout': 'b'*40})()
            def prepare(*args, **kwargs):
                self.assertNotIn('GITHUB_TOKEN', os.environ)
                return dict(os.environ)
            def fixture(*args):
                self.assertEqual(args[-1]['GITHUB_TOKEN'], 'unit-test-fake-token')
                self.assertNotIn('GITHUB_TOKEN', os.environ)
                return Path(temporary), proof
            argv = ['run.py', '--workspace', '26.3', '--source-sha', 'b'*40, '--output', str(output), '--diagnostic-pr48-fixture']
            with mock.patch.object(sys, 'argv', argv), mock.patch.dict(sys.modules, {'diagnostic_fixture': diag}), \
                    mock.patch.dict(os.environ, {'DISPLAY': ':unit-test', 'GITHUB_TOKEN': 'unit-test-fake-token'}, clear=True), \
                    mock.patch.object(qa, 'checked', side_effect=checked), mock.patch.object(qa.shutil, 'which', return_value='/unit-test'), \
                    mock.patch.object(diag, 'prepare_fixture', side_effect=fixture), \
                    mock.patch.object(qa, 'prepare_renderer', side_effect=prepare), mock.patch.object(qa, 'Driver', Driver), \
                    contextlib.redirect_stdout(io.StringIO()) as stdout:
                qa.main()
            self.assertIn('diagnostic', stdout.getvalue()); self.assertIn('full same-source native acceptance', stdout.getvalue())
            self.assertEqual(len([kind for kind, env in seen if kind == 'launch']), 3)
            for kind, env in seen: self.assertNotIn('GITHUB_TOKEN', env)
            for scale in (2, 3, 4):
                manifest = json.loads((output/('unit-test-scale'+str(scale))/'diagnostic-evidence-manifest.json').read_text())
                self.assertEqual(manifest['source_sha'], 'b'*40); self.assertFalse(manifest['native_acceptance'])
                self.assertEqual(manifest['fixture_producer']['producer_source_sha'], diag.PRODUCER_SHA)

    def test_read_denial_records_exact_blocker_without_launching_or_probing(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary)/'evidence'
            argv = ['run.py', '--workspace', '26.3', '--source-sha', 'b'*40, '--output', str(output), '--diagnostic-pr48-fixture']
            blocker = f'GET {diag.API}/actions/artifacts/{diag.ARTIFACT_ID}/zip returned HTTP 403'
            with mock.patch.object(sys, 'argv', argv), mock.patch.dict(sys.modules, {'diagnostic_fixture': diag}), \
                    mock.patch.dict(os.environ, {'DISPLAY': ':unit-test', 'GITHUB_TOKEN': 'unit-test-fake-token'}, clear=True), \
                    mock.patch.object(qa, 'checked', return_value=type('Result', (), {'stdout': 'b'*40})()), \
                    mock.patch.object(qa.shutil, 'which', return_value='/unit-test'), \
                    mock.patch.object(diag, 'prepare_fixture', side_effect=diag.DiagnosticError(blocker)), \
                    mock.patch.object(qa, 'prepare_renderer') as renderer, mock.patch.object(qa, 'Driver') as driver:
                with self.assertRaises(diag.DiagnosticError): qa.main()
                renderer.assert_not_called(); driver.assert_not_called()
                self.assertNotIn('GITHUB_TOKEN', os.environ)
            self.assertEqual((output/'diagnostic-fixture-failure.txt').read_text(), blocker+'\n')

    def test_diagnostic_flag_cannot_be_combined_with_normal_bootstrap_or_fixture_io(self):
        for workspace, extra in [('1.21.1', []), ('26.3', ['--fixture-input', '/unit-test']), ('26.3', ['--fixture-output', '/unit-test'])]:
            argv = ['run.py', '--workspace', workspace, '--source-sha', 'b'*40, '--output', '/unit-test', '--diagnostic-pr48-fixture', *extra]
            with self.subTest(workspace=workspace, extra=extra), mock.patch.object(sys, 'argv', argv), \
                    mock.patch.object(qa, 'checked') as checked:
                with self.assertRaises(qa.QaError): qa.main()
                checked.assert_not_called()


if __name__ == '__main__':
    unittest.main()
