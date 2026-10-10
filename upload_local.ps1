# Compatibility entry point for legacy local uploads. No build or upload is performed.
# Use the verified full-inventory publisher described in docs/releasing.md.
param(
    [string]$Version = "",
    [string]$Workspace = "26.1.2",
    [switch]$CurseForgeOnly,
    [switch]$ModrinthOnly,
    [switch]$DryRun
)

throw "Direct per-workspace uploads are disabled. Use the Publish workflow or scripts/publish-verified-release.mjs with a complete source-bound verified ten-JAR manifest and retained upload journal. See docs/releasing.md."
