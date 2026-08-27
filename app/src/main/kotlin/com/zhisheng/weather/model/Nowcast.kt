package com.zhisheng.weather.model

import kotlin.math.abs
import kotlin.math.roundToInt

// 短时降水与主屏一句话（v0.0.8）：现在是否在下，只看「此刻」而不是序列里最早的一场雨。
data class RainTiming(
    val rainingNow: Boolean,
    val minutesUntilStart: Int?,
    val minutesUntilEnd: Int? = null,
) {
    val hasRain: Boolean get() = rainingNow || minutesUntilStart != null
}

object Nowcast {
    const val WET_THRESHOLD = 0.02f
    const val NOW_WINDOW_MS = 2 * 60_000L
    const val MINUTE_MS = 60_000L
    private const val STOP_DRY_CONFIRM_MS = 8 * MINUTE_MS

    fun accumulatedMmToRate(valueMm: Float, periodMinutes: Int): Float {
        if (!valueMm.isFinite() || valueMm <= 0f || periodMinutes <= 0) return 0f
        return valueMm * (60f / periodMinutes)
    }

    fun minuteSeries(
        values: List<Float>,
        startMillis: Long,
        stepMs: Long = MINUTE_MS,
        phase: PrecipitationPhase = PrecipitationPhase.RAIN,
    ): List<MinutePrecip> {
        if (values.isEmpty() || stepMs <= 0L) return emptyList()
        return values.mapIndexed { i, v ->
            MinutePrecip(startMillis + i * stepMs, v.coerceAtLeast(0f), phase)
        }
    }

    // 把 15 分钟粒度（Open-Meteo）等稀采样拉成逐分钟柱，和风/小米的 120 点图视觉一致。
    // 超出最后一个采样点的分钟沿用该点（区间数据：15 分钟桶代表随后一刻钟）。
    fun densifyToMinutes(
        points: List<MinutePrecip>,
        horizonMin: Int = 120,
    ): List<MinutePrecip> {
        if (points.size < 2 || horizonMin <= 0) return points
        val sorted = points.sortedBy { it.timeMillis }
        val start = sorted.first().timeMillis
        val out = ArrayList<MinutePrecip>(horizonMin)
        var i = 0
        for (m in 0 until horizonMin) {
            val t = start + m * MINUTE_MS
            while (i + 1 < sorted.size && sorted[i + 1].timeMillis <= t) i++
            out.add(MinutePrecip(t, sorted[i].precip.coerceAtLeast(0f), sorted[i].phase))
        }
        return out
    }

    fun horizonLabel(points: List<MinutePrecip>): String {
        if (points.size < 2) return "120 分钟"
        val mins = ((points.last().timeMillis - points.first().timeMillis) / MINUTE_MS).toInt()
            .coerceAtLeast(1)
        return if (mins >= 120) "2 小时" else "$mins 分钟"
    }

    fun intensityLabel(rateMmPerHour: Float): String = when {
        rateMmPerHour < WET_THRESHOLD -> "无降水"
        rateMmPerHour < 2.5f -> "小雨"
        rateMmPerHour < 8f -> "中雨"
        rateMmPerHour < 16f -> "大雨"
        else -> "强降水"
    }

    fun sourceLabel(source: String?): String = when (source?.uppercase()) {
        "QWEATHER" -> "和风"
        "CAIYUN" -> "彩云"
        "XIAOMI" -> "小米"
        "OPEN-METEO" -> "公共源"
        "SIMULATION" -> "模拟"
        else -> source?.takeIf { it.isNotBlank() } ?: "天气源"
    }

    fun rainTiming(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        wet: Float = WET_THRESHOLD,
        currentPrecip: Boolean = false,
    ): RainTiming {
        val sorted = minutes.sortedBy { it.timeMillis }
        val seriesNow = seriesWetAt(sorted, nowMillis, wet)
        val rainingNow = currentPrecip || seriesNow
        if (rainingNow) {
            val end = if (seriesNow) minutesUntilDry(sorted, nowMillis, wet) else null
            return RainTiming(true, 0, end)
        }
        val start = minutesUntilWet(sorted, nowMillis, wet)
        return RainTiming(false, start, null)
    }

