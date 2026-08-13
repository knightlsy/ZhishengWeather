package com.zhisheng.weather.model

// WMO weather_code → 条件枚举（单一真源）。
// v0.0.4 合并原双份映射：OpenMeteoSource.wmo（带昼夜变体）与 WeatherRepository.fromWmoCode（无变体），
// 两处行为不一致会漂移。isDay 只影响 0/1/2 三个码；逐日行代表整天，固定传 true。
fun wmoToCondition(code: Int?, isDay: Boolean = true): WeatherCondition = when (code) {
    0 -> if (isDay) WeatherCondition.CLEAR else WeatherCondition.CLEAR_NIGHT
    1, 2 -> if (isDay) WeatherCondition.PARTLY_CLOUDY else WeatherCondition.PARTLY_CLOUDY_NIGHT
    3 -> WeatherCondition.OVERCAST
    45, 48 -> WeatherCondition.FOG
    51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
    95, 96, 99 -> WeatherCondition.THUNDERSTORM
    else -> WeatherCondition.CLOUDY
}
