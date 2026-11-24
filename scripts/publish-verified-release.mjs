#!/usr/bin/env node
/** Publish only a complete, source-bound inventory. No implicit local secret reads or blind retries.
 *  NightBeam publishes to CurseForge only (24 Nov 2026); the existing Modrinth project stays up as it is but gets no
 *  new versions. */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { pathToFileURL } from 'node:url';
import { curseForgeIds } from './curseforge-release-catalog.mjs';

const curseForgeId = id => Number.isSafeInteger(id) && id > 0 && id <= 0xffffffff;

export function validatePlan(manifest, directory) {
  if (manifest?.schema_version !== 1 || manifest.complete_inventory !== true || manifest.artifact_count !== 8
      || !/^[0-9a-f]{40}$/.test(manifest.source_commit) || !/^\d+\.\d+\.\d+$/.test(manifest.release_version)
      || manifest.mod_id !== 'rpg_attribute_system' || !Array.isArray(manifest.files) || manifest.files.length !== 8) {
    throw new Error('A complete verified eight-JAR manifest is required');
  }
  const expected = new Set(['1.20.1:fabric','1.20.1:forge','1.21.1:fabric','1.21.1:neoforge',
    '26.2:fabric','26.2:neoforge','26.3:fabric','26.3:neoforge']);
  const root = fs.realpathSync(directory);
  const entries = manifest.files.map(file => {
    const combination = `${file.minecraft}:${file.loader}`;
    if (!expected.delete(combination) || file.version !== manifest.release_version || file.source_commit !== manifest.source_commit
        || file.file_name !== `rpg_attribute_system-${file.loader}-${file.minecraft}-${manifest.release_version}.jar`
        || !Array.isArray(file.required_mod_ids) || file.required_mod_ids.some(id => !['fabric-api','jauml'].includes(id))) {
      throw new Error('Invalid/mixed/duplicate release entry: ' + file.file_name);
    }
    const expectedDeps = file.loader === 'fabric' && ['1.20.1','1.21.1'].includes(file.minecraft)
      ? ['fabric-api','jauml'] : file.loader === 'fabric' ? ['fabric-api']
      : ['1.20.1','1.21.1'].includes(file.minecraft) ? ['jauml'] : [];
    if (JSON.stringify([...file.required_mod_ids].sort()) !== JSON.stringify(expectedDeps))
      throw new Error('Required dependency plan differs from reviewed loader source');
    const resolved = fs.realpathSync(path.resolve(root, file.path));
    const relative = path.relative(root, resolved);
    if (relative.startsWith('..' + path.sep) || relative === '..' || path.isAbsolute(relative)
        || path.basename(resolved) !== file.file_name || fs.lstatSync(path.resolve(root, file.path)).isSymbolicLink()) {
      throw new Error('Artifact path escapes verified directory');
    }
    const binary = fs.readFileSync(resolved);
    if (binary.length !== file.size_bytes || ['sha256','sha512','sha1'].some(algorithm =>
      crypto.createHash(algorithm).update(binary).digest('hex') !== file[algorithm])) {
      throw new Error('Artifact changed after verification: ' + file.file_name);
    }
    return {...file, absolute_path: resolved};
  });
  if (expected.size) throw new Error('Incomplete release plan');
  return entries;
}

export function curseForgeDescriptor(entry, version, changelog, ids, dependencyMap) {
  const loaderId = ids.loader[entry.loader], gameId = ids.minecraft[entry.minecraft];
  if (!Number.isInteger(loaderId) || !Number.isInteger(gameId) || !Number.isInteger(ids.client) || !Number.isInteger(ids.server)) {
    throw new Error('Incomplete exact CurseForge game/loader metadata');
  }
  const projects = entry.required_mod_ids.map(id => {
    const slug = dependencyMap[id]?.curseforge_slug, projectID = dependencyMap[id]?.curseforge_id;
    if (!slug || !curseForgeId(projectID)) throw new Error('Missing verified CurseForge dependency mapping: ' + id);
    // The Upload API rejects a string projectID (HTTP 400, errorCode 1002) despite its documented example.
    return {slug, projectID, type: 'requiredDependency'};
  });
  return {changelog, changelogType: 'markdown', displayName: `${version} · ${entry.loader} · ${entry.minecraft}`,
    gameVersions: [gameId, loaderId, ids.client, ids.server], releaseType: 'release',
    ...(projects.length ? {relations: {projects}} : {})};
}

