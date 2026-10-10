"""Real Linux descendant/pidfd checks and adversarial proof doubles; no Minecraft rendering."""
import copy
import importlib.util
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import time
from types import SimpleNamespace
import unittest
from unittest import mock

spec = importlib.util.spec_from_file_location('process_identity_tests', Path(__file__).resolve().parents[2] / 'scripts/client-qa/process_identity.py')
identity = importlib.util.module_from_spec(spec)
spec.loader.exec_module(identity)


class ClientProofTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(); self.addCleanup(self.temp.cleanup)
        self.directory = Path(self.temp.name)
        self.executable = Path(sys.executable).resolve()
        self.properties = {'ras.clientQaRunId': 'unit-test-only', 'ras.clientQaSourceSha': 'a' * 40, 'ras.clientQaScale': '2'}
        self.root = SimpleNamespace(pid=10, start_ticks=100, parent_pid=None, alive=lambda: True,
                                    facts={'pid': 10, 'start_ticks': 100})
        self.client = SimpleNamespace(pid=12, start_ticks=120, parent_pid=10, alive=lambda: True,
                                      facts={'pid': 12, 'start_ticks': 120})
        self.tracker = identity.ProcessTracker(); self.tracker.refresh = mock.Mock()
        self.tracker.launcher_pid = 10; self.tracker.bindings = {10: self.root, 12: self.client}
        self.details = {'pid': 12, 'ppid': 10, 'start_ticks': 120, 'uids': [os.getuid(), os.geteuid(), os.getuid(), os.getuid()],
                        'executable': str(self.executable), 'cwd': str(self.directory.resolve()),
                        'argv': ['java', *['-D' + key + '=' + value for key, value in self.properties.items()],
                                 '-DauthenticationToken=unit-test-private']}

    def verify(self, details=None, pid=12):
        with mock.patch.object(identity, 'process_details', return_value=details or self.details):
            return self.tracker.verify_client(pid, executable=self.executable, cwd=self.directory, properties=self.properties)

    def test_complete_independent_proof_has_no_raw_command_or_environment(self):
        proof = self.verify(); rendered = json.dumps(proof)
        self.assertTrue(proof['pidfd_bound']); self.assertEqual(proof['verified_qa_properties'], self.properties)
        self.assertNotIn('argv', proof); self.assertNotIn('environment', proof); self.assertNotIn('unit-test-private', rendered)

    def test_rejects_foreign_predating_reused_wrong_user_executable_and_cwd(self):
        for field, value in [('start_ticks', 119), ('uids', [-1] * 4),
                             ('executable', '/foreign/java'), ('cwd', '/foreign/game')]:
            with self.subTest(field=field):
                data = copy.deepcopy(self.details); data[field] = value
                with self.assertRaises(identity.OwnershipError): self.verify(data)
        with self.assertRaises(identity.OwnershipError): self.verify(pid=99)
        with self.assertRaises(identity.OwnershipError): self.verify(pid=10)

    def test_rejects_missing_wrong_or_duplicate_nonce_sha_scale_jvm_arguments(self):
        for key in self.properties:
            for mutation in ('missing', 'wrong', 'duplicate'):
                with self.subTest(key=key, mutation=mutation):
                    data = copy.deepcopy(self.details); arg = '-D' + key + '=' + self.properties[key]
                    if mutation == 'missing': data['argv'].remove(arg)
                    if mutation == 'wrong': data['argv'][data['argv'].index(arg)] = '-D' + key + '=foreign'
                    if mutation == 'duplicate': data['argv'].append(arg)
                    with self.assertRaises(identity.OwnershipError): self.verify(data)

    def test_rejects_retained_client_pid_swap_or_disconnected_ancestry(self):
        self.verify(); self.tracker.client_pid = 99
        with self.assertRaises(identity.OwnershipError): self.verify()
        self.tracker.client_pid = None; self.client.parent_pid = 99
        with self.assertRaises(identity.OwnershipError): self.verify()

    def test_numeric_pid_reuse_or_exited_pidfd_never_receives_a_signal(self):
        binding = identity.Binding.__new__(identity.Binding)
        binding.pid = 43210; binding.fd = 987; binding.start_ticks = 100
        for ready, start in [(False, 101), (True, 100)]:
            with self.subTest(ready=ready, start=start), \
                    mock.patch.object(identity.select, 'select', return_value=([987] if ready else [], [], [])), \
                    mock.patch.object(identity, 'process_stat', return_value={'start_ticks': start, 'state': 'S'}), \
                    mock.patch.object(identity.signal, 'pidfd_send_signal') as send:
                self.assertFalse(binding.send(signal.SIGTERM)); send.assert_not_called()

    def test_signal_uses_bound_kernel_handle_not_numeric_pid_or_group(self):
        binding = identity.Binding.__new__(identity.Binding); binding.pid = 43210; binding.fd = 987
        with mock.patch.object(binding, 'alive', return_value=True), \
                mock.patch.object(identity.signal, 'pidfd_send_signal') as send, mock.patch.object(identity.os, 'kill') as kill, \
                mock.patch.object(identity.os, 'killpg') as group:
            self.assertTrue(binding.send(signal.SIGTERM)); send.assert_called_once_with(987, signal.SIGTERM, None, 0)
            kill.assert_not_called(); group.assert_not_called()

    def test_failed_refresh_still_terminates_and_closes_only_retained_bindings(self):
        tracker = identity.ProcessTracker()
        def binding(pid, parent):
            alive = [True]
            def send(sig):
                if not alive[0]: return False
                alive[0] = False
                return True
            return SimpleNamespace(pid=pid, parent_pid=parent, send=mock.Mock(side_effect=send),
                                   alive=lambda: alive[0], close=mock.Mock())
        root, child = binding(10, None), binding(12, 10)
        tracker.bindings = {10: root, 12: child}
        tracker.refresh = mock.Mock(side_effect=RuntimeError('unit-test-private'))
        with mock.patch.object(identity.os, 'kill') as kill, mock.patch.object(identity.os, 'killpg') as group:
            report = tracker.cleanup(seconds=0)
            self.assertEqual(report['term_pids'], [12, 10]); self.assertEqual(report['kill_pids'], [])
            self.assertEqual(report['refresh_error_type'], 'RuntimeError')
            self.assertNotIn('unit-test-private', json.dumps(report))
            root.close.assert_called_once(); child.close.assert_called_once()
            kill.assert_not_called(); group.assert_not_called()
        self.assertEqual(tracker.bindings, {})



