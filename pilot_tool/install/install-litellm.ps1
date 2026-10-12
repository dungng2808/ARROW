# Cài LiteLLM proxy vào ARROW\litellm (môi trường Python riêng, không commit) cho Windows.
# Chạy: powershell -ExecutionPolicy Bypass -File install\install-litellm.ps1
# Phiên bản theo Report 2: LiteLLM 1.91.0.
$ErrorActionPreference = "Stop"
$version = if ($env:LITELLM_VERSION) { $env:LITELLM_VERSION } else { "1.91.0" }
$arrowRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$target = Join-Path $arrowRoot "litellm"

$python = $null
foreach ($candidate in @("py -3.12", "py -3.11", "py -3.10", "python")) {
    $parts = $candidate.Split(" ")
    try {
        & $parts[0] $parts[1..($parts.Length - 1)] -c "import sys; sys.exit(0 if sys.version_info >= (3, 10) else 1)" 2>$null
        if ($LASTEXITCODE -eq 0) { $python = $parts; break }
    } catch { }
}
if (-not $python) { throw "Cần Python 3.10 trở lên (https://www.python.org/downloads/)." }

& $python[0] $python[1..($python.Length - 1)] -m venv --clear $target
& "$target\Scripts\python.exe" -m pip install --upgrade pip | Out-Null
& "$target\Scripts\python.exe" -m pip install "litellm[proxy]==$version"
Write-Host "Đã cài LiteLLM vào $target\Scripts\litellm.exe (pilot_tool tự tìm ở đây; chạy lại: arrow doctor)"
