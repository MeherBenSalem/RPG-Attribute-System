"""Pure timing/snapshot regressions; no Minecraft or native-render acceptance."""
import copy
import json
from types import SimpleNamespace
import unittest
from unittest import mock
import test_startup as startup
import test_window_identity as native_tests

qa = startup.qa


class CaptureFreshnessTests(unittest.TestCase):
    setUp = startup.StartupTests.setUp
    driver = startup.StartupTests.driver

    def ready(self):
        data = copy.deepcopy(self.data)
        data.update(status='READY', state='ALLOCATION', page=0, sequence=10,
                    server_player_bound=True, client_player_present=True, client_level_present=True,
                    expected_attributes_synced=True, synced_attribute_ids=sorted(qa.EXPECTED_IDS),
                    level=0, spare_points=6, next_level_xp=140,
                    panel=dict(x=0, y=0, width=320, height=240, native_scale=1, rows=1),
                    player_variables={'total_xp': 0, 'attributes': {'attribute_5': .1}},
                    movement_speed_base=.1, movement_speed_value=.1)
        for button in data['buttons']: button['focused'] = False
        data['buttons'][0]['focused'] = True
        return data

    def fake_clock(self):
        value = SimpleNamespace(elapsed=0)
        def sleep(seconds): value.elapsed += seconds
        return value, mock.patch.object(qa.time, 'monotonic', side_effect=lambda: value.elapsed), \
            mock.patch.object(qa.time, 'time', side_effect=lambda: self.now/1000 + value.elapsed), \
            mock.patch.object(qa.time, 'sleep', side_effect=sleep)

    def pointer(self, x=1, y=1, **kwargs):
        return dict(pointer_same_screen=True, pointer_on_window=True, pointer_x=x, pointer_y=y, **kwargs)

    def progress(self):
        data = copy.deepcopy(self.data)
        data.update(screen_class='net.minecraft.client.gui.screens.worldselection.UpgradeFilesScreen',
                    screen_title_key='upgradeWorld.title')
        data.pop('screen_message', None); data.pop('screen_message_key', None)
        data['buttons'] = [dict(label='Cancel', translation_key='gui.cancel', x=0, y=100,
                                width=100, height=20, active=True)]
        return data

    def key(self, data):
        return (data['screen_class'], data.get('screen_title_key'), data.get('screen_message_key'), data['overlay_class'])

    def test_already_parked_exact_owned_native_pointer_has_no_input_or_departure_wait(self):
        driver = self.driver(); driver.window_id = '7654'; ready = self.ready()
        driver.verify_native_window.return_value = self.pointer()
        with mock.patch.object(qa, 'read_json', return_value=ready), mock.patch.object(qa, 'checked') as commands:
            driver.park_native_pointer(ready, (1, 1))
        commands.assert_not_called()
        self.assertEqual(driver.verify_native_window.call_args.args, (ready,))
        self.assertTrue(driver.verify_native_window.call_args.kwargs['require_focus'])
        self.assertLessEqual(driver.verify_native_window.call_args.kwargs['deadline'], driver.deadline)

    def test_async_move_uses_no_sync_and_polls_actual_target_once_with_fresh_owner_focus_checks(self):
        driver = self.driver(); driver.window_id = '7654'; ready = self.ready()
        driver.verify_native_window.side_effect = [self.pointer(9, 9), self.pointer(9, 9), self.pointer()]
        clock, monotonic, wall, sleep = self.fake_clock()
        with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', return_value=ready), \
                mock.patch.object(qa, 'checked') as commands:
            driver.park_native_pointer(ready, (1, 1))
        self.assertEqual(commands.call_args.args[0], ['xdotool', 'mousemove', '--window', '7654', '1', '1'])
        self.assertEqual(commands.call_count, 1); self.assertLessEqual(commands.call_args.kwargs['timeout'], 3)
        self.assertEqual(driver.verify_native_window.call_count, 3); self.assertLess(clock.elapsed, 3)

    def test_stuck_pointer_fails_at_absolute_bound_without_capture_or_repeated_input(self):
        driver = self.driver(); driver.window_id = '7654'; ready = self.ready()
        driver.verify_native_window.return_value = self.pointer(9, 9)
        deadline = driver.deadline
        clock, monotonic, wall, sleep = self.fake_clock()
        with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', return_value=ready), \
                mock.patch.object(qa, 'checked') as commands, mock.patch.object(driver, 'capture') as capture:
            with self.assertRaisesRegex(qa.QaError, 'within bound'): driver.park_native_pointer(ready, (1, 1))
        self.assertGreaterEqual(clock.elapsed, 3); self.assertLess(clock.elapsed, 3.1)
        self.assertEqual(commands.call_count, 1); capture.assert_not_called(); self.assertEqual(driver.deadline, deadline)

    def test_foreign_pointer_or_failed_native_owner_focus_drawability_never_authorizes_parking_input(self):
        for change in ('screen', 'window', 'owner', 'focus', 'drawable'):
            driver = self.driver(); driver.window_id = '7654'; ready = self.ready()
            facts = self.pointer()
            if change == 'screen': facts['pointer_same_screen'] = False
            if change == 'window': facts['pointer_on_window'] = False
            driver.verify_native_window.return_value = facts
            if change in ('owner', 'focus', 'drawable'): driver.verify_native_window.side_effect = qa.QaError(change)
            with self.subTest(change=change), mock.patch.object(qa, 'read_json', return_value=ready), \
                    mock.patch.object(qa, 'checked') as commands:
                with self.assertRaises(qa.QaError): driver.park_native_pointer(ready, (1, 1))
                commands.assert_not_called()

    def test_owner_or_pointer_foreign_after_move_fails_before_capture(self):
        for change in ('owner', 'pointer'):
            driver = self.driver(); driver.window_id = '7654'; ready = self.ready()
            foreign = self.pointer(); foreign['pointer_on_window'] = False
            driver.verify_native_window.side_effect = [self.pointer(9, 9), qa.QaError('foreign') if change == 'owner' else foreign]
            clock, monotonic, wall, sleep = self.fake_clock()
            with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', return_value=ready), \
                    mock.patch.object(qa, 'checked') as commands:
                with self.assertRaises(qa.QaError): driver.park_native_pointer(ready, (1, 1))
            self.assertEqual(commands.call_count, 1)

    def test_stale_expected_refreshes_only_counters_and_archives_exact_fresh_record(self):
        driver = self.driver(); driver.window_id = '7654'; driver.launched_ms = self.now-30000
        expected = self.ready(); expected['written_at_ms'] -= 20000
        fresh = copy.deepcopy(expected); fresh.update(written_at_ms=self.now, sequence=400)
        original = copy.deepcopy(expected)
        image = SimpleNamespace(size=(1280, 960), getextrema=lambda: [(0, 255)]*3)
        image.convert = lambda mode: image
        opened = mock.MagicMock(); opened.__enter__.return_value = image
        with mock.patch.object(qa, 'read_json', return_value=fresh), \
                mock.patch.object(driver, 'find_window', return_value=(0, 0)), \
                mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=SimpleNamespace(open=lambda path: opened))}), \
                mock.patch.dict(qa.os.environ, {'DISPLAY': ':84'}), mock.patch.object(qa, 'checked') as commands:
            driver.capture('pure-unit', expected, verify=False)
        self.assertEqual(json.loads((self.root/'pure-unit.json').read_text()), fresh)
        self.assertEqual(expected, original); self.assertEqual(commands.call_count, 1)

    def test_refresh_waits_bounded_for_fresh_identical_record_without_input(self):
        driver = self.driver(); driver.launched_ms = self.now-30000
        stale = self.ready(); stale['written_at_ms'] -= 6000
        fresh = copy.deepcopy(stale); fresh.update(written_at_ms=self.now, sequence=11)
        clock, monotonic, wall, sleep = self.fake_clock()
        with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', side_effect=[stale, fresh]), \
                mock.patch.object(qa, 'checked') as commands:
            self.assertEqual(driver.fresh_capture_snapshot(stale), fresh)
        commands.assert_not_called(); self.assertEqual(clock.elapsed, .05)

    def test_refresh_stale_or_missing_record_times_out_without_extending_case_deadline(self):
        for missing in (False, True):
            driver = self.driver(); driver.launched_ms = self.now-30000; driver.deadline = .2
            stale = self.ready(); stale['written_at_ms'] -= 6000
            clock, monotonic, wall, sleep = self.fake_clock()
            with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', return_value=None if missing else stale), \
                    mock.patch.object(qa, 'checked') as commands:
                with self.assertRaisesRegex(qa.QaError, 'within bound'): driver.fresh_capture_snapshot(stale)
            commands.assert_not_called(); self.assertEqual(driver.deadline, .2); self.assertLessEqual(clock.elapsed, .25)

    def test_refresh_rejects_any_state_control_label_focus_player_prompt_sync_or_window_substitution(self):
        for change in ('state', 'page', 'control', 'label', 'active', 'focus', 'player', 'speed', 'sync',
                       'prompt', 'overlay', 'window', 'scale', 'flags', 'unexpected', 'numeric-type', 'boolean-type'):
            driver = self.driver(); expected = self.ready(); latest = copy.deepcopy(expected)
            if change == 'state': latest['state'] = 'COMBAT'
            if change == 'page': latest['page'] = 1
            if change == 'control': latest['buttons'][0]['x'] += 1
            if change == 'label': latest['buttons'][0]['label'] = 'other'
            if change == 'active': latest['buttons'][0]['active'] = False
            if change == 'focus': latest['buttons'][0]['focused'] = False
            if change == 'player': latest['player_variables']['attributes']['attribute_5'] = .1025
            if change == 'speed': latest['movement_speed_value'] = .1025
            if change == 'sync': latest['server_player_bound'] = False
            if change == 'prompt': latest['screen_message_key'] = 'other'
            if change == 'overlay': latest['overlay_class'] = 'other'
            if change == 'window': latest['x11_window_id'] = 9999
            if change == 'scale': latest['actual_gui_scale'] = 3
            if change == 'flags': latest['window_flags'] |= 8
            if change == 'unexpected': latest['private-untrusted-value'] = 'do-not-export'
            if change == 'numeric-type': latest['player_variables']['total_xp'] = 0.0
            if change == 'boolean-type': latest['buttons'][0]['focused'] = 1
            with self.subTest(change=change), mock.patch.object(qa, 'read_json', return_value=latest), \
                    mock.patch.object(qa, 'checked') as commands:
                with self.assertRaises(qa.QaError) as caught: driver.fresh_capture_snapshot(expected)
                commands.assert_not_called(); self.assertNotIn('private-untrusted-value', str(caught.exception))
                self.assertNotIn('do-not-export', str(caught.exception))

    def test_refresh_rejects_timestamp_sequence_regressions_including_while_waiting(self):
        for change in ('timestamp', 'sequence', 'sequence-type', 'during-wait'):
            driver = self.driver(); driver.launched_ms = self.now-30000
            expected = self.ready(); latest = copy.deepcopy(expected)
            if change == 'timestamp': latest['written_at_ms'] -= 1
            if change == 'sequence': latest['sequence'] -= 1
            if change == 'sequence-type': latest['sequence'] = True
            if change == 'during-wait':
                expected['written_at_ms'] -= 6000
                latest = copy.deepcopy(expected); latest['sequence'] += 2
                regressed = copy.deepcopy(latest); regressed['sequence'] -= 1
            clock, monotonic, wall, sleep = self.fake_clock()
            with monotonic, wall, sleep, mock.patch.object(qa, 'read_json',
                    side_effect=[latest, regressed] if change == 'during-wait' else None,
                    return_value=latest), self.subTest(change=change):
                with self.assertRaisesRegex(qa.QaError, 'regressed'): driver.fresh_capture_snapshot(expected)

    def test_actual_capture_keeps_five_second_anchor_and_current_freshness_limits(self):
        driver = self.driver(); driver.launched_ms = self.now-30000
        fresh = self.ready(); captured = copy.deepcopy(fresh); captured['written_at_ms'] -= 6000
        with mock.patch.object(qa, 'read_json', return_value=fresh), self.assertRaisesRegex(qa.QaError, 'expired'):
            driver.validate_capture_snapshot(captured)
        with mock.patch.object(qa, 'read_json', return_value=captured), self.assertRaises(qa.QaError):
            driver.validate_capture_snapshot(captured)

    def test_parking_carries_latest_counter_anchor_across_native_polls(self):
        driver = self.driver(); driver.window_id = '7654'; expected = self.ready()
        advanced = copy.deepcopy(expected); advanced['sequence'] = 12
        regressed = copy.deepcopy(advanced); regressed['sequence'] = 11
        driver.verify_native_window.return_value = self.pointer(9, 9)
        clock, monotonic, wall, sleep = self.fake_clock()
        with monotonic, wall, sleep, mock.patch.object(qa, 'read_json', side_effect=[expected, advanced, regressed]), \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaisesRegex(qa.QaError, 'regressed'): driver.park_native_pointer(expected, (1, 1))
        self.assertEqual(commands.call_count, 1); self.assertEqual(driver.verify_native_window.call_count, 2)

    def test_native_poll_at_end_of_parking_bound_gets_remaining_timeout_and_cannot_overrun_success(self):
        driver = self.driver(); del driver.verify_native_window
        ready = self.ready(); clock, monotonic, wall, sleep = self.fake_clock(); clock.elapsed = 2.95
        def slow(*args, **kwargs):
            clock.elapsed += .06
            facts = native_tests.WindowTests.facts(self)
            return SimpleNamespace(stdout=json.dumps(facts), returncode=0)
        with monotonic, wall, sleep, mock.patch.object(qa.subprocess, 'run', side_effect=slow) as query, \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaisesRegex(qa.QaError, 'absolute deadline'):
                driver.verify_native_window(ready, require_focus=True, deadline=3)
        self.assertGreater(query.call_args.kwargs['timeout'], 0)
        self.assertLessEqual(query.call_args.kwargs['timeout'], .051); commands.assert_not_called()

    def test_immediate_pre_frame_counter_is_the_post_frame_and_archived_anchor(self):
        for regress in (False, True):
            driver = self.driver(); driver.window_id = '7654'; expected = self.ready()
            records = []
            for sequence in (11, 12, 13, 12 if regress else 14):
                item = copy.deepcopy(expected); item['sequence'] = sequence; records.append(item)
            image = SimpleNamespace(size=(1280, 960), getextrema=lambda: [(0, 255)]*3)
            image.convert = lambda mode: image
            opened = mock.MagicMock(); opened.__enter__.return_value = image
            with self.subTest(regress=regress), mock.patch.object(qa, 'read_json', side_effect=records), \
                    mock.patch.object(driver, 'find_window', return_value=(0, 0)), \
                    mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=SimpleNamespace(open=lambda path: opened))}), \
                    mock.patch.dict(qa.os.environ, {'DISPLAY': ':84'}), mock.patch.object(qa, 'checked') as commands:
                if regress:
                    with self.assertRaisesRegex(qa.QaError, 'regressed'): driver.capture('guard', expected, verify=False)
                else:
                    driver.capture('guard', expected, verify=False)
                    self.assertEqual(json.loads((self.root/'guard.json').read_text())['sequence'], 13)
                    self.assertEqual(driver.last_capture_snapshot['sequence'], 14)
                self.assertEqual(commands.call_count, 1)

    def test_stable_second_frame_carries_first_frame_latest_counter(self):
        driver = self.driver(); expected = self.ready(); expected['state'] = 'WORLD'
        advanced = copy.deepcopy(expected); advanced['sequence'] = 12
        regressed = copy.deepcopy(advanced); regressed['sequence'] = 11
        calls = []
        def capture(name, ready):
            calls.append(ready['sequence'])
            if len(calls) == 1: driver.last_capture_snapshot = advanced
            else: driver.fresh_capture_snapshot(ready)
        clock, monotonic, wall, sleep = self.fake_clock()
        with monotonic, wall, sleep, mock.patch.object(driver, 'capture', side_effect=capture), \
                mock.patch.object(qa, 'read_json', return_value=regressed):
            with self.assertRaisesRegex(qa.QaError, 'regressed'): driver.stable_capture('pure-unit', expected)
        self.assertEqual(calls, [10, 12])

    def test_progress_transition_rejects_raw_frame_and_archives_sanitized_timing_fields_without_success_json(self):
        driver = self.driver(); driver.window_id = '7654'; progress = self.progress()
        latest = startup.StartupTests.join_prompt(self); latest['written_at_ms'] += 1
        def frame(command, **kwargs):
            (self.root/'startup-unit.png').write_bytes(b'pure-unit-frame-placeholder')
        with mock.patch.object(qa, 'read_json', side_effect=[progress, progress, progress, latest]), \
                mock.patch.object(driver, 'find_window', return_value=(0, 0)), \
                mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=mock.Mock())}), \
                mock.patch.dict(qa.os.environ, {'DISPLAY': ':84'}), mock.patch.object(qa, 'checked', side_effect=frame):
            with self.assertRaises(qa.StartupSnapshotTransition): driver.capture('startup-unit', progress, verify=False)
        self.assertFalse((self.root/'startup-unit.png').exists()); self.assertFalse((self.root/'startup-unit.json').exists())
        self.assertEqual((self.root/'startup-unit-rejected-001.png').read_bytes(), b'pure-unit-frame-placeholder')
        receipt = json.loads((self.root/'startup-unit-rejected-001.json').read_text())
        self.assertEqual(receipt['status'], 'REJECTED_UNVERIFIED_CAPTURE'); self.assertEqual(receipt['stage'], 'post_frame')
        self.assertEqual(receipt['captured_written_at_ms'], progress['written_at_ms'])
        self.assertEqual(receipt['latest_written_at_ms'], latest['written_at_ms'])
        self.assertIn('screen_class', receipt['differing_fields']); self.assertIn('buttons', receipt['differing_fields'])
        self.assertNotIn('buttons', receipt); self.assertNotIn('screen_title', receipt)
        driver.verify_native_window.assert_called_once_with(latest)

    def test_observation_only_transition_waits_then_requires_new_stable_prompt_capture_before_join(self):
        driver = self.driver(); driver.migration_phase = 'BACKUP_REQUESTED'; progress = self.progress()
        driver.startup_observed = (self.key(progress), self.now-1)
        deadline = driver.deadline
        rejection = qa.StartupSnapshotTransition('actual-transition', {})
        with mock.patch.object(qa, 'read_json', return_value=progress), \
                mock.patch.object(driver, 'capture', side_effect=rejection), mock.patch.object(qa, 'checked') as commands:
            driver.observe_startup()
        commands.assert_not_called(); self.assertEqual(driver.migration_phase, 'BACKUP_REQUESTED')
        self.assertIsNone(driver.startup_observed); self.assertEqual(driver.startup_captures, set())
        self.assertEqual(driver.deadline, deadline)
        latest = startup.StartupTests.join_prompt(self); events = []
        with mock.patch.object(qa, 'read_json', return_value=latest), \
                mock.patch.object(driver, 'capture', side_effect=startup.StartupTests.accepted_capture(driver, events)), \
                mock.patch.object(driver, 'find_window'), \
                mock.patch.object(qa, 'checked', side_effect=lambda cmd, **kwargs: events.append(cmd[1])):
            driver.observe_startup(); self.assertEqual(events, [])
            latest['written_at_ms'] += 1; driver.observe_startup()
        self.assertEqual(events, ['capture', 'mousemove', 'click']); self.assertEqual(driver.migration_phase, 'JOIN_REQUESTED')

    def test_actionable_prompt_transition_is_fatal_and_never_resettles_or_clicks(self):
        driver = self.driver(); driver.startup_observed = (self.key(self.data), self.now-1)
        with mock.patch.object(qa, 'read_json', return_value=self.data), \
                mock.patch.object(driver, 'capture', side_effect=qa.StartupSnapshotTransition('changed', {})), \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaises(qa.StartupSnapshotTransition): driver.observe_startup()
        commands.assert_not_called(); self.assertEqual(driver.migration_phase, 'COPIED')

    def test_cached_same_prompt_key_never_substitutes_for_new_actionable_capture(self):
        driver = self.driver(); key = self.key(self.data); driver.startup_observed = (key, self.now-1)
        driver.startup_captures.add(key)
        old = copy.deepcopy(self.data); old['game_load_finished'] = False
        driver.last_capture_snapshot = old
        with mock.patch.object(qa, 'read_json', return_value=self.data), \
                mock.patch.object(driver, 'capture', side_effect=qa.QaError('new capture failed')) as capture, \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaises(qa.QaError): driver.observe_startup()
        capture.assert_called_once(); commands.assert_not_called(); self.assertIsNone(driver.last_capture_snapshot)
        self.assertEqual(driver.migration_phase, 'COPIED')

    def test_successful_capture_and_pre_move_timestamps_anchor_each_subsequent_prompt_check(self):
        for stage in ('pre-move', 'pre-click'):
            driver = self.driver(); driver.startup_observed = (self.key(self.data), self.now-1)
            captured = copy.deepcopy(self.data); captured['written_at_ms'] += 2
            pre = copy.deepcopy(captured); pre['written_at_ms'] += -1 if stage == 'pre-move' else 1
            after = copy.deepcopy(captured)
            def accept(name, data, **kwargs): driver.last_capture_snapshot = captured
            events = []
            with self.subTest(stage=stage), mock.patch.object(qa, 'read_json', side_effect=[self.data, pre, after]), \
                    mock.patch.object(driver, 'capture', side_effect=accept), mock.patch.object(driver, 'find_window'), \
                    mock.patch.object(qa, 'checked', side_effect=lambda cmd, **kwargs: events.append(cmd[1])):
                with self.assertRaisesRegex(qa.QaError, 'changed'): driver.observe_startup()
            self.assertEqual(events, [] if stage == 'pre-move' else ['mousemove'])
            self.assertEqual(driver.migration_phase, 'COPIED')

    def test_startup_integrity_malformed_foreign_expired_or_counter_errors_are_not_transient(self):
        for change in ('nonce', 'pid', 'xid', 'width', 'malformed-control', 'malformed-title', 'foreign', 'regression', 'expired', 'expired-anchor'):
            driver = self.driver(); progress = self.progress(); latest = startup.StartupTests.join_prompt(self)
            if change == 'nonce': latest['run_id'] = 'foreign'
            if change == 'pid': latest['client_pid'] = 9999
            if change == 'xid': latest['x11_window_id'] = 9999
            if change == 'width': latest['window_width'] = 854
            if change == 'malformed-control': latest['buttons'][0]['x'] = -1
            if change == 'malformed-title': latest['screen_title_key'] = 123
            if change == 'foreign': driver.verify_native_window.side_effect = qa.QaError('foreign server owner')
            if change == 'regression': latest['written_at_ms'] -= 1
            if change == 'expired': driver.launched_ms -= 10000; latest['written_at_ms'] -= 6000
            if change == 'expired-anchor': driver.launched_ms -= 10000; progress['written_at_ms'] -= 6000
            with self.subTest(change=change), mock.patch.object(qa, 'read_json', return_value=latest):
                with self.assertRaises(qa.QaError) as caught: driver.matching_capture_snapshot(progress)
                self.assertNotIsInstance(caught.exception, qa.StartupSnapshotTransition)

    def test_ready_screen_transition_remains_terminal(self):
        driver = self.driver(); expected = self.ready(); latest = copy.deepcopy(expected); latest['state'] = 'COMBAT'
        with mock.patch.object(qa, 'read_json', return_value=latest):
            with self.assertRaises(qa.CaptureSnapshotError) as caught: driver.fresh_capture_snapshot(expected)
        self.assertNotIsInstance(caught.exception, qa.StartupSnapshotTransition)

    def test_cached_nonactionable_then_eligible_prompt_and_next_prompt_keep_distinct_accepted_frame_names(self):
        driver = self.driver(); names = []
        nonactionable = copy.deepcopy(self.data); nonactionable['game_load_finished'] = False
        driver.startup_observed = (self.key(nonactionable), self.now-1)
        def accept(name, data, **kwargs):
            names.append(name); driver.last_capture_snapshot = copy.deepcopy(data)
        latest = self.data
        with mock.patch.object(qa, 'read_json', side_effect=lambda path: latest), \
                mock.patch.object(driver, 'capture', side_effect=accept), mock.patch.object(driver, 'find_window'), \
                mock.patch.object(qa, 'checked'):
            latest = nonactionable; driver.observe_startup()
            latest = copy.deepcopy(self.data); latest['written_at_ms'] += 1; driver.observe_startup()
            latest = startup.StartupTests.join_prompt(self); latest['written_at_ms'] += 2; driver.observe_startup()
            latest['written_at_ms'] += 1; driver.observe_startup()
        self.assertEqual(names, ['startup-01', 'startup-02', 'startup-03'])
        self.assertEqual(driver.startup_capture_count, 3); self.assertEqual(len(driver.startup_captures), 2)

    def test_prepare_rejection_never_attributes_an_old_rejected_png_to_this_attempt(self):
        driver = self.driver(); old = self.root/'slot-rejected-001.png'; old.write_bytes(b'earlier-attempt')
        progress = self.progress(); latest = startup.StartupTests.join_prompt(self)
        with mock.patch.object(qa, 'read_json', return_value=latest), \
                mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=mock.Mock())}), \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaises(qa.StartupSnapshotTransition): driver.capture('slot', progress, verify=False)
        commands.assert_not_called(); self.assertEqual(old.read_bytes(), b'earlier-attempt')
        receipt = json.loads((self.root/'slot-rejected-001.json').read_text())
        self.assertEqual(receipt['stage'], 'prepare'); self.assertIsNone(receipt['rejected_frame'])

    def test_startup_transition_requires_always_emitted_title_and_title_key_strings_on_both_records(self):
        for record in ('captured', 'latest'):
            for key in ('screen_title', 'screen_title_key'):
                for missing in (False, True):
                    driver = self.driver(); captured = self.progress(); latest = startup.StartupTests.join_prompt(self)
                    target = captured if record == 'captured' else latest
                    if missing: target.pop(key)
                    else: target[key] = None
                    with self.subTest(record=record, key=key, missing=missing), \
                            mock.patch.object(qa, 'read_json', return_value=latest):
                        with self.assertRaisesRegex(qa.QaError, 'Malformed startup') as caught:
                            driver.matching_capture_snapshot(captured)
                    self.assertNotIsInstance(caught.exception, qa.StartupSnapshotTransition)
                    driver.verify_native_window.assert_not_called()

    def test_exact_backup_and_confirm_classes_require_string_message_pair_optional_on_other_screens(self):
        for record in ('captured', 'latest'):
            for screen in ('net.minecraft.client.gui.screens.BackupConfirmScreen', 'net.minecraft.client.gui.screens.ConfirmScreen'):
                for key in ('screen_message', 'screen_message_key'):
                    for missing in (False, True):
                        driver = self.driver(); captured = self.progress(); latest = startup.StartupTests.join_prompt(self)
                        target = captured if record == 'captured' else latest
                        target.update(screen_class=screen, screen_message='ordinary test message', screen_message_key='test.key')
                        if missing: target.pop(key)
                        else: target[key] = None
                        with self.subTest(record=record, screen=screen, key=key, missing=missing), \
                                mock.patch.object(qa, 'read_json', return_value=latest):
                            with self.assertRaisesRegex(qa.QaError, 'Malformed startup') as caught:
                                driver.matching_capture_snapshot(captured)
                        self.assertNotIsInstance(caught.exception, qa.StartupSnapshotTransition)
        driver = self.driver(); progress = self.progress(); latest = startup.StartupTests.join_prompt(self)
        self.assertNotIn('screen_message', progress); self.assertNotIn('screen_message_key', progress)
        with mock.patch.object(qa, 'read_json', return_value=latest), self.assertRaises(qa.StartupSnapshotTransition):
            driver.matching_capture_snapshot(progress)

    def test_repeated_rejected_attempts_preserve_unique_receipt_and_pixel_associations(self):
        driver = self.driver()
        failure = qa.StartupSnapshotTransition('ordinary observed transition',
            {'captured_written_at_ms': self.now, 'latest_written_at_ms': self.now+1, 'differing_fields': ['screen_class']})
        def produced_frame(*args):
            driver.capture_stage = 'post_frame'
            (self.root/'same-slot.png').write_bytes(b'earlier-rejected-native-placeholder')
            raise failure
        with mock.patch.object(driver, 'capture_frame', side_effect=produced_frame):
            with self.assertRaises(qa.StartupSnapshotTransition): driver.capture('same-slot', self.progress(), verify=False)
        earlier_json = (self.root/'same-slot-rejected-001.json').read_bytes()
        earlier_png = (self.root/'same-slot-rejected-001.png').read_bytes()
        with mock.patch.object(driver, 'capture_frame', side_effect=failure):
            with self.assertRaises(qa.StartupSnapshotTransition): driver.capture('same-slot', self.progress(), verify=False)
        self.assertEqual((self.root/'same-slot-rejected-001.json').read_bytes(), earlier_json)
        self.assertEqual((self.root/'same-slot-rejected-001.png').read_bytes(), earlier_png)
        self.assertEqual(json.loads(earlier_json)['rejected_frame'], 'same-slot-rejected-001.png')
        self.assertIsNone(json.loads((self.root/'same-slot-rejected-002.json').read_text())['rejected_frame'])
        self.assertEqual(driver.capture_rejection_count, 2); self.assertEqual(driver.startup_capture_count, 0)


if __name__ == '__main__': unittest.main()
