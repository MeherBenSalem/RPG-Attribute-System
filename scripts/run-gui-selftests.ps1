# Run RAS in-game GUI self-tests for all six loaders (demo mode, auto-exit).
$ErrorActionPreference = 'Continue'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$repo = Split-Path -Parent $root
$env:Path = [System.Environment]::GetEnvironmentVariable('Path','Machine') + ';' + [System.Environment]::GetEnvironmentVariable('Path','User')

$tests = @(
    @{ Name = '1.20.1 Fabric';  Dir = '1.20.1'; Task = ':fabric:runClientSelfTest';   Jdk = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'; Result = 'fabric\run-selftest\ras-gui-selftest-result.txt' },
    @{ Name = '1.20.1 Forge';   Dir = '1.20.1'; Task = ':forge:runClientSelfTest';    Jdk = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'; Result = 'forge\runs\client-selftest\ras-gui-selftest-result.txt' },
    @{ Name = '1.21.1 Fabric';  Dir = '1.21.1'; Task = ':fabric:runClientSelfTest';   Jdk = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'; Result = 'fabric\runs\client-selftest\ras-gui-selftest-result.txt' },
    @{ Name = '1.21.1 NeoForge'; Dir = '1.21.1'; Task = ':neoforge:runClientSelfTest'; Jdk = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'; Result = 'neoforge\runs\client-selftest\ras-gui-selftest-result.txt' },
    @{ Name = '26.2 Fabric';    Dir = '26.2';   Task = ':fabric:runClientSelfTest';   Jdk = 'C:\Program Files\Java\jdk-25.0.3'; Result = 'fabric\runs\client-selftest\ras-gui-selftest-result.txt' },
    @{ Name = '26.2 NeoForge';  Dir = '26.2';   Task = ':neoforge:runClientSelfTest'; Jdk = 'C:\Program Files\Java\jdk-25.0.3'; Result = 'neoforge\runs\client-selftest\ras-gui-selftest-result.txt' }
)

$summary = @()
foreach ($t in $tests) {
    Write-Host "`n========== $($t.Name) ==========" -ForegroundColor Cyan
    $env:JAVA_HOME = $t.Jdk
    Push-Location (Join-Path $repo $t.Dir)
    & .\gradlew.bat $t.Task --no-daemon 2>&1 | Out-Null
    $code = $LASTEXITCODE
    Pop-Location

    $resultPath = Join-Path (Join-Path $repo $t.Dir) $t.Result
    $line = if (Test-Path $resultPath) { (Get-Content $resultPath -Raw).Trim() } else { 'NO_RESULT_FILE' }
    $summary += [pscustomobject]@{ Loader = $t.Name; Exit = $code; Result = $line }
    Write-Host $line
}

Write-Host "`n========== SUMMARY ==========" -ForegroundColor Green
$summary | Format-Table -AutoSize
$fail = $summary | Where-Object { $_.Result -notmatch '^PASS' }
if ($fail) { exit 1 }
exit 0
