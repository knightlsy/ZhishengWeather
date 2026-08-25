package com.zhisheng.weather.model

import kotlin.math.abs
import java.time.Instant
import java.time.ZoneId

// Hero 高低温跟时刻走（v0.0.9）：国内源白天 10:00–20:00。
// 10 点前看昨夜最低和今天最高；白天看今天高/低；20 点后看今夜最低和明天最高。
data class HeroRange(
    val leftLabel: String,
    val left: Double?,
    val rightLabel: String,
    val right: Double?,
) {
    val hasAny: Boolean get() = left != null || right != null
}

object HeroTemps {
    const val DAY_START_HOUR = 10
    const val NIGHT_START_HOUR = 20
    const val FEELS_GAP_C = 1.5

    fun range(
        daily: List<DailyWeather>,
        yesterday: YesterdayInfo?,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): HeroRange {
        val hour = Instant.ofEpochMilli(nowMillis).atZone(zone).hour
        val today = daily.getOrNull(0)
        val tomorrow = daily.getOrNull(1)
        val todayHigh = today?.high
        val todayLow = today?.low
        return when {
            hour < DAY_START_HOUR -> HeroRange(
                leftLabel = "昨低",
                left = yesterday?.low,
                rightLabel = "今高",
                right = todayHigh,
            )
            hour >= NIGHT_START_HOUR -> HeroRange(
                leftLabel = "夜低",
                // 0.0.9-debug 修复：20 点后的「夜低」应指今夜将出现的最低温。
                // 今日 low 是今晨已发生的过去值（8 月夜间降温时 21 点显示的
                // 是早上 6 点的温度）；今夜最低通常落在明晨，取明日 low。
                left = tomorrow?.low ?: todayLow,
                rightLabel = "明高",
                right = tomorrow?.high ?: todayHigh,
            )
            else -> HeroRange(
                leftLabel = "高",
                left = todayHigh,
                rightLabel = "低",
                right = todayLow,
            )
        }
    }

    fun showFeelsLike(temperature: Double?, feelsLike: Double?): Boolean {
        if (feelsLike == null) return false
        if (temperature == null) return true
        return abs(feelsLike - temperature) >= FEELS_GAP_C
    }
}
