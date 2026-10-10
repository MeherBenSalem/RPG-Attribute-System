import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {DEFAULTS} from '../dist/defaults.mjs';
import {MOB_DEFAULT,clone,mobPreview,parseJson,validatePack} from '../dist/engine.mjs';
import {readZip,writeZip} from '../dist/zip.mjs';
import {MOB_PATH,STATS_PATH,createFreshWorkspace,markWorkspaceFileEdited,replaceWorkspaceJson,
 exportWorkspace,importWorkspaceZip,workspaceFileText,workspacePack,workspaceIssues} from '../dist/workspace.mjs';

const shared=new URL('../../../tests/fixtures/mob-xp-contract.json',import.meta.url);
const fixture=process.argv[2]||fileURLToPath(fs.existsSync(shared)?shared:new URL('./mob-xp-contract.json',import.meta.url));
const temporary=!process.argv[3];
const output=process.argv[3]||fs.mkdtempSync(path.join(os.tmpdir(),'ras-studio-exports-'));
fs.mkdirSync(output,{recursive:true});
const decoder=new TextDecoder('utf-8',{fatal:true});
const cases=JSON.parse(fs.readFileSync(fixture,'utf8')).cases;
const generated=[];
let checks=0;
const eq=(actual,expected,label)=>{assert.deepEqual(actual,expected,label);checks++};
let currentInitializerRoots=0;

function statsInitializerDefaults(source){
 const body=source.split('private static void createStatsDisplayConfig() {')[1]?.split('\n    private static ')[0];
 assert.ok(body,'Actual createStatsDisplayConfig method must exist');
 const defaults={};
 for(const match of body.matchAll(/Services\.CONFIG\.(setStringValue|addStringToArray)\(dir, file, ("(?:[^"\\]|\\.)*"),\s*("(?:[^"\\]|\\.)*")\s*\);/g)){
  const [,operation,keyText,valueText]=match,key=JSON.parse(keyText),value=JSON.parse(valueText);
  if(operation==='addStringToArray')(defaults[key]??=[]).push(value);else defaults[key]=value;
 }
 assert.equal(defaults.totals?.length,4,'Initializer must seed exactly four literal totals');
 return defaults;
}

async function verifyStats(workspace,expected,label){
 // Validation, raw preview, and actual ZIP serialization use the same projection.
 eq(workspaceIssues(workspace).errors,[],label+' shared blocking validator');
 eq(workspaceIssues(workspace),validatePack(workspacePack(workspace),{allowMob:workspace.target==='4.3.0'}),label+' validator projected pack');
 const files=await readZip(writeZip(exportWorkspace(workspace)));
 const text=decoder.decode(files[STATS_PATH]);
 eq(parseJson(text),expected,label+' actual ZIP contents');
 eq(workspaceFileText(workspace,STATS_PATH),text,label+' raw file preview agrees');
 eq(workspacePack(workspace)[STATS_PATH],expected,label+' workspace pack agrees');
 eq(exportWorkspace(workspace,'server')[STATS_PATH],text,label+' server export agrees');
 eq(Object.hasOwn(exportWorkspace(workspace,'client'),STATS_PATH),false,label+' server-owned scope');
 return text;
}

