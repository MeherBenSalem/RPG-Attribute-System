#!/usr/bin/env python3
"""Offline credential preflight checks. No network or real credentials are used."""
import importlib.util
import io
import json
from pathlib import Path
import unittest
from unittest.mock import patch
import urllib.error

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('preflight',ROOT/'scripts/release-credentials-preflight.py')
preflight=importlib.util.module_from_spec(spec);spec.loader.exec_module(preflight)

class CredentialTests(unittest.TestCase):
    def environment(self):
        return {'MODRINTH_TOKEN':'fake-mr','CURSEFORGE_TOKEN':'fake-cf','CURSEFORGE_API_KEY':'fake-key'}
    def responses(self):
        return [(200,{'id':'owner','email':'private@example.invalid','payout_data':{'secret':'private'}}),
                (200,[{'user':{'id':'owner'},'accepted':True,'permissions':1}]),
                (200,[{'id':123}]),(200,{'data':{'id':1079687}})]
    def check(self,responses):
        with patch.object(preflight,'read',side_effect=responses):return preflight.check(self.environment())
    def test_absent_secrets_make_no_requests(self):
        with patch.object(preflight,'read') as read:
            result=preflight.check({});read.assert_not_called()
        self.assertFalse(result['read_preflight_verified'])
    def test_authorized_reads_emit_booleans_only(self):
        result=self.check(self.responses())
        self.assertTrue(result['read_preflight_verified'])
        self.assertFalse(result['modrinth_version_create_scope_read_verifiable'])
        self.assertFalse(result['curseforge_project_upload_right_read_verifiable'])
        self.assertTrue(all(isinstance(value,bool) for key,value in result.items() if key!='schema_version'))
        for private in ['fake-mr','fake-cf','fake-key','private@example.invalid','payout_data','owner']:
            self.assertNotIn(private,json.dumps(result))
    def test_token_headers_sent_only_to_expected_services(self):
        with patch.object(preflight,'read',side_effect=self.responses()) as read:preflight.check(self.environment())
        calls=[call.args for call in read.call_args_list]
        self.assertEqual('https://api.modrinth.com/v2/user',calls[0][0])
        self.assertEqual({'Authorization':'fake-mr'},calls[0][1])
        self.assertEqual('https://api.modrinth.com/v2/project/d85UTOuq/members',calls[1][0])
        self.assertEqual({'X-Api-Token':'fake-cf'},calls[2][1])
        self.assertEqual({'x-api-key':'fake-key'},calls[3][1])
    def test_missing_read_scope_is_unverified(self):
        result=self.check([(401,None),*self.responses()[2:]])
        self.assertTrue(result['modrinth_token_present'])
        self.assertFalse(result['modrinth_authenticated_identity_read'])
        self.assertFalse(result['read_preflight_verified'])
    def test_exact_accepted_member_and_upload_bit_required(self):
        for members in [[],[{'user':{'id':'other'},'accepted':True,'permissions':1}],
                        [{'user':{'id':'owner'},'accepted':False,'permissions':1}],
                        [{'user':{'id':'owner'},'accepted':True,'permissions':2}],
                        [{'user':{'id':'owner'},'accepted':True,'permissions':True}],
                        [{'user':{'id':'owner'},'accepted':True,'permissions':-1}],
                        [None,{'user':None}],self.responses()[1][1]*2]:
            responses=self.responses();responses[1]=(200,members)
            with self.subTest(members=members):self.assertFalse(self.check(responses)['read_preflight_verified'])
    def test_wrong_or_malformed_project_and_catalog_fail_closed(self):
        for index,payload in [(2,None),(2,[]),(2,{}),(3,None),(3,{'data':None}),(3,{'data':{'id':1}})]:
            responses=self.responses();responses[index]=(200,payload)
            with self.subTest(index=index,payload=payload):self.assertFalse(self.check(responses)['read_preflight_verified'])
    def test_http_error_body_is_not_parsed_or_logged(self):
        error=urllib.error.HTTPError('https://api.modrinth.com/v2/user',401,'failure',{},io.BytesIO(b'private-token-body'))
        with patch.object(preflight.urllib.request,'urlopen',side_effect=error),patch('sys.stdout',new_callable=io.StringIO) as output:
            self.assertEqual((401,None),preflight.read('https://api.modrinth.com/v2/user',{'Authorization':'fake-mr'}))
            self.assertEqual('',output.getvalue())
    def test_network_failure_is_unverified_without_exception_details(self):
        with patch.object(preflight.urllib.request,'urlopen',side_effect=RuntimeError('private-token-body')):
            self.assertEqual((0,None),preflight.read('https://api.modrinth.com/v2/user',{}))
    def test_workflow_secret_job_is_owner_and_same_repo_only(self):
        import yaml
        workflow=yaml.safe_load((ROOT/'.github/workflows/verify.yml').read_text())
        condition=workflow['jobs']['release-credential-preflight']['if']
        for guard in ["github.repository == 'MeherBenSalem/RPG-Attribute-System'","github.actor_id == '74470806'",
                      'github.event.pull_request.head.repo.full_name == github.repository','github.event.pull_request.user.id == 74470806']:
            self.assertIn(guard,condition)

if __name__=='__main__':unittest.main(verbosity=2)
