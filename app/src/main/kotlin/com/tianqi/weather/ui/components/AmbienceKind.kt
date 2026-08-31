package com.tianqi.weather.ui.components

import com.tianqi.weather.model.WeatherCondition
import com.tianqi.weather.model.WeatherData
import com.tianqi.weather.model.WeatherIntensity
import com.tianqi.weather.model.ThermalModifier

// ═══════════════════════════════════════════════════════════
// 氛围层的种类判定（v0.0.9）
// 单独成文件、不依赖 Compose，是为了能被单测直接覆盖：
// 「这种天气该放哪一套动效」是产品判断，不该埋在 Canvas 里。
// ═══════════════════════════════════════════════════════════

enum class AmbienceKind {
    NONE,
    CLEAR_DAY,      // 晴（昼）：仪器待机
    STARFIELD,      // 晴（夜）：星点像素
    PARTLY_CLOUDY,  // 多云：横向色块缓移
    OVERCAST,       // 阴：同结构，更密更慢更暗
    DRIZZLE,        // 小雨：数据雨，列更少更慢
    RAIN,           // 雨：数据雨
    SLEET,          // 雨夹雪：数据雨夹漂移亮点
    SNOW,           // 雪：下落的 · / *
    STORM,          // 雷暴：扫描线 + 闪白
    HAIL,           // 冰雹：硬边菱形/方块
    FREEZING_RAIN,  // 冻雨/冻毛毛雨：雨线 + 晶体刻度
    FOG,            // 雾：点阵呼吸
    HAZE,           // 霾：压对比度 + 极慢污浊漂移
    SAND,           // 沙尘：橙色砂粒横向刮过
    WIND,           // 大风：水平短划 / < 字符流
}

// 每一种 WeatherCondition 都有自己的一套，不再几种天气挤同一个分支（v0.0.9）。
// night 由日出日落推出：小米 weathercn 的国标现象码没有昼夜变体，
// 只看 condition 会让夜里的晴天也走白天那套呼吸网格。
fun ambienceKindOf(condition: WeatherCondition?, night: Boolean = false): AmbienceKind =
    when (condition) {
        null -> AmbienceKind.NONE
        WeatherCondition.UNKNOWN -> AmbienceKind.NONE
        WeatherCondition.CLEAR -> if (night) AmbienceKind.STARFIELD else AmbienceKind.CLEAR_DAY
        WeatherCondition.CLEAR_NIGHT -> AmbienceKind.STARFIELD
        WeatherCondition.PARTLY_CLOUDY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> AmbienceKind.PARTLY_CLOUDY
        WeatherCondition.CLOUDY, WeatherCondition.OVERCAST -> AmbienceKind.OVERCAST
        WeatherCondition.DRIZZLE -> AmbienceKind.DRIZZLE
        WeatherCondition.RAIN -> AmbienceKind.RAIN
        WeatherCondition.SLEET -> AmbienceKind.SLEET
        WeatherCondition.SNOW -> AmbienceKind.SNOW
        WeatherCondition.THUNDERSTORM -> AmbienceKind.STORM
        WeatherCondition.HAIL -> AmbienceKind.HAIL
        WeatherCondition.FREEZING_RAIN, WeatherCondition.FREEZING_DRIZZLE -> AmbienceKind.FREEZING_RAIN
        WeatherCondition.FOG -> AmbienceKind.FOG
        WeatherCondition.HAZE -> AmbienceKind.HAZE
        WeatherCondition.SAND -> AmbienceKind.SAND
        WeatherCondition.WIND -> AmbienceKind.WIND
    }

data class AmbienceSpec(
    val kind: AmbienceKind,
    val intensity: WeatherIntensity = WeatherIntensity.MODERATE,
    val windDirectionDeg: Float = 270f,
    val windSpeedKmh: Float = 0f,
    val visibilityKm: Float? = null,
    val cloudCover: Float? = null,
    val precipMmPerHour: Float = 0f,
    val shower: Boolean = false,
    val aqi: Int? = null,
    val thermal: ThermalModifier = ThermalModifier.NONE,
)

fun ambienceSpecOf(data: WeatherData?, night: Boolean = false): AmbienceSpec {
    val current = data?.current
    val profile = current?.profile
    val inferredIntensity = when {
        profile?.intensity != null -> profile.intensity
        (current?.precipMm ?: 0.0) >= 7.6 -> WeatherIntensity.HEAVY
        (current?.precipMm ?: 0.0) >= 2.5 -> WeatherIntensity.MODERATE
        else -> WeatherIntensity.LIGHT
    }
    return AmbienceSpec(
        kind = ambienceKindOf(current?.condition, night),
        intensity = inferredIntensity,
        windDirectionDeg = current?.windDirectionDeg?.toFloat() ?: 270f,
        windSpeedKmh = current?.windSpeed?.toFloat()?.coerceAtLeast(0f) ?: 0f,
        visibilityKm = current?.visibility?.toFloat(),
        cloudCover = current?.cloudCover?.toFloat(),
        precipMmPerHour = current?.precipMm?.toFloat()?.coerceAtLeast(0f) ?: 0f,
        shower = profile?.shower == true,
        aqi = data?.aqi?.value,
        thermal = profile?.thermal ?: ThermalModifier.NONE,
    )
}

fun AmbienceKind.isDynamic(): Boolean = this in setOf(
    AmbienceKind.CLEAR_DAY,
    AmbienceKind.STARFIELD,
    AmbienceKind.PARTLY_CLOUDY,
    AmbienceKind.OVERCAST,
    AmbienceKind.DRIZZLE,
    AmbienceKind.RAIN,
    AmbienceKind.SLEET,
    AmbienceKind.SNOW,
    AmbienceKind.STORM,
    AmbienceKind.HAIL,
    AmbienceKind.FREEZING_RAIN,
    AmbienceKind.FOG,
    AmbienceKind.HAZE,
    AmbienceKind.SAND,
    AmbienceKind.WIND,
)

// 「现在是不是夜里」：有日出日落就按当地日出日落，缺一个就退回 06:00 / 19:00。
// minutes 全部是当天零点起的分钟数。极昼极夜（日出等于日落）也退回固定档，
// 不去猜——氛围层猜错只是背景不对，不值得为它编数据。
fun isNightAt(sunrise: String?, sunset: String?, nowMinutes: Int): Boolean {
    val rise = clockMinutes(sunrise)
    val set = clockMinutes(sunset)
    if (rise == null || set == null || rise >= set) {
        return nowMinutes < 6 * 60 || nowMinutes >= 19 * 60
    }
    return nowMinutes < rise || nowMinutes >= set
}

// "06:12" / "6:12" / "2026-08-25T06:12+08:00" 都能取到时分；取不到返回 null。
internal fun clockMinutes(raw: String?): Int? {
    val s = raw?.trim().orEmpty()
    if (s.isEmpty()) return null
    val colon = s.indexOf(':')
    if (colon <= 0) return null
    val h = s.substring(0, colon).takeLast(2).trimStart('T', '-', ' ').toIntOrNull() ?: return null
    val m = s.substring(colon + 1).take(2).toIntOrNull() ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return h * 60 + m
}
