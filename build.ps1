# TianQiWeather build helper
# Usage:
#   .\build.ps1                                  # assemblePublicRelease
#   .\build.ps1 -Task assembleDebug
#   .\build.ps1 -Task assembleRelease            # maintainer-local build
#   .\build.ps1 -Task 'testDebugUnitTest lintDebug'      # tests + lint

[CmdletBinding()]
param(
    [string]$Task = 'assemblePublicRelease',
    [string]$JdkDir = $env:JAVA_HOME
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($JdkDir)) {
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($java) {
        $JdkDir = Split-Path -Parent (Split-Path -Parent $java.Source)
    }
}
if ([string]::IsNullOrWhiteSpace($JdkDir) -or
    -not (Test-Path -LiteralPath (Join-Path $JdkDir 'bin\java.exe') -PathType Leaf)) {
    Write-Error 'JDK 17 not found. Set JAVA_HOME or pass -JdkDir before running this script.'
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

# Use cmd's native redirection so Gradle output remains readable under Windows PowerShell 5.1.
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
