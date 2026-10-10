"""Metadata/watchdog unit regressions. These tests do not run or render Minecraft."""
import copy
import importlib.util
from pathlib import Path
import tempfile
import time
import unittest
from unittest import mock
from types import SimpleNamespace

spec=importlib.util.spec_from_file_location('client_qa',Path(__file__).resolve().parents[2]/'scripts/client-qa/run.py')
qa=importlib.util.module_from_spec(spec)
spec.loader.exec_module(qa)

class EvidenceTests(unittest.TestCase):
    def setUp(self):
        self.now=int(time.time()*1000)
        self.data={'schema_version':1,'status':'READY','run_id':'test-run-only','source_sha':'a'*40,
          'mc':'1.21.1','loader':'fabric','written_at_ms':self.now,'requested_gui_scale':4,'actual_gui_scale':4,
          'window_width':1280,'window_height':960,'gui_width':320,'gui_height':240,
          'server_player_bound':True,'client_player_present':True,'client_level_present':True,'expected_attributes_synced':True,
          'synced_attribute_ids':[f'attribute_{i}' for i in range(1,9)],'level':1,'spare_points':0,'next_level_xp':190,
          'sequence':3,'state':'ALLOCATION','panel':{'x':6,'y':6,'width':308,'height':228,'native_scale':1,'rows':5},
          'buttons':[{'label':label,'x':x,'y':11,'width':20,'height':20,'active':True} for label,x in [('one',10),('two',32),('three',54)]]}
    def validate(self,data):
        return qa.validate_ready(data,run_id='test-run-only',sha='a'*40,mc='1.21.1',scale=4,
                                 launched_ms=self.now-10,expected_state='ALLOCATION',since=2)
    def test_complete_metadata(self): self.assertEqual(self.validate(self.data)['sequence'],3)
    def test_rejects_all_false_positive_dimensions(self):
        mutations={'run_id':'old-run-id','source_sha':'b'*40,'status':'PASS','mc':'26.3','loader':'neoforge',
          'written_at_ms':self.now-10000,'actual_gui_scale':3,'gui_height':180,'window_width':854,
          'server_player_bound':False,'client_player_present':False,'client_level_present':False,
          'expected_attributes_synced':False,'synced_attribute_ids':[],'level':-1,'next_level_xp':0,
          'spare_points':float('nan'),'sequence':2,'state':'WORLD','buttons':[]}
        for field,value in mutations.items():
            with self.subTest(field=field):
                data=copy.deepcopy(self.data);data[field]=value
                with self.assertRaises(qa.QaError): self.validate(data)
    def test_rejects_unreadable_controls(self):
        for field,value in [('label',''),('width',14),('height',14),('x',-1),('y',240)]:
            with self.subTest(field=field):
                data=copy.deepcopy(self.data);data['buttons'][0][field]=value
                with self.assertRaises(qa.QaError): self.validate(data)
    def test_rejects_nonfinite_timestamp(self):
        for timestamp in (float('nan'),float('inf'),float('-inf')):
            data=copy.deepcopy(self.data);data['written_at_ms']=timestamp
            with self.assertRaises(qa.QaError): self.validate(data)
    def test_cleanup_terminates_owned_group_after_launcher_exit(self):
        driver=qa.Driver(Path('/unused-unit-test'), '1.21.1', Path('/unused-unit-test'), 'a'*40, None)
        driver.pgid=43210
        driver.process=SimpleNamespace(poll=lambda: 0)
        with mock.patch.object(qa.os,'killpg') as kill, mock.patch.object(qa,'checked',return_value=SimpleNamespace(stdout='43210 Z\n')):
            driver.cleanup()
            kill.assert_called_once_with(43210,qa.signal.SIGTERM)
        self.assertIsNone(driver.pgid)
    def test_rejects_pre_input_readiness(self):
        with self.assertRaises(qa.QaError):
            qa.validate_ready(self.data,run_id='test-run-only',sha='a'*40,mc='1.21.1',scale=4,
                              launched_ms=self.now-10,not_before_ms=self.now+1)
    def test_fresh_level_zero_is_ready(self):
        data=copy.deepcopy(self.data);data['level']=0
        self.validate(data)
    def test_rejects_missing_native_panel(self):
        data=copy.deepcopy(self.data);data['panel']['native_scale']=.76
        with self.assertRaises(qa.QaError): self.validate(data)
    def test_world_evidence_still_needs_real_player_and_sync(self):
        data=copy.deepcopy(self.data);data['state']='WORLD';data['buttons']=[];data.pop('panel')
        qa.validate_ready(data,run_id='test-run-only',sha='a'*40,mc='1.21.1',scale=4,launched_ms=self.now-10,expected_state='WORLD',since=2)
        data['client_player_present']=False
        with self.assertRaises(qa.QaError):
            qa.validate_ready(data,run_id='test-run-only',sha='a'*40,mc='1.21.1',scale=4,launched_ms=self.now-10,expected_state='WORLD',since=2)
    def test_hosted_hooks_are_default_off_and_no_eula_write(self):
        root=Path(__file__).resolve().parents[2]
        for version in ('1.21.1','26.3'):
            source=(root/version/'common/src/main/java/tn/nightbeam/ras/client/RasGuiSelfTest.java').read_text()
            self.assertIn('Boolean.getBoolean("ras.clientQa")',source)
            self.assertIn('System.nanoTime() - STARTED > STARTUP_LIMIT_NS',source)
            self.assertLess(source.index('System.nanoTime() - STARTED > STARTUP_LIMIT_NS'),source.index('if (client.player == null || client.level == null'))
            self.assertNotIn('eula=true',source)
        driver=(root/'scripts/client-qa/run.py').read_text()
        self.assertIn('start_new_session=True',driver)
        self.assertIn('timeout=kwargs.pop("timeout", 20)',driver)
        self.assertIn('self.last_action_ms = int(time.time()*1000)',driver)
        self.assertIn('if self.pgid is not None:',driver)
        self.assertIn('os.killpg(self.pgid',driver)
        self.assertNotIn('pkill',driver)
        self.assertNotIn('eula=true',driver)


