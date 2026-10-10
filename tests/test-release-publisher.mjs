#!/usr/bin/env node
/**
 * Offline upload-safety regression tests. Run with:
 *   node --test tests/test-release-publisher.mjs
 *
 * Fake .jar bytes test the Node publisher's attested inventory boundary; the
 * independent Python suite tests actual ZIP/JAR metadata and source provenance.
 * Every fetch is replaced. No network, account credentials, or store writes.
 */
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test, {after} from 'node:test';
import {
  validatePlan, modrinthDescriptor, curseForgeDescriptor,
  matchingModrinth, matchingCurseForge, main,
} from '../scripts/publish-verified-release.mjs';

const VERSION = '4.3.0';
const COMMIT = '0123456789abcdef0123456789abcdef01234567';
const ORIGINAL_430_COMMIT='09a559f500daa21ce6dbc60d80ea0b490a605915';
const MODRINTH_PROJECT = 'd85UTOuq';
const CURSEFORGE_PROJECT = '1079687';
const CHANGELOG = 'Synthetic offline release changelog.\n';
// Independent source-metadata expectations, never imported from the uploader.
const TARGETS = [
  ['1.20.1', 'fabric', ['fabric-api', 'jauml'], 17],
  ['1.20.1', 'forge', ['jauml'], 17],
  ['1.21.1', 'fabric', ['fabric-api', 'jauml'], 21],
  ['1.21.1', 'neoforge', ['jauml'], 21],
  ['26.1.2', 'fabric', ['fabric-api'], 25],
  ['26.1.2', 'neoforge', [], 25],
  ['26.2', 'fabric', ['fabric-api'], 25],
  ['26.2', 'neoforge', [], 25],
  ['26.3', 'fabric', ['fabric-api'], 25],
  ['26.3', 'neoforge', [], 25],
];
const DEPENDENCIES = {
  jauml: {modrinth: 'ihvBalM2', curseforge_slug: 'jauml', curseforge_id: 1281310},
  'fabric-api': {modrinth: 'P7dR8mSH', curseforge_slug: 'fabric-api', curseforge_id: 306612},
};
const IDS = {
  client: 99701, server: 99702,
  loader: {fabric: 99703, forge: 99704, neoforge: 99705},
  minecraft: {'1.20.1': 99710, '1.21.1': 99711, '26.1.2': 99712, '26.2': 99713, '26.3': 99714},
};
const LOADER_NAMES = {fabric: 'Fabric', forge: 'Forge', neoforge: 'NeoForge'};
const ORIGINAL_FETCH = globalThis.fetch;
const ENV_KEYS = ['MODRINTH_TOKEN', 'CURSEFORGE_TOKEN', 'CURSEFORGE_API_KEY', 'MODRINTH_ID', 'CURSEFORGE_ID'];
const ORIGINAL_ENV = Object.fromEntries(ENV_KEYS.map(key => [key, process.env[key]]));
const clone = value => structuredClone(value);
const hash = (bytes, algorithm) => crypto.createHash(algorithm).update(bytes).digest('hex');
const forbiddenFetch = async () => { throw new Error('External network is forbidden by this test suite'); };
globalThis.fetch = forbiddenFetch;
after(() => {
  globalThis.fetch = ORIGINAL_FETCH;
  for (const key of ENV_KEYS) {
    if (ORIGINAL_ENV[key] === undefined) delete process.env[key];
    else process.env[key] = ORIGINAL_ENV[key];
  }
});

function fixture(t) {
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'ras-publisher-offline-'));
  t.after(() => fs.rmSync(root, {recursive: true, force: true}));
  const directory = path.join(root, 'artifacts');
  fs.mkdirSync(directory);
  const files = TARGETS.map(([minecraft, loader, required, java], index) => {
    const file_name = `rpg_attribute_system-${loader}-${minecraft}-${VERSION}.jar`;
    const bytes = Buffer.from(`Synthetic JAR bytes: ${COMMIT} ${file_name} ${index}\n`);
    fs.writeFileSync(path.join(directory, file_name), bytes);
    return {
      file_name, path: file_name, minecraft, loader, java,
      version: VERSION, source_commit: COMMIT, size_bytes: bytes.length,
      sha1: hash(bytes, 'sha1'), sha256: hash(bytes, 'sha256'), sha512: hash(bytes, 'sha512'),
      required_mod_ids: [...required],
      mod_metadata_path: loader === 'fabric' ? 'fabric.mod.json' : `META-INF/${loader === 'forge' ? 'mods' : 'neoforge.mods'}.toml`,
    };
  });
  const manifest = {
    schema_version: 1, mod_id: 'rpg_attribute_system', release_version: VERSION,
    source_commit: COMMIT, complete_inventory: true, artifact_count: 10, files,
  };
  const paths = Object.fromEntries(['manifest', 'dependencies', 'changelog', 'state'].map(key => [key, path.join(root, `${key}.${key === 'changelog' ? 'md' : 'json'}`)]));
  fs.writeFileSync(paths.manifest, JSON.stringify(manifest));
  fs.writeFileSync(paths.dependencies, JSON.stringify(DEPENDENCIES));
  fs.writeFileSync(paths.changelog, CHANGELOG);
  return {
    root, directory, manifest, files, paths,
    argv(platforms = 'both') {
      return ['--manifest', paths.manifest, '--directory', directory, '--dependencies', paths.dependencies,
        '--changelog', paths.changelog, '--state', paths.state, '--platforms', platforms];
    },
    state() {return JSON.parse(fs.readFileSync(paths.state, 'utf8'));},
    saveManifest() {fs.writeFileSync(paths.manifest, JSON.stringify(manifest));},
  };
}
function fakeCredentials(t, overrides = {}) {
  const prior = Object.fromEntries(ENV_KEYS.map(key => [key, process.env[key]]));
  t.after(() => {
    for (const key of ENV_KEYS) {
      if (prior[key] === undefined) delete process.env[key];
      else process.env[key] = prior[key];
    }
  });
  for (const key of ENV_KEYS) delete process.env[key];
  Object.assign(process.env, {
    MODRINTH_TOKEN: 'offline-test-not-a-token',
    CURSEFORGE_TOKEN: 'offline-test-not-a-token',
    CURSEFORGE_API_KEY: 'offline-test-not-a-key',
  });
  for (const [key, value] of Object.entries(overrides)) {
    if (value === undefined) delete process.env[key];
    else process.env[key] = value;
  }
}
function mrDescriptor(entry) {return modrinthDescriptor(entry, VERSION, CHANGELOG, MODRINTH_PROJECT, DEPENDENCIES);}
function cfDescriptor(entry) {return curseForgeDescriptor(entry, VERSION, CHANGELOG, IDS, DEPENDENCIES);}
function mrRecord(entry, id = `MR${String(TARGETS.findIndex(([mc, loader]) => mc === entry.minecraft && loader === entry.loader) + 1).padStart(6, '0')}`) {
  const descriptor = mrDescriptor(entry);
  return {
    id, project_id: MODRINTH_PROJECT, version_number: descriptor.version_number,
    version_type: 'release', status: 'listed', loaders: [...descriptor.loaders], game_versions: [...descriptor.game_versions],
    dependencies: clone(descriptor.dependencies),
    files: [{filename: entry.file_name, primary: true, size: entry.size_bytes,
      hashes: {sha512: entry.sha512, sha1: entry.sha1}}],
  };
}
function cfRecord(entry, id = TARGETS.findIndex(([mc, loader]) => mc === entry.minecraft && loader === entry.loader) + 1001) {
  return {
    id, modId: Number(CURSEFORGE_PROJECT), fileName: entry.file_name,
    displayName: `${VERSION} · ${entry.loader} · ${entry.minecraft}`,
    releaseType: 1, fileLength: entry.size_bytes, fileStatus: 4, isAvailable: true,
    gameVersions: [entry.minecraft, LOADER_NAMES[entry.loader], 'Client', 'Server'],
    hashes: [{algo: 1, value: entry.sha1}],
    dependencies: entry.required_mod_ids.map(dep => ({modId: DEPENDENCIES[dep].curseforge_id, relationType: 3})),
  };
}
function isolationFixture(t) {
  const fx=fixture(t);fakeCredentials(t);
  fx.manifest.source_commit=ORIGINAL_430_COMMIT;
  fx.files.forEach(entry=>{entry.source_commit=ORIGINAL_430_COMMIT;});fx.saveManifest();
  const state={schema_version:1,source_commit:ORIGINAL_430_COMMIT,version:VERSION,publicly_verified:false,
    uploads:[...fx.files.map(entry=>({platform:'modrinth',file:entry.file_name,sha256:entry.sha256,status:'verified',id:mrRecord(entry).id,provider_status:'listed'})),
      {platform:'curseforge',file:fx.files[0].file_name,sha256:fx.files[0].sha256,status:'uncertain'}]};
  const save=()=>fs.writeFileSync(fx.paths.state,JSON.stringify(state,null,2)+'\n');save();
  return {...fx,inputState:state,saveState:save,isolateArgs:(preflight=false)=>[
    ...fx.argv(preflight?'both':'curseforge'),'--isolate-never-attempted-curseforge',
    ...(preflight?['--preflight-only','--require-public-modrinth']:[])]};
}
function jsonResponse(value, status = 200) {
  return new Response(JSON.stringify(value), {status, headers: {'content-type': 'application/json'}});
}
function cfPage(files, index = 0, totalCount = files.length) {
  return {data: files, pagination: {index, pageSize: 50, resultCount: files.length, totalCount}};
}
function cfHistorical(count, start = 0) {
  return Array.from({length: count}, (_, index) => ({id: start + index + 2001,
    fileName: `historical-${start + index}.jar`, displayName: `Historical ${start + index}`}));
}
function installFetch(t, handler) {
  const requests = [];
  globalThis.fetch = async (url, options = {}) => {
    const value = String(url);
    const parsed = new URL(value);
    assert.ok(['api.modrinth.com', 'api.curseforge.com', 'minecraft.curseforge.com'].includes(parsed.hostname),
      `Unexpected network destination: ${value}`);
    assert.equal(options.redirect, 'error', 'Authenticated store requests must reject redirects');
    const call = {url: value, method: options.method || 'GET', options};
    requests.push(call);
    return await handler(call, requests);
  };
  t.after(() => {globalThis.fetch = forbiddenFetch;});
  return requests;
}
function catalogFixture() {
  const mcTypes = Object.fromEntries(Object.keys(IDS.minecraft).map((name, index) => [name, 70100 + index]));
  const upload = [
    {name: 'Client', id: IDS.client, gameVersionTypeID: 70001},
    {name: 'Server', id: IDS.server, gameVersionTypeID: 70001},
    ...Object.entries(LOADER_NAMES).map(([loader, name]) => ({name, id: IDS.loader[loader], gameVersionTypeID: 70002})),
    ...Object.entries(IDS.minecraft).map(([name, id]) => ({name, id, gameVersionTypeID: mcTypes[name]})),
  ];
  const types = {data: [
    {id: 70001, gameId: 432, name: 'Environment', slug: 'environment', status: 1},
    {id: 70002, gameId: 432, name: 'Modloader', slug: 'modloader', status: 1},
    ...Object.entries(mcTypes).map(([name, id]) => ({id, gameId: 432, name: 'Minecraft ' + name,
      slug: 'minecraft-' + name.split('.').slice(0,2).join('-'), status: 1})),
  ]};
  const groups = {data: types.data.map(type => ({type: type.id,
    versions: upload.filter(row => row.gameVersionTypeID === type.id).map(({name, id}) => ({name, id}))}))};
  const canonical = Object.fromEntries(Object.entries(IDS.minecraft).map(([name, id], index) => [name, {data: {
    id: index + 200, gameVersionId: id, gameVersionTypeId: mcTypes[name], versionString: name,
    approved: false, gameVersionStatus: 1, gameVersionTypeStatus: 1,
  }}]));
  return {upload, types, groups, canonical};
}
function fakeStore(t, fx, options = {}) {
  const remote = {
    modrinth: clone(options.modrinth || []), curseforge: clone(options.curseforge || []), posts: [],
  };
  remote.requests = installFetch(t, async (call, requests) => {
    const custom = await options.intercept?.(call, remote, requests);
    if (custom !== undefined) return custom;
    if (call.method === 'GET' && call.url === `https://api.modrinth.com/v2/project/${MODRINTH_PROJECT}/version`) {
      return jsonResponse(remote.modrinth);
    }
    if (call.method === 'GET' && call.url.startsWith(`https://api.curseforge.com/v1/mods/${CURSEFORGE_PROJECT}/files?`)) {
      const offset = Number(new URL(call.url).searchParams.get('index'));
      return jsonResponse(cfPage(remote.curseforge.slice(offset, offset + 50), offset, remote.curseforge.length));
    }
    if (call.method === 'GET') {
      const catalog = catalogFixture();
      if (call.url === 'https://minecraft.curseforge.com/api/game/versions') return jsonResponse(catalog.upload);
      if (call.url === 'https://api.curseforge.com/v1/mods/1079687') return jsonResponse({data: {id: 1079687, gameId: 432}});
      if (call.url === 'https://api.curseforge.com/v1/games/432') return jsonResponse({data: {id: 432, slug: 'minecraft'}});
      if (call.url === 'https://api.curseforge.com/v1/games/432/version-types') return jsonResponse(catalog.types);
      if (call.url === 'https://api.curseforge.com/v2/games/432/versions') return jsonResponse(catalog.groups);
      const name = call.url.replace('https://api.curseforge.com/v1/minecraft/version/', '');
      if (catalog.canonical[name]) return jsonResponse(catalog.canonical[name]);
    }
    if (call.method === 'POST') {
      assert.ok(call.options.body instanceof FormData);
      const platform = call.url === 'https://api.modrinth.com/v2/version' ? 'modrinth'
        : call.url === `https://minecraft.curseforge.com/api/projects/${CURSEFORGE_PROJECT}/upload-file` ? 'curseforge' : null;
      assert.ok(platform, `Unexpected write endpoint: ${call.url}`);
      const file = call.options.body.get(platform === 'modrinth' ? 'file_0' : 'file');
      const entry = fx.files.find(row => row.file_name === file?.name);
      assert.ok(entry, 'Uploaded file must be in the attested inventory');
      const metadata = JSON.parse(call.options.body.get(platform === 'modrinth' ? 'data' : 'metadata'));
      assert.deepEqual(metadata, platform === 'modrinth' ? mrDescriptor(entry) : cfDescriptor(entry));
      assert.equal(hash(Buffer.from(await file.arrayBuffer()), 'sha256'), entry.sha256);
      assert.ok(call.options.signal instanceof AbortSignal, 'Network writes must have bounded timeouts');
      remote.posts.push({platform, file: entry.file_name, metadata});
      const record = platform === 'modrinth' ? mrRecord(entry) : cfRecord(entry);
      if (!options.pendingIndex) remote[platform].push(record);
      return jsonResponse(options.missingId ? {} : {id: record.id});
    }
    assert.fail(`Unmocked request: ${call.method} ${call.url}`);
  });
  return remote;
}
function assertDurableSubmission(fx, entry) {
  const state = fx.state();
  assert.equal(state.source_commit, COMMIT);
  assert.equal(state.version, VERSION);
  assert.ok(state.uploads.some(upload => upload.file === entry.file_name && upload.sha256 === entry.sha256),
    'The attempted file/hash must be journaled before an uncertain write');
  assert.ok(!fs.readFileSync(fx.paths.state, 'utf8').includes('offline-test-not-a-'), 'Journal must never contain credentials');
}

