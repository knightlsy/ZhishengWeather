param(
    [string]$ProjectRoot = (Get-Location).Path,
    [string]$City = "上海"
)

# 0.1.3 腕表玻璃小组件选择器预览图。
# 画布按约 2x 密度绘制，关键层级与真实 RemoteViews 保持一致。

Add-Type -AssemblyName System.Drawing

$outputDir = Join-Path $projectRoot "app\src\main\res\drawable-nodpi"
[System.IO.Directory]::CreateDirectory($outputDir) | Out-Null

# widget_bg.xml Liquid Glass：半透明石墨底 + 冷白高光边。
$bgStart  = [System.Drawing.Color]::FromArgb(196, 42, 55, 64)
$bgEnd    = [System.Drawing.Color]::FromArgb(184, 18, 25, 31)
$border   = [System.Drawing.Color]::FromArgb(86, 95, 123, 130)
# widget_colors.xml
$orange   = [System.Drawing.ColorTranslator]::FromHtml("#FF9830")
$cyan     = [System.Drawing.ColorTranslator]::FromHtml("#20F0FF")
$white    = [System.Drawing.ColorTranslator]::FromHtml("#F3FAF7")
$secondary= [System.Drawing.ColorTranslator]::FromHtml("#D4E2DE")
$tertiary = [System.Drawing.ColorTranslator]::FromHtml("#AABAB8")
$rule     = [System.Drawing.ColorTranslator]::FromHtml("#5E7880")
$cell     = [System.Drawing.Color]::FromArgb(34, 255, 255, 255)
$cellBorder = [System.Drawing.Color]::FromArgb(70, 77, 104, 113)

