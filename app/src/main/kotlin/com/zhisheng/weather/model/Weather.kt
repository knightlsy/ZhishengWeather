package com.zhisheng.weather.model

import kotlinx.serialization.Serializable

// 枳生天气 · UI 数据模型

@Serializable
data class City(
    val name: String,
    val affiliation: String,
    val latitude: Double,
    val longitude: Double,
    val locationKey: String,
)

// 以下模型全部 @Serializable：离线缓存（WeatherCache）按城市持久化最近一次 WeatherData（v0.0.4）
@Serializable
data class CurrentWeather(
    val temperature: Double? = null,
    val feelsLike: Double? = null,
    val condition: WeatherCondition? = null,
    val weatherText: String? = null,
    val humidity: Double? = null,
    val windSpeed: Double? = null,
    val windDirectionDeg: Double? = null,
    val pressure: Double? = null,
    val uvIndex: Int? = null,
    val visibility: Double? = null,
    val dewPoint: Double? = null,
    val cloudCover: Double? = null,
    val windGust: Double? = null,
    val precipMm: Double? = null,
)

@Serializable
data class HourlyWeather(
    val timeMillis: Long,
    val temperature: Double? = null,
    val condition: WeatherCondition? = null,
    val windSpeed: Double? = null,
    val precipProb: Int? = null,
    val aqi: Int? = null,
)

@Serializable
data class MinutePrecip(
    val timeMillis: Long,
    val precip: Float,
)

@Serializable
data class YesterdayInfo(
    val high: Double? = null,
    val low: Double? = null,
    val aqi: Int? = null,
    val condition: WeatherCondition? = null,
)

@Serializable
data class TyphoonInfo(
    val name: String? = null,
    val ename: String? = null,
    val type: String? = null,
    val windSpeed: Double? = null,
)

@Serializable
data class DailyWeather(
    val dateMillis: Long,
    val high: Double? = null,
    val low: Double? = null,
    val condition: WeatherCondition? = null,
    val windSpeed: Double? = null,
    val precipProbability: Int? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    val moonPhase: String? = null,
)

@Serializable
data class AqiInfo(
    val value: Int? = null,
    val level: String? = null,
    val primary: String? = null,
    val pm25: String? = null,
    val pm10: String? = null,
    val o3: String? = null,
    val no2: String? = null,
    val so2: String? = null,
    val co: String? = null,
    // 健康建议文案（小米 suggest，v0.0.4 接入；其余源为空）
    val suggest: String? = null,
)

@Serializable
data class LifeIndexExtra(
    val name: String,
    val en: String,
    val category: String,
)

@Serializable
data class AlertInfo(
    val title: String,
    val detail: String? = null,
    val level: String? = null,
    val pubTime: String? = null,
    // 三源等级归一（和风 severity 英文枚举 / 小米 level 中文），UI 按档着色（v0.0.4）
    val severity: AlertLevel = AlertLevel.UNKNOWN,
)

// 预警四档（国标蓝/黄/橙/红）
@Serializable
enum class AlertLevel {
    BLUE, YELLOW, ORANGE, RED, UNKNOWN
}

// 三源等级归一：中文色名（小米 level「黄色预警」）/ 英文色名（和风 color.code）/ 英文严重度（和风 severity）
fun alertLevelOf(raw: String?): AlertLevel {
    val r = raw?.trim().orEmpty()
    return when {
        r.isEmpty() -> AlertLevel.UNKNOWN
        r.contains("红") || r.equals("red", ignoreCase = true) -> AlertLevel.RED
        r.contains("橙") || r.equals("orange", ignoreCase = true) -> AlertLevel.ORANGE
        r.contains("黄") || r.equals("yellow", ignoreCase = true) -> AlertLevel.YELLOW
        r.contains("蓝") || r.equals("blue", ignoreCase = true) -> AlertLevel.BLUE
        // 和风 severity 英文枚举（无 color.code 时兜底）：国内 minor→蓝 / moderate→黄 / severe→橙 / extreme→红；
        // major/standard 为澳洲等地的补充档，就近归入橙/蓝（0.0.4 修复：此前英文枚举不匹配、全落 UNKNOWN）
        r.equals("extreme", ignoreCase = true) -> AlertLevel.RED
        r.equals("severe", ignoreCase = true) || r.equals("major", ignoreCase = true) -> AlertLevel.ORANGE
        r.equals("moderate", ignoreCase = true) -> AlertLevel.YELLOW
        r.equals("minor", ignoreCase = true) || r.equals("standard", ignoreCase = true) -> AlertLevel.BLUE
        else -> AlertLevel.UNKNOWN
    }
}

