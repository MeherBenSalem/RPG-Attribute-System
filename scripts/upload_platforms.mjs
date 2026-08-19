/**
 * Upload workspace jars to Modrinth + CurseForge from local builds.
 * Tokens: env vars or NightBeam-Knowledge-Base/secrets/local.env
 *
 * Usage:
 *   node scripts/upload_platforms.mjs --workspace 26.1.2 --version 4.2.0
 *   node scripts/upload_platforms.mjs --workspace 26.1.2 --version 4.2.0 --dry-run
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.join(__dirname, "..");

const MODRINTH_ID = "d85UTOuq";
const CURSEFORGE_ID = "1079687";
const MOD_TITLE = "RPG Attribute System";
const JAR_PREFIX = "rpg_attribute_system-";
const DEFAULT_WORKSPACE = "26.1.2";
const CURSEFORGE_GAME_ID = 432;
const RELEASE_STATE_PATH = path.join(ROOT, ".release-upload-state.json");

async function loadEnv() {
  const candidates = [
    path.join(ROOT, "secrets", "local.env"),
    path.join(process.env.USERPROFILE || "", "NightBeam-Knowledge-Base", "secrets", "local.env"),
    "C:\\Users\\mahou\\NightBeam-Knowledge-Base\\secrets\\local.env",
  ];
  for (const envPath of candidates) {
    if (!fs.existsSync(envPath)) continue;
    const text = fs.readFileSync(envPath, "utf8");
    for (const line of text.split(/\r?\n/)) {
      const m = line.match(/^([^#=]+)=(.*)$/);
      if (!m) continue;
      const k = m[1].trim();
      const v = m[2].trim();
      if (!process.env[k]) process.env[k] = v;
    }
    console.log("Loaded secrets from", envPath);
    return;
  }
}

function parseArgs(argv) {
  const out = {
    curseforgeOnly: false,
    modrinthOnly: false,
    dryRun: false,
    workspace: DEFAULT_WORKSPACE,
  };
  for (let i = 2; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--version") out.version = argv[++i];
    else if (a === "--workspace") out.workspace = argv[++i];
    else if (a === "--changelog-file") out.changelogFile = argv[++i];
    else if (a === "--curseforge-only") out.curseforgeOnly = true;
    else if (a === "--modrinth-only") out.modrinthOnly = true;
    else if (a === "--dry-run") out.dryRun = true;
    else throw new Error(`Unknown arg ${a}`);
  }
  if (!out.version) throw new Error("--version required");
  return out;
}

function parseJar(jar) {
  const n = path.basename(jar);
  let loader = "fabric";
  if (/neoforge/i.test(n)) loader = "neoforge";
  else if (/forge/i.test(n)) loader = "forge";
  else if (/fabric/i.test(n)) loader = "fabric";
  const m = n.match(
    /^rpg_attribute_system-(?:fabric|forge|neoforge)-(.+)-(\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?)\.jar$/i,
  );
  const game = m ? m[1] : null;
  return { jar, name: n, loader, game };
}

function resolveWorkspaceDir(workspace) {
  const workspaceDir = path.resolve(ROOT, workspace);
  if (!fs.existsSync(workspaceDir)) {
    throw new Error(`Workspace does not exist: ${workspace}`);
  }
  return workspaceDir;
}

function collectWorkspaceJars(workspaceDir, version) {
  // Loader jars are produced under per-loader build dirs (fabric/forge/neoforge).
  // This is intentionally broad so older workspaces (e.g. 1.20.1) that include Forge
  // still publish all variants.
  const loaderDirs = ["fabric", "forge", "neoforge"];
  const jars = [];
  for (const loaderDir of loaderDirs) {
    const libsDir = path.join(workspaceDir, loaderDir, "build", "libs");
    if (!fs.existsSync(libsDir)) continue;
    for (const name of fs.readdirSync(libsDir)) {
      if (!name.startsWith(JAR_PREFIX)) continue;
      if (!name.endsWith(`-${version}.jar`)) continue;
      if (
        name.includes("-common-") ||
        name.includes("-sources") ||
        name.includes("-javadoc") ||
        name.includes("-dev") ||
        name.includes("-shadow")
      ) {
        continue;
      }
      jars.push(path.join(libsDir, name));
    }
  }
  const deduped = [...new Set(jars)].sort();
  return deduped;
}

function readChangelog(version, changelogFile) {
  const changelogCandidates = [
    changelogFile,
    path.join(ROOT, `RPG-Attribute-System-${version}-PatchNotes.md`),
    path.join(ROOT, "PATCH_NOTES.md"),
    path.join(ROOT, "CHANGELOG.md"),
  ].filter(Boolean);
  for (const f of changelogCandidates) {
    if (fs.existsSync(f)) {
      console.log("Changelog:", f);
      return fs.readFileSync(f, "utf8");
    }
  }
  return `## ${MOD_TITLE} ${version}\n\nLocal release upload.`;
}

function loadReleaseState() {
  if (!fs.existsSync(RELEASE_STATE_PATH)) {
    return { uploads: [] };
  }
  try {
    const parsed = JSON.parse(fs.readFileSync(RELEASE_STATE_PATH, "utf8"));
    if (Array.isArray(parsed.uploads)) return parsed;
  } catch (error) {
    console.warn("Ignoring unreadable release state:", error.message);
  }
  return { uploads: [] };
}

function saveReleaseState(state) {
  fs.writeFileSync(RELEASE_STATE_PATH, `${JSON.stringify(state, null, 2)}\n`, "utf8");
}

function hasRecordedUpload(state, platform, workspace, version, parsedJar) {
  return state.uploads.some(
    (entry) =>
      entry.platform === platform &&
      entry.workspace === workspace &&
      entry.version === version &&
      entry.fileName === parsedJar.name,
  );
}

function recordUpload(state, platform, workspace, version, parsedJar, remoteId) {
  if (hasRecordedUpload(state, platform, workspace, version, parsedJar)) return;
  state.uploads.push({
    platform,
    workspace,
    version,
    loader: parsedJar.loader,
    game: parsedJar.game,
    fileName: parsedJar.name,
    remoteId,
    recordedAt: new Date().toISOString(),
  });
  saveReleaseState(state);
}

async function fetchJson(url, options = {}, allow404 = false) {
  const res = await fetch(url, options);
  if (allow404 && res.status === 404) return null;
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`${url} -> ${res.status} ${text.slice(0, 500)}`);
  }
  return text ? JSON.parse(text) : null;
}

async function fetchExistingModrinth() {
  const payload = await fetchJson(`https://api.modrinth.com/v2/project/${MODRINTH_ID}/version`);
  const versions = Array.isArray(payload) ? payload : [];
  const versionNumbers = new Set();
  const fileNames = new Set();
  for (const version of versions) {
    if (version.version_number) versionNumbers.add(version.version_number);
    for (const file of version.files || []) {
      if (file.filename) fileNames.add(file.filename);
    }
  }
  return { versionNumbers, fileNames };
}

async function fetchExistingCurseForge(apiKey) {
  const payload = await fetchJson(
    `https://api.curseforge.com/v1/mods/${CURSEFORGE_ID}/files?pageSize=200&index=0`,
    { headers: { "x-api-key": apiKey, Accept: "application/json" } },
  );
  const fileNames = new Set();
  const displayNames = new Set();
  for (const file of payload?.data || []) {
    if (file.fileName) fileNames.add(file.fileName);
    if (file.displayName) displayNames.add(file.displayName);
  }
  return { fileNames, displayNames };
}

async function fetchCurseForgeVersionContext(token, apiKey) {
  const legacyPayload = await fetchJson("https://minecraft.curseforge.com/api/game/versions", {
    headers: { "X-Api-Token": token },
  });
  const legacyFlat = Array.isArray(legacyPayload) ? legacyPayload : legacyPayload?.data || [];
  const findLegacy = (want, preferType) => {
    const hits = legacyFlat.filter((v) => v.name === want);
    if (preferType != null) {
      const hit = hits.find((v) => v.gameVersionTypeID === preferType);
      if (!hit) throw new Error(`No CurseForge legacy version for ${want} (type ${preferType})`);
      return hit.id;
    }
    if (!hits[0]) throw new Error(`No CurseForge legacy version for ${want}`);
    return hits[0].id;
  };
  const versionTypesPayload = await fetchJson(
    `https://api.curseforge.com/v1/games/${CURSEFORGE_GAME_ID}/version-types`,
    { headers: { "x-api-key": apiKey, Accept: "application/json" } },
  );
  const minecraftTypeIds = [];
  for (const type of versionTypesPayload?.data || []) {
    if (/minecraft/i.test(type.name) && !/bukkit|spigot|paper|plugin/i.test(type.name)) {
      minecraftTypeIds.push(type.id);
    }
  }
  return {
    legacyFlat,
    clientId: findLegacy("Client"),
    serverId: findLegacy("Server"),
    minecraftTypeIds: [...minecraftTypeIds, 77784, 1],
  };
}

function createModrinthDescriptor(parsedJar, version, changelog) {
  return {
    name: `${version} · ${parsedJar.loader} · ${parsedJar.game}`,
    version_number: `${version}+${parsedJar.loader}-${parsedJar.game}`,
    changelog,
    dependencies: [],
    game_versions: [parsedJar.game],
    version_type: "release",
    loaders: [parsedJar.loader],
    featured: false,
    status: "listed",
    project_id: MODRINTH_ID,
    file_parts: ["file_0"],
    primary_file: "file_0",
  };
}

function createCurseForgeDescriptor(parsedJar, version, changelog, clientId, serverId, loaderId, gameId) {
  return {
    changelog,
    changelogType: "markdown",
    displayName: `${version} · ${parsedJar.loader} · ${parsedJar.game}`,
    gameVersions: [clientId, serverId, loaderId, gameId],
    releaseType: "release",
  };
}

async function resolveCurseForgeGameId(game, apiKey, legacyFlat, minecraftTypeIds) {
  const direct = await fetchJson(
    `https://api.curseforge.com/v1/minecraft/version/${encodeURIComponent(game)}`,
    { headers: { "x-api-key": apiKey, Accept: "application/json" } },
    true,
  );
  const directId = direct?.data?.gameVersionId;
  if (directId) {
    console.log("CF game", game, "-> gameVersionId", directId);
    return directId;
  }
  const slug = game.replace(/\./g, "-");
  const candidates = legacyFlat.filter((v) => v.name === game || v.slug === slug);
  for (const typeId of minecraftTypeIds) {
    const hit = candidates.find((v) => v.gameVersionTypeID === typeId);
    if (hit) {
      console.log("CF game", game, "-> legacy id", hit.id, "type", hit.gameVersionTypeID);
      return hit.id;
    }
  }
  throw new Error(`No CurseForge game version id for ${game}`);
}

async function uploadToModrinth(parsed, workspace, version, changelog, token, dryRun, state) {
  const existing = await fetchExistingModrinth();
  let uploaded = 0;
  let skipped = 0;
  for (const parsedJar of parsed) {
    const body = createModrinthDescriptor(parsedJar, version, changelog);
    if (
      hasRecordedUpload(state, "modrinth", workspace, version, parsedJar) ||
      existing.versionNumbers.has(body.version_number) ||
      existing.fileNames.has(parsedJar.name)
    ) {
      console.log("Modrinth skip existing", body.version_number, parsedJar.name);
      skipped += 1;
      continue;
    }
    if (dryRun) {
      console.log("[dry-run] Modrinth upload", body.version_number, parsedJar.name);
      uploaded += 1;
      continue;
    }
    const form = new FormData();
    form.append("data", JSON.stringify(body));
    form.append("file_0", new Blob([fs.readFileSync(parsedJar.jar)]), parsedJar.name);
    const res = await fetch("https://api.modrinth.com/v2/version", {
      method: "POST",
      headers: { Authorization: token },
      body: form,
    });
    const text = await res.text();
    if (!res.ok) {
      throw new Error(`Modrinth ${res.status} ${parsedJar.name} ${text.slice(0, 500)}`);
    }
    let remoteId = null;
    try {
      remoteId = JSON.parse(text).id ?? null;
    } catch {}
    recordUpload(state, "modrinth", workspace, version, parsedJar, remoteId);
    console.log("Modrinth OK", body.version_number, parsedJar.name);
    uploaded += 1;
  }
  console.log("Modrinth result:", uploaded, "upload(s),", skipped, "skip(s)");
}

async function uploadToCurseForge(parsed, workspace, version, changelog, token, apiKey, dryRun, state) {
  const LOADER_IDS = { fabric: 7499, forge: 7498, neoforge: 10150 };
  const existing = await fetchExistingCurseForge(apiKey);
  const ctx = await fetchCurseForgeVersionContext(token, apiKey);
  let uploaded = 0;
  let skipped = 0;
  for (const parsedJar of parsed) {
    const loaderId = LOADER_IDS[parsedJar.loader];
    if (!loaderId) throw new Error(`Unknown loader for ${parsedJar.name}`);
    const gameId = await resolveCurseForgeGameId(
      parsedJar.game,
      apiKey,
      ctx.legacyFlat,
      ctx.minecraftTypeIds,
    );
    const meta = createCurseForgeDescriptor(
      parsedJar,
      version,
      changelog,
      ctx.clientId,
      ctx.serverId,
      loaderId,
      gameId,
    );
    if (
      hasRecordedUpload(state, "curseforge", workspace, version, parsedJar) ||
      existing.fileNames.has(parsedJar.name) ||
      existing.displayNames.has(meta.displayName)
    ) {
      console.log("CurseForge skip existing", meta.displayName, parsedJar.name);
      skipped += 1;
      continue;
    }
    if (dryRun) {
      console.log("[dry-run] CurseForge upload", meta.displayName, parsedJar.name);
      uploaded += 1;
      continue;
    }
    const cfForm = new FormData();
    cfForm.append("metadata", JSON.stringify(meta));
    cfForm.append("file", new Blob([fs.readFileSync(parsedJar.jar)]), parsedJar.name);
    let lastText = "";
    for (let attempt = 1; attempt <= 4; attempt++) {
      const res = await fetch(
        `https://minecraft.curseforge.com/api/projects/${CURSEFORGE_ID}/upload-file`,
        { method: "POST", headers: { "X-Api-Token": token }, body: cfForm },
      );
      lastText = await res.text();
      if (res.ok) {
        let remoteId = null;
        try {
          remoteId = JSON.parse(lastText).id ?? null;
        } catch {}
        recordUpload(state, "curseforge", workspace, version, parsedJar, remoteId);
        console.log("CurseForge OK", meta.displayName, parsedJar.name, lastText.slice(0, 120));
        uploaded += 1;
        break;
      }
      if ((res.status === 429 || res.status === 503) && attempt < 4) {
        console.warn("CurseForge", res.status, "for", parsedJar.name, "- retry", attempt);
        await new Promise((resolve) => setTimeout(resolve, 15000 * attempt));
        continue;
      }
      throw new Error(`CurseForge ${res.status} ${parsedJar.name} ${lastText.slice(0, 500)}`);
    }
  }
  console.log("CurseForge result:", uploaded, "upload(s),", skipped, "skip(s)");
}

async function main() {
  await loadEnv();
  const args = parseArgs(process.argv);
  const version = args.version.replace(/^v/, "");
  const workspaceDir = resolveWorkspaceDir(args.workspace);
  const changelog = readChangelog(version, args.changelogFile);
  const state = loadReleaseState();
  const jars = collectWorkspaceJars(workspaceDir, version);
  if (!jars.length) {
    throw new Error(`No ${JAR_PREFIX}*-${version}.jar in ${path.relative(ROOT, workspaceDir)} build outputs`);
  }
  const parsed = jars.map(parseJar);
  for (const parsedJar of parsed) {
    if (!parsedJar.game) throw new Error(`Cannot parse MC version from ${parsedJar.name}`);
  }
  const loaders = new Set(parsed.map((parsedJar) => parsedJar.loader));
  if (args.workspace === DEFAULT_WORKSPACE) {
    if (parsed.length !== 2 || !loaders.has("fabric") || !loaders.has("neoforge")) {
      throw new Error(
        `Expected exactly one Fabric jar and one NeoForge jar for ${DEFAULT_WORKSPACE}; found ${parsed.map((p) => p.name).join(", ")}`,
      );
    }
  }

  const { MODRINTH_TOKEN, CURSEFORGE_TOKEN, CURSEFORGE_API_KEY } = process.env;
  if (!MODRINTH_TOKEN || !CURSEFORGE_TOKEN || !CURSEFORGE_API_KEY) {
    throw new Error("Set MODRINTH_TOKEN, CURSEFORGE_TOKEN, CURSEFORGE_API_KEY (or secrets/local.env)");
  }

  console.log(
    `Uploading ${MOD_TITLE} ${version} from ${path.relative(ROOT, workspaceDir)} (${parsed.length} jars)`,
  );
  for (const parsedJar of parsed) {
    console.log("  ", parsedJar.name, "->", parsedJar.loader, parsedJar.game);
  }

  if (!args.curseforgeOnly) {
    await uploadToModrinth(
      parsed,
      args.workspace,
      version,
      changelog,
      MODRINTH_TOKEN,
      args.dryRun,
      state,
    );
  }

  if (!args.modrinthOnly) {
    await uploadToCurseForge(
      parsed,
      args.workspace,
      version,
      changelog,
      CURSEFORGE_TOKEN,
      CURSEFORGE_API_KEY,
      args.dryRun,
      state,
    );
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