    fun rainTimingLabel(timing: RainTiming): String? = when {
        timing.rainingNow && timing.minutesUntilEnd != null ->
            "${timing.minutesUntilEnd} 分钟后雨会停"
        timing.rainingNow -> "正在下雨"
        timing.minutesUntilStart != null -> "${timing.minutesUntilStart} 分钟后开始下雨"
        else -> null
    }

    fun briefingLine(data: WeatherData, unit: String, nowMillis: Long): String? {
        val precipNow = data.current.let { cur ->
            cur != null && (cur.condition?.isPrecipitation == true || (cur.precipMm ?: 0.0) > 0.05)
        }
        val timing = rainTiming(data.rainMinutes, nowMillis, currentPrecip = precipNow)
        val api = data.rainNowcast?.trim()?.takeIf { it.isNotEmpty() }?.let { tidyCopy(it) }

        if (timing.rainingNow) {
            // 分钟序列能给出明确停雨时刻时，主屏与分钟降水卡必须共用同一个结论。
            // 原先这里优先透传供应商的“半小时后雨渐停”，卡片却显示本地计算的
            // “29 分钟后雨会停”，数值虽一致，用户看到的却像两份互相冲突的预报。
            if (timing.minutesUntilEnd != null) return rainTimingLabel(timing)
            // 实况或分钟序列显示正在下雨时，接口「不会下雨」一律丢掉。
            if (api != null && !isDryNowcast(api)) return api
            return rainTimingLabel(timing)
        }
        if (timing.minutesUntilStart != null) {
            // 有可计算的分钟级开始时刻时，同样不用供应商的模糊取整文案覆盖它。
            return rainTimingLabel(timing)
        }
        // 没雨不是新闻，不把「不会下雨」写进第一句。
        // 雨在别处（距离文案）可以保留；近处有雨但此刻序列是干的，也让用户看到源站结论。
        if (api != null && looksLikeIncomingRain(api)) return api

        severeAlert(data.alerts)?.title?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        tempDeltaLine(data, unit, nowMillis)?.let { return it }
        mildAlert(data.alerts)?.title?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return null
    }

    // 分钟降水卡：只在接下来一段时间真有雨，或雨带近到值得看时出现。
    fun shouldShowPrecipCard(data: WeatherData, nowMillis: Long): Boolean {
        val precipNow = data.current.let { cur ->
            cur != null && (cur.condition?.isPrecipitation == true || (cur.precipMm ?: 0.0) > 0.05)
        }
        val timing = rainTiming(data.rainMinutes, nowMillis, currentPrecip = precipNow)
        if (timing.hasRain) return true
        val api = data.rainNowcast?.trim()?.takeIf { it.isNotEmpty() }
        if (api != null && looksLikeIncomingRain(api)) return true
        val km = data.rainDistanceKm ?: return false
        return km in 0.0..40.0
    }

    // 无柱且此刻也没雨才走晴窗；正在下雨时即使序列被裁空，也不能画成 CLEAR WINDOW。
    fun precipCardClearWindow(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        precipNow: Boolean,
    ): Boolean {
        val timing = rainTiming(minutes, nowMillis, currentPrecip = precipNow)
        return precipChartCeiling(minutes) <= 0f && !timing.hasRain
    }

    // 任何源只要确实返回当前/未来短时序列就展示；全 0 代表“有数据且未来无雨”，
    // 空列表才代表当前源没有这项能力或请求失败。
    fun shouldShowPrecipModule(data: WeatherData, nowMillis: Long): Boolean {
        val hasUsableSeries = data.rainMinutes.any { it.timeMillis >= nowMillis - NOW_WINDOW_MS }
        return hasUsableSeries || shouldShowPrecipCard(data, nowMillis)
    }

