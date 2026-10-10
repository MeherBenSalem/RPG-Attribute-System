import assert from 'node:assert/strict';
import fs from 'node:fs';
import test from 'node:test';
import {resolveCurseForgeCatalog, curseForgeIds} from '../scripts/curseforge-release-catalog.mjs';

// Sanitized hosted diagnostic38055073295 at reviewed helper4409214. These IDs
// are fixture evidence only. Production resolves fresh responses on every run.
const evidence = JSON.parse(fs.readFileSync(new URL('./fixtures/curseforge-catalog-430.json', import.meta.url)));
const targets = evidence.requested_names.slice(0,5).map(minecraft => ({minecraft}));
const clone = value => structuredClone(value);
function fixture() {
  const upload = Object.values(evidence.upload_candidates).flat().map(row => ({...row}));
  const canonical = Object.fromEntries(Object.entries(evidence.core_minecraft).map(([name, row]) => [name, {data: {...row}}]));
  const types = {data: clone(evidence.namespace_facts.version_types)};
  const core = Object.values(evidence.core_candidates).flat();
  const groups = {data: types.data.map(type => ({type: type.id,
    versions: core.filter(row => row.gameVersionTypeId === type.id).map(({id,name,slug}) => ({id,name,slug}))}))};
  return {upload, canonical, types, groups};
}
function resolve(fx) {return resolveCurseForgeCatalog(fx.upload, fx.canonical, fx.types, fx.groups, targets);}
const expected = {client: 9638, server: 9639, loader: {fabric: 7499, forge: 7498, neoforge: 10150},
  minecraft: {'1.20.1': 9990, '1.21.1': 11779, '26.1.2': 16082, '26.2': 16498, '26.3': 17045}};

test('actual same-name Bukkit/Addons candidates resolve only exact canonical Java pairs, independent of order', () => {
  const fx = fixture();
  assert.deepEqual(resolve(fx), expected);
  fx.upload.reverse(); fx.types.data.reverse(); fx.groups.data.reverse();
  for (const group of fx.groups.data) group.versions.reverse();
  assert.deepEqual(resolve(fx), expected);
  assert.notEqual(fx.canonical['1.20.1'].data.id, expected.minecraft['1.20.1']);
  assert.equal(fx.canonical['1.20.1'].data.approved, false);
});

test('no static IDs: valid simultaneous ID/type changes are resolved dynamically', () => {
  const fx = fixture();
  for (const row of fx.upload) {row.id += 100000; row.gameVersionTypeID += 100000;}
  for (const row of fx.types.data) row.id += 100000;
  for (const group of fx.groups.data) {group.type += 100000; for (const row of group.versions) row.id += 100000;}
  for (const record of Object.values(fx.canonical)) {record.data.gameVersionId += 100000; record.data.gameVersionTypeId += 100000;}
  const result = resolve(fx);
  for (const [name, value] of Object.entries(result.minecraft)) assert.equal(value, expected.minecraft[name] + 100000);
  assert.equal(result.loader.fabric, expected.loader.fabric + 100000);
});