// Top-level tests run serially because process.env and global fetch are shared.
test('descriptors declare the exact required dependency set for all ten source targets', async t => {
  const fx = fixture(t);
  for (let index = 0; index < TARGETS.length; index++) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}`, () => {
      const entry = fx.files[index];
      const required = TARGETS[index][2];
      const mr = mrDescriptor(entry), cf = cfDescriptor(entry);
      assert.deepEqual(mr.dependencies, required.map(id => ({project_id: DEPENDENCIES[id].modrinth, version_id: null, file_name: null, dependency_type: 'required'})));
      assert.deepEqual(cf.relations.projects, required.map(id => ({slug: DEPENDENCIES[id].curseforge_slug, projectID: String(DEPENDENCIES[id].curseforge_id), type: 'requiredDependency'})));
      assert.deepEqual(mr.game_versions, [entry.minecraft]);
      assert.deepEqual(mr.loaders, [entry.loader]);
      assert.equal(mr.version_number, `${VERSION}+${entry.loader}-${entry.minecraft}`);
      assert.equal(mr.project_id, MODRINTH_PROJECT);
      assert.deepEqual(cf.gameVersions, [IDS.minecraft[entry.minecraft], IDS.loader[entry.loader], IDS.client, IDS.server]);
    });
  }
});
test('descriptor generation fails closed on unknown dependency or incomplete CurseForge IDs', t => {
  const fx = fixture(t), entry = fx.files[0];
  assert.throws(() => modrinthDescriptor({...entry, required_mod_ids: ['unmapped']}, VERSION, CHANGELOG, MODRINTH_PROJECT, DEPENDENCIES));
  assert.throws(() => curseForgeDescriptor({...entry, required_mod_ids: ['unmapped']}, VERSION, CHANGELOG, IDS, DEPENDENCIES));
  for (const badIds of [
    {...IDS, client: undefined}, {...IDS, server: '99702'},
    {...IDS, loader: {...IDS.loader, fabric: undefined}}, {...IDS, minecraft: {...IDS.minecraft, '1.20.1': undefined}},
  ]) assert.throws(() => curseForgeDescriptor(entry, VERSION, CHANGELOG, badIds, DEPENDENCIES));
});
test('validatePlan accepts exactly ten unchanged artifacts bound to one release/source', t => {
  const fx = fixture(t), entries = validatePlan(fx.manifest, fx.directory);
  assert.equal(entries.length, 10);
  assert.deepEqual(entries.map(entry => entry.required_mod_ids), TARGETS.map(([, , required]) => required));
  for (const entry of entries) assert.equal(entry.absolute_path, path.join(fx.directory, entry.file_name));
});
test('validatePlan rejects incomplete, mixed, duplicate, or malformed inventories', async t => {
  const mutations = {
    'wrong schema': manifest => {manifest.schema_version = 2;},
    'not complete': manifest => {manifest.complete_inventory = false;},
    'wrong artifact count': manifest => {manifest.artifact_count = 9;},
    'nine files': manifest => {manifest.files.pop();},
    'extra file': manifest => {manifest.files.push(clone(manifest.files[0]));},
    'duplicate target': manifest => {manifest.files[1] = clone(manifest.files[0]);},
    'wrong source': manifest => {manifest.files[0].source_commit = 'f'.repeat(40);},
    'short source': manifest => {manifest.source_commit = 'abcdef';},
    'wrong release': manifest => {manifest.files[0].version = '4.2.9';},
    'unexpanded release': manifest => {manifest.release_version = '${version}';},
    'wrong mod': manifest => {manifest.mod_id = 'another_mod';},
    'wrong filename': manifest => {manifest.files[0].file_name = 'looks-like-release.jar';},
    'unlisted Minecraft target': manifest => {manifest.files[0].minecraft = '1.20.2';},
    'unlisted loader': manifest => {manifest.files[0].loader = 'quilt';},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, t => {
    const fx = fixture(t); mutate(fx.manifest);
    assert.throws(() => validatePlan(fx.manifest, fx.directory));
  });
});
test('validatePlan rejects wrong exact per-target dependencies including inherited 26.x Neo JA UML', async t => {
  for (let index = 0; index < TARGETS.length; index++) {
    const [mc, loader, required] = TARGETS[index];
    const variants = [['unknown dependency', [...required, 'unmapped-mod']]];
    if (required.length) {
      variants.push(['missing dependency', required.slice(1)]);
      variants.push(['duplicate dependency', [...required, required[0]]]);
    }
    variants.push(['extra allowed dependency', required.includes('jauml') ? [...required, 'fabric-api'] : [...required, 'jauml']]);
    for (const [name, dependencies] of variants) await t.test(`${mc} ${loader}: ${name}`, t => {
      const fx = fixture(t); fx.manifest.files[index].required_mod_ids = dependencies;
      assert.throws(() => validatePlan(fx.manifest, fx.directory));
    });
  }
});
test('validatePlan rejects each changed hash, byte count, and changed artifact bytes', async t => {
  for (const field of ['sha1', 'sha256', 'sha512', 'size_bytes', 'bytes']) await t.test(field, t => {
    const fx = fixture(t), entry = fx.manifest.files[0];
    if (field === 'bytes') fs.appendFileSync(path.join(fx.directory, entry.path), 'mutation');
    else if (field === 'size_bytes') entry.size_bytes++;
    else entry[field] = '0'.repeat(entry[field].length);
    assert.throws(() => validatePlan(fx.manifest, fx.directory), /changed|hash|size|verif/i);
  });
});
test('validatePlan rejects artifact symlinks and paths escaping the verified directory', async t => {
  for (const mode of ['symlink', 'escape']) await t.test(mode, t => {
    const fx = fixture(t), entry = fx.manifest.files[0];
    const original = path.join(fx.directory, entry.path), outside = path.join(fx.root, entry.file_name);
    fs.copyFileSync(original, outside);
    if (mode === 'symlink') {fs.unlinkSync(original); fs.symlinkSync(outside, original);}
    else entry.path = `../${entry.file_name}`;
    assert.throws(() => validatePlan(fx.manifest, fx.directory), /path|symlink|escape|directory/i);
  });
});
test('matchingModrinth accepts one exact match regardless of required dependency order', t => {
  const fx = fixture(t), entry = fx.files[0], existing = mrRecord(entry);
  existing.dependencies.reverse();
  assert.equal(matchingModrinth([existing], entry, mrDescriptor(entry)), existing.id);
  assert.equal(matchingModrinth([], entry, mrDescriptor(entry)), null);
  assert.throws(() => matchingModrinth([existing, clone(existing)], entry, mrDescriptor(entry)), /ambiguous/i);
});
test('matchingModrinth rejects filename/version collisions with wrong hash, target, or dependencies', async t => {
  const mutations = {
    'sha512': row => {row.files[0].hashes.sha512 = '0'.repeat(128);},
    'sha1': row => {row.files[0].hashes.sha1 = '0'.repeat(40);},
    'version': row => {row.version_number = '4.2.9+fabric-1.20.1';},
    'filename': row => {row.files[0].filename = 'different.jar';},
    'release type': row => {row.version_type = 'beta';},
    'loader': row => {row.loaders = ['forge'];},
    'extra loader': row => {row.loaders.push('forge');},
    'game': row => {row.game_versions = ['1.20.2'];},
    'extra game': row => {row.game_versions.push('1.20.2');},
    'missing dependency': row => {row.dependencies.pop();},
    'wrong dependency project': row => {row.dependencies[0].project_id = 'wrong-project';},
    'optional dependency': row => {row.dependencies[0].dependency_type = 'optional';},
    'extra required dependency': row => {row.dependencies.push({project_id: 'unexpected-project', dependency_type: 'required'});},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, t => {
    const fx = fixture(t), entry = fx.files[0], existing = mrRecord(entry); mutate(existing);
    assert.throws(() => matchingModrinth([existing], entry, mrDescriptor(entry)));
  });
  await t.test('26.x Neo has no inherited JA UML', t => {
    const fx = fixture(t), entry = fx.files[9], existing = mrRecord(entry);
    existing.dependencies.push({project_id: DEPENDENCIES.jauml.modrinth, dependency_type: 'required'});
    assert.throws(() => matchingModrinth([existing], entry, mrDescriptor(entry)));
  });
});
test('matchingModrinth requires explicit valid dependency metadata including empty 26.x Neo targets', async t => {
  const mutations = {
    'missing array': row => {delete row.dependencies;},
    'null array': row => {row.dependencies = null;},
    'object instead of array': row => {row.dependencies = {};},
    'null row': row => {row.dependencies.push(null);},
    'no target': row => {row.dependencies.push({dependency_type: 'optional'});},
    'all null targets': row => {row.dependencies.push({project_id: null, version_id: null, file_name: null, dependency_type: 'optional'});},
    'empty project ID': row => {row.dependencies.push({project_id: '', dependency_type: 'optional'});},
    'nonstring project ID': row => {row.dependencies.push({project_id: 1281310, dependency_type: 'optional'});},
    'non-base62 project ID': row => {row.dependencies.push({project_id: 'invalid-project', dependency_type: 'optional'});},
    'non-base62 version ID': row => {row.dependencies.push({project_id: 'ihvBalM2', version_id: 'invalid-version', dependency_type: 'optional'});},
    'empty external filename': row => {row.dependencies.push({file_name: ' ', dependency_type: 'optional'});},
    'nonstring external filename': row => {row.dependencies.push({file_name: 123, dependency_type: 'optional'});},
    'missing relation': row => {row.dependencies.push({project_id: 'ihvBalM2'});},
    'null relation': row => {row.dependencies.push({project_id: 'ihvBalM2', dependency_type: null});},
    'unknown relation': row => {row.dependencies.push({project_id: 'ihvBalM2', dependency_type: 'unknown'});},
  };
  for (const index of [0, 5, 7, 9]) for (const [name, mutate] of Object.entries(mutations)) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${name}`, t => {
      const fx = fixture(t), entry = fx.files[index], existing = mrRecord(entry);
      mutate(existing);
      assert.throws(() => matchingModrinth([existing], entry, mrDescriptor(entry)), /dependencies/i);
    });
  }
  await t.test('duplicate required destination', t => {
    const fx = fixture(t), entry = fx.files[0], existing = mrRecord(entry);
    existing.dependencies.push(clone(existing.dependencies[0]));
    assert.throws(() => matchingModrinth([existing], entry, mrDescriptor(entry)), /differs/i);
  });
  await t.test('unresolved required version or external file cannot stand in for a reviewed project', t => {
    const fx = fixture(t), entry = fx.files[0];
    for (const target of [{project_id: null, version_id: 'IIJJKKLL'}, {project_id: null, file_name: 'external.jar'}]) {
      const existing = mrRecord(entry);
      existing.dependencies[0] = {...target, dependency_type: 'required'};
      assert.throws(() => matchingModrinth([existing], entry, mrDescriptor(entry)), /differs/i);
    }
  });
  await t.test('documented optional, incompatible, embedded and external targets remain valid', t => {
    const fx = fixture(t), entry = fx.files[9], existing = mrRecord(entry);
    existing.dependencies = ['optional', 'incompatible', 'embedded'].map(dependency_type => ({project_id: 'ihvBalM2', dependency_type}));
    existing.dependencies.push({project_id: null, version_id: 'IIJJKKLL', file_name: null, dependency_type: 'optional'},
      {project_id: null, version_id: null, file_name: 'external.jar', dependency_type: 'embedded'});
    assert.equal(matchingModrinth([existing], entry, mrDescriptor(entry)), existing.id);
  });
});
test('matchingCurseForge accepts one exact match and recognizes uppercase SHA1', t => {
  const fx = fixture(t), entry = fx.files[0], existing = cfRecord(entry);
  existing.hashes[0].value = existing.hashes[0].value.toUpperCase();
  existing.dependencies.reverse();
  assert.equal(matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES), existing.id);
  assert.equal(matchingCurseForge([], entry, cfDescriptor(entry), DEPENDENCIES), null);
  assert.throws(() => matchingCurseForge([existing, clone(existing)], entry, cfDescriptor(entry), DEPENDENCIES), /ambiguous/i);
});
test('matchingCurseForge rejects filename/display collisions with wrong hash, target, or dependencies', async t => {
  const mutations = {
    'sha1': row => {row.hashes[0].value = '0'.repeat(40);},
    'missing sha1': row => {row.hashes = [{algo: 2, value: '0'.repeat(32)}];},
    'display name': row => {row.displayName = '4.2.9 · fabric · 1.20.1';},
    'filename': row => {row.fileName = 'different.jar';},
    'release type': row => {row.releaseType = 2;},
    'loader': row => {row.gameVersions = ['1.20.1', 'Forge', 'Client', 'Server'];},
    'extra loader': row => {row.gameVersions.push('Forge');},
    'game': row => {row.gameVersions = ['1.20.2', 'Fabric', 'Client', 'Server'];},
    'extra game': row => {row.gameVersions.push('1.20.2');},
    'missing dependency': row => {row.dependencies.pop();},
    'wrong dependency project': row => {row.dependencies[0].modId = 987654321;},
    'optional dependency': row => {row.dependencies[0].relationType = 2;},
    'extra required dependency': row => {row.dependencies.push({modId: 987654321, relationType: 3});},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, t => {
    const fx = fixture(t), entry = fx.files[0], existing = cfRecord(entry); mutate(existing);
    assert.throws(() => matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES));
  });
  await t.test('26.x Neo has no inherited JA UML', t => {
    const fx = fixture(t), entry = fx.files[9], existing = cfRecord(entry);
    existing.dependencies.push({modId: DEPENDENCIES.jauml.curseforge_id, relationType: 3});
    assert.throws(() => matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES));
  });
});
test('matchingCurseForge requires explicit well-formed dependencies even for empty 26.x Neo targets', async t => {
  const mutations = {
    'missing array': row => {delete row.dependencies;},
    'null array': row => {row.dependencies = null;},
    'object instead of array': row => {row.dependencies = {};},
    'null row': row => {row.dependencies.push(null);},
    'missing mod ID': row => {row.dependencies.push({relationType: 2});},
    'string mod ID': row => {row.dependencies.push({modId: '1281310', relationType: 2});},
    'zero mod ID': row => {row.dependencies.push({modId: 0, relationType: 2});},
    'negative mod ID': row => {row.dependencies.push({modId: -1, relationType: 2});},
    'noninteger mod ID': row => {row.dependencies.push({modId: 1.5, relationType: 2});},
    'above unsigned int32 mod ID': row => {row.dependencies.push({modId: 0x100000000, relationType: 2});},
    'unsafe mod ID': row => {row.dependencies.push({modId: Number.MAX_SAFE_INTEGER + 1, relationType: 2});},
    'missing relation type': row => {row.dependencies.push({modId: 1281310});},
    'string relation type': row => {row.dependencies.push({modId: 1281310, relationType: '3'});},
    'zero relation type': row => {row.dependencies.push({modId: 1281310, relationType: 0});},
    'unknown relation type': row => {row.dependencies.push({modId: 1281310, relationType: 7});},
    'noninteger relation type': row => {row.dependencies.push({modId: 1281310, relationType: 2.5});},
  };
  for (const index of [0, 5, 7, 9]) for (const [name, mutate] of Object.entries(mutations)) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${name}`, t => {
      const fx = fixture(t), entry = fx.files[index], existing = cfRecord(entry);
      mutate(existing);
      assert.throws(() => matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES), /dependencies/i);
    });
  }
  await t.test('duplicate required destination', t => {
    const fx = fixture(t), entry = fx.files[0], existing = cfRecord(entry);
    existing.dependencies.push(clone(existing.dependencies[0]));
    assert.throws(() => matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES), /differs/i);
  });
  await t.test('documented non-required relation types are valid rows', t => {
    const fx = fixture(t), entry = fx.files[9], existing = cfRecord(entry);
    existing.dependencies = [1, 2, 4, 5, 6].map(relationType => ({modId: 1281310, relationType}));
    assert.equal(matchingCurseForge([existing], entry, cfDescriptor(entry), DEPENDENCIES), existing.id);
  });
});
test('an otherwise exact existing record with a missing or malformed provider ID cannot trigger a fresh POST', async t => {
  const variants = {
    modrinth: [undefined, null, '', ' ', 123, {}, [], 'invalid-id'],
    curseforge: [undefined, null, '', '1001', 0, -1, 1.5, {}, [], 0x100000000],
  };
  for (const [platform, ids] of Object.entries(variants)) for (const id of ids) await t.test(`${platform}: ${JSON.stringify(id)}`, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const entry = fx.files[0];
    const row = platform === 'modrinth' ? mrRecord(entry) : cfRecord(entry);
    if (id === undefined) delete row.id;
    else row.id = id;
    assert.throws(() => platform === 'modrinth' ? matchingModrinth([row], entry, mrDescriptor(entry))
      : matchingCurseForge([row], entry, cfDescriptor(entry), DEPENDENCIES));
    const store = fakeStore(t, fx, {[platform]: [row]});
    await assert.rejects(main(fx.argv(platform)), /differs|inventory|deduplicate/i);
    assert.equal(store.requests.filter(call => call.method === 'POST').length, 0);
  });
});
test('malformed POST response IDs remain uncertain and block all later or repeated uploads', async t => {
  const variants = {modrinth: [null, '', ' ', 123, {}, [], 'invalid-id'],
    curseforge: [null, '', '1001', 0, -1, 1.5, {}, [], 0x100000000]};
  for (const [platform, ids] of Object.entries(variants)) for (const id of ids) await t.test(`${platform}: ${JSON.stringify(id)}`, async t => {
    const fx = fixture(t); fakeCredentials(t);
    let posts = 0;
    fakeStore(t, fx, {intercept(call) {
      if (call.method === 'POST') {posts++; return jsonResponse({id});}
    }});
    await assert.rejects(main(fx.argv(platform)), /uncertain|reconcile|outcome/i);
    assert.equal(posts, 1);
    assertDurableSubmission(fx, fx.files[0]);
    const receipt = fx.state().uploads[0];
    assert.equal(receipt.status, 'uncertain');
    assert.equal(receipt.id, undefined);
    await assert.rejects(main(fx.argv(platform)), /reconcile|pending|submitted/i);
    assert.equal(posts, 1);
    assert.deepEqual(fx.state().uploads[0], receipt);
  });
});
test('dry run needs no tokens and makes no network requests or writes to either platform', async t => {
  const fx = fixture(t);
  fakeCredentials(t, {MODRINTH_TOKEN: undefined, CURSEFORGE_TOKEN: undefined, CURSEFORGE_API_KEY: undefined});
  const calls = installFetch(t, () => assert.fail('Dry-run fetch is forbidden'));
  const state = await main([...fx.argv(), '--dry-run']);
  assert.equal(state.uploads.length, 20);
  assert.equal(calls.length, 0);
  assert.ok(state.uploads.every(upload => upload.status.startsWith('dry-run')));
  assert.deepEqual(fx.state(), state);
});
test('every selected-platform credential is checked before the first network request/write', async t => {
  const variants = [
    ['both', 'MODRINTH_TOKEN'], ['both', 'CURSEFORGE_TOKEN'], ['both', 'CURSEFORGE_API_KEY'],
    ['modrinth', 'MODRINTH_TOKEN'], ['curseforge', 'CURSEFORGE_TOKEN'], ['curseforge', 'CURSEFORGE_API_KEY'],
  ];
  for (const [platform, key] of variants) await t.test(`${platform}: ${key}`, async t => {
    const fx = fixture(t); fakeCredentials(t, {[key]: undefined});
    const calls = installFetch(t, () => assert.fail('Missing credential must fail before fetch'));
    await assert.rejects(main(fx.argv(platform)), /TOKEN|API_KEY|credential/i);
    assert.equal(calls.length, 0);
  });
});
test('platform-only recovery does not require the other platform credentials', async t => {
  for (const platform of ['modrinth', 'curseforge']) await t.test(platform, async t => {
    const fx = fixture(t);
    fakeCredentials(t, platform === 'modrinth' ? {CURSEFORGE_TOKEN: undefined, CURSEFORGE_API_KEY: undefined} : {MODRINTH_TOKEN: undefined});
    const store = fakeStore(t, fx, {
      modrinth: fx.files.map(entry => mrRecord(entry)), curseforge: fx.files.map(entry => cfRecord(entry)),
    });
    const state = await main(fx.argv(platform));
    assert.equal(state.uploads.length, 10);
    assert.ok(state.uploads.every(upload => upload.platform === platform));
    assert.equal(store.posts.length, 0);
  });
});
test('unmapped or incomplete dependencies fail before first POST even for platform-only recovery', async t => {
  const variants = {
    'missing JA UML': map => {delete map.jauml;},
    'missing Fabric API': map => {delete map['fabric-api'];},
    'missing MR mapping': map => {delete map.jauml.modrinth;},
    'missing CF slug': map => {delete map.jauml.curseforge_slug;},
    'missing CF project ID': map => {delete map.jauml.curseforge_id;},
    'noninteger CF project ID': map => {map.jauml.curseforge_id = '1281310';},
  };
  for (const platform of ['both', 'modrinth', 'curseforge']) for (const [name, mutate] of Object.entries(variants)) await t.test(`${platform}: ${name}`, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const dependencies = clone(DEPENDENCIES); mutate(dependencies);
    fs.writeFileSync(fx.paths.dependencies, JSON.stringify(dependencies));
    const calls = installFetch(t, () => assert.fail('Invalid dependency mapping must fail before fetch'));
    await assert.rejects(main(fx.argv(platform)), /dependency|mapping/i);
    assert.equal(calls.length, 0);
  });
});
test('changed bytes fail before first POST both initially and after an inventory GET', async t => {
  for (const platform of ['modrinth', 'curseforge']) for (const duringGet of [false, true]) await t.test(`${platform}: ${duringGet ? 'during GET' : 'before preflight'}`, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const mutate = () => fs.appendFileSync(path.join(fx.directory, fx.files[0].path), 'changed');
    if (!duringGet) mutate();
    let changed = false;
    const store = fakeStore(t, fx, {intercept(call) {
      if (duringGet && call.method === 'GET' && !changed) {mutate(); changed = true;}
    }});
    await assert.rejects(main(fx.argv(platform)), /changed|hash|size|verif/i);
    assert.equal(store.requests.filter(call => call.method === 'POST').length, 0);
  });
});
test('exactly matching complete releases make zero POSTs and produce verified-existing receipts', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const store = fakeStore(t, fx, {modrinth: fx.files.map(entry => mrRecord(entry)), curseforge: fx.files.map(entry => cfRecord(entry))});
  const state = await main(fx.argv());
  assert.equal(store.posts.length, 0);
  assert.equal(state.uploads.length, 20);
  assert.ok(state.uploads.every(upload => upload.status === 'verified-existing' && upload.sha256));
});
test('resume exactly matching Modrinth then partial CurseForge without reposting existing files', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const store = fakeStore(t, fx, {
    modrinth: fx.files.map(entry => mrRecord(entry)), curseforge: fx.files.slice(0, 4).map(entry => cfRecord(entry)),
  });
  const state = await main(fx.argv());
  assert.deepEqual(store.posts.map(post => [post.platform, post.file]), fx.files.slice(4).map(entry => ['curseforge', entry.file_name]));
  assert.equal(state.uploads.length, 20);
  assert.equal(state.uploads.filter(upload => upload.status === 'verified-existing').length, 14);
  assert.equal(state.uploads.filter(upload => upload.status === 'verified').length, 6);
  const before = store.posts.length;
  const resumed = await main(fx.argv());
  assert.equal(store.posts.length, before, 'A second run must not repost any matching artifact');
  assert.ok(resumed.uploads.every(upload => upload.status === 'verified-existing'));
});
test('full synthetic release publishes exact multipart metadata and attested bytes once per platform/target', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const store = fakeStore(t, fx);
  const state = await main(fx.argv());
  assert.equal(store.posts.length, 20);
  assert.equal(new Set(store.posts.map(post => `${post.platform}:${post.file}`)).size, 20);
  assert.equal(state.uploads.length, 20);
  assert.ok(state.uploads.every(upload => upload.status === 'verified'));
  assert.deepEqual(fx.state(), state);
});
test('uncertain POST/timeout is journaled and never blindly retried within a run or rerun', async t => {
  for (const platform of ['modrinth', 'curseforge']) await t.test(platform, async t => {
    const fx = fixture(t); fakeCredentials(t);
    let posts = 0;
    const store = fakeStore(t, fx, {intercept(call) {
      if (call.method === 'POST') {posts++; throw new DOMException('Synthetic accepted-then-timeout', 'TimeoutError');}
    }});
    await assert.rejects(main(fx.argv(platform)), /timeout|uncertain|reconcile|Synthetic/i);
    assert.equal(posts, 1, 'Uncertain write must not be retried in the same run');
    assertDurableSubmission(fx, fx.files[0]);
    await assert.rejects(main(fx.argv(platform)), /uncertain|pending|submitted|reconcile|visibility|index|outcome/i);
    assert.equal(posts, 1, 'Rerun must reconcile the durable attempt before issuing another POST');
    assert.equal(store.requests.filter(call => call.method === 'POST').length, 1);
  });
});
test('POST response missing ID is journaled and blocks a blind rerun', async t => {
  for (const platform of ['modrinth', 'curseforge']) await t.test(platform, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const store = fakeStore(t, fx, {missingId: true, pendingIndex: true});
    await assert.rejects(main(fx.argv(platform)), /ID|outcome|reconcile/i);
    assert.equal(store.posts.length, 1);
    assertDurableSubmission(fx, fx.files[0]);
    await assert.rejects(main(fx.argv(platform)), /uncertain|pending|submitted|reconcile|visibility|index|outcome/i);
    assert.equal(store.posts.length, 1);
  });
});
test('submitted release pending remote indexing blocks subsequent POSTs and a blind rerun', async t => {
  for (const platform of ['modrinth', 'curseforge']) await t.test(platform, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const store = fakeStore(t, fx, {pendingIndex: true});
    await assert.rejects(main(fx.argv(platform)), /visibility|index|review|pending|reconcile/i);
    assert.equal(store.posts.length, 1);
    assertDurableSubmission(fx, fx.files[0]);
    await assert.rejects(main(fx.argv(platform)), /uncertain|pending|submitted|reconcile|visibility|index|outcome/i);
    assert.equal(store.posts.length, 1, 'An accepted upload awaiting indexing must not be resubmitted');
  });
});
test('an uncertain upload can resume after its exact hash-bound record becomes visible', async t => {
  for (const platform of ['modrinth', 'curseforge']) await t.test(platform, async t => {
    const fx = fixture(t); fakeCredentials(t);
    let failed = false, attempted = 0;
    const store = fakeStore(t, fx, {intercept(call, remote) {
      if (call.method === 'POST') {
        attempted++;
        if (!failed) {
          failed = true;
          remote[platform].push(platform === 'modrinth' ? mrRecord(fx.files[0]) : cfRecord(fx.files[0]));
          throw new DOMException('Synthetic timeout after successful acceptance', 'TimeoutError');
        }
      }
    }});
    await assert.rejects(main(fx.argv(platform)), /timeout|uncertain|reconcile|Synthetic/i);
    assert.equal(attempted, 1);
    const state = await main(fx.argv(platform));
    assert.equal(attempted, 10);
    assert.equal(store.posts.length, 9);
    assert.ok(store.posts.every(post => post.file !== fx.files[0].file_name));
    assert.equal(state.uploads.length, 10);
  });
});

test('non-timeout uncertain POST outcomes also block blind retries until reconciliation', async t => {
  const failures = {
    'connection loss': () => {throw new TypeError('Synthetic connection lost after transmission');},
    'HTTP 502': () => new Response('Synthetic gateway failure', {status: 502}),
    'invalid JSON': () => new Response('{malformed', {status: 200}),
    'empty response': () => new Response('', {status: 200}),
  };
  for (const platform of ['modrinth', 'curseforge']) for (const [name, fail] of Object.entries(failures)) await t.test(`${platform}: ${name}`, async t => {
    const fx = fixture(t); fakeCredentials(t);
    let posts = 0;
    fakeStore(t, fx, {intercept(call) {
      if (call.method === 'POST') {posts++; return fail();}
    }});
    await assert.rejects(main(fx.argv(platform)));
    assert.equal(posts, 1);
    assertDurableSubmission(fx, fx.files[0]);
    await assert.rejects(main(fx.argv(platform)));
    assert.equal(posts, 1, 'An uncertain HTTP/parse/transport outcome must not trigger a duplicate upload');
  });
});
test('recovery journal with wrong source, release, file hash, or malformed contents fails closed', async t => {
  const mutations = {
    'wrong schema': state => {state.schema_version = 999;},
    'wrong source': state => {state.source_commit = 'f'.repeat(40);},
    'wrong release': state => {state.version = '4.2.9';},
    'wrong hash': state => {state.uploads[0].sha256 = '0'.repeat(64);},
    'unknown file': state => {state.uploads[0].file = 'unverified.jar';},
    'unknown platform': state => {state.uploads[0].platform = 'another-store';},
    'malformed uploads': state => {state.uploads = 'corrupt';},
    'malformed JSON': () => {},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const state = {schema_version: 1, source_commit: COMMIT, version: VERSION, uploads: [{
      platform: 'modrinth', file: fx.files[0].file_name, sha256: fx.files[0].sha256,
      id: 'previously-submitted-id', status: 'submitted',
    }]};
    mutate(state);
    fs.writeFileSync(fx.paths.state, name === 'malformed JSON' ? '{broken' : JSON.stringify(state));
    const original = fs.readFileSync(fx.paths.state, 'utf8');
    const store = fakeStore(t, fx);
    await assert.rejects(main(fx.argv('modrinth')));
    assert.equal(store.requests.filter(call => call.method === 'POST').length, 0);
    assert.equal(fs.readFileSync(fx.paths.state, 'utf8'), original, 'Invalid recovery evidence must not be silently overwritten');
  });
});
test('dry-run cannot erase the durable journal of a live pending submission', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const original = JSON.stringify({schema_version: 1, source_commit: COMMIT, version: VERSION, uploads: [{
    platform: 'modrinth', file: fx.files[0].file_name, sha256: fx.files[0].sha256,
    id: 'previously-submitted-id', status: 'submitted',
  }]}, null, 2) + '\n';
  fs.writeFileSync(fx.paths.state, original);
  const calls = installFetch(t, () => assert.fail('Dry-run must never fetch'));
  try {await main([...fx.argv('modrinth'), '--dry-run']);} catch (error) {
    assert.match(error.message, /state|journal|dry.run|pending|submission|existing|reconcile/i);
  }
  assert.equal(calls.length, 0);
  assert.equal(fs.readFileSync(fx.paths.state, 'utf8'), original, 'Dry-run must preserve pending upload evidence');
});
test('CurseForge recovery exhausts pagination before deciding an artifact is missing', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const unrelated = cfHistorical(50);
  const store = fakeStore(t, fx, {curseforge: [...unrelated, ...fx.files.map(entry => cfRecord(entry))]});
  const state = await main(fx.argv('curseforge'));
  assert.equal(store.posts.length, 0);
  assert.equal(state.uploads.length, 10);
  assert.ok(store.requests.some(call => call.url.includes('index=50')));
  assert.deepEqual(store.requests.filter(call => call.url.includes('/files?'))
    .map(call => Number(new URL(call.url).searchParams.get('index'))), [0, 50]);
});
test('malformed, truncated, changing, or duplicate CurseForge inventories stop all provider POSTs', async t => {
  const mutations = {
    'missing pagination': page => {delete page.pagination;},
    'null pagination': page => {page.pagination = null;},
    'missing index': page => {delete page.pagination.index;},
    'missing page size': page => {delete page.pagination.pageSize;},
    'missing result count': page => {delete page.pagination.resultCount;},
    'missing total count': page => {delete page.pagination.totalCount;},
    'string index': page => {page.pagination.index = '0';},
    'string page size': page => {page.pagination.pageSize = '50';},
    'string result count': page => {page.pagination.resultCount = '50';},
    'string total count': page => {page.pagination.totalCount = '60';},
    'wrong index': page => {page.pagination.index++;},
    'zero page size': page => {page.pagination.pageSize = 0;},
    'wrong page size': page => {page.pagination.pageSize = 25;},
    'oversized page size': page => {page.pagination.pageSize = 51;},
    'negative result count': page => {page.pagination.resultCount = -1;},
    'wrong result count': page => {page.pagination.resultCount--;},
    'negative total count': page => {page.pagination.totalCount = -1;},
    'total count smaller than page': page => {page.pagination.totalCount = 40;},
    'total count above Core bound': page => {page.pagination.totalCount = 10001;},
    'unsafe total count': page => {page.pagination.totalCount = Number.MAX_SAFE_INTEGER + 1;},
    'missing data': page => {delete page.data;},
    'nonarray data': page => {page.data = {};},
    'short first page with further records': page => {page.data.pop(); page.pagination.resultCount--;},
    'empty page with further records': page => {page.data = []; page.pagination.resultCount = 0;},
    'oversized data': page => {page.data.push(cfHistorical(1, 100)[0]); page.pagination.resultCount++;},
    'duplicate ID within first page': page => {page.data[1] = clone(page.data[0]);},
    'null file row': page => {page.data[0] = null;},
    'missing file ID': page => {delete page.data[0].id;},
    'string file ID': page => {page.data[0].id = '2001';},
    'zero file ID': page => {page.data[0].id = 0;},
    'missing filename': page => {delete page.data[0].fileName;},
    'missing display name': page => {delete page.data[0].displayName;},
    'growing total count': (page, index) => {if (index === 50) page.pagination.totalCount++;},
    'shrinking total count': (page, index) => {if (index === 50) page.pagination.totalCount--;},
    'short last page': (page, index) => {if (index === 50) {page.data.pop(); page.pagination.resultCount--;}},
    'duplicate ID across pages': (page, index, inventory) => {if (index === 50) page.data[0] = clone(inventory[0]);},
  };
  for (const [name, mutate] of Object.entries(mutations)) await t.test(name, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const inventory = [...cfHistorical(50), ...fx.files.map(entry => cfRecord(entry))];
    const store = fakeStore(t, fx, {intercept(call) {
      if (call.url.includes('/files?')) {
        const index = Number(new URL(call.url).searchParams.get('index'));
        const page = cfPage(clone(inventory.slice(index, index + 50)), index, inventory.length);
        mutate(page, index, inventory);
        return jsonResponse(page);
      }
    }});
    await assert.rejects(main(fx.argv()), /inventory|deduplicate|bound/i);
    assert.equal(store.requests.filter(call => call.method === 'POST').length, 0,
      'An incomplete deduplication inventory must stop both providers before any write');
    assert.equal(fx.state().uploads.length, 0);
    assert.equal(fx.state().publicly_verified, false);
  });
});
test('CurseForge inventory reads at most 10000 records and never requests an out-of-bounds page', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const store = fakeStore(t, fx, {curseforge: [...cfHistorical(9990), ...fx.files.map(entry => cfRecord(entry))]});
  const state = await main(fx.argv('curseforge'));
  assert.equal(store.posts.length, 0);
  assert.equal(state.publicly_verified, true);
  const requests = store.requests.filter(call => call.url.includes('/files?'));
  assert.equal(requests.length, 200);
  for (const call of requests) {
    const params = new URL(call.url).searchParams;
    assert.ok(Number(params.get('index')) + Number(params.get('pageSize')) <= 10000);
  }
  assert.equal(new URL(requests.at(-1).url).searchParams.get('index'), '9950');
});
test('malformed or wrong CurseForge dependency inventories fail preflight before either provider writes', async t => {
  for (const index of [0, 5, 7, 9]) for (const mode of ['missing', 'null', 'wrong required', 'unknown relation', 'missing relation']) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${mode}`, async t => {
      const fx = fixture(t); fakeCredentials(t);
      const inventory = fx.files.map(entry => cfRecord(entry));
      const row = inventory[index];
      if (mode === 'missing') delete row.dependencies;
      else if (mode === 'null') row.dependencies = null;
      else row.dependencies = [{modId: 1281310, ...(mode === 'wrong required' ? {relationType: 3}
        : mode === 'unknown relation' ? {relationType: 999} : {})}];
      const store = fakeStore(t, fx, {curseforge: inventory});
      await assert.rejects(main(fx.argv()), /dependencies|differs/i);
      assert.equal(store.requests.filter(call => call.method === 'POST').length, 0);
      assert.equal(fx.state().uploads.length, 0);
    });
  }
});
test('accepted CurseForge POST followed by missing or wrong indexed dependencies retains submitted journal and halts', async t => {
  for (const index of [0, 5, 7, 9]) for (const mode of ['missing', 'null', 'wrong required', 'unknown relation', 'missing relation']) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${mode}`, async t => {
      const fx = fixture(t); fakeCredentials(t);
      const entry = fx.files[index];
      const store = fakeStore(t, fx, {curseforge: fx.files.slice(0, index).map(row => cfRecord(row)),
        intercept(call, remote) {
          if (call.url.includes('/files?') && remote.posts.length) {
            const row = remote.curseforge.find(row => row.fileName === entry.file_name);
            if (mode === 'missing') delete row.dependencies;
            else if (mode === 'null') row.dependencies = null;
            else row.dependencies = [{modId: 1281310, ...(mode === 'wrong required' ? {relationType: 3}
              : mode === 'unknown relation' ? {relationType: 999} : {})}];
          }
        }});
      await assert.rejects(main(fx.argv('curseforge')), /dependencies|differs/i);
      assert.equal(store.posts.length, 1, 'The successful ID-only POST must halt before the next upload');
      assertDurableSubmission(fx, entry);
      const receipt = fx.state().uploads.find(row => row.file === entry.file_name);
      assert.equal(receipt.status, 'submitted');
      assert.equal(receipt.id, cfRecord(entry).id);
      assert.equal(fx.state().publicly_verified, false);
      await assert.rejects(main(fx.argv('curseforge')), /dependencies|differs/i);
      assert.equal(store.posts.length, 1, 'Invalid indexing metadata must never permit a blind second POST');
      assert.deepEqual(fx.state().uploads.find(row => row.file === entry.file_name), receipt);
    });
  }
});
test('invalid Modrinth dependencies fail preflight before either provider writes', async t => {
  for (const index of [0, 5, 7, 9]) for (const mode of ['missing', 'null', 'wrong required', 'unknown relation', 'missing relation']) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${mode}`, async t => {
      const fx = fixture(t); fakeCredentials(t);
      const inventory = fx.files.map(entry => mrRecord(entry));
      const row = inventory[index];
      if (mode === 'missing') delete row.dependencies;
      else if (mode === 'null') row.dependencies = null;
      else row.dependencies = [{project_id: 'ihvBalM2', ...(mode === 'wrong required' ? {dependency_type: 'required'}
        : mode === 'unknown relation' ? {dependency_type: 'unknown'} : {})}];
      const store = fakeStore(t, fx, {modrinth: inventory});
      await assert.rejects(main(fx.argv()), /dependencies|differs/i);
      assert.equal(store.requests.filter(call => call.method === 'POST').length, 0);
      assert.equal(fx.state().uploads.length, 0);
    });
  }
});
test('accepted Modrinth POST followed by invalid indexed dependencies retains submitted journal and halts', async t => {
  for (const index of [0, 5, 7, 9]) for (const mode of ['missing', 'null', 'wrong required', 'unknown relation', 'missing relation']) {
    await t.test(`${TARGETS[index][0]} ${TARGETS[index][1]}: ${mode}`, async t => {
      const fx = fixture(t); fakeCredentials(t);
      const entry = fx.files[index];
      const store = fakeStore(t, fx, {modrinth: fx.files.slice(0, index).map(row => mrRecord(row)),
        intercept(call, remote) {
          if (call.url === `https://api.modrinth.com/v2/project/${MODRINTH_PROJECT}/version` && remote.posts.length) {
            const row = remote.modrinth.find(row => row.version_number === mrDescriptor(entry).version_number);
            if (mode === 'missing') delete row.dependencies;
            else if (mode === 'null') row.dependencies = null;
            else row.dependencies = [{project_id: 'ihvBalM2', ...(mode === 'wrong required' ? {dependency_type: 'required'}
              : mode === 'unknown relation' ? {dependency_type: 'unknown'} : {})}];
          }
        }});
      await assert.rejects(main(fx.argv('modrinth')), /dependencies|differs/i);
      assert.equal(store.posts.length, 1);
      assertDurableSubmission(fx, entry);
      const receipt = fx.state().uploads.find(row => row.file === entry.file_name);
      assert.equal(receipt.status, 'submitted');
      assert.equal(receipt.id, mrRecord(entry).id);
      assert.equal(fx.state().publicly_verified, false);
      await assert.rejects(main(fx.argv('modrinth')), /dependencies|differs/i);
      assert.equal(store.posts.length, 1, 'Invalid dependency metadata must stop any next or rerun POST');
      assert.deepEqual(fx.state().uploads.find(row => row.file === entry.file_name), receipt);
    });
  }
});
test('missing or ambiguous CurseForge game catalog IDs fail before the first CurseForge POST', async t => {
  for (const mode of ['missing game', 'duplicate game', 'missing loader']) await t.test(mode, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const store = fakeStore(t, fx, {intercept(call) {
      if (call.url === 'https://minecraft.curseforge.com/api/game/versions') {
        const data = [
          {name: 'Client', id: IDS.client}, {name: 'Server', id: IDS.server},
          ...Object.entries(LOADER_NAMES).map(([loader, name]) => ({name, id: IDS.loader[loader]})),
          ...Object.entries(IDS.minecraft).map(([name, id]) => ({name, id})),
        ];
        if (mode === 'missing game') return jsonResponse(data.filter(item => item.name !== '26.3'));
        if (mode === 'duplicate game') return jsonResponse([...data, {name: '26.3', id: 88888}]);
        return jsonResponse(data.filter(item => item.name !== 'NeoForge'));
      }
    }});
    await assert.rejects(main(fx.argv('curseforge')), /missing|ambiguous|catalog|version/i);
    assert.equal(store.posts.length, 0);
  });
});


