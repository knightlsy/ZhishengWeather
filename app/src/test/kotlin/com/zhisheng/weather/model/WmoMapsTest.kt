package com.zhisheng.weather.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WmoMapsTest {

    @Test
    fun dayNightVariantsFollowIsDay() {
        assertEquals(WeatherCondition.CLEAR, wmoToCondition(0, true))
        assertEquals(WeatherCondition.CLEAR_NIGHT, wmoToCondition(0, false))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, wmoToCondition(1, true))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, wmoToCondition(2, true))
        assertEquals(WeatherCondition.PARTLY_CLOUDY_NIGHT, wmoToCondition(1, false))
        assertEquals(WeatherCondition.PARTLY_CLOUDY_NIGHT, wmoToCondition(2, false))
    }

    @Test
    fun nonVariantCodesIgnoreIsDay() {
        assertEquals(WeatherCondition.OVERCAST, wmoToCondition(3, false))
        assertEquals(WeatherCondition.FOG, wmoToCondition(45, true))
        assertEquals(WeatherCondition.FOG, wmoToCondition(48, false))
        assertEquals(WeatherCondition.DRIZZLE, wmoToCondition(53, true))
        assertEquals(WeatherCondition.RAIN, wmoToCondition(61, false))
        assertEquals(WeatherCondition.RAIN, wmoToCondition(80, true))
        assertEquals(WeatherCondition.SNOW, wmoToCondition(85, false))
        assertEquals(WeatherCondition.THUNDERSTORM, wmoToCondition(95, true))
        assertEquals(WeatherCondition.THUNDERSTORM, wmoToCondition(99, false))
    }

    @Test
    fun unknownCodesFallBackToCloudy() {
        assertEquals(WeatherCondition.CLOUDY, wmoToCondition(999, true))
        assertEquals(WeatherCondition.CLOUDY, wmoToCondition(-1, false))
        assertEquals(WeatherCondition.CLOUDY, wmoToCondition(null, true))
    }
}
