param(
    [string]$ProjectRoot = (Get-Location).Path,
    [string]$City = "上海"
)

# 小组件选择器预览图生成（v0.0.5 重写）
# 原则：与真实布局 1:1 对齐——字号 = 布局 sp × 2（画布按 2x 密度），颜色取自
# widget_colors.xml / widget_bg.xml 同值，示范城市默认上海（不再用开发机城市）。
# 布局参照：widget_small/medium/large.xml（v0.0.5 字号放大后）。

Add-Type -AssemblyName System.Drawing

$outputDir = Join-Path $projectRoot "app\src\main\res\drawable-nodpi"
[System.IO.Directory]::CreateDirectory($outputDir) | Out-Null

# widget_bg.xml 渐变与描边
$bgStart  = [System.Drawing.ColorTranslator]::FromHtml("#0B1118")
$bgEnd    = [System.Drawing.ColorTranslator]::FromHtml("#12171F")
$border   = [System.Drawing.ColorTranslator]::FromHtml("#48535F")
# widget_colors.xml
$orange   = [System.Drawing.ColorTranslator]::FromHtml("#FF9830")
$cyan     = [System.Drawing.ColorTranslator]::FromHtml("#20F0FF")
$white    = [System.Drawing.ColorTranslator]::FromHtml("#E8F0E8")
$secondary= [System.Drawing.ColorTranslator]::FromHtml("#C8D8C8")
$tertiary = [System.Drawing.ColorTranslator]::FromHtml("#95A395")
$rule     = [System.Drawing.ColorTranslator]::FromHtml("#303841")

# 示范数据（上海，8 月）
$dateLabel = (Get-Date).ToString("M月d日 ddd", [System.Globalization.CultureInfo]::GetCultureInfo("zh-CN"))
$temp = "29°"
$range = "晴  ·  33° / 27°"
$details = "体感 32°  ·  湿度 68%"
$detailsLarge = "体感 32°  ·  湿度 68%  ·  风 3 km/h"
$aqiLine = "AQI 42 优  ·  降水 10%"
$hours = @("14时", "15时", "16时", "17时")
$temps = @("33°", "32°", "31°", "30°")
$days = @(
    @("今天", "晴", "27° ~ 33°"),
    @("周四", "多云", "28° ~ 34°"),
    @("周五", "晴", "27° ~ 32°")
)

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
        [System.Drawing.Rectangle]::new(0, 0, $width, $height), $bgStart, $bgEnd, 45
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

# 晴：圆盘 + 八根短射线（对应 weather_sun 的意象）
function Draw-Sun($graphics, [float]$cx, [float]$cy, [float]$size) {
    $brush = [System.Drawing.SolidBrush]::new($cyan)
    $r = $size / 2
    $graphics.FillEllipse($brush, $cx - $r * 0.62, $cy - $r * 0.62, $r * 1.24, $r * 1.24)
    $pen = [System.Drawing.Pen]::new($cyan, 5)
    for ($i = 0; $i -lt 8; $i++) {
        $a = $i * [Math]::PI / 4
        $x1 = $cx + [Math]::Cos($a) * $r * 0.85
        $y1 = $cy + [Math]::Sin($a) * $r * 0.85
        $x2 = $cx + [Math]::Cos($a) * $r * 1.18
        $y2 = $cy + [Math]::Sin($a) * $r * 1.18
        $graphics.DrawLine($pen, $x1, $y1, $x2, $y2)
    }
    $pen.Dispose()
    $brush.Dispose()
}

function Draw-LiveDot($graphics, [float]$x, [float]$y) {
    $brush = [System.Drawing.SolidBrush]::new($cyan)
    $graphics.FillEllipse($brush, $x, $y, 12, 12)
    $brush.Dispose()
}

function Draw-Rule($graphics, [float]$x1, [float]$y1, [float]$x2, [float]$y2) {
    $pen = [System.Drawing.Pen]::new($rule, 2)
    $graphics.DrawLine($pen, $x1, $y1, $x2, $y2)
    $pen.Dispose()
}