async function verifyStatsDefaultTargets(){
 const historical=JSON.stringify(DEFAULTS);
 const frozen=value=>{if(value&&typeof value==='object'){Object.values(value).forEach(frozen);Object.freeze(value)}};
 frozen(DEFAULTS);
 const reference=statsInitializerDefaults(fs.readFileSync(new URL('../source-reference/ConfigInitializer.java',import.meta.url),'utf8'));
 eq(DEFAULTS[STATS_PATH],reference,'Pinned historical defaults match retained initializer');
 const roots=['1.20.1','1.21.1','26.1.2','26.2','26.3'];
 let current;
 if(fs.existsSync(shared)){
  for(const root of roots){
   const javaPackage=root==='26.2'?'java/tn':'tn';
   const source=new URL(`../../../${root}/common/src/main/java/${javaPackage}/nightbeam/ras/config/ConfigInitializer.java`,import.meta.url);
   const expected=statsInitializerDefaults(fs.readFileSync(source,'utf8'));
   const fresh=createFreshWorkspace();fresh.target='4.3.0';
   await verifyStats(fresh,expected,root+' current initializer fresh 4.3');
   current??=expected;
   eq(expected,current,root+' initializer delta agrees across independent roots');
   currentInitializerRoots++;
  }
  eq(currentInitializerRoots,5,'All five current initializers checked, including 26.2 nesting');
 }else{
  // A standalone checkout has no current runtime source. Record that distinction.
  const fresh=createFreshWorkspace();fresh.target='4.3.0';current=workspacePack(fresh)[STATS_PATH];
 }
 const fresh=createFreshWorkspace();
 eq(fresh.freshDefaults.has(STATS_PATH),true,'Fresh state explicitly identifies default stats');
 for(const target of ['4.2.6','4.3.0','4.2.6','4.3.0','4.2.6']){
  fresh.target=target;
  await verifyStats(fresh,target==='4.3.0'?current:reference,'Repeated fresh target '+target);
  eq(fresh.pack[STATS_PATH],reference,'Target switch does not mutate stored historical stats');
 }
 fresh.target='4.3.0';
 eq({...workspacePack(fresh),[MOB_PATH]:undefined},{...clone(DEFAULTS),[STATS_PATH]:current,[MOB_PATH]:undefined},'4.3 fresh delta changes only stats and mob file');
 replaceWorkspaceJson(fresh,'config/ras/settings.json','{"max_player_level":12,"exp_curve_max_level":12}\n');
 eq(fresh.freshDefaults.has(STATS_PATH),true,'Single-file settings import leaves untouched stats fresh');
 eq(fresh.freshDefaults.has('config/ras/settings.json'),false,'Single-file settings import only revokes its own freshness');
 await verifyStats(fresh,current,'Fresh stats after unrelated single-file import');

 const baseline=JSON.stringify(reference,null,2)+'\n';
 const custom=' {"totals":["[label]Total Mana Bonus[labelEnd][ids]9[idsEnd][mode]value[modeEnd]"],"precise_extension":0.10000000000000001,"order":[3,1,2]}\r\n';
 for(const original of [baseline,custom,' {"header_color":"#FFFFFF","unknown":true}\n']){
  const imported=await importWorkspaceZip(writeZip({[STATS_PATH]:original}));
  const workspace={...imported,target:'4.3.0',draftMob:clone(MOB_DEFAULT),patches:{}};
  eq(imported.freshDefaults.size,0,'ZIP imports never infer freshness from same-looking labels');
  for(const target of ['4.3.0','4.2.6','4.3.0']){
   workspace.target=target;
   eq(await verifyStats(workspace,parseJson(original),'Imported stats '+target),original,'Imported stats exact bytes '+target);
  }
  markWorkspaceFileEdited(workspace,STATS_PATH);
  workspace.pack[STATS_PATH].header_color='#123456';workspace.patches[STATS_PATH]=new Set(['header_color']);
  const expected={...parseJson(original),header_color:'#123456'};
  for(const target of ['4.2.6','4.3.0']){
   workspace.target=target;
   const text=await verifyStats(workspace,expected,'Edited imported stats '+target);
   if(original===custom)eq(text.includes('0.10000000000000001'),true,'Edited import retains untouched numeric lexeme');
  }
 }

 const omitted=await importWorkspaceZip(writeZip({'config/ras/settings.json':'{"max_player_level":12}\n'}));
 const omittedWorkspace={...omitted,target:'4.3.0',draftMob:clone(MOB_DEFAULT),patches:{}};
 for(const target of ['4.3.0','4.2.6','4.3.0']){
  omittedWorkspace.target=target;
  eq(workspaceIssues(omittedWorkspace).errors,[],'Missing imported stats passes export gate '+target);
  eq(Object.hasOwn(workspacePack(omittedWorkspace),STATS_PATH),false,'ZIP replacement keeps missing stats absent '+target);
  eq(Object.hasOwn(await readZip(writeZip(exportWorkspace(omittedWorkspace))),STATS_PATH),false,'Export leaves omitted stats to runtime '+target);
 }

 for(const target of ['4.2.6','4.3.0']){
  const edited=createFreshWorkspace();edited.target=target;
  markWorkspaceFileEdited(edited,STATS_PATH);
  edited.pack[STATS_PATH].header_color='#123456';edited.patches[STATS_PATH]=new Set(['header_color']);
  eq(edited.freshDefaults.has(STATS_PATH),false,'Known-field edit disables file delta '+target);
  const expected={...(target==='4.3.0'?current:reference),header_color:'#123456'};
  for(const next of ['4.3.0','4.2.6','4.3.0']){
   edited.target=next;await verifyStats(edited,expected,'Known-field edit pins displayed totals '+target+' to '+next);
  }
 }
 for(const original of [baseline,custom,JSON.stringify(current,null,2)+'\n']){
  const edited=createFreshWorkspace();edited.target='4.3.0';
  eq(workspaceIssues(edited,{jsonDrafts:{[STATS_PATH]:original}}).errors.some(error=>error.msg.includes('Unapplied')),true,'Unapplied raw stats blocks actual export');
  eq(edited.freshDefaults.has(STATS_PATH),true,'Unapplied raw draft does not revoke freshness');
  await verifyStats(edited,current,'Discarded raw draft leaves fresh projection');
  replaceWorkspaceJson(edited,STATS_PATH,original);
  eq(edited.freshDefaults.has(STATS_PATH),false,'Raw/single-file replacement pins intentional same-looking values');
  for(const target of ['4.2.6','4.3.0','4.2.6']){
   edited.target=target;
   eq(await verifyStats(edited,parseJson(original),'Applied raw/single-file stats '+target),original,'Applied raw text stays exact '+target);
  }
 }
 const rejected=createFreshWorkspace();rejected.target='4.3.0';
 assert.throws(()=>replaceWorkspaceJson(rejected,STATS_PATH,'{"totals":[],"totals":[]}'),/Duplicate/);checks++;
 eq(rejected.freshDefaults.has(STATS_PATH),true,'Rejected raw/import text does not revoke freshness');
 await verifyStats(rejected,current,'Rejected replacement keeps prior projection');
 const reset=createFreshWorkspace();
 eq(reset.target,'4.2.6','Fresh defaults reset restores released target');
 await verifyStats(reset,reference,'Fresh defaults reset restores historical values');
 reset.target='4.3.0';await verifyStats(reset,current,'Fresh defaults reset restores delta eligibility');
 eq(JSON.stringify(DEFAULTS),historical,'Historical DEFAULTS stay immutable throughout every state transition');
}

