#!/usr/bin/env python3
"""Validate GitHub workflow/job/artifact provenance before release recovery.

The token is used only for read-only requests to the fixed GitHub repository.
It is never written or printed. No archive contents or remote instructions authorize uploads.
"""
import argparse
import json
import os
import re
import urllib.request

REPOSITORY='MeherBenSalem/RPG-Attribute-System'
WORKFLOW='.github/workflows/publish.yml'
WORKSPACES=['1.20.1','1.21.1','26.1.2','26.2','26.3']


def validate_run(run,commit):
    if not isinstance(run,dict) or run.get('repository',{}).get('full_name')!=REPOSITORY:
        raise ValueError('Recovery run belongs to a different repository')
    if run.get('head_repository',{}).get('id')!=run.get('repository',{}).get('id'):
        raise ValueError('Fork recovery artifacts are not trusted')
    if run.get('head_sha')!=commit or run.get('path')!=WORKFLOW or run.get('name')!='Publish':
        raise ValueError('Recovery run source/workflow differs')
    if run.get('event') not in ['push','workflow_dispatch'] or run.get('status')!='completed':
        raise ValueError('Recovery run must be a completed authorized Publish invocation')


def validate_build_jobs(payload):
    jobs=payload.get('jobs',[]) if isinstance(payload,dict) else []
    for workspace in WORKSPACES:
        matches=[job for job in jobs if job.get('name')=='Build '+workspace]
        if len(matches)!=1 or matches[0].get('status')!='completed' or matches[0].get('conclusion')!='success':
            raise ValueError('Recovery lacks a successful exact build job for '+workspace)
        steps=matches[0].get('steps',[])
        built=[step for step in steps if step.get('name')=='Build both loaders and common checks at exact source commit']
        if len(built)!=1 or built[0].get('conclusion')!='success':
            raise ValueError('Recovery build did not execute the required source-bound build step')


def validate_artifacts(payload,commit,names):
    artifacts=payload.get('artifacts',[]) if isinstance(payload,dict) else []
    for name in names:
        matches=[artifact for artifact in artifacts if artifact.get('name')==name]
        if len(matches)!=1 or matches[0].get('expired') is not False:
            raise ValueError('Recovery artifact missing/ambiguous/expired: '+name)
        artifact=matches[0]
        if artifact.get('workflow_run',{}).get('head_sha')!=commit:
            raise ValueError('Recovery artifact head differs: '+name)
        if not re.fullmatch(r'sha256:[0-9a-f]{64}',artifact.get('digest','')):
            raise ValueError('Recovery artifact lacks trusted archive digest: '+name)


def may_have_published(payload):
    jobs=payload.get('jobs') if isinstance(payload,dict) else None
    if not isinstance(jobs,list): raise ValueError('Malformed publication job history')
    for job in jobs:
        if not isinstance(job,dict) or not isinstance(job.get('name'),str) or not isinstance(job.get('status'),str):
            raise ValueError('Malformed publication job record')
        if job.get('name')!='publish': continue
        if job.get('status')!='completed': return True
        steps=job.get('steps')
        if not isinstance(steps,list): raise ValueError('Malformed publication step history')
        for step in steps:
            if not isinstance(step,dict) or not isinstance(step.get('name'),str):
                raise ValueError('Malformed publication step record')
            if step.get('name')=='Publish/resume verified binaries with per-loader dependencies' and step.get('conclusion')!='skipped':
                return True
    return False


def latest_receipt(runs,job_payloads,current_run_id,receipt_run_id):
    relevant=[]
    for run in runs:
        run_id=str(run.get('id'))
        if run_id==str(current_run_id): continue
        if may_have_published(job_payloads.get(run_id)):
            if run.get('status')!='completed': raise ValueError('Parallel/incomplete publication attempt must be reconciled')
            relevant.append(run)
    if not relevant: raise ValueError('No prior attempted publication journal found')
    latest=max(relevant,key=lambda run:(run.get('created_at',''),int(run.get('id',0))))
    if str(latest.get('id'))!=str(receipt_run_id):
        raise ValueError('Stale receipt selected; recover the latest attempted publication journal')


def request(path):
    token=os.environ.get('GH_TOKEN')
    if not token: raise ValueError('GitHub read token required for provenance verification')
    url='https://api.github.com/repos/'+REPOSITORY+path
    req=urllib.request.Request(url,headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json',
                                           'X-GitHub-Api-Version':'2022-11-28','User-Agent':'NightBeam-RAS-release-verifier'})
    with urllib.request.urlopen(req,timeout=30) as response:
        return json.load(response)


