/** Strict joins between the documented Upload and Core catalog namespaces.
 * IDs are resolved afresh, never copied from a fixture or selected by row order.
 * https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api
 * https://docs.curseforge.com/rest-api/#get-specific-minecraft-version
 * https://docs.curseforge.com/rest-api/#get-version-types
 * https://docs.curseforge.com/rest-api/#get-versions---v2
 */
const id = value => Number.isSafeInteger(value) && value > 0 && value <= 0xffffffff;
const names = {fabric: 'Fabric', forge: 'Forge', neoforge: 'NeoForge'};
const minecraft = ['1.20.1','1.21.1','26.1.2','26.2','26.3'];
const unique = (rows, message) => {
  if (rows.length !== 1) throw new Error('Missing/ambiguous CurseForge ' + message);
  return rows[0];
};
const array = (value, limit, message) => {
  if (!Array.isArray(value) || value.length > limit) throw new Error('Malformed/oversized CurseForge ' + message);
  return value;
};

export function resolveCurseForgeCatalog(uploadPayload, canonical, typesPayload, groupsPayload, entries) {
  const upload = array(Array.isArray(uploadPayload) ? uploadPayload : uploadPayload?.data, 100000, 'upload catalog');
  const types = array(typesPayload?.data, 512, 'version types');
  const groups = array(groupsPayload?.data, 512, 'Core version groups');
  const typeIds = new Set();
  for (const type of types) {
    if (!type || !id(type.id) || typeIds.has(type.id) || type.gameId !== 432
        || typeof type.name !== 'string' || typeof type.slug !== 'string' || ![1,2].includes(type.status))
      throw new Error('Malformed/duplicate CurseForge namespace type');
    typeIds.add(type.id);
  }
  const groupIds = new Set(), core = [];
  for (const group of groups) {
    if (!group || !id(group.type) || !typeIds.has(group.type) || groupIds.has(group.type))
      throw new Error('Malformed/duplicate CurseForge Core namespace group');
    groupIds.add(group.type);
    const versions = array(group.versions, 100000, 'Core version group');
    if (core.length + versions.length > 100000) throw new Error('Oversized CurseForge Core catalog');
    for (const row of versions) {
      if (!row || !id(row.id) || typeof row.name !== 'string') throw new Error('Malformed CurseForge Core version');
      core.push({...row, gameVersionTypeId: group.type});
    }
  }
  for (const row of upload) {
    if (!row || !id(row.id) || !id(row.gameVersionTypeID) || typeof row.name !== 'string')
      throw new Error('Malformed CurseForge upload version');
  }
  function namespace(name, slug) {
    const type = unique(types.filter(row => row.name === name && row.slug === slug), 'namespace: ' + name);
    if (type.status !== 1) throw new Error('Inactive CurseForge namespace: ' + name);
    return type.id;
  }
  function join(name, typeId, expectedId) {
    const uploaded = unique(upload.filter(row => row.name === name && row.gameVersionTypeID === typeId), 'upload version/type: ' + name);
    const indexed = unique(core.filter(row => row.name === name && row.gameVersionTypeId === typeId), 'Core version/type: ' + name);
    if (uploaded.id !== indexed.id || (expectedId !== undefined && uploaded.id !== expectedId))
      throw new Error('CurseForge canonical/name/type/ID mismatch: ' + name);
    return uploaded.id;
  }
  const loaderType = namespace('Modloader', 'modloader'), environmentType = namespace('Environment', 'environment');
  const result = {client: join('Client', environmentType), server: join('Server', environmentType), loader: {}, minecraft: {}};
  for (const [loader, name] of Object.entries(names)) result.loader[loader] = join(name, loaderType);
  const targets = [...new Set(entries.map(entry => entry.minecraft))];
  for (const name of targets) {
    if (!minecraft.includes(name)) throw new Error('Unreviewed CurseForge Minecraft target');
    const record = canonical?.[name]?.data;
    if (!record || record.versionString !== name || !id(record.id) || !id(record.gameVersionId) || !id(record.gameVersionTypeId)
        || typeof record.approved !== 'boolean' || record.gameVersionStatus !== 1 || record.gameVersionTypeStatus !== 1)
      throw new Error('Malformed/inactive CurseForge canonical Minecraft version: ' + name);
    const type = unique(types.filter(row => row.id === record.gameVersionTypeId), 'canonical namespace: ' + name);
    const expectedSlug = 'minecraft-' + name.split('.').slice(0,2).join('-');
    if (type.status !== 1 || type.slug !== expectedSlug) throw new Error('CurseForge canonical Minecraft namespace differs: ' + name);
    // The service currently returns approved:false with documented Approved (1)
    // and Normal (1) status enums. The boolean is retained as schema evidence;
    // it is not assigned an undocumented publication-eligibility meaning.
    result.minecraft[name] = join(name, record.gameVersionTypeId, record.gameVersionId);
  }
  const selected = [result.client,result.server,...Object.values(result.loader),...Object.values(result.minecraft)];
  if (new Set(selected).size !== selected.length) throw new Error('Overlapping CurseForge selected IDs');
  return result;
}

export async function curseForgeIds(token, apiKey, entries, request, capture = () => {}) {
  const uploadHeaders = {'X-Api-Token': token}, coreHeaders = {'x-api-key': apiKey};
  const get = (url, headers) => request(url, {headers, redirect: 'error'});
  const project = await get('https://api.curseforge.com/v1/mods/1079687', coreHeaders);
  const game = await get('https://api.curseforge.com/v1/games/432', coreHeaders);
  if (project?.data?.id !== 1079687 || project?.data?.gameId !== 432 || game?.data?.id !== 432 || game?.data?.slug !== 'minecraft')
    throw new Error('CurseForge release project/game namespace differs');
  const upload = await get('https://minecraft.curseforge.com/api/game/versions', uploadHeaders);
  const types = await get('https://api.curseforge.com/v1/games/432/version-types', coreHeaders);
  const groups = await get('https://api.curseforge.com/v2/games/432/versions', coreHeaders);
  const canonical = {};
  for (const name of [...new Set(entries.map(entry => entry.minecraft))]) {
    if (!minecraft.includes(name)) throw new Error('Unreviewed CurseForge Minecraft target');
    canonical[name] = await get('https://api.curseforge.com/v1/minecraft/version/' + name, coreHeaders);
  }
  const ids = resolveCurseForgeCatalog(upload, canonical, types, groups, entries);
  capture({selected_ids: ids, canonical_minecraft: Object.fromEntries(Object.entries(canonical).map(([name, payload]) =>
    [name, Object.fromEntries(['id','gameVersionId','versionString','gameVersionTypeId','approved','gameVersionStatus','gameVersionTypeStatus']
      .map(field => [field, payload.data[field]]))]))});
  return ids;
}