async function exportCase(name,original,context,expectedVp,expectedRule,edit,fresh=false){
 // These are the editor's actual ZIP import and export functions, not a copied serializer.
 // A numeric editor field can represent the runtime overflow vector as a double.
 // Import deliberately rejects its unsafe integer; never claim it round-tripped.
 const imported=fresh?{pack:{[MOB_PATH]:JSON.parse(original)},originals:{},extra:{}}
  :await importWorkspaceZip(writeZip({[MOB_PATH]:original}));
 const workspace={...imported,target:'4.3.0',draftMob:clone(imported.pack[MOB_PATH]),patches:{}};
 if(edit){edit(workspace.draftMob);workspace.patches[MOB_PATH]=new Set(['rules']);}
 eq(workspaceIssues(workspace).errors,[],name+' actual editor export validation');
 const zip=writeZip(exportWorkspace(workspace));
 const restored=await readZip(zip),json=decoder.decode(restored[MOB_PATH]);
 if(!edit&&!fresh)eq(json,original,name+' untouched JSON bytes');
 const exported=fresh?JSON.parse(json):parseJson(json),c=context;
 const preview=mobPreview(exported,{default_vp_rates:c.effective_rate},{
  entity:c.entity,tags:c.tags,health:c.max_health,armor:c.armor,difficulty:c.difficulty
 });
 eq(preview.total,expectedVp,name+' preview value');
 eq(preview.rule==='Legacy health basis'?null:preview.rule,expectedRule,name+' preview winner');
 const filename=`mob-export-${generated.length}.json`;
 fs.writeFileSync(path.join(output,filename),json);
 fs.writeFileSync(path.join(output,filename.replace('.json','.zip')),zip);
 generated.push({name,config_file:filename,mode:fresh?'fresh-number-input':'zip-import',context,expected_vp:expectedVp,expected_rule:expectedRule,
  preview_vp:preview.total,preview_rule:preview.rule==='Legacy health basis'?null:preview.rule});
 return json;
}

