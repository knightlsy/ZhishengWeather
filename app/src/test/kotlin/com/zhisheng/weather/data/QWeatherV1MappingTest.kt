package com.zhisheng.weather.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class QWeatherV1MappingTest {
    @Test
    fun decimalPrecipitationProbabilityParsesAndNormalizes() {
        val parsed = Json.decodeFromString<QwHourly>(
            """{"hours":[{"forecastTime":"2026-08-27T00:00+08:00","precipitation":{"probability":0.31}}]}""",
        )

        assertEquals(0.31, parsed.hours.single().precipitation?.probability ?: -1.0, 0.0001)
        assertEquals(31, WeatherRepository.normalizeQwProbability(0.31))
    }

    @Test
    fun probabilityNormalizationAlsoAcceptsPercentShapedFallbacks() {
        assertEquals(31, WeatherRepository.normalizeQwProbability(31.0))
        assertEquals(100, WeatherRepository.normalizeQwProbability(120.0))
        assertEquals(0, WeatherRepository.normalizeQwProbability(-0.2))
    }
}