test('legacy per-workspace Node entry point rejects uploads including dry-run', () => {
  const entry = fileURLToPath(new URL('../scripts/upload_platforms.mjs', import.meta.url));
  for (const flags of [[], ['--dry-run'], ['--workspace', '26.1.2', '--version', VERSION]]) {
    const result = spawnSync(process.execPath, [entry, ...flags], {encoding: 'utf8', env: {}});
    assert.equal(result.status, 1);
    assert.equal(result.stdout, '');
    assert.match(result.stderr, /Direct per-workspace uploads are disabled/);
    assert.match(result.stderr, /source-bound verified ten-JAR manifest/);
    assert.match(result.stderr, /docs\/releasing\.md/);
  }
});

test('legacy PowerShell entry point only accepts compatibility flags and fails closed', () => {
  const entry = fileURLToPath(new URL('../upload_local.ps1', import.meta.url));
  const source = fs.readFileSync(entry, 'utf8').replace(/^\s*#.*$/gm, '').trim();
  // No credential reads, build subprocesses, network calls or delegated uploader before failure.
  assert.match(source, /^param\([\s\S]*?\)\s*throw "Direct per-workspace uploads are disabled[^"\r\n]*"$/);
  assert.match(source, /retained upload journal/);
  for (const flag of ['Version', 'Workspace', 'CurseForgeOnly', 'ModrinthOnly', 'DryRun']) {
    assert.ok(source.includes('$' + flag), 'Preserve compatibility flag: ' + flag);
  }
});

test('legacy PowerShell entry point rejects a native dry-run when PowerShell is available', t => {
  const probe = spawnSync('pwsh', ['-NoLogo', '-NoProfile', '-Command', '$PSVersionTable.PSVersion.ToString()'], {encoding: 'utf8'});
  if (probe.error?.code === 'ENOENT') {t.skip('PowerShell is not installed; static fail-closed check still runs'); return;}
  assert.equal(probe.status, 0, probe.stderr);
  const entry = fileURLToPath(new URL('../upload_local.ps1', import.meta.url));
  const result = spawnSync('pwsh', ['-NoLogo', '-NoProfile', '-File', entry, '-DryRun'], {encoding: 'utf8', env: {}});
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /Direct per-workspace uploads are disabled/);
});