try{
 await verifyStatsDefaultTargets();
 for(const test of cases){
  const config=clone(test.config);
  config.unknown_extension={preserve:true,ordered:[3,1,2]};
  for(const rule of config.rules||[])rule.custom_note='preserved';
  const original=JSON.stringify(config,null,2)+'\n';
  const fresh=test.name==='overflow rejected';
  if(fresh){await assert.rejects(()=>importWorkspaceZip(writeZip({[MOB_PATH]:original})),/precision/);checks++;}
  await exportCase(test.name,original,test.context,test.expected_vp,test.expected_rule,undefined,fresh);
 }
 const context={entity:'minecraft:zombie',tags:['minecraft:undead'],max_health:20,effective_rate:1,difficulty:'hard',armor:2};
 const original=' {\n  "schema_version":1, "enabled":true,\n  "precise_extension":0.10000000000000001, "unknown_extension":{"order":[3,1,2]},\n  "difficulty_weighting":{"enabled":true,"hard":2},\n  "armor_weighting":{"enabled":true,"per_armor_point":0.1},\n  "rules":[{"id":"edited","entity":"minecraft:zombie","base_xp":20,"custom_note":"keep me"}]\n}\n';
 const edited=await exportCase('edited imported rule with omitted defaults',original,context,96,'edited',mob=>{mob.rules[0].base_xp=40});
 eq(edited.includes('0.10000000000000001'),true,'Unchanged numeric lexeme after rule edit');
 eq(parseJson(edited).unknown_extension,{order:[3,1,2]},'Unknown root object and ordered array after rule edit');
 eq(parseJson(edited).rules[0].custom_note,'keep me','Unknown rule field after known rule edit');
 eq(Object.hasOwn(parseJson(edited).armor_weighting,'max_multiplier'),false,'Omitted armor cap stays absent');
 eq(Object.hasOwn(parseJson(edited).difficulty_weighting,'normal'),false,'Omitted difficulty values stay absent');
 await exportCase('schema-only import keeps optional fields absent','{"schema_version":1,"plugin_key":false}\n',
  {...context,effective_rate:1.5},30,null);

 const settings=' {"max_player_level":12,"exp_curve_max_level":12,"unknown":0.10000000000000001,"array":[3,1,2]}\n';
 const source={
  'config/ras/settings.json':settings,
  'config/ras/display/settings.json':'{"enable":true}\n',
  [MOB_PATH]:original,
  'config/ras/custom_plugin.json':'{"extension":{"keep":42}}\n',
  'config/ras/readme.txt':'Untouched supporting text.\r\n',
  'config/ras/display/icon.png':new Uint8Array([0x89,0x50,0x4e,0x47,0,255,127])
 };
 const imported=await importWorkspaceZip(writeZip(source));
 const workspace={...imported,target:'4.3.0',draftMob:clone(imported.pack[MOB_PATH]),patches:{}};
 const roundTrip=await readZip(writeZip(exportWorkspace(workspace)));
 for(const [name,data] of Object.entries(source))eq(roundTrip[name],typeof data==='string'?new TextEncoder().encode(data):data,'Workspace bytes '+name);
 workspace.pack['config/ras/settings.json'].max_player_level=15;
 workspace.patches['config/ras/settings.json']=new Set(['max_player_level']);
 const changed=await readZip(writeZip(exportWorkspace(workspace)));
 const changedText=decoder.decode(changed['config/ras/settings.json']);
 eq(parseJson(changedText).max_player_level,15,'Known settings edit exported');
 eq(changedText.includes('0.10000000000000001'),true,'Untouched settings lexeme preserved');
 eq(parseJson(changedText).array,[3,1,2],'Untouched settings array order preserved');
 workspace.target='4.2.6';
 eq(Object.hasOwn(exportWorkspace(workspace),MOB_PATH),false,'4.2.6 excludes development mob file');
 workspace.target='4.3.0';
 eq(Object.keys(exportWorkspace(workspace,'client')),['config/ras/display/icon.png','config/ras/display/settings.json'],'Client scope retains client files only');
 eq(Object.keys(exportWorkspace(workspace,'server')).every(name=>!name.includes('/display/')),true,'Server scope excludes client files');
 await assert.rejects(()=>importWorkspaceZip(writeZip({'config/ras/bad.json':'{"duplicate":1,"duplicate":2}'})),/Duplicate/);checks++;
 await assert.rejects(()=>importWorkspaceZip(writeZip({'config/ras/readme.txt':'No JSON'})),/no JSON/);checks++;
 // Invalid import is never assigned to the current workspace; existing exports remain intact.
 eq(parseJson(exportWorkspace(workspace)['config/ras/settings.json']).max_player_level,15,'Rejected import leaves existing workspace unchanged');
 eq(workspaceIssues(workspace,{jsonDrafts:{'config/ras/settings.json':'Unapplied'}}).errors.some(error=>error.msg.includes('Unapplied')),true,'Unapplied raw JSON blocks the actual export gate');
 eq(workspaceIssues(workspace,{invalid:new Map([['config/ras/settings.json|max_player_level','Invalid number']])}).errors.some(error=>error.msg==='Invalid number'),true,'Invalid form field blocks the actual export gate');
 eq(workspaceIssues(workspace,{mobEnabledForExport:false}).errors.length>0,true,'Unverified development target blocks the actual export gate');
 fs.writeFileSync(path.join(output,'studio-export-contract.json'),JSON.stringify({cases:generated},null,2)+'\n');
 console.log(JSON.stringify({exportChecks:checks,currentInitializerRoots,actualZipCases:generated.length,
  importedZipCases:generated.filter(test=>test.mode==='zip-import').length,result:'passed'}));
}finally{
 if(temporary)fs.rmSync(output,{recursive:true,force:true});
}