/** An uncertain CurseForge receipt whose POST got a synchronous HTTP 400 JSON answer without a file ID. */
export function rejectedByCurseForge(row) {
  const diagnostic=row?.request_diagnostic;
  return row?.platform==='curseforge' && row.status==='uncertain' && !Object.hasOwn(row,'id')
    && diagnostic?.stage==='http-status' && diagnostic.method==='POST' && diagnostic.http_status===400
    && diagnostic.response_json_type==='object' && diagnostic.response_id_type==='absent';
}

export function matchingCurseForge(existing, entry, descriptor, dependencyMap) {
  const matches = existing.filter(file => file.fileName === entry.file_name || file.displayName === descriptor.displayName);
  if (!matches.length) return null;
  if (matches.length !== 1) throw new Error('Ambiguous existing CurseForge release: ' + entry.file_name);
  const file = matches[0];
  const tags = file.gameVersions || [];
  const loaderTags = tags.filter(tag => ['Fabric','Forge','NeoForge','Quilt'].includes(tag));
  const mcTags = tags.filter(tag => /^(?:1\.\d+(?:\.\d+)?|26(?:\.\d+)+)$/.test(tag));
  if (JSON.stringify(loaderTags) !== JSON.stringify([{fabric:'Fabric',forge:'Forge',neoforge:'NeoForge'}[entry.loader]])
      || JSON.stringify(mcTags) !== JSON.stringify([entry.minecraft]) || !tags.includes('Client') || !tags.includes('Server'))
    throw new Error('Existing CurseForge target tags differ: ' + entry.file_name);
  const sha1 = (file.hashes || []).find(hash => hash.algo === 1)?.value?.toLowerCase();
  // Core FileDependency.relationType is the documented 1..6 enum. An absent
  // dependency list is not evidence of an empty required-dependency set.
  if (!Array.isArray(file.dependencies) || file.dependencies.some(dep => !dep
      || !curseForgeId(dep.modId)
      || !Number.isInteger(dep.relationType) || dep.relationType < 1 || dep.relationType > 6)) {
    throw new Error('Malformed CurseForge dependencies; cannot verify artifact: ' + entry.file_name);
  }
  const required = file.dependencies.filter(dep => dep.relationType === 3).map(dep => dep.modId).sort((a,b) => a-b);
  const expectedRequired = entry.required_mod_ids.map(id => dependencyMap[id]?.curseforge_id).sort((a,b) => a-b);
  if (!curseForgeId(file.id) || file.modId !== 1079687 || file.fileName !== entry.file_name || file.displayName !== descriptor.displayName || sha1 !== entry.sha1
      || file.releaseType !== 1 || !(file.gameVersions || []).includes(entry.minecraft)
      || !(file.gameVersions || []).includes({fabric:'Fabric',forge:'Forge',neoforge:'NeoForge'}[entry.loader])
      || expectedRequired.some(id => !curseForgeId(id))
      || JSON.stringify(required) !== JSON.stringify(expectedRequired)) {
    throw new Error('Existing CurseForge artifact differs; refusing overwrite/duplicate: ' + entry.file_name);
  }
  return file.id;
}

export function isPubliclyReleased(platform, record) {
  if (platform === 'curseforge') return record?.isAvailable === true && [4,10].includes(record?.fileStatus);
  return false;
}

const errorTypes=new Set(['Error','TypeError','RangeError','SyntaxError','TimeoutError','AbortError']);
const transportCodes=new Set(['ECONNRESET','ECONNREFUSED','ENOTFOUND','EAI_AGAIN','ETIMEDOUT',
  'UND_ERR_CONNECT_TIMEOUT','UND_ERR_HEADERS_TIMEOUT','UND_ERR_BODY_TIMEOUT','UND_ERR_SOCKET']);
const valueType=value=>value===null?'null':Array.isArray(value)?'array':typeof value;
function requestFailure(stage, facts, error) {
  // Never retain arbitrary error messages, causes, URLs, headers or body text:
  // any of those can echo authentication values. Fixed enums and shapes only.
  const diagnostic={schema_version:1,stage,...facts,
    error_type:errorTypes.has(error?.name)?error.name:'Error',
    transport_code:transportCodes.has(error?.code)?error.code:
      transportCodes.has(error?.cause?.code)?error.cause.code:null,
    transmission_outcome:'unknown'};
  const failure=new Error(`Store request failed at ${stage}${facts.http_status?` (HTTP${facts.http_status})`:''}`);
  failure.requestDiagnostic=diagnostic;
  return failure;
}