test('credentialed read-only preflight validates actual catalogs without any journal changes or POST', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const state = {schema_version: 1, source_commit: COMMIT, version: VERSION, publicly_verified: true,
    uploads: fx.files.map(entry => ({platform: 'modrinth', file: entry.file_name, sha256: entry.sha256, status: 'verified', id: mrRecord(entry).id}))};
  const bytes = JSON.stringify(state, null, 2) + '\n';
  fs.writeFileSync(fx.paths.state, bytes);
  const store = fakeStore(t, fx, {modrinth: fx.files.map(entry => mrRecord(entry))});
  const result = await main([...fx.argv('both'), '--preflight-only', '--require-public-modrinth']);
  assert.equal(result.preflight_verified, true);
  assert.deepEqual(result.curseforge_ids, IDS);
  assert.equal(store.posts.length, 0);
  assert.equal(fs.readFileSync(fx.paths.state, 'utf8'), bytes);
  await assert.rejects(main([...fx.argv(), '--preflight-only', '--dry-run']), /mutually exclusive/);
});
test('failed read-only preflight preserves journal and sends no POST', async t => {
  const fx = fixture(t); fakeCredentials(t);
  const bytes = JSON.stringify({schema_version: 1, source_commit: COMMIT, version: VERSION, uploads: [], publicly_verified: false});
  fs.writeFileSync(fx.paths.state, bytes);
  const store = fakeStore(t, fx, {intercept(call) {
    if (call.url.endsWith('/minecraft/version/26.3')) return jsonResponse({data: {...catalogFixture().canonical['26.3'].data, gameVersionId: 55555}});
  }});
  await assert.rejects(main([...fx.argv('curseforge'), '--preflight-only']), /mismatch/);
  assert.equal(store.posts.length, 0);
  assert.equal(fs.readFileSync(fx.paths.state, 'utf8'), bytes);
});

