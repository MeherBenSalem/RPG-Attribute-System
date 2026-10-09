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
function mrRecord(entry, id = `mr-${entry.loader}-${entry.minecraft}`) {
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
function jsonResponse(value, status = 200) {
  return new Response(JSON.stringify(value), {status, headers: {'content-type': 'application/json'}});
}
function installFetch(t, handler) {
  const requests = [];
  globalThis.fetch = async (url, options = {}) => {
    const value = String(url);
    const parsed = new URL(value);
    assert.ok(['api.modrinth.com', 'api.curseforge.com', 'minecraft.curseforge.com'].includes(parsed.hostname),
      `Unexpected network destination: ${value}`);
    const call = {url: value, method: options.method || 'GET', options};
    requests.push(call);
    return await handler(call, requests);
  };
  t.after(() => {globalThis.fetch = forbiddenFetch;});
  return requests;
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
      return jsonResponse({data: remote.curseforge.slice(offset, offset + 50), pagination: {totalCount: remote.curseforge.length}});
    }
    if (call.method === 'GET' && call.url === 'https://minecraft.curseforge.com/api/game/versions') {
      const data = [
        {name: 'Client', id: IDS.client}, {name: 'Server', id: IDS.server},
        ...Object.entries(LOADER_NAMES).map(([loader, name]) => ({name, id: IDS.loader[loader]})),
        ...Object.entries(IDS.minecraft).map(([name, id]) => ({name, id})),
      ];
      return jsonResponse(data);
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
  const unrelated = Array.from({length: 50}, (_, index) => ({id: index + 1, fileName: `historical-${index}.jar`, displayName: `Historical ${index}`}));
  const store = fakeStore(t, fx, {curseforge: [...unrelated, ...fx.files.map(entry => cfRecord(entry))]});
  const state = await main(fx.argv('curseforge'));
  assert.equal(store.posts.length, 0);
  assert.equal(state.uploads.length, 10);
  assert.ok(store.requests.some(call => call.url.includes('index=50')));
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
