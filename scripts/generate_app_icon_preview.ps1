[CmdletBinding()]
param(
    [ValidateRange(128, 2048)]
    [int]$Size = 512,
    [ValidateRange(0.0, 0.3)]
    [double]$Inset = 0.14
)

$ErrorActionPreference = 'Stop'
$ProjectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$SourcePath = Join-Path $ProjectRoot 'assets\branding\ic_launcher_character_source.png'
$OutputPath = Join-Path $ProjectRoot 'assets\app-icon.png'

if (-not (Test-Path -LiteralPath $SourcePath -PathType Leaf)) {
    throw "Missing character icon source: $SourcePath"
}

Add-Type -AssemblyName System.Drawing

$source = [Drawing.Image]::FromFile($SourcePath)
$canvas = New-Object Drawing.Bitmap(
    $Size,
    $Size,
    [Drawing.Imaging.PixelFormat]::Format32bppArgb
)
$graphics = [Drawing.Graphics]::FromImage($canvas)

try {
    $graphics.Clear([Drawing.Color]::Black)
    $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceOver
    $graphics.CompositingQuality = [Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::HighQuality

    $padding = [int][Math]::Round($Size * $Inset)
    $contentSize = $Size - (2 * $padding)
    $target = New-Object Drawing.Rectangle($padding, $padding, $contentSize, $contentSize)
    $graphics.DrawImage($source, $target)

    $canvas.Save($OutputPath, [Drawing.Imaging.ImageFormat]::Png)
}
finally {
    $graphics.Dispose()
    $canvas.Dispose()
    $source.Dispose()
}

Write-Host "Generated README app icon: $OutputPath ($Size x $Size, inset $Inset)"
