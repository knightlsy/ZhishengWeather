package com.zhisheng.weather.model

// WMO weather_code → 条件枚举（单一真源）。
// v0.0.4 合并原双份映射：OpenMeteoSource.wmo（带昼夜变体）与 WeatherRepository.fromWmoCode（无变体），
// 两处行为不一致会漂移。isDay 只影响 0/1/2 三个码；逐日行代表整天，固定传 true。
fun wmoToCondition(code: Int?, isDay: Boolean = true): WeatherCondition =
    wmoProfile(code, isDay).condition

fun wmoProfile(code: Int?, isDay: Boolean = true): WeatherProfile {
    val raw = code?.toString()
    fun p(
        condition: WeatherCondition,
        intensity: WeatherIntensity? = null,
        phase: PrecipitationPhase = PrecipitationPhase.NONE,
        shower: Boolean = false,
        thunder: Boolean = false,
        freezing: Boolean = false,
    ) = WeatherProfile(condition, intensity, phase, shower, thunder, freezing, source = "OPEN-METEO", rawCode = raw)
    return when (code) {
        0 -> p(if (isDay) WeatherCondition.CLEAR else WeatherCondition.CLEAR_NIGHT)
        1, 2 -> p(if (isDay) WeatherCondition.PARTLY_CLOUDY else WeatherCondition.PARTLY_CLOUDY_NIGHT)
        3 -> p(WeatherCondition.OVERCAST)
        45 -> p(WeatherCondition.FOG, WeatherIntensity.LIGHT)
        48 -> p(WeatherCondition.FOG, WeatherIntensity.HEAVY)
        51 -> p(WeatherCondition.DRIZZLE, WeatherIntensity.LIGHT, PrecipitationPhase.RAIN)
        53 -> p(WeatherCondition.DRIZZLE, WeatherIntensity.MODERATE, PrecipitationPhase.RAIN)
        55 -> p(WeatherCondition.DRIZZLE, WeatherIntensity.HEAVY, PrecipitationPhase.RAIN)
        56 -> p(WeatherCondition.FREEZING_DRIZZLE, WeatherIntensity.LIGHT, PrecipitationPhase.FREEZING_DRIZZLE, freezing = true)
        57 -> p(WeatherCondition.FREEZING_DRIZZLE, WeatherIntensity.HEAVY, PrecipitationPhase.FREEZING_DRIZZLE, freezing = true)
        61 -> p(WeatherCondition.RAIN, WeatherIntensity.LIGHT, PrecipitationPhase.RAIN)
        63 -> p(WeatherCondition.RAIN, WeatherIntensity.MODERATE, PrecipitationPhase.RAIN)
        65 -> p(WeatherCondition.RAIN, WeatherIntensity.HEAVY, PrecipitationPhase.RAIN)
        66 -> p(WeatherCondition.FREEZING_RAIN, WeatherIntensity.LIGHT, PrecipitationPhase.FREEZING_RAIN, freezing = true)
        67 -> p(WeatherCondition.FREEZING_RAIN, WeatherIntensity.HEAVY, PrecipitationPhase.FREEZING_RAIN, freezing = true)
        71 -> p(WeatherCondition.SNOW, WeatherIntensity.LIGHT, PrecipitationPhase.SNOW)
        73 -> p(WeatherCondition.SNOW, WeatherIntensity.MODERATE, PrecipitationPhase.SNOW)
        75 -> p(WeatherCondition.SNOW, WeatherIntensity.HEAVY, PrecipitationPhase.SNOW)
        77 -> p(WeatherCondition.SNOW, WeatherIntensity.LIGHT, PrecipitationPhase.SNOW)
        80 -> p(WeatherCondition.RAIN, WeatherIntensity.LIGHT, PrecipitationPhase.RAIN, shower = true)
        81 -> p(WeatherCondition.RAIN, WeatherIntensity.MODERATE, PrecipitationPhase.RAIN, shower = true)
        82 -> p(WeatherCondition.RAIN, WeatherIntensity.HEAVY, PrecipitationPhase.RAIN, shower = true)
        85 -> p(WeatherCondition.SNOW, WeatherIntensity.LIGHT, PrecipitationPhase.SNOW, shower = true)
        86 -> p(WeatherCondition.SNOW, WeatherIntensity.HEAVY, PrecipitationPhase.SNOW, shower = true)
        95 -> p(WeatherCondition.THUNDERSTORM, WeatherIntensity.MODERATE, PrecipitationPhase.RAIN, shower = true, thunder = true)
        96, 99 -> p(WeatherCondition.HAIL, WeatherIntensity.HEAVY, PrecipitationPhase.HAIL, shower = true, thunder = true)
        else -> p(WeatherCondition.UNKNOWN)
    }
}
