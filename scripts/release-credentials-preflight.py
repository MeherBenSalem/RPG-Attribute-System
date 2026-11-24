#!/usr/bin/env python3
"""Read-only presence/identity checks. Emits booleans only, never credentials/private responses.

Read success cannot prove CF project upload rights; that limit is explicit rather than being silently treated as
permission proof. NightBeam publishes to CurseForge only (24 Nov 2026), so only the CurseForge credentials are checked.
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
    cf=environment.get('CURSEFORGE_TOKEN','')
    key=environment.get('CURSEFORGE_API_KEY','')
    result={'schema_version':1,'curseforge_token_present':bool(cf),'curseforge_api_key_present':bool(key),
            'curseforge_upload_token_authenticated_catalog_read':False,'curseforge_api_key_expected_project_read':False,
            'curseforge_project_upload_right_read_verifiable':False}
    if cf:
        status,catalog=read('https://minecraft.curseforge.com/api/game/versions',{'X-Api-Token':cf})
        rows=catalog if isinstance(catalog,list) else catalog.get('data') if isinstance(catalog,dict) else None
        result['curseforge_upload_token_authenticated_catalog_read']=status==200 and isinstance(rows,list) and bool(rows)
    if key:
        status,project=read('https://api.curseforge.com/v1/mods/1079687',{'x-api-key':key})
        result['curseforge_api_key_expected_project_read']=status==200 and isinstance(project,dict) and isinstance(project.get('data'),dict) and project['data'].get('id')==1079687
    result['read_preflight_verified']=all(result[field] for field in [
        'curseforge_token_present','curseforge_api_key_present',
        'curseforge_upload_token_authenticated_catalog_read','curseforge_api_key_expected_project_read'])
    return result

if __name__=='__main__':
    result=check(os.environ)
    with open('release-credential-preflight.json','w') as output:json.dump(result,output,indent=2);output.write('\n')
    print(json.dumps(result,sort_keys=True))
    if not result['read_preflight_verified']:
        print('Existing credential read checks are unverified. A missing read scope can differ from an invalid upload token; no credential changes or upload probes were performed.')
        raise SystemExit(1)
