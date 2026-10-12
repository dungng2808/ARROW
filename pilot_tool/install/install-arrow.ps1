# Cài lệnh `arrow` cho Windows: tạo %USERPROFILE%\.arrow\bin\arrow.cmd và thêm thư mục đó vào PATH của người dùng.
# Chạy: powershell -ExecutionPolicy Bypass -File install\install-arrow.ps1
# Chạy lại nếu chuyển thư mục ARROW sang chỗ khác. Sau git pull không cần chạy lại.
$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$bin = Join-Path $env:USERPROFILE ".arrow\bin"
New-Item -ItemType Directory -Force -Path $bin | Out-Null
$launcher = @"
@echo off
set "ARROW_PILOT_ROOT=$root"
dotnet run --project "$root\src\Arrow.Cli" --nologo -- %*
"@
Set-Content -Path (Join-Path $bin "arrow.cmd") -Value $launcher -Encoding ASCII

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if (-not ($userPath -split ";" | Where-Object { $_ -eq $bin })) {
    [Environment]::SetEnvironmentVariable("Path", "$bin;$userPath", "User")
    Write-Host "Đã thêm $bin vào PATH. Mở terminal mới để dùng lệnh arrow."
}

Write-Host "Đã cài lệnh arrow -> $bin\arrow.cmd (pilot_tool: $root)"
Write-Host "Thử: arrow --help"
