#!/usr/bin/env node
/** Publish only a complete, source-bound inventory. No implicit local secret reads or blind retries. */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { pathToFileURL } from 'node:url';
import { curseForgeIds } from './curseforge-release-catalog.mjs';

const modrinthId = id => typeof id === 'string' && /^[0-9A-Za-z]+$/.test(id);
const curseForgeId = id => Number.isSafeInteger(id) && id > 0 && id <= 0xffffffff;

export function validatePlan(manifest, directory) {
  if (manifest?.schema_version !== 1 || manifest.complete_inventory !== true || manifest.artifact_count !== 10
      || !/^[0-9a-f]{40}$/.test(manifest.source_commit) || !/^\d+\.\d+\.\d+$/.test(manifest.release_version)
      || manifest.mod_id !== 'rpg_attribute_system' || !Array.isArray(manifest.files) || manifest.files.length !== 10) {
    throw new Error('A complete verified ten-JAR manifest is required');
  }
  const expected = new Set(['1.20.1:fabric','1.20.1:forge','1.21.1:fabric','1.21.1:neoforge',
    '26.1.2:fabric','26.1.2:neoforge','26.2:fabric','26.2:neoforge','26.3:fabric','26.3:neoforge']);
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

export function modrinthDescriptor(entry, version, changelog, projectId, dependencyMap) {
  return {name: `${version} · ${entry.loader} · ${entry.minecraft}`,
    version_number: `${version}+${entry.loader}-${entry.minecraft}`, changelog,
    dependencies: entry.required_mod_ids.map(id => {
      const target = dependencyMap[id]?.modrinth;
      if (!target) throw new Error('Missing verified Modrinth dependency mapping: ' + id);
      return {project_id: target, version_id: null, file_name: null, dependency_type: 'required'};
    }), game_versions: [entry.minecraft], version_type: 'release', loaders: [entry.loader], featured: false,
    status: 'listed', project_id: projectId, file_parts: ['file_0'], primary_file: 'file_0'};
}

export function curseForgeDescriptor(entry, version, changelog, ids, dependencyMap) {
  const loaderId = ids.loader[entry.loader], gameId = ids.minecraft[entry.minecraft];
  if (!Number.isInteger(loaderId) || !Number.isInteger(gameId) || !Number.isInteger(ids.client) || !Number.isInteger(ids.server)) {
    throw new Error('Incomplete exact CurseForge game/loader metadata');
  }
  return {changelog, changelogType: 'markdown', displayName: `${version} · ${entry.loader} · ${entry.minecraft}`,
    gameVersions: [gameId, loaderId, ids.client, ids.server], releaseType: 'release',
    relations: {projects: entry.required_mod_ids.map(id => {
      const slug = dependencyMap[id]?.curseforge_slug;
      if (!slug) throw new Error('Missing verified CurseForge dependency mapping: ' + id);
      return {slug, projectID: String(dependencyMap[id].curseforge_id), type: 'requiredDependency'};
    })}};
}

export function matchingModrinth(existing, entry, descriptor) {
  const matches = existing.filter(version => version.version_number === descriptor.version_number
    || (version.files || []).some(file => file.filename === entry.file_name));
  if (!matches.length) return null;
  if (matches.length !== 1) throw new Error('Ambiguous existing Modrinth release: ' + entry.file_name);
  const version = matches[0], file = (version.files || []).find(file => file.filename === entry.file_name);
  // Modrinth allows nullable project/version IDs and external filenames, but
  // each dependency must have a valid relation and at least one usable target.
  if (!Array.isArray(version.dependencies) || version.dependencies.some(dep => !dep
      || !['required','optional','incompatible','embedded'].includes(dep.dependency_type)
      || (dep.project_id != null && !modrinthId(dep.project_id))
      || (dep.version_id != null && !modrinthId(dep.version_id))
      || (dep.file_name != null && (typeof dep.file_name !== 'string' || !dep.file_name.trim()))
      || (!modrinthId(dep.project_id) && !modrinthId(dep.version_id) && !dep.file_name))) {
    throw new Error('Malformed Modrinth dependencies; cannot verify artifact: ' + entry.file_name);
  }
  const required = version.dependencies.filter(dep => dep.dependency_type === 'required').map(dep => dep.project_id).sort();
  const expectedRequired = descriptor.dependencies.map(dep => dep.project_id).sort();
  if (!modrinthId(version.id) || version.project_id !== descriptor.project_id || !file || file.hashes?.sha512 !== entry.sha512 || file.hashes?.sha1 !== entry.sha1
      || version.version_number !== descriptor.version_number || version.version_type !== 'release'
      || JSON.stringify([...version.loaders].sort()) !== JSON.stringify(descriptor.loaders)
      || JSON.stringify([...version.game_versions].sort()) !== JSON.stringify(descriptor.game_versions)
      || expectedRequired.some(id => !modrinthId(id)) || JSON.stringify(required) !== JSON.stringify(expectedRequired)) {
    throw new Error('Existing Modrinth artifact differs; refusing overwrite/duplicate: ' + entry.file_name);
  }
  return version.id;
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
  if (platform === 'modrinth') return record?.status === 'listed' && record?.version_type === 'release';
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
  }
  if (!response.ok) throw requestFailure('http-status',facts);
  if (uploadIdValidator && !uploadIdValidator(payload?.id)) throw requestFailure('upload-id',facts);
  return payload;
}