# —— 2x2：accent 条 + 城市/日期 → 大温度+图标 → 范围 → 体感湿度 ——
function New-PreviewSmall($width, $height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $pad = 26
    Draw-RoundedPanel $graphics $width $height 46
    $accent = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accent, $pad, $pad, 8, 36)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 26) ($pad - 8) 30 $orange $true
    Draw-Text $graphics $dateLabel ($width - $pad - 190) ($pad + 4) 26 $cyan
    Draw-Text $graphics $temp $pad 88 92 $white $true
    Draw-Sun $graphics ($width - $pad - 60) 138 100
    Draw-Text $graphics $range $pad 238 28 $white $true
    Draw-Text $graphics $details $pad 288 24 $secondary
    $bitmap.Save((Join-Path $outputDir "widget_preview_small.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

# —— 4x2：顶行 + 左实况 + 分隔线 + 四小时 ——
function New-PreviewMedium($width, $height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $pad = 28
    Draw-RoundedPanel $graphics $width $height 46
    $accent = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accent, $pad, $pad, 8, 36)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 26) ($pad - 8) 30 $orange $true
    Draw-Text $graphics $dateLabel ($width - $pad - 350) ($pad + 4) 26 $cyan
    Draw-LiveDot $graphics ($width - $pad - 150) ($pad + 14)
    Draw-Text $graphics "UPD 12:00" ($width - $pad - 128) ($pad + 4) 24 $tertiary
    $split = [Math]::Round($width * 0.48)
    Draw-Rule $graphics $split 100 ($split) ($height - $pad)
    Draw-Text $graphics $temp $pad 96 88 $white $true
    Draw-Sun $graphics ($split - 105) 132 88
    Draw-Text $graphics $range $pad 246 28 $white $true
    Draw-Text $graphics $details $pad 296 24 $secondary
    for ($i = 0; $i -lt 4; $i++) {
        $x = $split + 50 + $i * 112
        Draw-Text $graphics $hours[$i] $x 128 24 $tertiary
        Draw-Sun $graphics ($x + 16) 172 44
        Draw-Text $graphics $temps[$i] $x 248 30 $white
    }
    $bitmap.Save((Join-Path $outputDir "widget_preview_medium.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

# —— 4x4：顶行 + 实况(含 AQI) + 分隔 + 四小时 + 分隔 + 三日 ——
function New-PreviewLarge($width, $height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $pad = 28
    Draw-RoundedPanel $graphics $width $height 46
    $accent = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accent, $pad, $pad, 8, 40)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 26) ($pad - 8) 32 $orange $true
    Draw-Text $graphics $dateLabel ($width - $pad - 320) ($pad + 2) 28 $cyan
    Draw-LiveDot $graphics ($width - $pad - 120) ($pad + 16)
    Draw-Text $graphics "UPD 12:00" ($width - $pad - 98) ($pad + 2) 24 $tertiary
    Draw-Text $graphics $temp $pad 90 96 $white $true
    Draw-Sun $graphics ($width - $pad - 130) 130 120
    Draw-Text $graphics $range $pad 256 30 $white $true
    Draw-Text $graphics $detailsLarge $pad 306 26 $secondary
    Draw-Text $graphics $aqiLine $pad 350 24 $cyan $true
    Draw-Rule $graphics $pad 396 ($width - $pad) 396
    for ($i = 0; $i -lt 4; $i++) {
        $x = $pad + 40 + $i * 160
        Draw-Text $graphics $hours[$i] $x 410 24 $tertiary
        Draw-Sun $graphics ($x + 18) 452 52
        Draw-Text $graphics $temps[$i] $x 500 30 $white
    }
    Draw-Rule $graphics $pad 545 ($width - $pad) 545
    $y = 560
    foreach ($d in $days) {
        Draw-Text $graphics $d[0] $pad $y 28 $white
        Draw-Text $graphics $d[1] ($pad + 140) $y 28 $secondary
        Draw-Text $graphics $d[2] ($width - $pad - 260) $y 28 $white
        $y += 58
    }
    $bitmap.Save((Join-Path $outputDir "widget_preview_large.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-PreviewSmall 360 360
New-PreviewMedium 720 360
New-PreviewLarge 720 720

Write-Host "previews written to $outputDir (city=$City, date=$dateLabel)"
