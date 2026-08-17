package com.zhisheng.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NowcastTest {

    private val t0 = 1_700_000_000_000L

    @Test
    fun minuteSeriesAlignsIndexToTimestamp() {
        val series = Nowcast.minuteSeries(listOf(0f, 0.2f, 0.4f), t0)
        assertEquals(3, series.size)
        assertEquals(t0, series[0].timeMillis)
        assertEquals(t0 + 60_000L, series[1].timeMillis)
        assertEquals(0.4f, series[2].precip)
    }

    @Test
    fun rainTimingDetectsRainNow() {
        val minutes = Nowcast.minuteSeries(listOf(0.2f, 0.3f), t0)
        val timing = Nowcast.rainTiming(minutes, t0)
        assertTrue(timing.rainingNow)
        assertEquals(0, timing.minutesUntilStart)
        assertEquals("正在下雨", Nowcast.rainTimingLabel(timing))
    }

    @Test
    fun rainTimingReportsMinutesUntilStart() {
        val values = MutableList(40) { 0f }
        values[35] = 0.2f
        val minutes = Nowcast.minuteSeries(values, t0)
        val timing = Nowcast.rainTiming(minutes, t0)
        assertFalse(timing.rainingNow)
        assertEquals(35, timing.minutesUntilStart)
        assertEquals("35 分钟后开始下雨", Nowcast.rainTimingLabel(timing))
    }

    @Test
    fun rainTimingIgnoresDrySeries() {
        val minutes = Nowcast.minuteSeries(List(120) { 0f }, t0)
        val timing = Nowcast.rainTiming(minutes, t0)
        assertFalse(timing.hasRain)
        assertNull(Nowcast.rainTimingLabel(timing))
    }

    @Test
    fun briefingPrefersComputedTimingWhenSeriesHasRain() {
        val values = MutableList(20) { 0f }
        values[10] = 0.5f
        val data = WeatherData(
            rainNowcast = "距离最近的降雨约在38公里以外~",
            rainMinutes = Nowcast.minuteSeries(values, t0),
        )
        assertEquals("10 分钟后开始下雨", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingPrefersRainOverTemperature() {
        val values = MutableList(20) { 0f }
        values[10] = 0.5f
        val data = WeatherData(
            rainMinutes = Nowcast.minuteSeries(values, t0),
            daily = listOf(
                DailyWeather(dateMillis = t0, high = 30.0, low = 20.0),
                DailyWeather(dateMillis = t0 + 86_400_000L, high = 20.0, low = 12.0),
            ),
        )
        assertEquals("10 分钟后开始下雨", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingUsesProviderNowcastWhenMinuteSeriesIsDry() {
        val data = WeatherData(
            rainNowcast = "未来两小时近处有雨约38公里外",
            rainMinutes = Nowcast.minuteSeries(List(120) { 0f }, t0),
        )
        assertEquals("未来两小时近处有雨约38公里外", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingKeepsNoRainNowcastFromApi() {
        val data = WeatherData(rainNowcast = "未来两小时不会下雨，放心出门吧")
        assertEquals("未来两小时不会下雨，放心出门吧", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingReportsTomorrowColder() {
        val data = WeatherData(
            daily = listOf(
                DailyWeather(dateMillis = t0, high = 28.0, low = 18.0),
                DailyWeather(dateMillis = t0 + 86_400_000L, high = 22.0, low = 14.0),
            ),
        )
        assertEquals("明天比今天低 6°", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingIgnoresSmallTemperatureSwing() {
        val data = WeatherData(
            daily = listOf(
                DailyWeather(dateMillis = t0, high = 20.0, low = 12.0),
                DailyWeather(dateMillis = t0 + 86_400_000L, high = 21.0, low = 12.0),
            ),
        )
        assertNull(Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun briefingPrefersRedAlertOverMildAlert() {
        val data = WeatherData(
            alerts = listOf(
                AlertInfo(title = "大风蓝色预警", severity = AlertLevel.BLUE),
                AlertInfo(title = "暴雨红色预警", severity = AlertLevel.RED),
            ),
        )
        assertEquals("暴雨红色预警", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun tidyCopyStripsTrailingWaveDash() {
        assertEquals(
            "未来两小时不会下雨，您可以放心出门",
            Nowcast.tidyCopy("未来两小时不会下雨，您可以放心出门~"),
        )
    }

    @Test
    fun looksLikeIncomingRainRejectsNegativesAndFarAway() {
        assertFalse(Nowcast.looksLikeIncomingRain("未来两小时无降水"))
        assertFalse(Nowcast.looksLikeIncomingRain("距离最近的降雨约在38公里以外"))
        assertTrue(Nowcast.looksLikeIncomingRain("35分钟后有雨"))
        assertTrue(Nowcast.looksLikeIncomingRain("正在下雨"))
    }

    @Test
    fun rainTimingIgnoresRainThatAlreadyEnded() {
        val values = MutableList(40) { if (it < 10) 0.2f else 0f }
        val timing = Nowcast.rainTiming(Nowcast.minuteSeries(values, t0), t0 + 25 * 60_000L)
        assertFalse(timing.rainingNow)
        assertNull(timing.minutesUntilStart)
        assertNull(Nowcast.rainTimingLabel(timing))
    }

    @Test
    fun briefingKeepsRainingWhenApiSaysNoRain() {
        val data = WeatherData(
            current = CurrentWeather(condition = WeatherCondition.DRIZZLE, weatherText = "小雨"),
            rainNowcast = "未来两小时不会下雨，放心出门吧",
            rainMinutes = Nowcast.minuteSeries(List(120) { 0f }, t0),
        )
        assertEquals("正在下雨", Nowcast.briefingLine(data, "c", t0))
    }

    @Test
    fun rainTimingReportsMinutesUntilEnd() {
        val values = MutableList(40) { if (it < 23) 0.03f else 0f }
        val timing = Nowcast.rainTiming(Nowcast.minuteSeries(values, t0), t0)
        assertTrue(timing.rainingNow)
        assertEquals(23, timing.minutesUntilEnd)
        assertEquals("23 分钟后雨会停", Nowcast.rainTimingLabel(timing))
    }

    @Test
    fun densifyTurnsFifteenMinuteBucketsIntoMinuteBars() {
        val coarse = Nowcast.minuteSeries(listOf(0f, 0f, 0.4f, 0.4f, 0f, 0f, 0f, 0f), t0, 15 * 60_000L)
        val dense = Nowcast.densifyToMinutes(coarse)
        assertEquals(120, dense.size)
        assertEquals(t0, dense[0].timeMillis)
        assertEquals(0f, dense[29].precip)
        assertEquals(0.4f, dense[30].precip)
        assertEquals(0.4f, dense[44].precip)
        assertEquals("+119min", Nowcast.horizonLabel(dense))
    }

    @Test
    fun densifyKeepsAlreadyMinuteSeriesShape() {
        val minutes = Nowcast.minuteSeries(List(120) { if (it == 10) 0.5f else 0f }, t0)
        val dense = Nowcast.densifyToMinutes(minutes)
        assertEquals(120, dense.size)
        assertEquals(0.5f, dense[10].precip)
        assertEquals(0f, dense[11].precip)
    }
}
