"""Parser/ownership/migration regressions only; these do not render or run Minecraft."""
import copy
import importlib.util
from pathlib import Path
import tempfile
import time
from types import SimpleNamespace
import unittest
from unittest import mock

spec = importlib.util.spec_from_file_location('client_qa_startup', Path(__file__).resolve().parents[2] / 'scripts/client-qa/run.py')
qa = importlib.util.module_from_spec(spec)
spec.loader.exec_module(qa)


class StartupTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.now = int(time.time() * 1000)
        self.data = {'schema_version': 1, 'status': 'STARTUP', 'run_id': 'unit-test-only', 'source_sha': 'a' * 40,
            'mc': '26.3', 'loader': 'fabric', 'written_at_ms': self.now, 'game_directory': str(self.root.resolve()),
            'requested_gui_scale': 4, 'actual_gui_scale': 4, 'window_width': 1280, 'window_height': 960,
            'gui_width': 320, 'gui_height': 240, 'client_pid': 4321, 'x11_window_id': 7654,
            'window_title': 'Minecraft* 26.3', 'window_flags': 8224, 'game_load_finished': True,
            'client_player_present': False, 'client_level_present': False, 'integrated_server_present': False,
            'overlay_class': 'none', 'screen_class': 'net.minecraft.client.gui.screens.BackupConfirmScreen',
            'screen_title_key': 'selectWorld.backupQuestion.file_fixing_required',
            'screen_message_key': 'selectWorld.backupWarning.file_fixing_required',
            'buttons': [dict(label=key, translation_key=key, x=x, y=100, width=100, height=20, active=True)
                        for key, x in [('selectWorld.backupJoinConfirmButton', 0),
                                       ('selectWorld.backupJoinSkipButton', 110), ('gui.cancel', 0)]]}

    def validate(self, data):
        return qa.validate_startup(data, run_id='unit-test-only', sha='a' * 40, scale=4,
                                   launched_ms=self.now - 10, game=self.root)

    def driver(self):
        driver = qa.Driver(self.root, '26.3', self.root, 'a' * 40, self.root)
        driver.game = driver.case = self.root
        driver.run_id = 'unit-test-only'
        driver.scale = 4
        driver.process_tracker = SimpleNamespace(verify_client=mock.Mock(return_value={'pidfd_bound': True}))
        driver.environment['JAVA_HOME'] = str(self.root / 'unit-test-jdk')
        driver.launched_ms = self.now - 10
        driver.migration_phase = 'COPIED'
        driver.fixture_identity = {'run_id': driver.run_id, 'source_sha': driver.sha,
                                   'destination': str((self.root / 'saves/world').resolve())}
        return driver

    def join_prompt(self):
        data = copy.deepcopy(self.data)
        data.update(screen_class='net.minecraft.client.gui.screens.ConfirmScreen',
                    screen_title_key='upgradeWorld.done', screen_message_key='upgradeWorld.joinNow')
        data['buttons'] = [dict(label=key, translation_key=key, x=x, y=100, width=100, height=20, active=True)
                           for key, x in [('gui.yes', 0), ('gui.no', 110)]]
        return data

    def test_valid_diagnostic_is_never_readiness(self):
        self.validate(self.data)
        with self.assertRaises(qa.QaError):
            qa.validate_ready(self.data, run_id='unit-test-only', sha='a' * 40, mc='26.3', scale=4,
                              launched_ms=self.now - 10)

    def test_rejects_stale_wrong_nonce_sha_directory_dimensions_and_native_identity(self):
        for field, value in [('status', 'PASS'), ('run_id', 'old'), ('source_sha', 'b' * 40),
                             ('mc', '1.21.1'), ('loader', 'neoforge'), ('written_at_ms', self.now - 6000),
                             ('written_at_ms', float('nan')), ('written_at_ms', self.now + 6000),
                             ('game_directory', '/other'), ('actual_gui_scale', 3), ('gui_width', 319),
                             ('window_width', 854), ('client_pid', True), ('client_pid', 0),
                             ('x11_window_id', -1), ('window_title', 'unowned'),
                             ('window_flags', 8224 | 8), ('window_flags', 8224 | 64)]:
            with self.subTest(field=field, value=value), self.assertRaises(qa.QaError):
                data = copy.deepcopy(self.data); data[field] = value; self.validate(data)

    def test_only_known_backup_and_ordered_join_are_allowed(self):
        self.assertEqual(qa.migration_button(self.data, 'COPIED')['translation_key'],
                         'selectWorld.backupJoinConfirmButton')
        self.assertIsNone(qa.migration_button(self.data, 'UNVERIFIED'))
        self.assertIsNone(qa.migration_button(self.data, 'BACKUP_REQUESTED'))
        joined = self.join_prompt()
        self.assertIsNone(qa.migration_button(joined, 'COPIED'))
        self.assertEqual(qa.migration_button(joined, 'BACKUP_REQUESTED')['translation_key'], 'gui.yes')
        self.assertIsNone(qa.migration_button(joined, 'JOIN_REQUESTED'))

    def test_unknown_screen_version_warning_message_overlay_and_post_world_state_are_not_dismissed(self):
        for field, value in [('screen_class', 'other.ConfirmScreen'), ('screen_title_key', 'selectWorld.backupQuestion.downgrade'),
                             ('screen_message_key', 'unknown'), ('overlay_class', 'LoadingOverlay'),
                             ('game_load_finished', False), ('client_player_present', True),
                             ('client_level_present', True), ('integrated_server_present', True)]:
            with self.subTest(field=field):
                data = copy.deepcopy(self.data); data[field] = value
                self.assertIsNone(qa.migration_button(data, 'COPIED'))

    def test_known_prompt_rejects_unexpected_inactive_or_outside_controls(self):
        for field, value in [('active', False), ('translation_key', 'unknown'), ('width', 19),
                             ('height', 19), ('x', -1), ('y', 239), ('label', '')]:
            with self.subTest(field=field), self.assertRaises(qa.QaError):
                data = copy.deepcopy(self.data); data['buttons'][0][field] = value
                qa.migration_button(data, 'COPIED')
        data = copy.deepcopy(self.data); data['buttons'].append(copy.deepcopy(data['buttons'][0]))
        with self.assertRaises(qa.QaError): qa.migration_button(data, 'COPIED')

    def test_native_window_uses_exact_owned_sdl_id_pid_title_and_size(self):
        driver = self.driver()
        def checked(command, **kwargs):
            responses = {'search': '7654\n', 'getwindowpid': '4321\n', 'getwindowname': 'Minecraft* 26.3\n',
                         'getwindowgeometry': 'WIDTH=1280\nHEIGHT=960\nX=0\nY=0\n'}
            return SimpleNamespace(stdout=responses.get(command[1], ''))
        with mock.patch.object(qa, 'read_json', side_effect=[None, self.data]), \
                mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), mock.patch.object(qa, 'checked', side_effect=checked) as commands:
            self.assertEqual(driver.find_window(), (0, 0))
            self.assertEqual(driver.window_id, '7654')
            self.assertIn(mock.call(['xdotool', 'search', '--onlyvisible', '--pid', '4321'], capture_output=True), commands.call_args_list)
            self.assertFalse(any('--name' in call.args[0] for call in commands.call_args_list))

    def test_foreign_identity_duplicate_window_and_pid_title_mismatch_fail(self):
        for group, listing, pid, title in [(999, '7654\n', '4321', 'Minecraft* 26.3'),
                                          (1234, '7654\n9999\n', '4321', 'Minecraft* 26.3'),
                                          (1234, '7654\n', '9999', 'Minecraft* 26.3'),
                                          (1234, '7654\n', '4321', 'unowned')]:
            with self.subTest(group=group, listing=listing, pid=pid, title=title):
                def checked(command, **kwargs):
                    return SimpleNamespace(stdout={'search': listing, 'getwindowpid': pid, 'getwindowname': title}.get(command[1], ''))
                driver = self.driver()
                with mock.patch.object(qa, 'read_json', side_effect=[None, self.data]), \
                        mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}, side_effect=qa.process_identity.OwnershipError('foreign identity') if group != 1234 else None), mock.patch.object(qa, 'checked', side_effect=checked):
                    with self.assertRaises(qa.QaError): driver.find_window()

    def test_actual_prompt_capture_precedes_single_physical_click(self):
        driver = self.driver()
        driver.startup_observed = ((self.data['screen_class'], self.data['screen_title_key'],
                                    self.data['screen_message_key'], 'none'), self.now - 1)
        events = []
        with mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                mock.patch.object(qa, 'read_json', return_value=self.data), \
                mock.patch.object(driver, 'capture', side_effect=lambda *a, **kw: events.append('capture')), \
                mock.patch.object(driver, 'find_window'), \
                mock.patch.object(qa, 'checked', side_effect=lambda command, **kw: events.append(command[1])):
            driver.observe_startup(); driver.observe_startup()
        self.assertEqual(events, ['capture', 'mousemove', 'click'])
        self.assertEqual(driver.migration_phase, 'BACKUP_REQUESTED')
        self.assertTrue((self.root / 'migration-backup_requested.json').is_file())

    def test_failed_capture_or_wrong_fixture_identity_prevents_input(self):
        for failure in ('capture', 'identity'):
            driver = self.driver()
            driver.startup_observed = ((self.data['screen_class'], self.data['screen_title_key'],
                                        self.data['screen_message_key'], 'none'), self.now - 1)
            if failure == 'identity': driver.fixture_identity['destination'] = '/other/world'
            with mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                mock.patch.object(qa, 'read_json', return_value=self.data), \
                    mock.patch.object(driver, 'capture', side_effect=qa.QaError('capture failed') if failure == 'capture' else None), \
                    mock.patch.object(qa, 'checked') as checked:
                with self.assertRaises(qa.QaError): driver.observe_startup()
                checked.assert_not_called()

    def test_unknown_prompt_is_captured_without_input_or_phase_change(self):
        driver = self.driver(); data = copy.deepcopy(self.data); data['screen_title_key'] = 'unknown'
        driver.startup_observed = ((data['screen_class'], 'unknown', data['screen_message_key'], 'none'), self.now - 1)
        with mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                mock.patch.object(qa, 'read_json', return_value=data), mock.patch.object(driver, 'capture') as capture, \
                mock.patch.object(qa, 'checked') as checked:
            driver.observe_startup()
            capture.assert_called_once(); checked.assert_not_called()
        self.assertEqual(driver.migration_phase, 'COPIED')

    def test_identity_correct_stale_hidden_unscaled_or_unconfigured_startup_waits_without_input(self):
        for field, value in [('written_at_ms', self.now - 6000), ('window_flags', 8224 | 8),
                             ('window_flags', 8224 | 64), ('actual_gui_scale', 1),
                             ('window_width', 854), ('gui_height', 180), ('x11_window_id', 0),
                             ('window_title', '')]:
            with self.subTest(field=field, value=value):
                driver = self.driver(); driver.launched_ms = self.now - 10000
                data = copy.deepcopy(self.data); data[field] = value
                with mock.patch.object(qa, 'read_json', return_value=data), \
                        mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                        mock.patch.object(driver, 'capture') as capture, mock.patch.object(qa, 'checked') as checked:
                    driver.observe_startup(); driver.observe_startup()
                    capture.assert_not_called(); checked.assert_not_called()
                self.assertEqual(driver.migration_phase, 'COPIED')
                self.assertIsNone(driver.startup_observed)

    def test_incomplete_startup_cannot_hide_bad_identity_or_foreign_process(self):
        for field, value, group in [('run_id', 'old', 1234), ('source_sha', 'b' * 40, 1234),
                                    ('game_directory', '/other', 1234), ('requested_gui_scale', 3, 1234),
                                    ('written_at_ms', self.now + 6000, 1234), ('client_pid', 0, 1234),
                                    ('window_flags', 8, 9999)]:
            with self.subTest(field=field, value=value, group=group):
                driver = self.driver(); data = copy.deepcopy(self.data); data['window_flags'] = 8; data[field] = value
                with mock.patch.object(qa, 'read_json', return_value=data), \
                        mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}, side_effect=qa.process_identity.OwnershipError('foreign identity') if group != 1234 else None), \
                        mock.patch.object(driver, 'capture') as capture, mock.patch.object(qa, 'checked') as checked:
                    with self.assertRaises(qa.QaError): driver.observe_startup()
                    capture.assert_not_called(); checked.assert_not_called()

    def test_incomplete_startup_recovers_only_after_fresh_stable_full_diagnostics(self):
        driver = self.driver(); hidden = copy.deepcopy(self.data); hidden['window_flags'] |= 8
        first = copy.deepcopy(self.data); first['written_at_ms'] -= 1
        latest = copy.deepcopy(self.data); events = []
        with mock.patch.object(qa, 'read_json', side_effect=[hidden, first, latest, latest, latest]), \
                mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                mock.patch.object(driver, 'capture', side_effect=lambda *a, **kw: events.append('capture')), \
                mock.patch.object(driver, 'find_window'), \
                mock.patch.object(qa, 'checked', side_effect=lambda command, **kw: events.append(command[1])):
            driver.observe_startup(); self.assertEqual(events, [])
            driver.observe_startup(); self.assertEqual(events, [])
            driver.observe_startup()
        self.assertEqual(events, ['capture', 'mousemove', 'click'])
        self.assertEqual(driver.migration_phase, 'BACKUP_REQUESTED')

    def test_prompt_overlay_controls_or_freshness_changed_after_capture_prevents_click(self):
        for change in ('prompt', 'overlay', 'geometry', 'stale'):
            for after_move in (False, True):
                with self.subTest(change=change, after_move=after_move):
                    driver = self.driver(); driver.launched_ms = self.now - 10000
                    driver.startup_observed = ((self.data['screen_class'], self.data['screen_title_key'],
                                                self.data['screen_message_key'], 'none'), self.now - 1)
                    changed = copy.deepcopy(self.data)
                    if change == 'prompt': changed['screen_title_key'] = 'unknown'
                    if change == 'overlay': changed['overlay_class'] = 'LoadingOverlay'
                    if change == 'geometry': changed['buttons'][0]['x'] += 1
                    if change == 'stale': changed['written_at_ms'] -= 6000
                    reads = [self.data, self.data, changed] if after_move else [self.data, changed]
                    events = []
                    with mock.patch.object(qa, 'read_json', side_effect=reads), \
                            mock.patch.object(driver.process_tracker, 'verify_client', return_value={'pidfd_bound': True}), \
                            mock.patch.object(driver, 'capture', side_effect=lambda *a, **kw: events.append('capture')), \
                            mock.patch.object(driver, 'find_window'), \
                            mock.patch.object(qa, 'checked', side_effect=lambda command, **kw: events.append(command[1])):
                        with self.assertRaises(qa.QaError): driver.observe_startup()
                    self.assertEqual(events, ['capture', 'mousemove'] if after_move else ['capture'])
                    self.assertEqual(driver.migration_phase, 'COPIED')
                    self.assertFalse((self.root / 'migration-backup_requested.json').exists())



if __name__ == '__main__': unittest.main()
