# Local upload to Modrinth + CurseForge (no GitHub Actions).
# Tokens from env or C:\Users\mahou\NightBeam-Knowledge-Base\secrets\local.env
#
# Usage:
#   .\upload_local.ps1 -Version 4.2.0
#   .\upload_local.ps1 -Version 4.2.0 -DryRun

param(
    [string]$Version = "4.2.0",
    [switch]$CurseForgeOnly,
    [switch]$ModrinthOnly,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$notes = Join-Path $root "RPG-Attribute-System-$Version-PatchNotes.md"
if (-not (Test-Path $notes)) { $notes = Join-Path $root "PATCH_NOTES.md" }

$nodeArgs = @("scripts/upload_platforms.mjs", "--version", $Version, "--changelog-file", $notes)
if ($CurseForgeOnly) { $nodeArgs += "--curseforge-only" }
if ($ModrinthOnly) { $nodeArgs += "--modrinth-only" }
if ($DryRun) { $nodeArgs += "--dry-run" }

Write-Host "=== Local upload RPG Attribute System v$Version ===" -ForegroundColor Green
node @nodeArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done. Verify:" -ForegroundColor Cyan
Write-Host "  https://modrinth.com/mod/rpg-attribute-system/versions"
Write-Host "  https://www.curseforge.com/minecraft/mc-mods/rpg-attribute-system/files"
