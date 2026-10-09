#!/usr/bin/env python3
"""Read-only presence/identity checks. Emits booleans only, never credentials/private responses.

Read success cannot prove MR VERSION_CREATE token scope or CF project upload rights;
those limits are explicit rather than being silently treated as permission proof.
"""
import json
import os
import urllib.error
import urllib.request


def read(url,headers):
    request=urllib.request.Request(url,headers={**headers,'User-Agent':'NightBeam-RAS-release-preflight'})
    try:
        with urllib.request.urlopen(request,timeout=30) as response:
            return response.status,json.load(response)
    except urllib.error.HTTPError as error:
        return error.code,None
    except Exception:
        return 0,None


def check(environment):
    mr=environment.get('MODRINTH_TOKEN','')
    cf=environment.get('CURSEFORGE_TOKEN','')
    key=environment.get('CURSEFORGE_API_KEY','')
    result={'schema_version':1,'modrinth_token_present':bool(mr),'curseforge_token_present':bool(cf),
            'curseforge_api_key_present':bool(key),'modrinth_authenticated_identity_read':False,
            'modrinth_accepted_project_membership':False,'modrinth_project_upload_right_read':False,
            'curseforge_upload_token_authenticated_catalog_read':False,'curseforge_api_key_expected_project_read':False,
            'modrinth_version_create_scope_read_verifiable':False,'curseforge_project_upload_right_read_verifiable':False}
    if mr:
        status,user=read('https://api.modrinth.com/v2/user',{'Authorization':mr})
        result['modrinth_authenticated_identity_read']=status==200 and isinstance(user,dict) and isinstance(user.get('id'),str) and bool(user['id'])
        if result['modrinth_authenticated_identity_read']:
            status,members=read('https://api.modrinth.com/v2/project/d85UTOuq/members',{'Authorization':mr})
            if status==200 and isinstance(members,list):
                own=[member for member in members if isinstance(member,dict) and isinstance(member.get('user'),dict) and member['user'].get('id')==user['id']]
                result['modrinth_accepted_project_membership']=len(own)==1 and own[0].get('accepted') is True
                permission=own[0].get('permissions') if len(own)==1 else None
                result['modrinth_project_upload_right_read']=result['modrinth_accepted_project_membership'] and not isinstance(permission,bool) and isinstance(permission,int) and permission>=0 and bool(permission&1)
    if cf:
        status,catalog=read('https://minecraft.curseforge.com/api/game/versions',{'X-Api-Token':cf})
        rows=catalog if isinstance(catalog,list) else catalog.get('data') if isinstance(catalog,dict) else None
        result['curseforge_upload_token_authenticated_catalog_read']=status==200 and isinstance(rows,list) and bool(rows)
    if key:
        status,project=read('https://api.curseforge.com/v1/mods/1079687',{'x-api-key':key})
        result['curseforge_api_key_expected_project_read']=status==200 and isinstance(project,dict) and isinstance(project.get('data'),dict) and project['data'].get('id')==1079687
    result['read_preflight_verified']=all(result[field] for field in [
        'modrinth_token_present','curseforge_token_present','curseforge_api_key_present','modrinth_authenticated_identity_read',
        'modrinth_accepted_project_membership','modrinth_project_upload_right_read',
        'curseforge_upload_token_authenticated_catalog_read','curseforge_api_key_expected_project_read'])
    return result

if __name__=='__main__':
    result=check(os.environ)
    with open('release-credential-preflight.json','w') as output:json.dump(result,output,indent=2);output.write('\n')
    print(json.dumps(result,sort_keys=True))
    if not result['read_preflight_verified']:
        print('Existing credential read checks are unverified. A missing read scope can differ from an invalid upload token; no credential changes or upload probes were performed.')
        raise SystemExit(1)