test('read-only audit reports exact approved or pending CF match facts and preserves the latest uncertain journal bytes', async t => {
  for (const fileStatus of [4,10,1,2,8]) await t.test(`file status ${fileStatus}`, async t=>{
    const fx=fixture(t);fakeCredentials(t);
    const cf=cfRecord(fx.files[0]);cf.fileStatus=fileStatus;cf.isAvailable=[4,10].includes(fileStatus);
    cf.downloadUrl='https://untrusted.invalid/reflected-secret';cf.unknown='offline-secret-reflection';
    const state={schema_version:1,source_commit:COMMIT,version:VERSION,publicly_verified:false,
      uploads:[...fx.files.map(entry=>({platform:'modrinth',file:entry.file_name,sha256:entry.sha256,status:'verified',id:mrRecord(entry).id})),
        {platform:'curseforge',file:fx.files[0].file_name,sha256:fx.files[0].sha256,status:'uncertain'}]};
    const bytes=JSON.stringify(state,null,2)+'\n';fs.writeFileSync(fx.paths.state,bytes);
    const store=fakeStore(t,fx,{modrinth:fx.files.map(entry=>mrRecord(entry)),curseforge:[cf]});
    const result=await main([...fx.argv(),'--preflight-only','--require-public-modrinth']);
    const audit=result.curseforge_inventory;
    assert.equal(audit.complete_core_inventory,true);assert.equal(audit.inventory_count,1);
    assert.equal(audit.author_pending_upload_visibility,'not-established');
    assert.equal(audit.journal_sha256,hash(bytes,'sha256'));assert.equal(audit.entries.length,10);
    const entry=audit.entries[0];assert.equal(entry.journal_status,'uncertain');assert.equal(entry.journal_id,null);
    assert.equal(entry.match.id,cf.id);assert.equal(entry.match.file_status,fileStatus);
    assert.equal(entry.match.sha1,fx.files[0].sha1);
    assert.deepEqual(entry.match.required_dependency_ids,[306612,1281310]);
    assert.equal(entry.match.publicly_released,[4,10].includes(fileStatus));
    assert.ok(audit.entries.slice(1).every(row=>row.match===null&&row.journal_status===null));
    assert.equal(store.posts.length,0);assert.equal(fs.readFileSync(fx.paths.state,'utf8'),bytes);
    assert.ok(!JSON.stringify(audit).includes('offline-secret-reflection'));
    assert.ok(!JSON.stringify(audit).includes('untrusted.invalid'));
  });
});

