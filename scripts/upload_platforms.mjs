#!/usr/bin/env node
// Legacy per-workspace uploads could publish partial/unverified binaries. Fail closed.
console.error('Direct per-workspace uploads are disabled. Use the Publish workflow or scripts/publish-verified-release.mjs with a complete source-bound verified ten-JAR manifest. See docs/releasing.md.');
process.exitCode = 1;