class RenderingDriverTests(unittest.TestCase):
    # Probe-output fixtures for parser regressions only. These never run/render a client.
    VULKAN_SUMMARY = '''Vulkan Instance Version: 1.3.275
Instance Extensions: count = 2
    VK_KHR_surface : extension revision 25
    VK_KHR_xlib_surface : extension revision 6
Devices:
GPU0:
    apiVersion = 1.3.290
    deviceType = PHYSICAL_DEVICE_TYPE_CPU
    deviceName = llvmpipe (LLVM 20.1.2, 256 bits)
    driverID = DRIVER_ID_MESA_LLVMPIPE
    driverName = llvmpipe
'''

    def test_accepts_verified_cpu_vulkan_with_x11_extensions(self):
        qa.validate_vulkan_renderer(self.VULKAN_SUMMARY)

    def test_rejects_missing_surface_wrong_device_and_multiple_devices(self):
        for bad in (
                self.VULKAN_SUMMARY.replace('VK_KHR_surface', 'VK_KHR_surface_fake'),
                self.VULKAN_SUMMARY.replace('VK_KHR_xlib_surface', 'VK_KHR_wayland_surface'),
                self.VULKAN_SUMMARY.replace('PHYSICAL_DEVICE_TYPE_CPU', 'PHYSICAL_DEVICE_TYPE_DISCRETE_GPU'),
                self.VULKAN_SUMMARY.replace('deviceName = llvmpipe', 'deviceName = unrelated'),
                self.VULKAN_SUMMARY.replace('DRIVER_ID_MESA_LLVMPIPE', 'DRIVER_ID_UNKNOWN'),
                self.VULKAN_SUMMARY + '\n    deviceName = llvmpipe\n'):
            with self.subTest(output=bad), self.assertRaises(qa.QaError):
                qa.validate_vulkan_renderer(bad)

    def test_selects_existing_packaged_lavapipe_manifest_for_probe_and_client(self):
        for filename in ('lvp_icd.x86_64.json', 'lvp_icd.json'):
            with self.subTest(filename=filename), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                manifest = root / filename
                manifest.write_text('{"ICD":{"library_path":"/usr/lib/x86_64-linux-gnu/libvulkan_lvp.so"}}')
                with mock.patch.object(qa.platform, 'machine', return_value='x86_64'):
                    environment = qa.renderer_environment('26.3', {'DISPLAY': ':test', 'VK_ICD_FILENAMES': '/old.json'}, root)
                self.assertEqual(environment['RAS_GRAPHICS_BACKEND'], 'vulkan')
                self.assertEqual(environment['SDL_VIDEO_DRIVER'], 'x11')
                self.assertEqual(environment['VK_DRIVER_FILES'], str(manifest.resolve()))
                self.assertNotIn('VK_ICD_FILENAMES', environment)

    def test_missing_or_unrelated_icd_fails_before_launch(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            with self.assertRaisesRegex(qa.QaError, 'Missing packaged'):
                qa.renderer_environment('26.3', {}, root)
            (root / 'lvp_icd.json').write_text('{"ICD":{"library_path":"/unrelated.so"}}')
            with self.assertRaisesRegex(qa.QaError, 'real Mesa driver'):
                qa.renderer_environment('26.3', {}, root)

    def test_old_client_retains_software_gl_without_vulkan_setup(self):
        environment = qa.renderer_environment('1.21.1', {'RAS_GRAPHICS_BACKEND': 'vulkan'})
        self.assertEqual(environment, {'LIBGL_ALWAYS_SOFTWARE': 'true', 'GALLIUM_DRIVER': 'llvmpipe'})

    def test_prohibits_gl_version_overrides_before_any_probe(self):
        for variable in ('MESA_GL_VERSION_OVERRIDE', 'MESA_GLSL_VERSION_OVERRIDE'):
            with self.subTest(variable=variable), self.assertRaisesRegex(qa.QaError, 'overrides are prohibited'):
                qa.renderer_environment('1.21.1', {variable: '4.5'})

    def test_failed_probe_keeps_actual_stdout_stderr(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            with mock.patch.object(qa, 'renderer_environment', return_value={'RAS_GRAPHICS_BACKEND': 'vulkan'}), \
                    mock.patch.object(qa.shutil, 'which', return_value='/usr/bin/vulkaninfo'), \
                    mock.patch.object(qa.subprocess, 'run', return_value=SimpleNamespace(returncode=1, stdout='probe stdout', stderr='probe stderr')):
                with self.assertRaisesRegex(qa.QaError, 'renderer setup failed'):
                    qa.prepare_renderer('26.3', root)
            self.assertEqual((root / 'renderer.txt').read_text(), 'probe stdout')
            self.assertEqual((root / 'renderer-stderr.txt').read_text(), 'probe stderr')

    def test_probe_uses_the_same_environment_returned_for_launch(self):
        environment = {'RAS_GRAPHICS_BACKEND': 'vulkan', 'VK_DRIVER_FILES': '/packaged/lvp_icd.json', 'SDL_VIDEO_DRIVER': 'x11'}
        with tempfile.TemporaryDirectory() as temporary, \
                mock.patch.object(qa, 'renderer_environment', return_value=environment), \
                mock.patch.object(qa.shutil, 'which', return_value='/usr/bin/vulkaninfo'), \
                mock.patch.object(qa.subprocess, 'run', return_value=SimpleNamespace(returncode=0, stdout=self.VULKAN_SUMMARY, stderr='')) as probe:
            self.assertEqual(qa.prepare_renderer('26.3', Path(temporary)), environment)
            self.assertEqual(probe.call_args.args[0], ['vulkaninfo', '--summary'])
            self.assertEqual(probe.call_args.kwargs['env'], environment)

    def test_fails_immediately_on_actual_selected_backend_errors_while_process_alive(self):
        for backend, failure in (('opengl', "Couldn't find matching GLX visual"),
                                 ('vulkan', "Vulkan is not supported: Installed Vulkan doesn't implement the VK_KHR_surface extension")):
            with self.subTest(backend=backend), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                driver = qa.Driver(root, '26.3', root, 'a'*40, None, {'RAS_GRAPHICS_BACKEND': backend})
                driver.case = driver.game = root
                driver.deadline = time.monotonic() + 1000
                driver.process = SimpleNamespace(poll=lambda: None)
                (root / 'gradle-client.log').write_text(failure)
                with mock.patch.object(qa.time, 'sleep') as sleep:
                    with self.assertRaisesRegex(qa.QaError, 'renderer creation failed'):
                        driver.wait_ready('ALLOCATION', seconds=1000)
                    sleep.assert_not_called()

    def test_ignores_harmless_audio_auth_and_other_backend_diagnostics(self):
        self.assertIsNone(qa.renderer_failure('Failed to open OpenAL device\nFailed to fetch user properties\nInvalidCredentialsException: Status: 401', 'vulkan'))
        self.assertIsNone(qa.renderer_failure('Failed to create backend OpenGL', 'vulkan'))

    def test_parks_pointer_before_native_frames_without_changing_keyboard_focus(self):
        root = Path('/unused-unit-test')
        driver = qa.Driver(root, '1.21.1', root, 'a'*40, None)
        driver.window_id = 'unit-test-window'
        ready = {'state': 'ALLOCATION', 'actual_gui_scale': 4,
                 'buttons': [{'x': 0, 'y': 0, 'width': 20, 'height': 20, 'focused': True}]}
        original = copy.deepcopy(ready)
        with mock.patch.object(driver, 'find_window'), mock.patch.object(driver, 'capture') as capture, \
                mock.patch.object(qa, 'checked') as checked, mock.patch.object(qa.time, 'sleep'):
            driver.stable_capture('unit-test-only', ready)
        self.assertEqual(checked.call_args.args[0], ['xdotool', 'mousemove', '--sync', '--window', 'unit-test-window', '1278', '1'])
        self.assertEqual(capture.call_count, 2)
        self.assertEqual(ready, original)

    def test_world_capture_does_not_move_grabbed_pointer(self):
        root = Path('/unused-unit-test')
        driver = qa.Driver(root, '1.21.1', root, 'a'*40, None)
        with mock.patch.object(driver, 'capture'), mock.patch.object(qa, 'checked') as checked, mock.patch.object(qa.time, 'sleep'):
            driver.stable_capture('unit-test-only', {'state': 'WORLD'})
        checked.assert_not_called()

    def test_native_capture_omits_pointer_and_disposable_options_disable_tutorial(self):
        source = (Path(__file__).resolve().parents[2] / 'scripts/client-qa/run.py').read_text()
        self.assertIn('"-draw_mouse", "0"', source)
        self.assertIn('tutorialStep:none', source)
        self.assertNotIn('MESA_GL_VERSION_OVERRIDE=', source)

if __name__=='__main__': unittest.main()