function curseForgeAudit(existing, entries, version, changelog, ids, dependencies, state, journalBytes, isolation) {
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
        isolation_class:isolation?(receipt?'excluded-journaled':remote?'never-attempted-exact-existing':'never-attempted-post-candidate'):null,
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
    else if (argv[i]==='--require-public-modrinth') args.requirePublicModrinth=true;
    else if (argv[i]==='--isolate-never-attempted-curseforge') args.isolateNeverAttemptedCurseForge=true;
    else if (['--manifest','--directory','--dependencies','--changelog','--state','--platforms'].includes(argv[i])) args[argv[i].slice(2)]=argv[++i];
    else throw new Error('Unknown argument: '+argv[i]);
  }
  if (args.dryRun && args.preflightOnly) throw new Error('Offline dry-run and credentialed read-only preflight are mutually exclusive');
  for (const key of ['manifest','directory','dependencies','changelog','state']) if (!args[key]) throw new Error('--'+key+' is required');
  const platforms=args.platforms||'both';
  if (!['both','modrinth','curseforge'].includes(platforms)) throw new Error('Unknown platforms');
  if (args.requirePublicModrinth && (!args.preflightOnly || platforms==='curseforge'))
    throw new Error('Public Modrinth recovery guard requires credentialed Modrinth preflight');
  if (args.isolateNeverAttemptedCurseForge && (args.dryRun || platforms==='modrinth' || (!args.preflightOnly && platforms!=='curseforge')))
    throw new Error('Isolation requires credentialed read-only preflight or CurseForge-only live publication');
  const manifest=JSON.parse(fs.readFileSync(args.manifest,'utf8'));
  const entries=validatePlan(manifest,args.directory);
  const dependencies=JSON.parse(fs.readFileSync(args.dependencies,'utf8'));
  const changelog=fs.readFileSync(args.changelog,'utf8');
  const version=manifest.release_version;
  const projectMr=process.env.MODRINTH_ID||'d85UTOuq', projectCf=process.env.CURSEFORGE_ID||'1079687';
  if (projectMr!=='d85UTOuq' || projectCf!=='1079687') throw new Error('Unexpected release destination');
  const journalBytes=fs.existsSync(args.state)?fs.readFileSync(args.state):null;
  const state=journalBytes!==null ? JSON.parse(journalBytes.toString('utf8'))
    : {schema_version:1,source_commit:manifest.source_commit,version,uploads:[]};
  if (state.schema_version!==1 || state.source_commit!==manifest.source_commit || state.version!==version || !Array.isArray(state.uploads))
    throw new Error('Invalid publication journal/source/version; reconcile before retry');
  const statuses=new Set(['dry-run','dry-run-metadata-IDs-placeholder','in-flight','uncertain','submitted','pending-publication','verified','verified-existing']);
  const seen=new Set();
  for (const row of state.uploads) {
    const entry=entries.find(entry=>entry.file_name===row.file);
    const key=row.platform+':'+row.file;
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
  const requireResolved=(platform,entry)=>{
    const row=prior(platform,entry);
    if (row && !row.status.startsWith('dry-run'))
      throw new Error('Prior upload is pending/submitted or missing from inventory; reconcile before retry: '+entry.file_name);
  };
  let isolation=null;
  if (args.isolateNeverAttemptedCurseForge) {
    const firstFile='rpg_attribute_system-fabric-1.20.1-4.3.0.jar';
    const first=entries.find(entry=>entry.file_name===firstFile),receipt=first&&prior('curseforge',first);
    const mrRows=state.uploads.filter(row=>row.platform==='modrinth');
    if (version!=='4.3.0' || manifest.source_commit!=='09a559f500daa21ce6dbc60d80ea0b490a605915'
        || !receipt || receipt.status!=='uncertain' || Object.hasOwn(receipt,'id') || receipt.sha256!==first.sha256
        || mrRows.length!==10 || entries.some(entry=>{const row=prior('modrinth',entry);
          return !row || !['verified','verified-existing'].includes(row.status) || !modrinthId(row.id);}))
      throw new Error('Isolation requires immutable09a4.3.0, ten preserved verified Modrinth rows and the unchanged first uncertain CurseForge receipt without ID');
    // No prior journal status, including a dry-run status, is permission for a
    // POST here. Every journaled target is frozen and excluded from this loop.
    isolation={scope:'never-attempted-curseforge',frozen_first_file:firstFile,
      selected_targets:entries.filter(entry=>!prior('curseforge',entry)).map(entry=>entry.file_name),
      excluded_targets:entries.filter(entry=>prior('curseforge',entry)).map(entry=>({
        file_name:entry.file_name,reason:entry.file_name===firstFile?'frozen-first-uncertain':'prior-journal-entry',
        journal_status:prior('curseforge',entry).status}))};
    isolation.selected_count=isolation.selected_targets.length;
    isolation.excluded_count=isolation.excluded_targets.length;
    isolation.unresolved_excluded_count=isolation.excluded_targets.filter(row=>!['verified','verified-existing'].includes(row.journal_status)).length;
    if (!args.preflightOnly && isolation.selected_count===0)
      throw new Error('No never-attempted CurseForge targets remain; use read-only preflight and retain every unresolved receipt');
  }
  if (!args.dryRun && !args.preflightOnly) {state.publicly_verified=false;save();}
  for (const entry of entries) for (const id of entry.required_mod_ids) {
    if (!dependencies[id]?.modrinth || !dependencies[id]?.curseforge_slug || !Number.isInteger(dependencies[id]?.curseforge_id))
      throw new Error('Incomplete verified dependency mapping: '+id);
  }
  if (!args.dryRun && platforms!=='curseforge' && !process.env.MODRINTH_TOKEN) throw new Error('MODRINTH_TOKEN required');
  if (!args.dryRun && platforms!=='modrinth' && (!process.env.CURSEFORGE_TOKEN || !process.env.CURSEFORGE_API_KEY))
    throw new Error('CURSEFORGE_TOKEN and CURSEFORGE_API_KEY required');
  if (!args.preflightOnly) {state.publicly_verified=false;save();}
  // Preflight both selected providers and all descriptors/conflicts before the first POST.
  let mrInventory=null,cfInventory=null,cfIds=null,cfCatalogEvidence=null;
  if (platforms!=='curseforge') {
    mrInventory=args.dryRun?[]:await requestJson(`https://api.modrinth.com/v2/project/${projectMr}/version`,{headers:{Authorization:process.env.MODRINTH_TOKEN}});
    if (!Array.isArray(mrInventory)) throw new Error('Malformed Modrinth release inventory');
    for (const entry of entries) {
      const duplicate=matchingModrinth(mrInventory,entry,modrinthDescriptor(entry,version,changelog,projectMr,dependencies));
      if (args.requirePublicModrinth) {
        const receipt=prior('modrinth',entry);
        const remote=mrInventory.find(record=>record.id===duplicate);
        if (!duplicate || !receipt || receipt.id!==duplicate || !['verified','verified-existing'].includes(receipt.status)
            || !isPubliclyReleased('modrinth',remote))
          throw new Error('Preserved Modrinth receipt is missing, changed or not publicly listed: '+entry.file_name);
      }
    }
  }
  if (platforms!=='modrinth') {
    cfInventory=args.dryRun?[]:await curseForgeFiles(projectCf,process.env.CURSEFORGE_API_KEY);
    cfIds=args.dryRun?{client:1,server:2,loader:{fabric:3,forge:4,neoforge:5},minecraft:Object.fromEntries(entries.map(entry=>[entry.minecraft,6]))}
      :await curseForgeIds(process.env.CURSEFORGE_TOKEN,process.env.CURSEFORGE_API_KEY,entries,requestJson,evidence=>{cfCatalogEvidence=evidence;});
    for (const entry of entries) matchingCurseForge(cfInventory,entry,curseForgeDescriptor(entry,version,changelog,cfIds,dependencies),dependencies);
  }
  if (args.preflightOnly) {
    console.log(`Read-only preflight verified release ${version}: ${entries.length} artifacts; no journal changes or store writes`);
    return {preflight_verified:true,source_commit:manifest.source_commit,version,platforms,curseforge_ids:cfIds,curseforge_catalog:cfCatalogEvidence,
      publication_scope:isolation?'never-attempted-curseforge':'full',curseforge_isolation:isolation,
      curseforge_inventory:platforms==='modrinth'?null:curseForgeAudit(cfInventory,entries,version,changelog,cfIds,dependencies,state,journalBytes,isolation)};
  }
  if (platforms!=='curseforge') {
    let existing=mrInventory;
    if (!Array.isArray(existing)) throw new Error('Malformed Modrinth release inventory');
    for (const entry of entries) {
      const descriptor=modrinthDescriptor(entry,version,changelog,projectMr,dependencies);
      const duplicate=matchingModrinth(existing,entry,descriptor);
      if (duplicate) {const remote=existing.find(item=>item.id===duplicate);record('modrinth',entry,isPubliclyReleased('modrinth',remote)?'verified-existing':'pending-publication',{id:duplicate,provider_status:remote?.status});continue;}
      if (args.dryRun) {record('modrinth',entry,'dry-run',{descriptor});continue;}
      requireResolved('modrinth',entry);
      validatePlan(manifest,args.directory);
      const form=new FormData();form.append('data',JSON.stringify(descriptor));
      form.append('file_0',new Blob([fs.readFileSync(entry.absolute_path)]),entry.file_name);
      record('modrinth',entry,'in-flight'); // Durable before the POST, including lost-response cases.
      let created;
      try {
        created=await requestJson('https://api.modrinth.com/v2/version',{method:'POST',headers:{Authorization:process.env.MODRINTH_TOKEN},body:form},modrinthId);
      } catch (error) {
        const diagnostic=error.requestDiagnostic??{schema_version:1,stage:'unknown',error_type:'Error',transmission_outcome:'unknown'};
        record('modrinth',entry,'uncertain',{request_diagnostic:diagnostic});
        throw new Error('Uncertain Modrinth upload outcome; reconcile before retry: '+entry.file_name+'; '+JSON.stringify(diagnostic));
      }
      record('modrinth',entry,'submitted',{id:created.id});
      existing=await requestJson(`https://api.modrinth.com/v2/project/${projectMr}/version`,{headers:{Authorization:process.env.MODRINTH_TOKEN}});
      if (matchingModrinth(existing,entry,descriptor)!==created.id) throw new Error('Modrinth upload awaits visibility; reconcile before retry');
      const remote=existing.find(item=>item.id===created.id);
      record('modrinth',entry,isPubliclyReleased('modrinth',remote)?'verified':'pending-publication',{id:created.id,provider_status:remote?.status});
    }
  }
  if (platforms!=='modrinth') {
    let existing=cfInventory;
    const ids=cfIds;
    for (const entry of entries) {
      // Frozen entries are skipped before even the existing-file reconciliation
      // branch. Isolation cannot change their status/ID/diagnostic or POST them.
      if (isolation && !isolation.selected_targets.includes(entry.file_name)) continue;
      const descriptor=curseForgeDescriptor(entry,version,changelog,ids,dependencies);
      const duplicate=matchingCurseForge(existing,entry,descriptor,dependencies);
      if (duplicate) {const remote=existing.find(item=>item.id===duplicate);record('curseforge',entry,isPubliclyReleased('curseforge',remote)?'verified-existing':'pending-publication',{id:duplicate,provider_status:remote?.fileStatus});continue;}
      if (args.dryRun) {record('curseforge',entry,'dry-run-metadata-IDs-placeholder',{descriptor});continue;}
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
      if (matchingCurseForge(existing,entry,descriptor,dependencies)!==created.id)
        throw new Error('CurseForge upload awaits indexing/review; reconcile before retry');
      const remote=existing.find(item=>item.id===created.id);
      record('curseforge',entry,isPubliclyReleased('curseforge',remote)?'verified':'pending-publication',{id:created.id,provider_status:remote?.fileStatus});
    }
  }
  const selected=state.uploads.filter(row=>platforms==='both'||row.platform===platforms);
  state.publicly_verified=!args.dryRun && selected.length===(platforms==='both'?20:10)
    && selected.every(row=>['verified','verified-existing'].includes(row.status));
  save();
  if (isolation) {
    const isolatedVerified=isolation.selected_targets.every(file=>{
      const row=state.uploads.find(row=>row.platform==='curseforge'&&row.file===file);
      return row && ['verified','verified-existing'].includes(row.status);
    });
    if (!isolatedVerified) throw new Error('Isolated CurseForge targets await publication; retain latest receipts without retrying journaled targets');
    console.log(`Verified isolated CurseForge targets: ${isolation.selected_targets.length}; full release remains unresolved`);
    return {...state,publication_scope:isolation.scope,curseforge_isolation:isolation,isolated_curseforge_verified:true};
  }
  if (!args.dryRun && !state.publicly_verified) throw new Error('Release artifacts submitted but public listing/approval is pending; retain receipts and resume read-only reconciliation');
  console.log(`${args.dryRun?'Dry-run':'Verified'} release ${version}: ${state.uploads.length} platform entries`);
  return state;
}

if (process.argv[1] && import.meta.url===pathToFileURL(process.argv[1]).href) {
  main().catch(error=>{console.error(error.message);process.exitCode=1;});
}
