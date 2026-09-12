#!/usr/bin/env node
'use strict';

/**
 * Detect loader + Minecraft game version from MultiLoader release jar names.
 *
 * Current convention (archivesName + version):
 * rpg_attribute_system-{loader}-{mcVersion}-{modVersion}.jar
 *
 * Minecraft versions may be classic (1.21.1) or calendar-style (26.1.2, 26.2).
 */

const path = require('path');

const LOADER_IDS = { fabric: 7499, forge: 7498, neoforge: 10150, quilt: 9153 };

const MC = String.raw`(26(?:\.\d+)+|1\.\d+(?:\.\d+)?)`;
const LOADER = String.raw`(?:fabric|neoforge|forge|quilt)`;
const MODVER = String.raw`\d+\.\d+\.\d+`;

/** @param {string} jar path or basename */
function detect(jar) {
  const n = path.basename(jar).toLowerCase();
  let loader = 'fabric';
  if (n.includes('neoforge')) loader = 'neoforge';
  else if (n.includes('forge')) loader = 'forge';
  else if (n.includes('quilt')) loader = 'quilt';

  const current = n.match(new RegExp(`-${LOADER}-(${MC})-${MODVER}\\.jar$`, 'i'));
  if (current) {
    return { loader, gv: current[1] };
  }

  const legacy = n.match(new RegExp(`-${LOADER}-(${MC})\\.jar$`, 'i'));
  if (legacy) {
    return { loader, gv: legacy[1] };
  }

  return { loader, gv: null };
}

/**
 * @param {string} jar
 * @returns {{ loader: string, gv: string }}
 */
function detectRequired(jar) {
  const { loader, gv } = detect(jar);
  if (gv) return { loader, gv };
  throw new Error(
    `Could not detect Minecraft version from jar name: ${path.basename(jar)}. ` +
      `Expected rpg_attribute_system-{loader}-{mcVersion}-{modVersion}.jar`,
  );
}

module.exports = { detect, detectRequired, LOADER_IDS };

if (require.main === module) {
  const jar = process.argv[2];
  if (!jar) {
    console.error('Usage: detect-jar-meta.js <jar>');
    process.exit(2);
  }
  console.log(JSON.stringify(detectRequired(jar)));
}