test('future uncertain upload diagnostics retain only bounded phase, HTTP and response-shape facts and never permit a retry', async t=>{
  const secret='offline-credential-reflection-do-not-log';
  const failures={
    'fetch':{stage:'fetch',type:'TypeError',code:'ECONNRESET',fail:()=>{const error=new TypeError(secret);error.cause={code:'ECONNRESET',message:secret};throw error;}},
    'timeout':{stage:'fetch',type:'TimeoutError',fail:()=>{throw new DOMException(secret,'TimeoutError');}},
    'HTTP JSON':{stage:'http-status',status:403,jsonType:'object',fail:()=>jsonResponse({error:secret,message:secret,token:secret},403)},
    'HTTP HTML':{stage:'http-status',status:502,jsonType:'invalid',fail:()=>new Response('<html>'+secret+'</html>',{status:502,headers:{'content-type':'text/html; reflected='+secret}})},
    'invalid JSON':{stage:'response-json',type:'SyntaxError',status:200,jsonType:'invalid',fail:()=>new Response('{'+secret,{headers:{'content-type':'application/json'}})},
    'wrong ID':{stage:'upload-id',status:200,jsonType:'object',idType:'string',fail:()=>jsonResponse({id:secret,message:secret})},
    'empty response':{stage:'upload-id',status:200,jsonType:'null',fail:()=>new Response('')},
    'body read':{stage:'response-body',type:'TypeError',status:200,fail:()=>({ok:true,status:200,headers:new Headers({'content-type':'application/json'}),text:async()=>{throw new TypeError(secret);}})},
  };
  for(const platform of ['modrinth','curseforge']) for(const [name,expected] of Object.entries(failures)) await t.test(`${platform}: ${name}`,async t=>{
    const fx=fixture(t);fakeCredentials(t);let posts=0;
    fakeStore(t,fx,{intercept(call){if(call.method==='POST'){posts++;return expected.fail();}}});
    let message;await assert.rejects(main(fx.argv(platform)),error=>{message=error.message;return /Uncertain/.test(message);});
    const row=fx.state().uploads[0],diagnostic=row.request_diagnostic;
    assert.equal(row.status,'uncertain');assert.equal(row.id,undefined);assert.equal(diagnostic.schema_version,1);
    assert.equal(diagnostic.stage,expected.stage);assert.equal(diagnostic.transmission_outcome,'unknown');
    assert.equal(diagnostic.error_type,expected.type??'Error');assert.equal(diagnostic.transport_code,expected.code??null);
    assert.equal(diagnostic.http_status,expected.status);assert.equal(diagnostic.response_json_type,expected.jsonType);
    if(expected.idType) assert.equal(diagnostic.response_id_type,expected.idType);
    assert.ok(!message.includes(secret));assert.ok(!JSON.stringify(fx.state()).includes(secret));
    assert.ok(!Object.hasOwn(diagnostic,'message'));assert.ok(!Object.hasOwn(diagnostic,'body'));
    await assert.rejects(main(fx.argv(platform)),/reconcile/);assert.equal(posts,1);
    assert.deepEqual(fx.state().uploads[0],row);
  });
});

