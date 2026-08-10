package com.zhisheng.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherRepositoryTest {

    @Test
    fun windDirectionHandlesCardinalAndBoundaryValues() {
        assertEquals("北", WeatherRepository.windDirection(0.0))
        assertEquals("东北", WeatherRepository.windDirection(22.5))
        assertEquals("东", WeatherRepository.windDirection(90.0))
        assertEquals("北", WeatherRepository.windDirection(360.0))
    }

    @Test
    fun windDirectionNormalizesOutOfRangeProviderValues() {
        assertEquals("西", WeatherRepository.windDirection(-90.0))
        assertEquals("东", WeatherRepository.windDirection(450.0))
        assertEquals("北", WeatherRepository.windDirection(720.0))
    }

    @Test
    fun windDirectionRejectsMissingAndNonFiniteValues() {
        assertNull(WeatherRepository.windDirection(null))
        assertNull(WeatherRepository.windDirection(Double.NaN))
        assertNull(WeatherRepository.windDirection(Double.POSITIVE_INFINITY))
        assertNull(WeatherRepository.windDirection(Double.NEGATIVE_INFINITY))
    }
}