def read_jobs(run_id,attempt=None):
    """Read every attempt, bounded to 1,000 jobs; incomplete snapshots fail closed."""
    prefix='/actions/runs/'+str(run_id)
    if attempt is not None: prefix+='/attempts/'+str(attempt)
    jobs=[];expected=None
    for page in range(1,11):
        payload=request(prefix+'/jobs?filter=all&per_page=100&page='+str(page))
        rows=payload.get('jobs') if isinstance(payload,dict) else None
        count=payload.get('total_count') if isinstance(payload,dict) else None
        if not isinstance(rows,list) or isinstance(count,bool) or not isinstance(count,int) or count<0 or count>1000 or len(rows)>100:
            raise ValueError('Malformed/oversized publication job history')
        if expected is None: expected=count
        if count!=expected: raise ValueError('Publication job history changed during pagination')
        jobs.extend(rows)
        if len(jobs)>expected: raise ValueError('Publication job history count differs')
        if len(jobs)==expected:
            ids=[job.get('id') for job in jobs if isinstance(job,dict)]
            if len(ids)!=len(jobs) or any(isinstance(job_id,bool) or not isinstance(job_id,int) or job_id<=0 for job_id in ids) or len(set(ids))!=len(ids):
                raise ValueError('Ambiguous/malformed publication job identities')
            result={'jobs':jobs}
            may_have_published(result)  # Validate publication records even if this caller only needs builds.
            return result
        if len(rows)<100: raise ValueError('Truncated publication job history')
    raise ValueError('Publication job history exceeds pagination bound')


def check_fresh(runs,job_payloads,current_run_id):
    for run in runs:
        run_id=str(run.get('id'))
        if run_id!=str(current_run_id) and may_have_published(job_payloads.get(run_id)):
            raise ValueError('A prior publication invocation exists. Recover its latest artifacts/receipts; never blindly start over.')


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--commit',required=True)
    parser.add_argument('--source-run-id')
    parser.add_argument('--receipt-run-id')
    parser.add_argument('--current-run-id')
    parser.add_argument('--check-fresh',action='store_true')
    args=parser.parse_args()
    if not re.fullmatch(r'[0-9a-f]{40}',args.commit): raise ValueError('Invalid source commit')
    for value in [args.source_run_id,args.receipt_run_id,args.current_run_id]:
        if value and not re.fullmatch(r'[1-9]\d*',value): raise ValueError('Invalid workflow run ID')
    if args.check_fresh:
        if not args.current_run_id: raise ValueError('Current run ID required')
        payload=request('/actions/workflows/publish.yml/runs?head_sha='+args.commit+'&per_page=100')
        runs=payload.get('workflow_runs')
        if not isinstance(runs,list): raise ValueError('Malformed Publish history')
        if len(runs)>=100: raise ValueError('Publication history exceeds safe bound; cannot prove freshness')
        jobs={str(run['id']):read_jobs(run['id']) for run in runs if str(run.get('id'))!=args.current_run_id}
        check_fresh(runs,jobs,args.current_run_id)
        print('PASS no prior publication invocation at this exact source commit')
        return
    if not args.source_run_id or not args.receipt_run_id: raise ValueError('Source and receipt run IDs required')
    if not args.current_run_id: raise ValueError('Current recovery run ID required')
    history=request('/actions/workflows/publish.yml/runs?head_sha='+args.commit+'&per_page=100').get('workflow_runs')
    if not isinstance(history,list) or len(history)>=100: raise ValueError('Cannot establish complete publication history')
    relevant_jobs={str(run['id']):read_jobs(run['id'])
                   for run in history if str(run.get('id'))!=args.current_run_id}
    latest_receipt(history,relevant_jobs,args.current_run_id,args.receipt_run_id)
    validate_run(request('/actions/runs/'+args.source_run_id),args.commit)
    validate_run(request('/actions/runs/'+args.receipt_run_id),args.commit)
    validate_build_jobs(read_jobs(args.source_run_id,attempt=1))
    validate_artifacts(request('/actions/runs/'+args.source_run_id+'/artifacts?per_page=100'),args.commit,
                       ['ras-release-'+args.commit+'-'+workspace for workspace in WORKSPACES])
    validate_artifacts(request('/actions/runs/'+args.receipt_run_id+'/artifacts?per_page=100'),args.commit,
                       ['release-provenance-'+args.commit])
    print('PASS recovery workflow/job/artifact source provenance; receipt contents still validated by publisher')

if __name__=='__main__': main()