@unittest.skipUnless(sys.platform == 'linux' and hasattr(os, 'pidfd_open') and hasattr(signal, 'pidfd_send_signal'), 'requires real Linux pidfds')
class ActualDetachedProcessTests(unittest.TestCase):
    def test_actual_detached_descendants_are_owned_foreign_sibling_is_rejected_and_left_alive(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary); game = directory / 'game'; game.mkdir()
            program = directory / 'controlled_task.py'
            program.write_text('''import pathlib, subprocess, sys, time
mode = sys.argv[1]
if mode == 'launcher':
    subprocess.Popen([sys.executable, __file__, 'daemon', *sys.argv[2:]], start_new_session=True).wait()
elif mode == 'daemon':
    child = subprocess.Popen([sys.executable, __file__, 'client', *sys.argv[2:]], cwd=pathlib.Path('game'))
    pathlib.Path('client.pid').write_text(str(child.pid))
    child.wait()
else:
    time.sleep(60)
''')
            properties = {'ras.clientQaRunId': 'detached-test-only', 'ras.clientQaSourceSha': 'b' * 40, 'ras.clientQaScale': '4'}
            args = ['-D' + key + '=' + value for key, value in properties.items()]
            tracker = identity.ProcessTracker(); launcher = foreign = None
            try:
                launcher = subprocess.Popen([sys.executable, str(program), 'launcher', *args], cwd=directory,
                                            start_new_session=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                tracker.bind_launcher(launcher.pid); limit = time.monotonic() + 5
                while not (directory / 'client.pid').exists() and time.monotonic() < limit:
                    tracker.refresh(); time.sleep(.01)
                self.assertTrue((directory / 'client.pid').exists())
                client_pid = int((directory / 'client.pid').read_text())
                proof = tracker.verify_client(client_pid, executable=sys.executable, cwd=game, properties=properties)
                self.assertNotEqual(proof['observed_ancestry'][0]['pgid'], proof['observed_ancestry'][-1]['pgid'])
                self.assertEqual(proof['observed_ancestry'][-1]['pid'], launcher.pid)
                # This controlled sibling has the same user/executable/cwd/QA arguments, but no owned ancestry.
                foreign = subprocess.Popen([sys.executable, str(program), 'client', *args], cwd=game,
                                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                with self.assertRaises(identity.OwnershipError):
                    tracker.verify_client(foreign.pid, executable=sys.executable, cwd=game, properties=properties)
                report = tracker.cleanup(seconds=1)
                self.assertIn(client_pid, report['term_pids']); self.assertNotIn(foreign.pid, report['term_pids'])
                self.assertNotIn(foreign.pid, report['kill_pids']); self.assertIsNone(foreign.poll())
                launcher.wait(timeout=5)
            finally:
                if tracker.bindings: tracker.cleanup(seconds=1)
                # Only the fixture process created directly by this test is stopped here.
                if foreign is not None and foreign.poll() is None: foreign.terminate(); foreign.wait(timeout=5)
                if launcher is not None and launcher.poll() is None: launcher.terminate(); launcher.wait(timeout=5)


if __name__ == '__main__': unittest.main()