test('a native Request validation error before transmission is still conservatively uncertain and unretryable',async t=>{
  const fx=fixture(t);fakeCredentials(t);let postCalls=0,constructedRequests=0;
  fakeStore(t,fx,{intercept(call){if(call.method==='POST'){
    postCalls++;
    // Synthetic invalid header, not any real environment credential. The real
    // Request constructor throws before a request could reach any transport.
    new Request(call.url,{...call.options,headers:{'X-Api-Token':'offline-token\u0100'}});
    constructedRequests++;assert.fail('No transport must be reached');
  }}});
  await assert.rejects(main(fx.argv('curseforge')),/Uncertain/);
  assert.equal(postCalls,1);assert.equal(constructedRequests,0);
  const row=fx.state().uploads[0];assert.equal(row.status,'uncertain');assert.equal(row.request_diagnostic.stage,'fetch');
  assert.equal(row.request_diagnostic.error_type,'TypeError');assert.equal(row.request_diagnostic.transmission_outcome,'unknown');
  await assert.rejects(main(fx.argv('curseforge')),/reconcile/);assert.equal(postCalls,1);
  assert.deepEqual(fx.state().uploads[0],row);
});

test('isolation preflight distinguishes excluded rows, exact existing files and never-attempted POST candidates without mutation',async t=>{
  const fx=isolationFixture(t),bytes=fs.readFileSync(fx.paths.state,'utf8');
  const store=fakeStore(t,fx,{modrinth:fx.files.map(entry=>mrRecord(entry)),curseforge:[cfRecord(fx.files[0]),cfRecord(fx.files[2])]});
  const result=await main(fx.isolateArgs(true));
  assert.equal(result.publication_scope,'never-attempted-curseforge');
  assert.deepEqual(result.curseforge_isolation.selected_targets,fx.files.slice(1).map(entry=>entry.file_name));
  assert.equal(result.curseforge_isolation.selected_count,9);assert.equal(result.curseforge_isolation.excluded_count,1);
  assert.equal(result.curseforge_isolation.unresolved_excluded_count,1);
  assert.equal(result.curseforge_inventory.journal_sha256,hash(bytes,'sha256'));
  assert.deepEqual(result.curseforge_inventory.entries.slice(0,3).map(row=>row.isolation_class),
    ['excluded-journaled','never-attempted-post-candidate','never-attempted-exact-existing']);
  assert.equal(store.posts.length,0);assert.equal(fs.readFileSync(fx.paths.state,'utf8'),bytes);
});