# 示范数据（上海，8 月）
$dateLabel = (Get-Date).ToString("M月d日 ddd", [System.Globalization.CultureInfo]::GetCultureInfo("zh-CN"))
$temp = "29°"
$range = "晴  ·  33° / 27°"
$details = "体感 32°  ·  湿度 68%"
$detailsLarge = "体感32° · 湿度68%"
$aqiLine = "AQI 42 优  ·  降水 10%"
$hours = @("14时", "15时", "16时", "17时")
$temps = @("33°", "32°", "31°", "30°")
$trendHours = @("14时", "15时", "16时", "17时", "18时", "19时")
$trendTemps = @(33, 32, 31, 30, 29, 29)
$lifeTips = @(@("紫外线", "较强"), @("穿衣", "天气较热"), @("运动", "适宜"))
$days = @(
    @("今天 27日", 27, 33),
    @("周五 28日", 28, 34),
    @("周六 29日", 27, 32),
    @("周日 30日", 26, 31),
    @("周一 31日", 27, 33),
    @("周二 1日", 28, 34),
    @("周三 2日", 27, 33)
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

function Draw-RoundedRect($graphics, [float]$x, [float]$y, [float]$width, [float]$height, [float]$radius, $fillColor, $strokeColor) {
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = $radius * 2
    $path.AddArc($x, $y, $d, $d, 180, 90)
    $path.AddArc($x + $width - $d, $y, $d, $d, 270, 90)
    $path.AddArc($x + $width - $d, $y + $height - $d, $d, $d, 0, 90)
    $path.AddArc($x, $y + $height - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    $brush = [System.Drawing.SolidBrush]::new($fillColor)
    $pen = [System.Drawing.Pen]::new($strokeColor, 2)
    $graphics.FillPath($brush, $path)
    $graphics.DrawPath($pen, $path)
    $brush.Dispose()
    $pen.Dispose()
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

# —— 2x2：城市和日期固定在同一左侧顶栏，不让最小规格丢地名 ——
function New-PreviewSmall($width, $height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $pad = 26
    Draw-RoundedPanel $graphics $width $height 46
    $accent = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accent, $pad, $pad, 4, 52)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 24) ($pad - 8) 30 $white $true
    Draw-Text $graphics $dateLabel ($pad + 24) ($pad + 28) 20 $tertiary
    Draw-LiveDot $graphics ($width - $pad - 16) ($pad + 16)
    Draw-Text $graphics "↻" ($width - $pad - 54) ($pad - 8) 30 $cyan
    Draw-Text $graphics $temp $pad 98 92 $white $true
    Draw-Sun $graphics ($width - $pad - 60) 148 92
    Draw-Text $graphics $range $pad 246 27 $secondary $true
    Draw-Text $graphics $details $pad 294 22 $tertiary
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
    $graphics.FillRectangle($accent, $pad, $pad, 4, 32)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 26) ($pad - 8) 30 $white $true
    Draw-Text $graphics $dateLabel ($width - $pad - 272) ($pad - 7) 20 $secondary
    Draw-Text $graphics "小米 · 12:00更新" ($width - $pad - 198) ($pad + 22) 16 $tertiary
    Draw-LiveDot $graphics ($width - $pad - 72) ($pad + 14)
    Draw-Text $graphics "↻" ($width - $pad - 42) ($pad - 9) 30 $cyan
    $split = [Math]::Round($width * 0.48)
    Draw-Text $graphics $temp $pad 96 88 $white $true
    Draw-Sun $graphics ($split - 105) 132 88
    Draw-Text $graphics $range $pad 246 28 $white $true
    Draw-Text $graphics $details $pad 296 24 $secondary
    $hourStart = $split + 12
    $hourWidth = ($width - $pad - $hourStart) / 4
    for ($i = 0; $i -lt 4; $i++) {
        $x = $hourStart + $i * $hourWidth
        Draw-Text $graphics $hours[$i] ($x + 10) 128 21 $tertiary
        Draw-Sun $graphics ($x + $hourWidth / 2) 172 42
        Draw-Text $graphics $temps[$i] ($x + 15) 248 27 $white
    }
    $bitmap.Save((Join-Path $outputDir "widget_preview_medium.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

# —— 4x4：顶行 + 实况 + 六小时温度磁带 + 生活指数 + 七日 ——
function New-PreviewLarge($width, $height) {
    $bitmap = [System.Drawing.Bitmap]::new($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $pad = 28
    Draw-RoundedPanel $graphics $width $height 46
    $accent = [System.Drawing.SolidBrush]::new($orange)
    $graphics.FillRectangle($accent, $pad, $pad, 4, 32)
    $accent.Dispose()
    Draw-Text $graphics $City ($pad + 26) ($pad - 8) 32 $white $true
    Draw-Text $graphics $dateLabel ($width - $pad - 276) ($pad - 7) 20 $secondary
    Draw-Text $graphics "小米 · 12:00更新" ($width - $pad - 198) ($pad + 22) 16 $tertiary
    Draw-LiveDot $graphics ($width - $pad - 72) ($pad + 16)
    Draw-Text $graphics "↻" ($width - $pad - 42) ($pad - 9) 30 $cyan
    Draw-Text $graphics $temp $pad 82 96 $white $true
    Draw-Sun $graphics 250 132 96
    Draw-Text $graphics $range 340 92 28 $white $true
    Draw-Text $graphics $detailsLarge 340 136 23 $secondary
    Draw-Text $graphics "风3km/h" 340 171 21 $tertiary
    Draw-Text $graphics $aqiLine 340 202 21 $cyan $true
    Draw-Rule $graphics $pad 236 ($width - $pad) 236

    Draw-Text $graphics "未来 6 小时" $pad 244 18 $tertiary
    Draw-Text $graphics "33° → 29°" 440 242 21 $cyan $true
    $graphLeft = $pad + 16
    $graphRight = $width - $pad - 16
    $graphTop = 286
    $graphBottom = 334
    $graphStep = ($graphRight - $graphLeft) / 5
    $guidePen = [System.Drawing.Pen]::new($rule, 2)
    $graphics.DrawLine($guidePen, $graphLeft, $graphBottom, $graphRight, $graphBottom)
    for ($i = 0; $i -lt 6; $i++) {
        $x = $graphLeft + $i * $graphStep
        $y = $graphBottom - 16 - (($trendTemps[$i] - 29) / 4.0) * 18
        $pointColor = if ($trendTemps[$i] -eq 33) { $orange } else { $cyan }
        $tickPen = [System.Drawing.Pen]::new($pointColor, $(if ($i -eq 0) { 5 } else { 3 }))
        $graphics.DrawLine($tickPen, $x, $y, $x, $graphBottom)
        $tickPen.Dispose()
        Draw-Text $graphics ("{0}°" -f $trendTemps[$i]) ($x - 18) ($graphTop - 16) 17 $pointColor $true
        Draw-Text $graphics $trendHours[$i] ($x - 20) 340 15 $tertiary
    }
    $guidePen.Dispose()

    Draw-Text $graphics "生活" $pad 374 17 $orange $true
    Draw-Text $graphics "紫外线 较强  ·  穿衣 天气较热  ·  运动 适宜" ($pad + 62) 374 18 $secondary
    Draw-Rule $graphics $pad 410 ($width - $pad) 410
    $y = 424
    for ($dayIndex = 0; $dayIndex -lt $days.Count; $dayIndex++) {
        $d = $days[$dayIndex]
        Draw-Text $graphics $d[0] $pad $y 23 $secondary
        Draw-Sun $graphics ($pad + 182) ($y + 14) 22
        $railLeft = $pad + 218
        $railRight = $width - $pad - 190
        Draw-Rule $graphics $railLeft ($y + 14) $railRight ($y + 14)
        $lowX = $railLeft + (($d[1] - 26) / 8.0) * ($railRight - $railLeft)
        $highX = $railLeft + (($d[2] - 26) / 8.0) * ($railRight - $railLeft)
        $signalColor = if ($dayIndex -eq 0) { $orange } else { $cyan }
        $segmentPen = [System.Drawing.Pen]::new($signalColor, 5)
        $segmentPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $segmentPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $graphics.DrawLine($segmentPen, $lowX, ($y + 14), $highX, ($y + 14))
        $segmentPen.Dispose()
        Draw-Text $graphics ("{0}° ~ {1}°" -f $d[1], $d[2]) ($width - $pad - 170) $y 23 $white
        $y += 36
    }
    $bitmap.Save((Join-Path $outputDir "widget_preview_large.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-PreviewSmall 360 360
New-PreviewMedium 720 360
New-PreviewLarge 720 720

Write-Host "previews written to $outputDir (city=$City, date=$dateLabel)"
