package com.tianqi.weather.model

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
        if (data.error != null) return dropPastHourly(data, nowMillis)
        var d = dropPastHourly(data, nowMillis)
        if (d.current == null) return d
        d = ensureCurrentHour(d, nowMillis)
        d = syncCurrentWithNowcast(d, nowMillis)
        d = overlayCurrentOntoNowHour(d, nowMillis)
        d = reconcileNowcastText(d, nowMillis)
        return d
    }

    // 与逐时 UI 共用同一格「现在」：优先包含当前时刻的小时格（10:50 属于 10:00），
    // 找不到时才看 10 分钟内即将开始的下一整点，或已被裁掉的过去 40 分钟格。
    fun currentHourIndex(
        hourly: List<HourlyWeather>,
        nowMillis: Long,
    ): Int {
        if (hourly.isEmpty()) return -1
        val containing = hourly.indexOfFirst { h ->
            h.timeMillis <= nowMillis && nowMillis < h.timeMillis + 3_600_000L
        }
        if (containing >= 0) return containing
        val upcoming = hourly.indexOfFirst { h ->
            val delta = h.timeMillis - nowMillis
            delta in 0..NOW_HOUR_FUTURE_MS
        }
        if (upcoming >= 0) return upcoming
        return hourly.indices
            .filter { hourly[it].timeMillis < nowMillis && nowMillis - hourly[it].timeMillis <= NOW_HOUR_PAST_MS }
            .minByOrNull { nowMillis - hourly[it].timeMillis }
            ?: -1
    }

    fun upcomingHourStartIndex(hourly: List<HourlyWeather>, nowMillis: Long): Int {
        val current = currentHourIndex(hourly, nowMillis)
        return if (current >= 0) current + 1 else 0
    }

    internal fun ensureCurrentHour(data: WeatherData, nowMillis: Long): WeatherData {
        val cur = data.current ?: return data
        if (currentHourIndex(data.hourly, nowMillis) >= 0) return data
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
                precipMm = cur.precipMm?.takeIf { it > 0.05 } ?: intensity.toDouble(),
            ),
        )
    }

    internal fun overlayCurrentOntoNowHour(data: WeatherData, nowMillis: Long): WeatherData {
        val cur = data.current ?: return data
        val idx = currentHourIndex(data.hourly, nowMillis)
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
}
