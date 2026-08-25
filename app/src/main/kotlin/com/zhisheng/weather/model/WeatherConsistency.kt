package com.zhisheng.weather.model

import kotlin.math.abs

// 把同一份 WeatherData 里互相打架的信号收成一套「现在」。
// 实况、逐时第一格、短时降水文案必须能同时成立，不能上头下雨、下面写没雨。
object WeatherConsistency {
    const val PAST_HOUR_GRACE_MS = 50 * 60_000L
    const val NOW_HOUR_PAST_MS = 40 * 60_000L
    const val NOW_HOUR_FUTURE_MS = 10 * 60_000L

    fun dropPastHourly(
        data: WeatherData,
        nowMillis: Long = System.currentTimeMillis(),
    ): WeatherData {
        if (data.hourly.isEmpty()) return data
        val kept = data.hourly.filter { it.timeMillis >= nowMillis - PAST_HOUR_GRACE_MS }
        return if (kept.size == data.hourly.size) data else data.copy(hourly = kept)
    }

    fun align(
        data: WeatherData,
        nowMillis: Long = System.currentTimeMillis(),
    ): WeatherData {
        if (data.error != null || data.current == null) return data
        var d = dropPastHourly(data, nowMillis)
        d = ensureCurrentHour(d, nowMillis)
        d = syncCurrentWithNowcast(d, nowMillis)
        d = overlayCurrentOntoNowHour(d, nowMillis)
        d = reconcileNowcastText(d, nowMillis)
        return d
    }

    internal fun ensureCurrentHour(data: WeatherData, nowMillis: Long): WeatherData {
        val cur = data.current ?: return data
        val coversNow = data.hourly.any { isCurrentSlot(it.timeMillis, nowMillis) }
        if (coversNow) return data
        val nowHour = HourlyWeather(
            timeMillis = nowMillis,
            temperature = cur.temperature,
            condition = cur.condition,
            windSpeed = cur.windSpeed,
            profile = cur.profile,
        )
        return data.copy(hourly = listOf(nowHour) + data.hourly)
    }

    internal fun syncCurrentWithNowcast(data: WeatherData, nowMillis: Long): WeatherData {
        val cur = data.current ?: return data
        val seriesWet = Nowcast.seriesWetAt(data.rainMinutes, nowMillis)
        if (cur.condition?.isPrecipitation == true || !seriesWet) return data
        val nearby = data.rainMinutes.filter { abs(it.timeMillis - nowMillis) <= Nowcast.NOW_WINDOW_MS }
        val intensity = nearby.maxOfOrNull { it.precip } ?: 0f
        val phase = nearby.maxByOrNull { it.precip }?.phase ?: cur.profile?.phase ?: PrecipitationPhase.RAIN
        val upgraded = when (phase) {
            PrecipitationPhase.SNOW -> WeatherCondition.SNOW
            PrecipitationPhase.MIXED -> WeatherCondition.SLEET
            PrecipitationPhase.FREEZING_RAIN -> WeatherCondition.FREEZING_RAIN
            PrecipitationPhase.FREEZING_DRIZZLE -> WeatherCondition.FREEZING_DRIZZLE
            PrecipitationPhase.HAIL -> WeatherCondition.HAIL
            else -> if (intensity >= 0.25f) WeatherCondition.RAIN else WeatherCondition.DRIZZLE
        }
        val profile = WeatherProfile(
            condition = upgraded,
            intensity = when {
                intensity >= 7.6f -> WeatherIntensity.HEAVY
                intensity >= 2.5f -> WeatherIntensity.MODERATE
                else -> WeatherIntensity.LIGHT
            },
            phase = phase,
            source = "NOWCAST",
        )
        return data.copy(
            current = cur.copy(
                condition = upgraded,
                weatherText = upgraded.label,
                profile = profile,
            ),
        )
    }

    internal fun overlayCurrentOntoNowHour(data: WeatherData, nowMillis: Long): WeatherData {
        val cur = data.current ?: return data
        val idx = data.hourly.indexOfFirst { isCurrentSlot(it.timeMillis, nowMillis) }
        if (idx < 0) return data
        val hour = data.hourly[idx]
        if (hour.condition == cur.condition && hour.temperature == cur.temperature) return data
        val patched = hour.copy(
            condition = cur.condition ?: hour.condition,
            temperature = cur.temperature ?: hour.temperature,
            windSpeed = cur.windSpeed ?: hour.windSpeed,
            profile = cur.profile ?: hour.profile,
        )
        val hours = data.hourly.toMutableList()
        hours[idx] = patched
        return data.copy(hourly = hours)
    }

    internal fun reconcileNowcastText(data: WeatherData, nowMillis: Long): WeatherData {
        val api = data.rainNowcast?.trim().orEmpty()
        if (api.isEmpty()) return data
        val precipNow = data.current.let { cur ->
            cur != null && (cur.condition?.isPrecipitation == true || (cur.precipMm ?: 0.0) > 0.05)
        }
        val timing = Nowcast.rainTiming(data.rainMinutes, nowMillis, currentPrecip = precipNow)
        if (timing.hasRain && Nowcast.isDryNowcast(api)) {
            return data.copy(rainNowcast = null)
        }
        return data
    }

    private fun isCurrentSlot(timeMillis: Long, nowMillis: Long): Boolean {
        val delta = timeMillis - nowMillis
        return delta in -NOW_HOUR_PAST_MS..NOW_HOUR_FUTURE_MS
    }
}
