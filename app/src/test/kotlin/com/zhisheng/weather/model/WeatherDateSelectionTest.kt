package com.zhisheng.weather.model

import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherDateSelectionTest {
    private val offsetSeconds = 8 * 3_600
    private val zone = ZoneOffset.ofHours(8)

    private fun at(day: Int, hour: Int = 0): Long =
        LocalDateTime.of(2026, 8, day, hour, 0).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun exactCityCalendarDateIsSelected() {
        val data = WeatherData(
            daily = listOf(
                DailyWeather(at(26), high = 30.0),
                DailyWeather(at(27), high = 28.0),
                DailyWeather(at(28), high = 27.0),
            ),
            utcOffsetSeconds = offsetSeconds,
        )

        assertEquals(28.0, data.todayDaily(at(27, 12))?.high!!, 0.0001)
        assertEquals(27.0, data.tomorrowDaily(at(27, 12))?.high!!, 0.0001)
        assertEquals(listOf(28.0, 27.0), data.currentAndFutureDaily(at(27, 12)).map { it.high })
    }

    @Test
    fun staleRowsAreNotRelabeledAsTodayOrTomorrow() {
        val data = WeatherData(
            daily = listOf(DailyWeather(at(25), high = 35.0), DailyWeather(at(26), high = 34.0)),
            utcOffsetSeconds = offsetSeconds,
        )

        assertNull(data.todayDaily(at(27, 12)))
        assertNull(data.tomorrowDaily(at(27, 12)))
        assertEquals(emptyList<DailyWeather>(), data.currentAndFutureDaily(at(27, 12)))
    }
}
