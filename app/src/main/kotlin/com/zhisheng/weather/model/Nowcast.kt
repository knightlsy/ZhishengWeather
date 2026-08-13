package com.zhisheng.weather.model

import kotlin.math.abs
import kotlin.math.roundToInt

// 短时降水与主屏一句话（v0.0.6）：纯函数，不读系统时钟以外的环境。
data class RainTiming(
    val rainingNow: Boolean,
    val minutesUntilStart: Int?,
) {
    val hasRain: Boolean get() = rainingNow || minutesUntilStart != null
}

object Nowcast {
    const val WET_THRESHOLD = 0.03f
    const val NOW_WINDOW_MS = 2 * 60_000L
    const val MINUTE_MS = 60_000L

    fun minuteSeries(
        values: List<Float>,
        startMillis: Long,
        stepMs: Long = MINUTE_MS,
    ): List<MinutePrecip> {
        if (values.isEmpty() || stepMs <= 0L) return emptyList()
        return values.mapIndexed { i, v ->
            MinutePrecip(startMillis + i * stepMs, v.coerceAtLeast(0f))
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
            out.add(MinutePrecip(t, sorted[i].precip.coerceAtLeast(0f)))
        }
        return out
    }

    fun horizonLabel(points: List<MinutePrecip>): String {
        if (points.size < 2) return "+120min"
        val mins = ((points.last().timeMillis - points.first().timeMillis) / MINUTE_MS).toInt()
            .coerceAtLeast(1)
        return "+${mins}min"
    }

    fun rainTiming(
        minutes: List<MinutePrecip>,
        nowMillis: Long,
        wet: Float = WET_THRESHOLD,
    ): RainTiming {
        val firstWet = minutes.firstOrNull { it.precip >= wet } ?: return RainTiming(false, null)
        val delta = firstWet.timeMillis - nowMillis
        if (delta <= NOW_WINDOW_MS) return RainTiming(true, 0)
        val mins = ((delta + 30_000L) / MINUTE_MS).toInt().coerceAtLeast(1)
        return RainTiming(false, mins)
    }

    fun rainTimingLabel(timing: RainTiming): String? = when {
        timing.rainingNow -> "正在下雨"
        timing.minutesUntilStart != null -> "${timing.minutesUntilStart} 分钟后开始下雨"
        else -> null
    }

    fun briefingLine(data: WeatherData, unit: String, nowMillis: Long): String? {
        // 接口原文优先（和风 summary / 小米 description），包括「不会下雨」——那也是两小时结论
        data.rainNowcast?.trim()?.takeIf { it.isNotEmpty() }?.let { return tidyCopy(it) }

        rainTimingLabel(rainTiming(data.rainMinutes, nowMillis))?.let { return it }
        severeAlert(data.alerts)?.title?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        tempDeltaLine(data, unit)?.let { return it }
        mildAlert(data.alerts)?.title?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        return null
    }

    fun tidyCopy(text: String): String =
        text.trim().trimEnd('~', '～').trimEnd()

    internal fun looksLikeIncomingRain(text: String): Boolean {
        if (text.isEmpty()) return false
        if (text.contains("不会下雨") || text.contains("无降水") || text.contains("不会有雨")) return false
        return text.contains("雨") || text.contains("雪") || text.contains("降水")
    }

    private fun severeAlert(alerts: List<AlertInfo>): AlertInfo? =
        alerts.firstOrNull { it.severity == AlertLevel.RED }
            ?: alerts.firstOrNull { it.severity == AlertLevel.ORANGE }

    private fun mildAlert(alerts: List<AlertInfo>): AlertInfo? =
        alerts.firstOrNull { it.severity == AlertLevel.YELLOW }
            ?: alerts.firstOrNull { it.severity == AlertLevel.BLUE }
            ?: alerts.firstOrNull()

    private fun tempDeltaLine(data: WeatherData, unit: String): String? {
        val today = data.daily.getOrNull(0)?.high ?: return null
        val tomorrow = data.daily.getOrNull(1)?.high ?: return null
        val delta = displayTemp(tomorrow, unit) - displayTemp(today, unit)
        if (abs(delta) < 3) return null
        return if (delta > 0) "明天比今天高 ${delta}°" else "明天比今天低 ${-delta}°"
    }

    private fun displayTemp(celsius: Double, unit: String): Int =
        if (unit == "f") (celsius * 9.0 / 5.0 + 32.0).roundToInt() else celsius.roundToInt()
}
