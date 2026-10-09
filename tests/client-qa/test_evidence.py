"""Metadata/watchdog unit regressions. These tests do not run or render Minecraft."""
import copy
import importlib.util
from pathlib import Path
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

if __name__=='__main__': unittest.main()