async function requestJson(url, options = {}, uploadIdValidator = null) {
  const facts={method:options.method==='POST'?'POST':'GET'};
  let response;
  try {response=await fetch(url, {...options, redirect: 'error', signal: AbortSignal.timeout(options.method === 'POST' ? 60000 : 30000)});}
  catch(error) {throw requestFailure('fetch',facts,error);}
  facts.http_status=response.status;
  const contentType=response.headers.get('content-type')?.split(';')[0].trim().toLowerCase();
  facts.response_content_type=['application/json','text/html','text/plain'].includes(contentType)?contentType:contentType?'other':'absent';
  let body;
  try {body=await response.text();}
  catch(error) {throw requestFailure('response-body',facts,error);}
  facts.response_bytes=Buffer.byteLength(body);
  let payload;
  try {payload=body?JSON.parse(body):null;facts.response_json_type=valueType(payload);}
  catch(error) {facts.response_json_type='invalid';if(response.ok) throw requestFailure('response-json',facts,error);}
  if (payload && typeof payload==='object' && !Array.isArray(payload)) {
    facts.response_id_type=Object.hasOwn(payload,'id')?valueType(payload.id):'absent';
    facts.response_has_error=Object.hasOwn(payload,'error');
    facts.response_has_message=Object.hasOwn(payload,'message');
    // CurseForge's numeric validation code is a fixed fact, unlike its message text.
    if (Number.isSafeInteger(payload.errorCode)) facts.response_error_code=payload.errorCode;
  }
  if (!response.ok) throw requestFailure('http-status',facts);
  if (uploadIdValidator && !uploadIdValidator(payload?.id)) throw requestFailure('upload-id',facts);
  return payload;
}

function curseForgeAudit(existing, entries, version, changelog, ids, dependencies, state, journalBytes) {
  return {schema_version:1,observed_at:new Date().toISOString(),project_id:1079687,
    complete_core_inventory:true,inventory_count:existing.length,
    author_pending_upload_visibility:'not-established',
    journal_sha256:journalBytes===null?null:crypto.createHash('sha256').update(journalBytes).digest('hex'),
    entries:entries.map(entry=>{
      const descriptor=curseForgeDescriptor(entry,version,changelog,ids,dependencies);
      const id=matchingCurseForge(existing,entry,descriptor,dependencies);
      const remote=existing.find(file=>file.id===id);
      const receipt=state.uploads.find(row=>row.platform==='curseforge'&&row.file===entry.file_name);
      return {file_name:entry.file_name,expected_sha1:entry.sha1,expected_sha256:entry.sha256,
        expected_required_dependency_ids:entry.required_mod_ids.map(dep=>dependencies[dep].curseforge_id).sort((a,b)=>a-b),
        journal_status:receipt?.status??null,journal_id:curseForgeId(receipt?.id)?receipt.id:null,
        match:remote?{id,mod_id:remote.modId,file_name:remote.fileName,display_name:remote.displayName,
          sha1:remote.hashes.find(hash=>hash.algo===1).value.toLowerCase(),
          required_dependency_ids:remote.dependencies.filter(dep=>dep.relationType===3).map(dep=>dep.modId).sort((a,b)=>a-b),
          target_tags:remote.gameVersions.filter(tag=>[entry.minecraft,{fabric:'Fabric',forge:'Forge',neoforge:'NeoForge'}[entry.loader],'Client','Server'].includes(tag)),
          release_type:remote.releaseType,file_status:Number.isInteger(remote.fileStatus)?remote.fileStatus:null,
          is_available:typeof remote.isAvailable==='boolean'?remote.isAvailable:null,
          publicly_released:isPubliclyReleased('curseforge',remote)}:null};
    })};
}

