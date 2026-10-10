import {DEFAULTS} from './defaults.mjs';
import {MOB_DEFAULT,clone,parseJson,patchRootJson,scopeOf,validatePack,xpCurve} from './engine.mjs';
import {readZip} from './zip.mjs';

export const MOB_PATH='config/ras/mob_xp.json';
export const STATS_PATH='config/ras/stats_display.json';

export function createFreshWorkspace(){
 return {pack:Object.assign(Object.create(null),clone(DEFAULTS)),originals:Object.create(null),
  extra:Object.create(null),patches:Object.create(null),target:'4.2.6',draftMob:clone(MOB_DEFAULT),
  freshDefaults:new Set(Object.keys(DEFAULTS))};
}

// Only untouched, explicitly fresh files receive target deltas. Matching imported
// labels are never evidence of freshness. Freeze the displayed target before editing.
export function markWorkspaceFileEdited(workspace,path){
 if(workspace.freshDefaults?.has(path)){
  workspace.pack[path]=workspacePack(workspace)[path];
  workspace.freshDefaults.delete(path);
 }
}

export function replaceWorkspaceJson(workspace,path,text){
 const object=parseJson(text);
 workspace.pack[path]=object;
 workspace.originals[path]=text;
 workspace.patches[path]=new Set();
 delete workspace.extra[path];
 workspace.freshDefaults?.delete(path);
 return object;
}

export async function importWorkspaceZip(bytes){
 const files=await readZip(bytes),pack=Object.create(null),originals=Object.create(null),extra=Object.create(null);
 const decoder=new TextDecoder('utf-8',{fatal:true});
 for(const [path,data] of Object.entries(files)){
  if(path.toLowerCase().endsWith('.json')){
   const text=decoder.decode(data);
   pack[path]=parseJson(text);
   originals[path]=text;
  }else extra[path]=data;
 }
 if(!Object.keys(pack).length)throw Error('ZIP contains no JSON configuration files.');
 return {pack,originals,extra,freshDefaults:new Set()};
}

export function workspacePack({pack,target,draftMob,freshDefaults}){
 const out=Object.assign(Object.create(null),pack);
 // The pinned 4.2.6 snapshot remains immutable. All five current 4.3.0
 // ConfigInitializer.createStatsDisplayConfig methods changed only total 3.
 if(target==='4.3.0'&&freshDefaults?.has(STATS_PATH)&&Object.hasOwn(out,STATS_PATH)){
  out[STATS_PATH]={...out[STATS_PATH],totals:DEFAULTS[STATS_PATH].totals.map((line,index)=>
   index===2?'[label]Total Attack Speed Bonus[labelEnd][ids]3[idsEnd][mode]bonus[modeEnd]':line)};
 }
 if(target==='4.3.0')out[MOB_PATH]=draftMob;else delete out[MOB_PATH];
 return out;
}

export function workspaceIssues(workspace,{mobEnabledForExport=true,jsonDrafts={},invalid=new Map()}={}){
 const result=validatePack(workspacePack(workspace),{allowMob:mobEnabledForExport&&workspace.target==='4.3.0'});
 for(const path of Object.keys(jsonDrafts))result.errors.push({path,msg:'Unapplied JSON edits are retained. Apply or discard them before export.'});
 for(const [reference,msg] of invalid)result.errors.push({path:reference.split('|')[0],msg});
 if(workspace.target==='4.3.0'&&!mobEnabledForExport)result.errors.unshift({path:'4.3.0 contract preview',msg:'Export is unavailable until the runtime adopts this contract and parity is verified.'});
 const settings=workspace.pack['config/ras/settings.json'];
 if(settings&&xpCurve(settings).some(row=>!Number.isSafeInteger(Math.round(row.xp))||!Number.isSafeInteger(Math.round(row.total)))){
  result.errors.push({path:'config/ras/settings.json',msg:'Derived XP exceeds exact browser numeric precision. Reduce the curve before exporting.'});
 }
 return result;
}

// The browser editor and the production-parser regression use this same exporter.
export function workspacePaths({pack,extra={},target}){
 return [...new Set([
  ...Object.keys(pack).filter(path=>target==='4.3.0'||path!==MOB_PATH),
  ...Object.keys(extra),
  ...(target==='4.3.0'?[MOB_PATH]:[])
 ])].sort();
}

export function workspaceFileText(workspace,path){
 const {originals={},patches={}}=workspace;
 const object=workspacePack(workspace)[path];
 return patchRootJson(originals[path],object,patches[path]||new Set());
}

export function exportWorkspace(workspace,scope='all'){
 if(!['all','server','client'].includes(scope))throw Error('Unsupported export scope.');
 const out=Object.create(null);
 for(const path of workspacePaths(workspace)){
  if(scope!=='all'&&scopeOf(path)!==scope)continue;
  out[path]=path===MOB_PATH||Object.hasOwn(workspace.pack,path)
   ?workspaceFileText(workspace,path):workspace.extra[path];
 }
 return out;
}
