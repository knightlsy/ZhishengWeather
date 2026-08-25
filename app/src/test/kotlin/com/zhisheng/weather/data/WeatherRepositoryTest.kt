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

    @Test
    fun lockedSourceDoesNotAcceptAnotherProvidersCache() {
        assertEquals(true, SourcePref.AUTO.matches("XIAOMI"))
        assertEquals(true, SourcePref.AUTO.matches("OPEN-METEO"))
        assertEquals(false, SourcePref.AUTO.matches("CAIYUN"))
        assertEquals(false, SourcePref.AUTO.matches("QWEATHER"))
        assertEquals(true, SourcePref.XIAOMI.matches("XIAOMI"))
        assertEquals(false, SourcePref.OPEN_METEO.matches("XIAOMI"))
        assertEquals(false, SourcePref.QWEATHER.matches("OPEN-METEO"))
        assertEquals(true, SourcePref.CAIYUN.matches("CAIYUN"))
        assertEquals(false, SourcePref.CAIYUN.matches("XIAOMI"))
        assertEquals(true, SourcePref.OPEN_METEO.matches("OPEN-METEO"))
    }

    @Test
    fun qweatherStaysHiddenUntilDeveloperMode() {
        assertEquals(
            listOf(SourcePref.AUTO, SourcePref.XIAOMI, SourcePref.OPEN_METEO),
            SourcePref.visible(developerMode = false),
        )
        assertEquals(
            listOf(
                SourcePref.AUTO,
                SourcePref.XIAOMI,
                SourcePref.OPEN_METEO,
                SourcePref.CAIYUN,
                SourcePref.QWEATHER,
            ),
            SourcePref.visible(developerMode = true),
        )
    }

    @Test
    fun qweatherLockFallsBackToAutoWithoutDeveloperMode() {
        assertEquals(SourcePref.AUTO, SourcePref.QWEATHER.effective(developerMode = false))
        assertEquals(SourcePref.QWEATHER, SourcePref.QWEATHER.effective(developerMode = true))
        assertEquals(SourcePref.AUTO, SourcePref.CAIYUN.effective(developerMode = false))
        assertEquals(SourcePref.CAIYUN, SourcePref.CAIYUN.effective(developerMode = true))
        assertEquals(SourcePref.AUTO, SourcePref.AUTO.effective(developerMode = false))
        assertEquals(SourcePref.XIAOMI, SourcePref.XIAOMI.effective(developerMode = false))
    }
}
