param([string]$ProjectRoot = (Get-Location).Path)

Add-Type -AssemblyName System.Drawing

$outputDir = Join-Path $projectRoot "app\src\main\res\drawable-nodpi"
[System.IO.Directory]::CreateDirectory($outputDir) | Out-Null

$bg = [System.Drawing.ColorTranslator]::FromHtml("#1F232B")
$border = [System.Drawing.ColorTranslator]::FromHtml("#46505B")
$orange = [System.Drawing.ColorTranslator]::FromHtml("#FFA047")
$cyan = [System.Drawing.ColorTranslator]::FromHtml("#75EAF3")
$white = [System.Drawing.ColorTranslator]::FromHtml("#F4F8F4")
$secondary = [System.Drawing.ColorTranslator]::FromHtml("#A9B5AC")
$rule = [System.Drawing.ColorTranslator]::FromHtml("#303841")

function New-Font([float]$size, [System.Drawing.FontStyle]$style = [System.Drawing.FontStyle]::Regular) {
    return [System.Drawing.Font]::new("Microsoft YaHei UI", $size, $style, [System.Drawing.GraphicsUnit]::Pixel)
}

function Draw-RoundedPanel($graphics, [int]$width, [int]$height, [int]$radius) {
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $diameter = $radius * 2
    $path.AddArc(1, 1, $diameter, $diameter, 180, 90)
    $path.AddArc($width - $diameter - 2, 1, $diameter, $diameter, 270, 90)
    $path.AddArc($width - $diameter - 2, $height - $diameter - 2, $diameter, $diameter, 0, 90)
    $path.AddArc(1, $height - $diameter - 2, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    $fill = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        [System.Drawing.Rectangle]::new(0, 0, $width, $height),
        [System.Drawing.ColorTranslator]::FromHtml("#0B1118"),
        [System.Drawing.ColorTranslator]::FromHtml("#202730"),
        35
    )
    $stroke = [System.Drawing.Pen]::new($border, 2)
    $graphics.FillPath($fill, $path)
    $graphics.DrawPath($stroke, $path)
    $fill.Dispose()
    $stroke.Dispose()
    $path.Dispose()
}

function Draw-Text($graphics, [string]$text, [float]$x, [float]$y, [float]$size, $color, [bool]$bold = $false) {
    $style = if ($bold) { [System.Drawing.FontStyle]::Bold } else { [System.Drawing.FontStyle]::Regular }
    $font = New-Font $size $style
    $brush = [System.Drawing.SolidBrush]::new($color)
    $graphics.DrawString($text, $font, $brush, $x, $y)
    $font.Dispose()
    $brush.Dispose()
}

function Draw-Crescent($graphics, [float]$x, [float]$y, [float]$size) {
    $cyanBrush = [System.Drawing.SolidBrush]::new($cyan)
    $bgBrush = [System.Drawing.SolidBrush]::new($bg)
    $graphics.FillEllipse($cyanBrush, $x, $y, $size, $size)
    $graphics.FillEllipse($bgBrush, $x + $size * 0.28, $y - $size * 0.08, $size * 0.92, $size * 0.92)
    $graphics.FillEllipse($cyanBrush, $x + $size * 0.78, $y + $size * 0.18, 5, 5)
    $graphics.FillEllipse($cyanBrush, $x + $size * 0.68, $y + $size * 0.32, 3, 3)
    $cyanBrush.Dispose()
    $bgBrush.Dispose()
}

function New-Preview([string]$name, [int]$width, [int]$height, [ValidateSet("small", "medium", "large")]$kind) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    Draw-RoundedPanel $graphics $width $height ([Math]::Round([Math]::Min($width, $height) * 0.07))

    $pad = [Math]::Round([Math]::Min($width, $height) * 0.07)
    $accentBrush = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accentBrush, $pad, $pad, 8, 42)
    $accentBrush.Dispose()
    Draw-Text $graphics "金川区" ($pad + 20) ($pad - 4) 30 $orange $true
    Draw-Text $graphics "8月12日 周三" ($width - $pad - 170) ($pad + 2) 20 $cyan

    if ($kind -eq "small") {
        Draw-Text $graphics "19°" $pad 90 94 $white $true
        Draw-Crescent $graphics ($width - $pad - 78) 112 70
        Draw-Text $graphics "晴  ·  29° / 15°" $pad 225 25 $white $true
        Draw-Text $graphics "体感 19°  ·  湿度 52%" $pad 270 20 $secondary
    } elseif ($kind -eq "medium") {
        $liveBrush = [System.Drawing.SolidBrush]::new($cyan)
        $graphics.FillEllipse($liveBrush, $width - $pad - 14, $pad + 13, 7, 7)
        $liveBrush.Dispose()
        $split = [Math]::Round($width * 0.48)
        $pen = [System.Drawing.Pen]::new($rule, 2)
        $graphics.DrawLine($pen, $split, 92, $split, $height - $pad)
        $pen.Dispose()
        Draw-Text $graphics "19°" $pad 93 88 $white $true
        Draw-Crescent $graphics ($split - 100) 120 64
        Draw-Text $graphics "晴  ·  29° / 15°" $pad 232 24 $white $true
        Draw-Text $graphics "体感 19°  ·  湿度 52%" $pad 277 18 $secondary
        $hours = @("2时", "3时", "4时", "5时")
        $temps = @("18°", "17°", "16°", "15°")
        for ($i = 0; $i -lt 4; $i++) {
            $x = $split + 42 + $i * 84
            Draw-Text $graphics $hours[$i] $x 116 17 $secondary
            Draw-Crescent $graphics ($x + 5) 158 34
            Draw-Text $graphics $temps[$i] $x 224 24 $white
        }
    } else {
        $liveBrush = [System.Drawing.SolidBrush]::new($cyan)
        $graphics.FillEllipse($liveBrush, $width - $pad - 14, $pad + 13, 7, 7)
        $liveBrush.Dispose()
        Draw-Text $graphics "19°" $pad 90 110 $white $true
        Draw-Crescent $graphics ($width - $pad - 115) 130 100
        Draw-Text $graphics "晴  ·  29° / 15°" $pad 250 28 $white $true
        Draw-Text $graphics "体感 19°  ·  湿度 52%  ·  风 3 km/h" $pad 300 20 $secondary
        Draw-Text $graphics "AQI 35 优  ·  降水 0%" $pad 340 19 $cyan $true
        $pen = [System.Drawing.Pen]::new($rule, 2)
        $graphics.DrawLine($pen, $pad, 385, $width - $pad, 385)
        $graphics.DrawLine($pen, $pad, 515, $width - $pad, 515)
        $pen.Dispose()
        $hours = @("2时", "3时", "4时", "5时")
        $temps = @("18°", "17°", "16°", "15°")
        for ($i = 0; $i -lt 4; $i++) {
            $x = $pad + 65 + $i * 155
            Draw-Text $graphics $hours[$i] $x 400 18 $secondary
            Draw-Crescent $graphics ($x + 8) 440 38
            Draw-Text $graphics $temps[$i] $x 480 21 $white
        }
        Draw-Text $graphics "今天     ☀     15° ~ 29°" $pad 535 22 $white
        Draw-Text $graphics "周四     ☁     16° ~ 30°" $pad 590 22 $white
        Draw-Text $graphics "周五     ☀     17° ~ 31°" $pad 645 22 $white
    }

    $target = Join-Path $outputDir $name
    $bitmap.Save($target, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-Preview "widget_preview_small.png" 360 360 "small"
New-Preview "widget_preview_medium.png" 720 360 "medium"
New-Preview "widget_preview_large.png" 720 720 "large"