@Serializable
data class WeatherData(
    val current: CurrentWeather? = null,
    val hourly: List<HourlyWeather> = emptyList(),
    val daily: List<DailyWeather> = emptyList(),
    val aqi: AqiInfo? = null,
    val alerts: List<AlertInfo> = emptyList(),
    val updateTime: Long? = null,
    val rainNowcast: String? = null,
    val rainMinutes: List<MinutePrecip> = emptyList(),
    val carWashOk: Boolean? = null,
    val sportsOk: Boolean? = null,
    val extraIndices: List<LifeIndexExtra> = emptyList(),
    val yesterday: YesterdayInfo? = null,
    val typhoons: List<TyphoonInfo> = emptyList(),
    // 雨区距离（km）：小米分钟降水 kmNum，仅该源返回时非空（v0.0.4）
    val rainDistanceKm: Double? = null,
    val dataSource: String? = null,
    val error: String? = null,
)

@Serializable
enum class WeatherCondition(val label: String) {
    CLEAR("晴"),
    CLEAR_NIGHT("晴"),
    PARTLY_CLOUDY("多云"),
    PARTLY_CLOUDY_NIGHT("多云"),
    CLOUDY("阴"),
    OVERCAST("阴"),
    RAIN("雨"),
    DRIZZLE("小雨"),
    THUNDERSTORM("雷阵雨"),
    SNOW("雪"),
    SLEET("雨夹雪"),
    FOG("雾"),
    HAZE("霾"),
    SAND("沙尘"),
    WIND("大风");

    companion object {
        fun fromCode(code: String?): WeatherCondition = when (code) {
            "0", "00" -> CLEAR
            "1", "01" -> PARTLY_CLOUDY
            "3", "7", "8", "9", "03", "07", "08", "09", "10", "11", "12", "21", "22", "23", "24", "25" -> RAIN
            "4", "04" -> THUNDERSTORM
            "5", "05" -> RAIN
            "6", "06", "19" -> SLEET
            "13", "14", "15", "16", "17", "26", "27", "28" -> SNOW
            "18", "32", "49", "57" -> FOG
            "20", "29", "30" -> WIND
            "53", "54", "55", "56" -> HAZE
            else -> CLOUDY
        }

        // 和风 condition：icon 带昼夜变体（100 晴日 / 150 晴夜），code 恒为白天码。
        // 优先 icon，缺失时退回 code（v0.0.2：修复夜间显示太阳）
        fun fromQw(icon: String?, code: String?): WeatherCondition =
            fromQwCode(icon?.takeIf { it.isNotBlank() } ?: code)

        // 和风天气图标码 → 条件（1xx 白天 / 15x 夜间 / 3xx 雨 / 4xx 雪 / 5xx 视程）
        fun fromQwCode(code: String?): WeatherCondition = when (code) {
            "100" -> CLEAR
            "150" -> CLEAR_NIGHT
            "101", "102", "103" -> PARTLY_CLOUDY
            "151", "152", "153" -> PARTLY_CLOUDY_NIGHT
            "104" -> OVERCAST
            "302", "303" -> THUNDERSTORM
            "304" -> THUNDERSTORM
            "309", "399" -> DRIZZLE
            "300", "301", "305", "306", "307", "308", "310", "311", "312", "313",
            "314", "315", "316", "317", "318" -> RAIN
            "404", "405" -> SLEET
            "400", "401", "402", "403", "406", "407", "408", "409", "410", "499" -> SNOW
            "500", "501", "509", "510" -> FOG
            "503", "504", "507", "508" -> SAND
            "502", "511", "512", "513", "514", "515" -> HAZE
            else -> CLOUDY
        }
    }
}