async function curseForgeFiles(projectId, apiKey) {
  // https://docs.curseforge.com/rest-api/#pagination-limits and #schemapagination
  // require index + pageSize <= 10000, with all four pagination fields present.
  const pageSize=50, limit=10000, files=[], seen=new Set();
  let totalCount=null;
  for (let index=0; index+pageSize<=limit; index+=pageSize) {
    const payload=await requestJson(`https://api.curseforge.com/v1/mods/${projectId}/files?pageSize=${pageSize}&index=${index}`,
      {headers:{'x-api-key':apiKey}});
    const pagination=payload?.pagination;
    if (!Array.isArray(payload?.data) || !pagination
        || !['index','pageSize','resultCount','totalCount'].every(key => Number.isSafeInteger(pagination[key]))
        || pagination.index!==index || pagination.pageSize!==pageSize
        || pagination.resultCount!==payload.data.length || pagination.totalCount<index
        || pagination.totalCount>limit || pagination.resultCount!==Math.min(pageSize,pagination.totalCount-index)
        || (totalCount!==null && pagination.totalCount!==totalCount)) {
      throw new Error('Malformed/truncated/changing CurseForge file inventory; cannot deduplicate');
    }
    totalCount=pagination.totalCount;
    for (const file of payload.data) {
      if (!file || !curseForgeId(file.id) || seen.has(file.id)
          || typeof file.fileName!=='string' || !file.fileName
          || typeof file.displayName!=='string' || !file.displayName) {
        throw new Error('Malformed/duplicate CurseForge file inventory; cannot deduplicate');
      }
      seen.add(file.id);
    }
    files.push(...payload.data);
    if (files.length===totalCount) return files;
  }
  throw new Error('CurseForge file inventory exceeds safe bound; cannot deduplicate');
}

