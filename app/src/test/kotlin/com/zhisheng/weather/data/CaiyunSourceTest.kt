package com.zhisheng.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.model.WeatherIntensity

class CaiyunSourceTest {

    @Test
    fun forecastKeypointIsA24HourSummaryNotCurrentWeather() {
        val data = WeatherData(
            current = CurrentWeather(condition = WeatherCondition.CLEAR, weatherText = "晴"),
            rainNowcast = "未来两小时无降水",
            forecastSummary = "多云，今天晚上22点钟后转小雨，其后晴",
            dataSource = "CAIYUN",
        )

        assertEquals("晴", data.current?.weatherText)
        assertEquals("未来两小时无降水", data.rainNowcast)
        assertEquals("多云，今天晚上22点钟后转小雨，其后晴", data.forecastSummary)
    }

    @Test
    fun hourlyProbabilityAcceptsPaidPlanPercentScale() {
        assertEquals(37, CaiyunSource.normalizeProbability(CaiyunTimed(probability = 37.0).probability))
        assertEquals(37, CaiyunSource.normalizeProbability(CaiyunTimed(probability = 0.37).probability))
    }

    @Test
    fun hailAndIntensityRemainDistinct() {
        assertEquals(WeatherCondition.HAIL, CaiyunSource.skycon("LIGHT_HAIL"))
        assertEquals(WeatherIntensity.HEAVY, CaiyunSource.skyconProfile("HEAVY_RAIN")?.intensity)
        assertEquals(WeatherCondition.UNKNOWN, CaiyunSource.skycon("FUTURE_CODE"))
    }
    @Test
    fun mapsEveryLifeIndexReturnedByCurrentAccount() {
        val item = { desc: String -> listOf(CaiyunLifeIndexItem(desc = desc)) }
        val indices = CaiyunSource.mapLifeIndices(
            CaiyunLifeIndex(
                ultraviolet = item("最弱"),
                carWashing = item("适宜"),
                dressing = item("温暖"),
                comfort = item("舒适"),
                coldRisk = item("少发"),
            ),
        )

        assertEquals(listOf("紫外线", "洗车", "穿衣", "舒适", "感冒"), indices.map { it.name })
        assertEquals(listOf("最弱", "适宜", "温暖", "舒适", "少发"), indices.map { it.category })
    }

    @Test
    fun absentPaidBlockDoesNotCreateFakeIndices() {
        assertTrue(CaiyunSource.mapLifeIndices(null).isEmpty())
    }

    @Test
    fun probabilityAcceptsBothCaiyunValueConventionsWithoutDoubleScaling() {
        assertEquals(60, CaiyunSource.normalizeProbability(0.60))
        assertEquals(60, CaiyunSource.normalizeProbability(60.0))
        assertEquals(100, CaiyunSource.normalizeProbability(1.0))
        assertEquals(86, CaiyunSource.normalizeProbability(0.86))
        assertNull(CaiyunSource.normalizeProbability(6000.0))
        assertNull(CaiyunSource.normalizeProbability(Double.NaN))
    }

    @Test
    fun naiveDatetimeUsesFallbackOffsetInsteadOfPhoneZone() {
        val tokyo = 9 * 3_600
        val ms = CaiyunSource.parseTime("2026-08-26T09:00:00", tokyo)
        assertEquals(java.time.Instant.parse("2026-08-26T00:00:00Z").toEpochMilli(), ms)
    }

    @Test
    fun humidityAndCloudCoverAcceptBothRatioAndPercent() {
        assertEquals(65.0, CaiyunSource.ratioToPercent(0.65)!!, 0.0001)
        assertEquals(65.0, CaiyunSource.ratioToPercent(65.0)!!, 0.0001)
        assertNull(CaiyunSource.ratioToPercent(6500.0))
        assertNull(CaiyunSource.ratioToPercent(-0.1))
    }

    @Test
    fun dailyDayAndNightSkyconBecomeATurnPhrase() {
        val mapped = CaiyunSource.dailyDayNight("CLEAR_DAY", "CLOUDY", "PARTLY_CLOUDY_DAY")
        assertEquals(WeatherCondition.OVERCAST, mapped.first)
        assertEquals("晴转阴", mapped.second)
    }
}