test('every ambiguity, mismatch, malformed identity and inactive namespace fails closed', async t => {
  const mutations = {
    'duplicate exact upload': fx => fx.upload.push(clone(fx.upload[0])),
    'different ID same exact upload name/type': fx => fx.upload.push({...fx.upload[0], id: 200000}),
    'canonical ID is not upload ID': fx => {fx.canonical['1.20.1'].data.gameVersionId++;},
    'internal canonical ID cannot replace gameVersionId': fx => {fx.canonical['1.20.1'].data.gameVersionId = fx.canonical['1.20.1'].data.id;},
    'wrong canonical name': fx => {fx.canonical['1.20.1'].data.versionString = '1.20.2';},
    'canonical selected foreign namespace': fx => {fx.canonical['1.20.1'].data.gameVersionTypeId = 1; fx.canonical['1.20.1'].data.gameVersionId = 9994;},
    'missing canonical': fx => {delete fx.canonical['26.3'];},
    'deleted canonical': fx => {fx.canonical['26.3'].data.gameVersionStatus = 2;},
    'new canonical': fx => {fx.canonical['26.3'].data.gameVersionStatus = 3;},
    'deleted canonical type': fx => {fx.canonical['26.3'].data.gameVersionTypeStatus = 2;},
    'nonboolean approved': fx => {fx.canonical['26.3'].data.approved = 'false';},
    'string ID': fx => {fx.upload[0].id = String(fx.upload[0].id);},
    'zero ID': fx => {fx.upload[0].id = 0;},
    'wrong type casing': fx => {fx.upload[0].gameVersionTypeId = fx.upload[0].gameVersionTypeID; delete fx.upload[0].gameVersionTypeID;},
    'duplicate Core group': fx => fx.groups.data.push(clone(fx.groups.data[0])),
    'duplicate Core exact candidate': fx => fx.groups.data[0].versions.push(clone(fx.groups.data[0].versions[0])),
    'Core candidate ID mismatch': fx => {fx.groups.data[0].versions[0].id++;},
    'missing Core group': fx => fx.groups.data.pop(),
    'duplicate namespace ID': fx => fx.types.data.push(clone(fx.types.data[0])),
    'ambiguous environment semantic namespace': fx => {fx.types.data.push({...fx.types.data[0], id: 200000});},
    'inactive loader namespace': fx => {fx.types.data.find(row => row.slug === 'modloader').status = 2;},
    'wrong namespace game': fx => {fx.types.data[0].gameId = 999;},
    'wrong namespace slug': fx => {fx.types.data.find(row => row.id === 75125).slug = 'bukkit';},
    'missing loader': fx => {fx.upload = fx.upload.filter(row => row.name !== 'NeoForge');},
    'oversized upload': fx => {fx.upload = Array(100001).fill(fx.upload[0]);},
    'oversized types': fx => {fx.types.data = Array(513).fill(fx.types.data[0]);},
    'malformed groups': fx => {fx.groups.data = null;},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, () => {
    const fx = fixture(); mutate(fx); assert.throws(() => resolve(fx));
  });
});

test('same-name foreign loader/environment rows cannot override reviewed semantic namespace', () => {
  const fx = fixture();
  for (const name of ['Fabric','Client']) {
    fx.upload.push({id: name === 'Fabric' ? 200001 : 200002, name, gameVersionTypeID: 1});
    fx.groups.data.find(row => row.type === 1).versions.push({id: name === 'Fabric' ? 200001 : 200002, name});
  }
  assert.deepEqual(resolve(fx), expected);
});

test('all fresh catalog reads use fixed GET routes and separated auth headers with redirects rejected', async () => {
  const fx = fixture(), calls = [];
  const request = async (url, options) => {
    calls.push({url, options});
    assert.equal(options.redirect, 'error');
    assert.equal(options.method, undefined);
    assert.equal(options.body, undefined);
    if (url === 'https://minecraft.curseforge.com/api/game/versions') {
      assert.deepEqual(options.headers, {'X-Api-Token': 'fake-upload'}); return fx.upload;
    }
    assert.deepEqual(options.headers, {'x-api-key': 'fake-core'});
    if (url === 'https://api.curseforge.com/v1/mods/1079687') return {data: {id: 1079687, gameId: 432}};
    if (url === 'https://api.curseforge.com/v1/games/432') return {data: {id: 432, slug: 'minecraft'}};
    if (url === 'https://api.curseforge.com/v1/games/432/version-types') return fx.types;
    if (url === 'https://api.curseforge.com/v2/games/432/versions') return fx.groups;
    const name = url.replace('https://api.curseforge.com/v1/minecraft/version/', '');
    assert.ok(fx.canonical[name]); return fx.canonical[name];
  };
  let audit;
  assert.deepEqual(await curseForgeIds('fake-upload','fake-core',targets,request,value => {audit = value;}), expected);
  assert.equal(audit.canonical_minecraft['1.20.1'].approved, false);
  assert.equal(audit.canonical_minecraft['1.20.1'].gameVersionStatus, 1);
  assert.deepEqual(audit.selected_ids, expected);
  assert.ok(!JSON.stringify(audit).includes('fake-'));
  assert.equal(calls.length, 10);
  await assert.rejects(curseForgeIds('fake-upload','fake-core',targets,async () => ({data: {id: 1079687, gameId: 999}})), /namespace/);
});
