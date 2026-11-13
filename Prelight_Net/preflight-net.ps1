# Build once with tools/dotnet-local.ps1, then forward all CLI arguments.
$preflightExecutable = Join-Path $PSScriptRoot 'src/Preflight.Cli/bin/Release/net8.0/preflight-net.dll'
if (-not (Test-Path -LiteralPath $preflightExecutable)) { throw 'Build PreflightNet.sln -c Release first.' }
& dotnet $preflightExecutable @args
exit $LASTEXITCODE
