# Slice Pixel RPG UI Pack assets for RAS book GUI (Windows wrapper).
param(
    [string]$Source = "",
    [string]$Out = "",
    [switch]$Synthetic
)

$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Py = Get-Command python -ErrorAction SilentlyContinue
if (-not $Py) { $Py = Get-Command python3 -ErrorAction SilentlyContinue }
if (-not $Py) { throw "Python 3 with Pillow is required. Run: pip install Pillow" }

$ArgsList = @("$Root\scripts\slice-pixel-ui.py")
if ($Source) { $ArgsList += @("--source", $Source) }
if ($Out) { $ArgsList += @("--out", $Out) }
if ($Synthetic) { $ArgsList += "--synthetic" }

& $Py.Source @ArgsList
exit $LASTEXITCODE
