#!/usr/bin/env python3
import copy
import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch

spec=importlib.util.spec_from_file_location('recovery',Path(__file__).resolve().parents[1]/'scripts/validate-release-recovery.py')
recovery=importlib.util.module_from_spec(spec);spec.loader.exec_module(recovery)
SHA='a'*40

class RecoveryTests(unittest.TestCase):
    def run_payload(self):
        return {'repository':{'full_name':recovery.REPOSITORY,'id':42},'head_repository':{'id':42},
                'head_sha':SHA,'path':recovery.WORKFLOW,'name':'Publish','event':'push','status':'completed'}
    def test_exact_run(self): recovery.validate_run(self.run_payload(),SHA)
    def test_foreign_wrong_or_running_run(self):
        mutations=[('head_sha','b'*40),('path','.github/workflows/other.yml'),('name','Other'),('event','pull_request'),('status','in_progress')]
        for key,value in mutations:
            with self.subTest(key=key):
                payload=self.run_payload();payload[key]=value
                with self.assertRaises(ValueError):recovery.validate_run(payload,SHA)
        for payload in [dict(self.run_payload(),repository={'full_name':'other/repo','id':42}),dict(self.run_payload(),head_repository={'id':43})]:
            with self.assertRaises(ValueError):recovery.validate_run(payload,SHA)
    def build_jobs(self):
        return {'jobs':[{'name':'Build '+workspace,'status':'completed','conclusion':'success','steps':[
            {'name':'Build both loaders and common checks at exact source commit','conclusion':'success'}]}
            for workspace in recovery.WORKSPACES]}
    def test_exact_build_jobs(self): recovery.validate_build_jobs(self.build_jobs())
    def test_failed_missing_duplicate_or_noop_build(self):
        for edit in [lambda p:p['jobs'].pop(),lambda p:p['jobs'].append(copy.deepcopy(p['jobs'][0])),
                     lambda p:p['jobs'][0].update(conclusion='failure'),lambda p:p['jobs'][0].update(status='in_progress'),
                     lambda p:p['jobs'][0].update(steps=[]),lambda p:p['jobs'][0]['steps'][0].update(conclusion='skipped')]:
            payload=self.build_jobs();edit(payload)
            with self.assertRaises(ValueError):recovery.validate_build_jobs(payload)
    def artifact(self):return {'name':'expected','expired':False,'workflow_run':{'head_sha':SHA},'digest':'sha256:'+'b'*64}
    def test_exact_artifact(self):recovery.validate_artifacts({'artifacts':[self.artifact()]},SHA,['expected'])
    def test_untrusted_artifact(self):
        for edit in [lambda a:a.update(expired=True),lambda a:a.update(name='other'),
                     lambda a:a.update(workflow_run={'head_sha':'c'*40}),lambda a:a.update(digest=''),
                     lambda a:a.update(digest='sha256:bad')]:
            artifact=self.artifact();edit(artifact)
            with self.assertRaises(ValueError):recovery.validate_artifacts({'artifacts':[artifact]},SHA,['expected'])
        for artifacts in [[],[self.artifact(),self.artifact()]]:
            with self.assertRaises(ValueError):recovery.validate_artifacts({'artifacts':artifacts},SHA,['expected'])

    def test_latest_receipt_and_parallel_writer(self):
        runs=[{'id':1,'created_at':'2026-10-09T10:00:00Z','status':'completed'},
              {'id':2,'created_at':'2026-10-09T11:00:00Z','status':'completed'},
              {'id':3,'created_at':'2026-10-09T12:00:00Z','status':'in_progress'}]
        writer={'jobs':[{'name':'publish','status':'completed','steps':[{'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'failure'}]}]}
        jobs={'1':writer,'2':writer}
        with self.assertRaises(ValueError):recovery.latest_receipt(runs,jobs,3,1)
        recovery.latest_receipt(runs,jobs,3,2)
        runs.append({'id':4,'created_at':'2026-10-09T12:01:00Z','status':'in_progress'})
        jobs['4']={'jobs':[{'name':'publish','status':'in_progress'}]}
        with self.assertRaises(ValueError):recovery.latest_receipt(runs,jobs,3,2)
    def test_skipped_noop_does_not_hide_latest_receipt(self):
        runs=[{'id':1,'created_at':'2026-10-09T10:00:00Z','status':'completed'},
              {'id':2,'created_at':'2026-10-09T11:00:00Z','status':'completed'}]
        writer={'jobs':[{'name':'publish','status':'completed','steps':[{'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'failure'}]}]}
        noop={'jobs':[{'name':'publish','status':'completed','steps':[{'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'skipped'}]}]}
        recovery.latest_receipt(runs,{'1':writer,'2':noop},3,1)
    def test_publish_only_rerun_guard_present_before_store_action(self):
        import yaml
        workflow=yaml.safe_load((Path(__file__).resolve().parents[1]/'.github/workflows/publish.yml').read_text())
        steps=workflow['jobs']['publish']['steps']
        guard=next(i for i,step in enumerate(steps) if step.get('name')=='Reject live publish-only job reruns without fresh recovery')
        upload=next(i for i,step in enumerate(steps) if step.get('name')=='Publish/resume verified binaries with per-loader dependencies')
        self.assertLess(guard,upload)
        self.assertIn('GITHUB_RUN_ATTEMPT',steps[guard]['run'])

    def test_standalone_dry_run_cannot_replace_uncertain_live_journal(self):
        import yaml
        workflow=yaml.safe_load((Path(__file__).resolve().parents[1]/'.github/workflows/publish.yml').read_text())
        guard=next(step for step in workflow['jobs']['resolve']['steps'] if step.get('name')=='Reject blind fresh publication after a prior attempt')
        self.assertNotIn('dry_run',guard['if'])
        self.assertEqual('ras-release-publication',workflow['concurrency']['group'])
        runs=[{'id':1,'created_at':'2026-10-09T10:00:00Z','status':'completed'}]
        uncertain={'jobs':[{'name':'publish','status':'completed','steps':[{'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'failure'}]}]}
        # A wrote an uncertain live attempt. B cannot run standalone (even dry-run).
        with self.assertRaises(ValueError):recovery.check_fresh(runs,{'1':uncertain},2)
        # B's failed freshness guard has no writer job. C must still recover A.
        runs.append({'id':2,'created_at':'2026-10-09T11:00:00Z','status':'completed'})
        recovery.latest_receipt(runs,{'1':uncertain,'2':{'jobs':[]}},3,1)
        with self.assertRaises(ValueError):recovery.latest_receipt(runs,{'1':uncertain,'2':{'jobs':[]}},3,2)

    def job(self,job_id,name='other',steps=None):
        return {'id':job_id,'name':name,'status':'completed','steps':steps or []}
    def test_old_write_attempt_survives_blocked_job_rerun(self):
        jobs=[self.job(1,'publish',[{'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'failure'}]),
              self.job(2,'publish',[{'name':'Reject live publish-only job reruns without fresh recovery','conclusion':'failure'},
                                     {'name':'Publish/resume verified binaries with per-loader dependencies','conclusion':'skipped'}])]
        with patch.object(recovery,'request',return_value={'total_count':2,'jobs':jobs}) as read:
            payload=recovery.read_jobs(1)
            self.assertIn('filter=all',read.call_args.args[0])
        with self.assertRaises(ValueError):recovery.check_fresh([{'id':1}],{'1':payload},2)

    def test_bounded_complete_job_pagination(self):
        jobs=[self.job(i) for i in range(1,103)]
        pages=[{'total_count':102,'jobs':jobs[:100]},{'total_count':102,'jobs':jobs[100:]}]
        with patch.object(recovery,'request',side_effect=pages) as read:
            self.assertEqual(102,len(recovery.read_jobs(1)['jobs']))
            self.assertIn('page=2',read.call_args.args[0])
        for payload in [{}, {'total_count':2,'jobs':[]},{'total_count':1001,'jobs':[]},
                        {'total_count':True,'jobs':[]},{'total_count':1,'jobs':[self.job(0)]},
                        {'total_count':2,'jobs':[self.job(1),self.job(1)]},
                        {'total_count':1,'jobs':[{'id':1,'name':'publish','status':'completed','steps':None}]}]:
            with self.subTest(payload=payload),patch.object(recovery,'request',return_value=payload):
                with self.assertRaises(ValueError):recovery.read_jobs(1)
        with patch.object(recovery,'request',side_effect=[pages[0],{'total_count':103,'jobs':jobs[100:]}]):
            with self.assertRaises(ValueError):recovery.read_jobs(1)

    def test_malformed_job_history_cannot_prove_no_write(self):
        for payload in [None,{}, {'jobs':None},{'jobs':[None]}, {'jobs':[{'name':'publish'}]},
                        {'jobs':[{'name':'publish','status':'completed','steps':[None]}]}]:
            with self.subTest(payload=payload),self.assertRaises(ValueError):recovery.may_have_published(payload)

if __name__=='__main__':unittest.main(verbosity=2)
