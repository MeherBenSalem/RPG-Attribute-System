"""Source/ABI/parser/adversarial checks; positive UNIX XRes proof is a separate CI preflight."""
import copy
import ctypes
import json
import subprocess
import time
from types import SimpleNamespace
import unittest
from unittest import mock
import test_startup as startup
qa = startup.qa

w = qa.window_identity


class WindowTests(unittest.TestCase):
    setUp = startup.StartupTests.setUp
    driver = startup.StartupTests.driver

    def facts(self):
        return {'schema_version': 1, 'display': ':84', 'xid': 7654,
            'written_at_ms': int(time.time()*1000), 'exists': True, 'x_errors': [],
            'xres_version': [1, 2], 'matching_client_ranges': 1, 'resource_base': 4096,
            'resource_mask': 4095, 'xres_query_success': True,
            'server_ids': [{'resource_base': 4096, 'mask': 2, 'length_bytes': 4, 'pid': 4321}],
            'default_root': 99, 'root': 99, 'window_class': 1, 'net_wm_pid': None,
            'map_state': 2, 'title': 'Minecraft* 26.3', 'translated': True,
            'width': 1280, 'height': 960, 'border_width': 0, 'override_redirect': False,
            'x': 0, 'y': 0, 'root_width': 1280, 'root_height': 960, 'root_child': True,
            'overlapping_windows_above': 0, 'focused': True, 'pointer_same_screen': True,
            'pointer_on_window': True, 'pointer_x': 200, 'pointer_y': 440}

    def owner(self, facts):
        return w.validate_owner(facts, xid=7654, pid=4321, display=':84')

    def drawable(self, facts):
        return w.drawable(facts, title='Minecraft* 26.3', width=1280, height=960)

    def actual_driver(self):
        driver = self.driver(); del driver.verify_native_window
        return driver

    def response(self, facts=None, **kwargs):
        data = self.facts() if facts is None else copy.deepcopy(facts)
        data['written_at_ms'] = int(time.time()*1000)
        return SimpleNamespace(stdout=json.dumps(data), returncode=0)

    def test_official_native_abi_sizes_and_offsets(self):
        self.assertEqual(ctypes.sizeof(ctypes.c_ulong), 8)
        self.assertEqual(ctypes.sizeof(w.Attributes), 136)
        self.assertEqual(w.Attributes.root.offset, 32)
        self.assertEqual(w.Attributes.map_state.offset, 92)
        self.assertEqual(ctypes.sizeof(w.ClientSpec), 16)
        self.assertEqual(w.ClientSpec.mask.offset, 8)
        self.assertEqual(ctypes.sizeof(w.ClientValue), 32)
        self.assertEqual(w.ClientValue.length.offset, 16)
        self.assertEqual(w.ClientValue.value.offset, 24)

    def test_native_fact_receipts_keep_their_own_error_snapshot_across_later_queries(self):
        # Exercise the actual facts() receipt lifecycle using mocked native calls, not an X server.
        native = object.__new__(w.NativeDisplay)
        native.name, native.display, native.errors = ':84', 1, []
        native.x, native.res = mock.Mock(), mock.Mock()
        native.x.XDefaultRootWindow.return_value = 99
        ranges = (w.ClientRange * 1)(w.ClientRange(4096, 4095))
        identities = (w.ClientValue * 1)(w.ClientValue(w.ClientSpec(4096, 2), 4, 1))
        windows = (ctypes.c_ulong * 1)(7654)
        def set_value(pointer, kind, value):
            ctypes.cast(pointer, ctypes.POINTER(kind))[0] = value
        def attributes(display, xid, pointer):
            if xid == 9999:
                native.errors.append({'resource_id': xid, 'code': 3, 'request': 3})
                return 0
            value = ctypes.cast(pointer, ctypes.POINTER(w.Attributes)).contents
            value.root, value.window_class, value.map_state = 99, 1, 2
            value.width, value.height = 1280, 960
            return 1
        def version(display, major, minor):
            set_value(major, ctypes.c_int, 1); set_value(minor, ctypes.c_int, 2); return 1
        def clients(display, count, pointer):
            set_value(count, ctypes.c_int, 1)
            set_value(pointer, ctypes.POINTER(w.ClientRange), ctypes.cast(ranges, ctypes.POINTER(w.ClientRange)))
            return 1
        def client_ids(display, count, spec, number, pointer):
            set_value(number, ctypes.c_long, 1)
            set_value(pointer, ctypes.POINTER(w.ClientValue), ctypes.cast(identities, ctypes.POINTER(w.ClientValue)))
            return 0
        def tree(display, root, returned_root, parent, pointer, count):
            set_value(count, ctypes.c_uint, 1)
            set_value(pointer, ctypes.POINTER(ctypes.c_ulong), ctypes.cast(windows, ctypes.POINTER(ctypes.c_ulong)))
            return 1
        native.x.XGetWindowAttributes.side_effect = attributes
        native.x.XTranslateCoordinates.return_value = 1
        native.x.XQueryPointer.return_value = 0
        native.x.XQueryTree.side_effect = tree
        native.res.XResQueryVersion.side_effect = version
        native.res.XResQueryClients.side_effect = clients
        native.res.XResQueryClientIds.side_effect = client_ids
        native.res.XResGetClientPid.return_value = 4321
        native.property = mock.Mock(return_value=None)
        earlier = native.facts(7654)
        self.owner(earlier)
        self.assertEqual(earlier['x_errors'], [])
        later = native.facts(9999)
        self.assertEqual(later['x_errors'], [{'resource_id': 9999, 'code': 3, 'request': 3}])
        self.assertEqual(earlier['x_errors'], [])
        self.assertIsNot(earlier['x_errors'], native.errors)
        self.assertIsNot(later['x_errors'], native.errors)
        native.facts(7654)  # Clearing/reusing the native callback list cannot erase either earlier receipt.
        self.assertEqual(earlier['x_errors'], [])
        self.assertEqual(later['x_errors'], [{'resource_id': 9999, 'code': 3, 'request': 3}])

    def test_local_only_display_rejects_tcp_hostname_and_missing_before_library_access(self):
        for display in ('', 'localhost:99', '127.0.0.1:99', 'remote:99', 'unix/:99', ':99/x'):
            with self.subTest(display=display), mock.patch.dict('os.environ', {'DISPLAY': display}), \
                    mock.patch.object(w.C, 'CDLL') as loader:
                with self.assertRaises(w.WindowError): w.NativeDisplay()
                loader.assert_not_called()

    def test_absent_or_matching_hint_needs_exact_server_pid_and_resource_range(self):
        facts = self.facts(); self.owner(facts); self.assertTrue(self.drawable(facts))
        facts['net_wm_pid'] = 4321; self.owner(facts)
        # The server returns the normalized resource base, not the queried window XID.
        self.assertNotEqual(facts['server_ids'][0]['resource_base'], facts['xid'])

    def test_rejects_absent_old_incompatible_malformed_ambiguous_and_foreign_proof(self):
        for field, value in [('schema_version', 2), ('display', ':85'), ('xid', 9999),
            ('xid', True), ('xres_version', None), ('xres_version', [1]), ('xres_version', [1, True]),
            ('xres_version', [1, 1]), ('xres_version', [2, 0]), ('matching_client_ranges', 0),
            ('matching_client_ranges', 2), ('matching_client_ranges', True), ('xres_query_success', False),
            ('resource_base', 0), ('resource_mask', -1), ('resource_mask', True),
            ('server_ids', None), ('server_ids', []), ('exists', False),
            ('x_errors', [{'code': 3}]), ('root', 98), ('window_class', 2),
            ('net_wm_pid', 9876), ('net_wm_pid', {'invalid': True})]:
            with self.subTest(field=field, value=value):
                facts = self.facts(); facts[field] = value
                with self.assertRaises(w.WindowError): self.owner(facts)
        for key, value in [('resource_base', 7654), ('mask', 1), ('length_bytes', 1),
                            ('pid', 9999), ('pid', -1), ('pid', True)]:
            facts = self.facts(); facts['server_ids'][0][key] = value
            with self.subTest(key=key), self.assertRaises(w.WindowError): self.owner(facts)
        facts = self.facts(); facts['server_ids'] *= 2
        with self.assertRaises(w.WindowError): self.owner(facts)

    def test_mapping_title_geometry_foreground_and_root_bounds_never_authorize_drawability(self):
        for field, value in [('map_state', 0), ('map_state', 1), ('translated', False),
            ('title', 'foreign'), ('width', 854), ('height', 480), ('border_width', 1),
            ('override_redirect', True), ('x', -1), ('y', 1), ('root_width', 1920),
            ('root_child', False), ('overlapping_windows_above', 1)]:
            with self.subTest(field=field):
                facts = self.facts(); facts[field] = value
                self.assertFalse(self.drawable(facts))

    def test_native_subprocess_is_bounded_exact_xid_and_rechecks_live_process(self):
        driver = self.actual_driver()
        with mock.patch.object(qa.subprocess, 'run', side_effect=lambda *a, **k: self.response()) as run:
            self.assertEqual(driver.verify_native_window(self.data)['xid'], 7654)
        self.assertEqual(run.call_args.args[0][-2:], ['--xid', '7654'])
        self.assertEqual(run.call_args.kwargs['timeout'], 3)
        self.assertEqual(driver.process_tracker.verify_client.call_count, 2)
        artifact = json.loads((self.root/'x11-window-first.json').read_text())
        self.assertNotIn('title', artifact['native'])
        self.assertTrue(artifact['native']['title_matches'])

    def test_foreign_title_not_exported_even_when_server_owner_fails(self):
        driver = self.actual_driver(); facts = self.facts()
        facts['title'] = 'private foreign title'; facts['server_ids'][0]['pid'] = 9999
        with mock.patch.object(qa.subprocess, 'run', side_effect=lambda *a, **k: self.response(facts)):
            with self.assertRaises(qa.QaError): driver.verify_native_window(self.data)
        self.assertNotIn('private foreign title', (self.root/'x11-window-latest.json').read_text())
        self.assertEqual(json.loads((self.root/'x11-window-latest.json').read_text())['native']['server_ids'][0]['pid'], 9999)

    def test_native_timeout_malformed_reply_or_deadline_fails_without_input(self):
        for kind in ('timeout', 'malformed', 'failed', 'deadline'):
            driver = self.actual_driver()
            if kind == 'deadline': driver.deadline = time.monotonic()-1
            result = SimpleNamespace(stdout='not JSON' if kind == 'malformed' else json.dumps({'error_type': 'WindowError'}), returncode=1)
            with mock.patch.object(qa.subprocess, 'run', return_value=result,
                    side_effect=subprocess.TimeoutExpired('redacted', 3) if kind == 'timeout' else None), \
                    mock.patch.object(qa, 'checked') as commands:
                with self.assertRaises(qa.QaError): driver.verify_native_window(self.data)
                commands.assert_not_called()

    def test_native_transient_mapping_title_geometry_waits_without_capture_or_input(self):
        for field, value in [('map_state', 0), ('width', 854), ('title', 'not applied yet'),
                             ('overlapping_windows_above', 1)]:
            driver = self.actual_driver(); facts = self.facts(); facts[field] = value
            with mock.patch.object(qa, 'read_json', return_value=self.data), \
                    mock.patch.object(qa.subprocess, 'run', side_effect=lambda *a, **k: self.response(facts)), \
                    mock.patch.object(driver, 'capture') as capture, mock.patch.object(qa, 'checked') as commands:
                driver.observe_startup(); capture.assert_not_called(); commands.assert_not_called()
                self.assertEqual(driver.migration_phase, 'COPIED'); self.assertIsNone(driver.startup_observed)

    def test_focus_pointer_mapping_or_identity_change_after_capture_blocks_click(self):
        for change in ('focus', 'pointer', 'map', 'geometry', 'owner'):
            driver = self.driver()
            driver.startup_observed = ((self.data['screen_class'], self.data['screen_title_key'],
                self.data['screen_message_key'], 'none'), self.now-1)
            def native(data, **kwargs):
                if kwargs.get('pointer') is not None: raise qa.QaError(change)
                return {'x': 0, 'y': 0}
            driver.verify_native_window.side_effect = native
            events = []
            with mock.patch.object(qa, 'read_json', return_value=self.data), \
                    mock.patch.object(driver, 'capture', side_effect=startup.StartupTests.accepted_capture(driver, events)), \
                    mock.patch.object(driver, 'find_window'), \
                    mock.patch.object(qa, 'checked', side_effect=lambda command, **k: events.append(command[1])):
                with self.assertRaises(qa.QaError): driver.observe_startup()
            self.assertEqual(events, ['capture', 'mousemove']); self.assertEqual(driver.migration_phase, 'COPIED')

    def test_native_focus_and_pointer_proof_and_startup_expiry_fail(self):
        for field, value in [('focused', False), ('pointer_same_screen', False),
                             ('pointer_on_window', False), ('pointer_x', 201)]:
            driver = self.actual_driver(); facts = self.facts(); facts[field] = value
            with mock.patch.object(qa.subprocess, 'run', side_effect=lambda *a, **k: self.response(facts)):
                with self.assertRaises(qa.QaError): driver.verify_native_window(self.data, require_focus=True, pointer=(200, 440))
        driver = self.actual_driver()
        def expire(*args, **kwargs):
            self.data['written_at_ms'] -= 6000
            return self.response()
        with mock.patch.object(qa.subprocess, 'run', side_effect=expire), self.assertRaises(qa.QaError):
            driver.verify_native_window(self.data)

    def test_normal_click_rechecks_same_state_page_owner_and_selected_control_after_mouse(self):
        for change in ('state', 'page', 'owner', 'control', 'inactive'):
            driver = self.driver(); ready = copy.deepcopy(self.data)
            ready.update(status='READY', state='ALLOCATION', page=0, sequence=1, panel={})
            latest = copy.deepcopy(ready)
            if change == 'state': latest['state'] = 'COMBAT'
            if change == 'page': latest['page'] = 1
            if change == 'owner': latest['x11_window_id'] = 9999
            if change == 'control': latest['buttons'][0]['x'] += 1
            if change == 'inactive': latest['buttons'][0]['active'] = False
            events = []
            with mock.patch.object(qa, 'read_json', side_effect=[ready, latest]), \
                    mock.patch.object(qa, 'validate_ready'), mock.patch.object(driver, 'find_window'), \
                    mock.patch.object(qa, 'checked', side_effect=lambda cmd, **k: events.append(cmd[1])):
                with self.assertRaises(qa.QaError): driver.click(ready, ready['buttons'][0]['label'])
            self.assertEqual(events, ['mousemove']); self.assertEqual(driver.last_action_ms, 0)

    def test_normal_key_rechecks_selected_screen_after_window_focus(self):
        driver = self.driver(); ready = copy.deepcopy(self.data)
        ready.update(status='READY', state='ALLOCATION', page=0, sequence=1, panel={})
        latest = copy.deepcopy(ready); latest['state'] = 'COMBAT'
        with mock.patch.object(qa, 'read_json', side_effect=[ready, latest]), \
                mock.patch.object(qa, 'validate_ready'), mock.patch.object(driver, 'find_window'), \
                mock.patch.object(qa, 'checked') as commands:
            with self.assertRaises(qa.QaError): driver.key('Tab')
            commands.assert_not_called()

    def test_capture_rejects_wrong_snapshot_xid_or_replaced_window_or_geometry(self):
        for change in ('snapshot', 'owner', 'geometry'):
            driver = self.driver(); calls = []
            def find():
                calls.append('probe')
                driver.window_id = '9999' if change == 'snapshot' or (change == 'owner' and len(calls) == 2) else '7654'
                return (1, 0) if change == 'geometry' and len(calls) == 2 else (0, 0)
            with mock.patch.object(driver, 'find_window', side_effect=find), \
                    mock.patch.object(driver, 'fresh_capture_snapshot', return_value=self.data), \
                    mock.patch.object(driver, 'validate_capture_snapshot'), \
                    mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=mock.Mock())}), \
                    mock.patch.dict(qa.os.environ, {'DISPLAY': ':84'}), mock.patch.object(qa, 'checked') as commands:
                with self.assertRaises(qa.QaError): driver.capture('unit-not-native', self.data, verify=False)
                self.assertEqual(commands.call_count, 0 if change == 'snapshot' else 1)

    def test_workflow_preflight_does_not_create_driver_output_or_pass_read_token(self):
        from pathlib import Path
        text = (Path(__file__).resolve().parents[2]/'.github/workflows/client-qa.yml').read_text()
        self.assertEqual(text.count('env -u GITHUB_TOKEN timeout 20s python3 scripts/client-qa/window_identity.py'), 2)
        self.assertEqual(text.count('trap preserve_preflight EXIT'), 2)
        self.assertEqual(text.count('vulkan-tools libxres1'), 2)
        for name in ('ras-client-qa-263', 'ras-client-qa-263-diagnostic'):
            self.assertIn('--output "$RUNNER_TEMP/' + name + '-x11-preflight.json"', text)
            self.assertNotIn('--output "$RUNNER_TEMP/' + name + '/x11-infrastructure-preflight.json"', text)

    def test_loom_nested_xvfb_override_is_only_explicit_hosted_263_self_test(self):
        from pathlib import Path
        root = Path(__file__).resolve().parents[2]
        source = (root/'26.3/fabric/build.gradle').read_text()
        block = """if (System.getenv('RAS_CLIENT_QA') == 'true') {
    tasks.named('runClientSelfTest') {
        getUseXvfb().set(false)
    }
}"""
        self.assertIn(block, source)
        self.assertEqual(source.count('getUseXvfb()'), 1)
        self.assertEqual(source.count('tasks.named(\'runClientSelfTest\')'), 1)
        for version in ('1.20.1', '1.21.1', '26.1.2', '26.2'):
            self.assertNotIn('getUseXvfb()', (root/version/'fabric/build.gradle').read_text())

    def test_capture_snapshot_rejects_same_xid_changed_screen_page_control_prompt_or_expiry(self):
        for status in ('STARTUP', 'READY'):
            for change in ('state', 'page', 'control', 'prompt', 'overlay', 'older', 'expired'):
                driver = self.driver(); captured = copy.deepcopy(self.data)
                captured.update(status=status, state='ALLOCATION', page=0, panel={})
                latest = copy.deepcopy(captured)
                if change == 'state': latest['state'] = 'COMBAT'
                if change == 'page': latest['page'] = 1
                if change == 'control': latest['buttons'][0]['x'] += 1
                if change == 'prompt': latest['screen_title_key'] = 'unknown'
                if change == 'overlay': latest['overlay_class'] = 'other'
                if change == 'older': latest['written_at_ms'] -= 1
                if change == 'expired': captured['written_at_ms'] -= 6000
                with mock.patch.object(qa, 'read_json', return_value=latest), mock.patch.object(qa, 'validate_ready'):
                    with self.assertRaises(qa.QaError): driver.validate_capture_snapshot(captured)

    def test_capture_checks_snapshot_before_and_after_native_frame(self):
        driver = self.driver(); driver.window_id = '7654'
        with mock.patch.object(driver, 'find_window', return_value=(0, 0)), \
                mock.patch.object(driver, 'fresh_capture_snapshot', return_value=self.data), \
                mock.patch.dict('sys.modules', {'PIL': SimpleNamespace(Image=mock.Mock())}), \
                mock.patch.object(driver, 'validate_capture_snapshot', side_effect=[self.data, qa.QaError('changed same-XID prompt')]) as snapshots, \
                mock.patch.dict(qa.os.environ, {'DISPLAY': ':84'}), mock.patch.object(qa, 'checked') as commands:
            with self.assertRaises(qa.QaError): driver.capture('unit-not-native', self.data, verify=False)
            self.assertEqual(snapshots.call_count, 2); self.assertEqual(commands.call_count, 1)

    def test_preflight_failure_reaps_only_explicit_child_and_saves_source_bound_failure(self):
        child = SimpleNamespace(pid=123, stdin=mock.Mock(), stdout=mock.Mock(), terminate=mock.Mock(),
                                kill=mock.Mock(), wait=mock.Mock(), poll=mock.Mock(side_effect=[None, 0]))
        child.stdout.readline.return_value = '{"pid":999,"xid":7654}'
        native = SimpleNamespace(close=mock.Mock())
        target = self.root/'preflight.json'
        with mock.patch.object(w, 'NativeDisplay', return_value=native), \
                mock.patch.object(w.subprocess, 'Popen', return_value=child), \
                mock.patch.object(w.select, 'select', return_value=([child.stdout], [], [])):
            with self.assertRaises(w.WindowError): w.self_test(target, 'a'*40)
        child.terminate.assert_called_once(); child.kill.assert_not_called(); native.close.assert_called_once()
        report = json.loads(target.read_text())
        self.assertEqual(report['status'], 'FAIL'); self.assertEqual(report['source_sha'], 'a'*40)
        self.assertTrue(report['explicit_children_reaped']); self.assertEqual(report['error_type'], 'WindowError')


if __name__ == '__main__': unittest.main()