export async function main(argv=process.argv.slice(2)) {
  const args={};
  for (let i=0;i<argv.length;i++) {
    if (argv[i]==='--dry-run') args.dryRun=true;
    else if (argv[i]==='--preflight-only') args.preflightOnly=true;
    else if (argv[i]==='--retry-rejected-curseforge') args.retryRejectedCurseForge=true;
    else if (argv[i]==='--continue-past-pending-curseforge') args.continuePastPendingCurseForge=true;
    else if (['--manifest','--directory','--dependencies','--changelog','--state','--platforms'].includes(argv[i])) args[argv[i].slice(2)]=argv[++i];
    else throw new Error('Unknown argument: '+argv[i]);
  }
  if (args.dryRun && args.preflightOnly) throw new Error('Offline dry-run and credentialed read-only preflight are mutually exclusive');
  for (const key of ['manifest','directory','dependencies','changelog','state']) if (!args[key]) throw new Error('--'+key+' is required');
  // --platforms stays accepted for older command lines, but CurseForge is the only destination.
  const platforms=args.platforms||'curseforge';
  if (platforms!=='curseforge') throw new Error('Unknown platforms: NightBeam publishes to CurseForge only');
  if (args.retryRejectedCurseForge && args.dryRun)
    throw new Error('Rejected-receipt retry requires credentialed read-only preflight or live publication');
  if (args.continuePastPendingCurseForge && (args.dryRun || args.preflightOnly))
    throw new Error('Continuing past pending CurseForge review requires live publication');
  const manifest=JSON.parse(fs.readFileSync(args.manifest,'utf8'));
  const entries=validatePlan(manifest,args.directory);
  const dependencies=JSON.parse(fs.readFileSync(args.dependencies,'utf8'));
  const changelog=fs.readFileSync(args.changelog,'utf8');
  const version=manifest.release_version;
  const projectCf=process.env.CURSEFORGE_ID||'1079687';
  if (projectCf!=='1079687') throw new Error('Unexpected release destination');
  const journalBytes=fs.existsSync(args.state)?fs.readFileSync(args.state):null;
  const state=journalBytes!==null ? JSON.parse(journalBytes.toString('utf8'))
    : {schema_version:1,source_commit:manifest.source_commit,version,uploads:[]};
  if (state.schema_version!==1 || state.source_commit!==manifest.source_commit || state.version!==version || !Array.isArray(state.uploads))
    throw new Error('Invalid publication journal/source/version; reconcile before retry');
  const statuses=new Set(['dry-run','dry-run-metadata-IDs-placeholder','in-flight','uncertain','rejected','submitted','pending-publication','verified','verified-existing']);
  const seen=new Set();
  for (const row of state.uploads) {
    const entry=entries.find(entry=>entry.file_name===row.file);
    const key=row.platform+':'+row.file;
    // Journals written before 24 Nov 2026 may still hold receipts of the old second store; they are kept as they are
    // and never acted on.
    if (!entry || !['modrinth','curseforge'].includes(row.platform) || row.sha256!==entry.sha256
        || !statuses.has(row.status) || seen.has(key)) throw new Error('Invalid publication journal/hash/entry; reconcile before retry');
    seen.add(key);
  }
  if (args.dryRun && state.uploads.some(row=>!row.status.startsWith('dry-run')))
    throw new Error('Dry-run cannot erase an existing publication journal; choose a fresh state file');
  const save=()=>{
    const temporary=args.state+'.tmp';
    fs.writeFileSync(temporary,JSON.stringify(state,null,2)+'\n');
    fs.renameSync(temporary,args.state);
  };
  const prior=(platform,entry)=>state.uploads.find(row=>row.platform===platform&&row.file===entry.file_name);
  const record=(platform,entry,status,extra={})=>{
    const row=prior(platform,entry);
    const value={platform,file:entry.file_name,sha256:entry.sha256,status,...extra};
    if (row) Object.assign(row,value); else state.uploads.push(value);
    save();
  };
  // Once one file of this release is verified on CurseForge, its metadata is proven, so later uploads need not
  // wait for each review. Files still under review keep their submitted receipt and file ID and are never reposted.
  const curseForgeProven=()=>state.uploads.some(row=>row.platform==='curseforge'&&['verified','verified-existing'].includes(row.status)&&curseForgeId(row.id));
  const awaitingCurseForgeReview=entry=>{
    const row=prior('curseforge',entry);
    return ['submitted','pending-publication'].includes(row?.status) && curseForgeId(row.id);
  };
  const requireResolved=(platform,entry)=>{
    const row=prior(platform,entry);
    if (row && !row.status.startsWith('dry-run') && row.status!=='rejected')
      throw new Error('Prior upload is pending/submitted or missing from inventory; reconcile before retry: '+entry.file_name);
  };
  if (!args.dryRun && !args.preflightOnly) {state.publicly_verified=false;save();}
  for (const entry of entries) for (const id of entry.required_mod_ids) {
    if (!dependencies[id]?.curseforge_slug || !Number.isInteger(dependencies[id]?.curseforge_id))
      throw new Error('Incomplete verified dependency mapping: '+id);
  }
  if (!args.dryRun && (!process.env.CURSEFORGE_TOKEN || !process.env.CURSEFORGE_API_KEY))
    throw new Error('CURSEFORGE_TOKEN and CURSEFORGE_API_KEY required');
  if (!args.preflightOnly) {state.publicly_verified=false;save();}
  // Preflight the CurseForge inventory, catalog and all descriptors/conflicts before the first POST.
  let cfInventory=null,cfIds=null,cfCatalogEvidence=null;
  cfInventory=args.dryRun?[]:await curseForgeFiles(projectCf,process.env.CURSEFORGE_API_KEY);
  cfIds=args.dryRun?{client:1,server:2,loader:{fabric:3,forge:4,neoforge:5},minecraft:Object.fromEntries(entries.map(entry=>[entry.minecraft,6]))}
    :await curseForgeIds(process.env.CURSEFORGE_TOKEN,process.env.CURSEFORGE_API_KEY,entries,requestJson,evidence=>{cfCatalogEvidence=evidence;});
  for (const entry of entries) matchingCurseForge(cfInventory,entry,curseForgeDescriptor(entry,version,changelog,cfIds,dependencies),dependencies);
  // A synchronous HTTP 400 validation answer with no file ID is the provider refusing that exact request.
  // Retry such a receipt only on explicit request, and only while the complete live inventory has no copy.
  let rejectedRetry=null;
  if (args.retryRejectedCurseForge) {
    rejectedRetry=entries.filter(entry=>rejectedByCurseForge(prior('curseforge',entry))).map(entry=>{
      if (matchingCurseForge(cfInventory,entry,curseForgeDescriptor(entry,version,changelog,cfIds,dependencies),dependencies))
        throw new Error('HTTP 400 receipt has a matching CurseForge file; reconcile instead of retrying: '+entry.file_name);
      return entry;
    });
    if (!rejectedRetry.length) throw new Error('No CurseForge receipt was rejected with HTTP 400; nothing to retry');
  }
  if (args.preflightOnly) {
    console.log(`Read-only preflight verified release ${version}: ${entries.length} artifacts; no journal changes or store writes`);
    return {preflight_verified:true,source_commit:manifest.source_commit,version,platforms,curseforge_ids:cfIds,curseforge_catalog:cfCatalogEvidence,
      publication_scope:'full',
      curseforge_rejected_retry:rejectedRetry&&rejectedRetry.map(entry=>entry.file_name),
      curseforge_inventory:curseForgeAudit(cfInventory,entries,version,changelog,cfIds,dependencies,state,journalBytes)};
  }
  {
    let existing=cfInventory;
    const ids=cfIds;
    for (const entry of rejectedRetry||[]) {
      const row=prior('curseforge',entry);
      record('curseforge',entry,'rejected',{request_diagnostic:row.request_diagnostic,
        rejection_evidence:{http_status:400,response_id_type:'absent',remote_match:null,inventory_count:existing.length,observed_at:new Date().toISOString()}});
    }
    for (const entry of entries) {
      const descriptor=curseForgeDescriptor(entry,version,changelog,ids,dependencies);
      const duplicate=matchingCurseForge(existing,entry,descriptor,dependencies);
      if (duplicate) {const remote=existing.find(item=>item.id===duplicate);record('curseforge',entry,isPubliclyReleased('curseforge',remote)?'verified-existing':'pending-publication',{id:duplicate,provider_status:remote?.fileStatus});continue;}
      if (args.dryRun) {record('curseforge',entry,'dry-run-metadata-IDs-placeholder',{descriptor});continue;}
      if (args.continuePastPendingCurseForge && awaitingCurseForgeReview(entry) && curseForgeProven()) continue;
      requireResolved('curseforge',entry);
      validatePlan(manifest,args.directory);
      const form=new FormData();form.append('metadata',JSON.stringify(descriptor));
      form.append('file',new Blob([fs.readFileSync(entry.absolute_path)]),entry.file_name);
      record('curseforge',entry,'in-flight');
      let created;
      try {
        created=await requestJson(`https://minecraft.curseforge.com/api/projects/${projectCf}/upload-file`,
          {method:'POST',headers:{'X-Api-Token':process.env.CURSEFORGE_TOKEN},body:form},curseForgeId);
      } catch (error) {
        const diagnostic=error.requestDiagnostic??{schema_version:1,stage:'unknown',error_type:'Error',transmission_outcome:'unknown'};
        record('curseforge',entry,'uncertain',{request_diagnostic:diagnostic});
        throw new Error('Uncertain CurseForge upload outcome; reconcile before retry: '+entry.file_name+'; '+JSON.stringify(diagnostic));
      }
      record('curseforge',entry,'submitted',{id:created.id});
      existing=await curseForgeFiles(projectCf,process.env.CURSEFORGE_API_KEY);
      if (matchingCurseForge(existing,entry,descriptor,dependencies)!==created.id) {
        if (args.continuePastPendingCurseForge && curseForgeProven()) continue;
        throw new Error('CurseForge upload awaits indexing/review; reconcile before retry');
      }
      const remote=existing.find(item=>item.id===created.id);
      record('curseforge',entry,isPubliclyReleased('curseforge',remote)?'verified':'pending-publication',{id:created.id,provider_status:remote?.fileStatus});
    }
  }
  const selected=state.uploads.filter(row=>row.platform==='curseforge');
  const expectedRows=entries.length;
  const verifiedStatus=row=>['verified','verified-existing'].includes(row.status);
  const hasProviderId=row=>curseForgeId(row.id);
  state.publicly_verified=!args.dryRun && selected.length===expectedRows && selected.every(verifiedStatus);
  save();
  if (!args.dryRun && !state.publicly_verified) {
    // Every file was accepted by its store (it has a provider file ID) and only awaits review/listing,
    // which is normal for CurseForge. That is a finished upload, not a failure. Anything else still fails.
    // CurseForge FileStatus: 2 ChangesRequired, 5 Rejected, 6 MalwareDetected, 7 Deleted, 15 FailedPublishing.
    const refused=row=>row.platform==='curseforge' && [2,5,6,7,15].includes(row.provider_status);
    if (selected.some(refused))
      throw new Error('CurseForge refused or removed a release file: '+selected.filter(refused).map(row=>row.file).join(', '));
    const accepted=selected.length===expectedRows && selected.every(row=>
      ['submitted','pending-publication','verified','verified-existing'].includes(row.status) && hasProviderId(row));
    if (!accepted) throw new Error('Release incomplete: not every artifact was accepted by its store; retain receipts and resume read-only reconciliation');
    const waiting=selected.filter(row=>!verifiedStatus(row));
    console.log(`::warning title=Store approval pending::Release ${version}: every file was accepted; CurseForge approval pending for ${waiting.length} file(s). A later read-only run reconciles them.`);
    console.log(`Submitted release ${version}: ${selected.length} CurseForge entries, ${waiting.length} awaiting store approval`);
    return state;
  }
  console.log(`${args.dryRun?'Dry-run':'Verified'} release ${version}: ${selected.length} CurseForge entries`);
  return state;
}

if (process.argv[1] && import.meta.url===pathToFileURL(process.argv[1]).href) {
  main().catch(error=>{console.error(error.message);process.exitCode=1;});
}