    // 分钟图按实际峰值选离散标尺，弱降水不会被固定 0.3 mm/h 的上限压成细线。
    fun precipChartCeiling(points: List<MinutePrecip>): Float {
        val max = points.maxOfOrNull { it.precip.coerceAtLeast(0f) } ?: 0f
        return when {
            max <= 0f -> 0f
            max <= 0.05f -> 0.05f
            max <= 0.1f -> 0.1f
            max <= 0.25f -> 0.25f
            max <= 0.5f -> 0.5f
            max <= 1f -> 1f
            max <= 2f -> 2f
            max <= 5f -> 5f
            else -> kotlin.math.ceil(max.toDouble()).toFloat()
        }
    }

    fun tidyCopy(text: String): String =
        text.trim().trimEnd('~', '～').trimEnd()

    internal fun isDryNowcast(text: String): Boolean {
        if (text.isEmpty()) return false
        return text.contains("不会下雨") || text.contains("无降水") || text.contains("不会有雨") ||
            text.contains("无降雨") || text.contains("没有雨")
    }

    internal fun looksLikeIncomingRain(text: String): Boolean {
        if (text.isEmpty()) return false
        if (isDryNowcast(text)) return false
        if (text.contains("以外") || text.contains("远离")) return false
        return text.contains("雨") || text.contains("雪") || text.contains("降水")
    }

    internal fun seriesWetAt(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        wet: Float = WET_THRESHOLD,
    ): Boolean {
        if (minutes.isEmpty()) return false
        val window = minutes.filter { abs(it.timeMillis - nowMillis) <= NOW_WINDOW_MS }
        if (window.isNotEmpty()) return window.any { it.precip >= wet }
        val first = minutes.first()
        return first.timeMillis > nowMillis &&
            first.timeMillis - nowMillis <= NOW_WINDOW_MS &&
            first.precip >= wet
    }

    private fun minutesUntilWet(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        wet: Float,
    ): Int? {
        val firstWet = minutes.firstOrNull { it.timeMillis > nowMillis + NOW_WINDOW_MS && it.precip >= wet }
            ?: return null
        val mins = ((firstWet.timeMillis - nowMillis + 30_000L) / MINUTE_MS).toInt().coerceAtLeast(1)
        return mins
    }

    private fun minutesUntilDry(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        wet: Float,
    ): Int? {
        val after = minutes.filter { it.timeMillis >= nowMillis }
        if (after.isEmpty()) return null
        var dryStart: MinutePrecip? = null
        for (p in after) {
            if (p.precip < wet) {
                if (dryStart == null) dryStart = p
                if (p.timeMillis - dryStart.timeMillis >= STOP_DRY_CONFIRM_MS) {
                    return ((dryStart!!.timeMillis - nowMillis + 30_000L) / MINUTE_MS).toInt()
                        .coerceAtLeast(1)
                }
            } else {
                dryStart = null
            }
        }
        if (dryStart != null && after.last().precip < wet) {
            return ((dryStart!!.timeMillis - nowMillis + 30_000L) / MINUTE_MS).toInt()
                .coerceAtLeast(1)
        }
        return null
    }

    private fun severeAlert(alerts: List<AlertInfo>): AlertInfo? =
        alerts.firstOrNull { it.severity == AlertLevel.RED }
            ?: alerts.firstOrNull { it.severity == AlertLevel.ORANGE }

    private fun mildAlert(alerts: List<AlertInfo>): AlertInfo? =
        alerts.firstOrNull { it.severity == AlertLevel.YELLOW }
            ?: alerts.firstOrNull { it.severity == AlertLevel.BLUE }
            ?: alerts.firstOrNull()

    private fun tempDeltaLine(data: WeatherData, unit: String, nowMillis: Long): String? {
        val today = data.todayDaily(nowMillis)?.high ?: return null
        val tomorrow = data.tomorrowDaily(nowMillis)?.high ?: return null
        val delta = displayTemp(tomorrow, unit) - displayTemp(today, unit)
        if (abs(delta) < 3) return null
        return if (delta > 0) "明天比今天高 ${delta}°" else "明天比今天低 ${-delta}°"
    }

    private fun displayTemp(celsius: Double, unit: String): Int =
        if (unit == "f") (celsius * 9.0 / 5.0 + 32.0).roundToInt() else celsius.roundToInt()
}