test('explicit live isolation serially processes exactly nine unjournaled CF targets and keeps all MR/first CF rows unchanged',async t=>{
  const fx=isolationFixture(t),frozen=clone(fx.inputState.uploads);
  const store=fakeStore(t,fx,{curseforge:[cfRecord(fx.files[0])]});
  const result=await main(fx.isolateArgs());
  assert.equal(store.posts.length,9);assert.ok(store.posts.every(row=>row.platform==='curseforge'&&row.file!==fx.files[0].file_name));
  assert.deepEqual(store.posts.map(row=>row.file),fx.files.slice(1).map(entry=>entry.file_name));
  assert.deepEqual(fx.state().uploads.slice(0,11),frozen);
  assert.equal(result.publicly_verified,false);assert.equal(result.isolated_curseforge_verified,true);
  assert.equal(result.curseforge_isolation.selected_count,9);assert.equal(result.curseforge_isolation.excluded_count,1);
  assert.ok(fx.state().uploads.slice(11).every(row=>row.platform==='curseforge'&&row.status==='verified'));
  const postIndexes=store.requests.flatMap((row,index)=>row.method==='POST'?[index]:[]);
  for(let i=1;i<postIndexes.length;i++) assert.ok(store.requests.slice(postIndexes[i-1]+1,postIndexes[i]).some(row=>row.method==='GET'&&row.url.includes('/files?')));
  const before=fs.readFileSync(fx.paths.state,'utf8');
  await assert.rejects(main(fx.isolateArgs()),/No never-attempted/);assert.equal(store.posts.length,9);
  assert.equal(fs.readFileSync(fx.paths.state,'utf8'),before);
});

test('every journaled CF status is excluded from isolation POST and reconciliation',async t=>{
  for(const status of ['dry-run','dry-run-metadata-IDs-placeholder','in-flight','uncertain','submitted','pending-publication','verified','verified-existing']) await t.test(status,async t=>{
    const fx=isolationFixture(t);fx.inputState.uploads.push({platform:'curseforge',file:fx.files[1].file_name,
      sha256:fx.files[1].sha256,status,id:cfRecord(fx.files[1]).id});fx.saveState();
    const frozen=clone(fx.inputState.uploads),store=fakeStore(t,fx,{curseforge:[cfRecord(fx.files[0]),cfRecord(fx.files[1])]});
    const result=await main(fx.isolateArgs());assert.equal(store.posts.length,8);
    assert.ok(store.posts.every(row=>![fx.files[0].file_name,fx.files[1].file_name].includes(row.file)));
    assert.deepEqual(fx.state().uploads.slice(0,12),frozen);assert.equal(result.publicly_verified,false);
    assert.equal(result.curseforge_isolation.excluded_count,2);assert.equal(result.curseforge_isolation.selected_count,8);
  });
});

test('first new uncertain isolation outcome stops serial writes and preserves all frozen receipts',async t=>{
  const fx=isolationFixture(t),frozen=clone(fx.inputState.uploads);let posts=0;
  const store=fakeStore(t,fx,{modrinth:fx.files.map(entry=>mrRecord(entry)),intercept(call){if(call.method==='POST'){posts++;return jsonResponse({message:'offline-reflection'},502);}}});
  await assert.rejects(main(fx.isolateArgs()),/Uncertain.*"http_status":502/);
  assert.equal(posts,1);assert.deepEqual(fx.state().uploads.slice(0,11),frozen);
  assert.equal(fx.state().uploads[11].file,fx.files[1].file_name);assert.equal(fx.state().uploads[11].status,'uncertain');
  const bytes=fs.readFileSync(fx.paths.state,'utf8'),audit=await main(fx.isolateArgs(true));
  assert.equal(audit.curseforge_isolation.selected_count,8);assert.equal(audit.curseforge_isolation.unresolved_excluded_count,2);
  assert.equal(posts,1);assert.equal(store.posts.length,0);assert.equal(fs.readFileSync(fx.paths.state,'utf8'),bytes);
});

test('isolation rejects changed source, frozen receipt, incomplete MR evidence and disallowed invocation modes before POST',async t=>{
  for(const mutation of ['source','missing first','first ID','first status','first hash','missing MR','unverified MR','offline dry run','both live','MR only']) await t.test(mutation,async t=>{
    const fx=isolationFixture(t);let args=fx.isolateArgs();
    if(mutation==='source'){fx.manifest.source_commit=COMMIT;fx.files.forEach(entry=>{entry.source_commit=COMMIT;});fx.inputState.source_commit=COMMIT;fx.saveManifest();}
    if(mutation==='missing first')fx.inputState.uploads.pop();
    if(mutation==='first ID')fx.inputState.uploads.at(-1).id=1001;
    if(mutation==='first status')fx.inputState.uploads.at(-1).status='verified';
    if(mutation==='first hash')fx.inputState.uploads.at(-1).sha256='f'.repeat(64);
    if(mutation==='missing MR')fx.inputState.uploads.shift();
    if(mutation==='unverified MR')fx.inputState.uploads[0].status='pending-publication';
    if(mutation==='offline dry run')args.push('--dry-run');
    if(mutation==='both live')args[args.indexOf('--platforms')+1]='both';
    if(mutation==='MR only')args[args.indexOf('--platforms')+1]='modrinth';
    fx.saveState();const bytes=fs.readFileSync(fx.paths.state,'utf8');
    const calls=installFetch(t,()=>assert.fail('Invalid isolation must fail before any fetch'));
    await assert.rejects(main(args));assert.equal(calls.length,0);assert.equal(fs.readFileSync(fx.paths.state,'utf8'),bytes);
  });
});

test('fresh conflicts in an excluded first file are fatal before any isolated POST',async t=>{
  const fx=isolationFixture(t),frozen=clone(fx.inputState.uploads),conflict=cfRecord(fx.files[0]);
  conflict.hashes[0].value='f'.repeat(40);const store=fakeStore(t,fx,{curseforge:[conflict]});
  await assert.rejects(main(fx.isolateArgs()),/differs/);assert.equal(store.posts.length,0);
  assert.deepEqual(fx.state().uploads,frozen);
});

test('isolated uploads pending indexing/public approval cannot claim scoped or full completion',async t=>{
  const fx=isolationFixture(t),frozen=clone(fx.inputState.uploads),store=fakeStore(t,fx,{pendingIndex:true});
  await assert.rejects(main(fx.isolateArgs()),/indexing|review/);assert.equal(store.posts.length,1);
  assert.deepEqual(fx.state().uploads.slice(0,11),frozen);assert.equal(fx.state().uploads[11].status,'submitted');
  assert.equal(fx.state().publicly_verified,false);
});

test('recovery preflight requires every preserved Modrinth ID freshly exact and publicly listed', async t => {
  for (const mutation of ['missing remote', 'hidden remote', 'draft remote', 'changed remote ID', 'changed journal ID', 'missing journal row', 'wrong hash', 'wrong dependency']) await t.test(mutation, async t => {
    const fx = fixture(t); fakeCredentials(t);
    const records = fx.files.map(entry => mrRecord(entry));
    const rows = fx.files.map(entry => ({platform: 'modrinth', file: entry.file_name, sha256: entry.sha256, status: 'verified', id: mrRecord(entry).id}));
    if (mutation === 'missing remote') records.pop();
    if (mutation === 'hidden remote') records[0].status = 'unlisted';
    if (mutation === 'draft remote') records[0].status = 'draft';
    if (mutation === 'changed remote ID') records[0].id = 'OtherMRid';
    if (mutation === 'changed journal ID') rows[0].id = 'OtherMRid';
    if (mutation === 'missing journal row') rows.pop();
    if (mutation === 'wrong hash') records[0].files[0].hashes.sha512 = 'f'.repeat(128);
    if (mutation === 'wrong dependency') records[0].dependencies.pop();
    const bytes = JSON.stringify({schema_version: 1, source_commit: COMMIT, version: VERSION, uploads: rows, publicly_verified: true}, null, 2) + '\n';
    fs.writeFileSync(fx.paths.state, bytes);
    const store = fakeStore(t, fx, {modrinth: records});
    await assert.rejects(main([...fx.argv('both'), '--preflight-only', '--require-public-modrinth']), /Modrinth/);
    assert.equal(store.posts.length, 0);
    assert.equal(fs.readFileSync(fx.paths.state, 'utf8'), bytes);
  });
});
