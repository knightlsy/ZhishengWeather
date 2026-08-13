# ZhishengWeather 0.0.4 build helper
# Usage:
#   .\build.ps1                                  # assembleRelease (full build, private signing)
#   .\build.ps1 -Task assembleDebug
#   .\build.ps1 -Task 'assembleRelease -PpublicBuild'   # public version, no credentials
#   .\build.ps1 -Task 'testDebugUnitTest lintDebug'      # tests + lint
#
# Why this script exists:
#   1. JAVA_HOME must be a pure-ASCII path. The original
#      `D:\金川党建数据大屏项目\tools\jdk-17.0.13+11` gets mangled when gradlew.bat
#      forwards it as an env var, and gradle rejects it as "invalid directory".
#      We pin it to the English-only tool copy at D:\android-build-tools\...
#   2. `--no-daemon` keeps gradle from forking a long-lived daemon that the
#      calling shell may kill. Slightly slower on cold start, but robust.
#   3. Log goes to build.log in the project root for later inspection.

[CmdletBinding()]
param(
    [string]$Task = 'assembleRelease'
)

$ErrorActionPreference = 'Continue'

# Pin JAVA_HOME to the pure-ASCII JDK copy (created 2026-08-11).
$JdkDir = 'D:\android-build-tools\jdk-17.0.13+11'
if (-not (Test-Path (Join-Path $JdkDir 'bin\java.exe'))) {
    Write-Error "JAVA_HOME target not found: $JdkDir`nBuild it first or copy the JDK there."
    exit 2
}
$env:JAVA_HOME = $JdkDir

# Run from the script's own directory (the project root).
Set-Location -LiteralPath $PSScriptRoot

$LogPath = Join-Path $PSScriptRoot 'build.log'
"=== build $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')  task=$Task  JAVA_HOME=$JdkDir ===" |
    Out-File -FilePath $LogPath -Encoding utf8

Write-Host ">> JAVA_HOME = $env:JAVA_HOME"
Write-Host ">> task      = $Task"
Write-Host ">> log       = $LogPath"

$gradlew = Join-Path $PSScriptRoot 'gradlew.bat'
if (-not (Test-Path $gradlew)) {
    Write-Error "gradlew.bat not found at $gradlew"
    exit 2
}

# Use cmd's native redirection so gradle's stdout+stderr land in build.log
# byte-for-byte (PowerShell's Tee-Object/Out-File mangles native output into
# one-char-per-line objects under PS 5.1, making the log unreadable).
$taskArgs = $Task -split '\s+'
$argList = ($taskArgs + @('--no-daemon', '--console=plain')) -join ' '
$cmdLine = "`"$gradlew`" $argList > `"$LogPath`" 2>&1"
cmd /c $cmdLine
$code = $LASTEXITCODE
"`n=== EXIT=$code  $(Get-Date -Format 'HH:mm:ss') ===" |
    Out-File -FilePath $LogPath -Append -Encoding utf8

Write-Host ""
Write-Host ">> gradle exit = $code"
exit $code
