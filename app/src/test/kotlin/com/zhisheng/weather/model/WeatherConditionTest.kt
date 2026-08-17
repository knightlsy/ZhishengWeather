package com.zhisheng.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun chinaCodesMapToFineGrainedConditions() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromCode("00"))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherCondition.fromCode("1"))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromCode("02"))
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromCode("7"))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromCode("08"))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromCode("4"))
        assertEquals(WeatherCondition.SAND, WeatherCondition.fromCode("20"))
        assertEquals("小雨", WeatherCondition.chinaLabel("07"))
        assertEquals("中雨", WeatherCondition.chinaLabel("8"))
        assertEquals("晴转雷阵雨", WeatherCondition.turnPhrase("0", "4"))
    }

    @Test
    fun accuAndChinaShareNumbersButNotMeanings() {
        assertEquals(WeatherCondition.DRIZZLE, WeatherCondition.fromXiaomi("7", "weathercn:101160601"))
        assertEquals(WeatherCondition.OVERCAST, WeatherCondition.fromXiaomi("7", "accu:123"))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromXiaomi("18", "weathercn:101010100"))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromXiaomi("18", "accu:123"))
    }

    @Test
    fun moreSignificantPrefersThunderOverClear() {
        assertEquals(
            WeatherCondition.THUNDERSTORM,
            WeatherCondition.moreSignificant(WeatherCondition.CLEAR, WeatherCondition.THUNDERSTORM),
        )
        assertEquals(
            WeatherCondition.DRIZZLE,
            WeatherCondition.moreSignificant(WeatherCondition.OVERCAST, WeatherCondition.DRIZZLE),
        )
    }

    @Test
    fun precipitationFlagCoversRainFamily() {
        assertTrue(WeatherCondition.DRIZZLE.isPrecipitation)
        assertTrue(WeatherCondition.THUNDERSTORM.isPrecipitation)
        assertFalse(WeatherCondition.OVERCAST.isPrecipitation)
        assertFalse(WeatherCondition.CLEAR.isPrecipitation)
    }
}
