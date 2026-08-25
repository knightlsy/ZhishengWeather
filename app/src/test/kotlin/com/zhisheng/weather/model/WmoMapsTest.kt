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
        assertEquals(WeatherCondition.HAIL, wmoToCondition(99, false))
        assertEquals(WeatherCondition.FREEZING_DRIZZLE, wmoToCondition(56, true))
        assertEquals(WeatherCondition.FREEZING_RAIN, wmoToCondition(67, false))
    }

    @Test
    fun unknownCodesStayUnknown() {
        assertEquals(WeatherCondition.UNKNOWN, wmoToCondition(999, true))
        assertEquals(WeatherCondition.UNKNOWN, wmoToCondition(-1, false))
        assertEquals(WeatherCondition.UNKNOWN, wmoToCondition(null, true))
    }
}
