package com.tianqi.weather.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.tianqi.weather.model.WeatherCondition

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

    @Test
    fun currentPrecipitationUsesIntensityInsteadOfHourlyAmount() {
        val parsed = Json.decodeFromString<QwCurrent>(
            """{"precipitation":{"amount":{"value":1.2,"unit":"mm"},"intensity":{"value":0.4,"unit":"mm/h"},"type":"rain"}}""",
        )

        assertEquals(0.4, WeatherRepository.qweatherCurrentPrecipRate(parsed.precipitation)!!, 0.0001)
    }

    @Test
    fun currentPrecipitationOmitsRateWhenOnlyAccumulatedAmountExists() {
        val parsed = Json.decodeFromString<QwCurrent>(
            """{"precipitation":{"amount":{"value":1.2,"unit":"mm"},"type":"rain"}}""",
        )

        assertNull(WeatherRepository.qweatherCurrentPrecipRate(parsed.precipitation))
    }

    @Test
    fun dailyForecastCombinesDayAndNightPrecipitation() {
        val parsed = Json.decodeFromString<QwDaily>(
            """{
                "days":[{
                  "forecastStartTime":"2026-08-27T00:00+08:00",
                  "daytime":{"condition":{"code":"100","text":"晴"},"precipitation":{"amount":{"value":0.2,"unit":"mm"},"probability":0.2}},
                  "nighttime":{"condition":{"code":"305","text":"小雨"},"precipitation":{"amount":{"value":1.3,"unit":"mm"},"probability":0.8}}
                }]
            }""".trimIndent(),
        ).days.single()

        assertEquals(1.5, WeatherRepository.qweatherDailyPrecipMm(parsed)!!, 0.0001)
        assertEquals(80, WeatherRepository.qweatherDailyProbability(parsed))
        assertEquals(WeatherCondition.DRIZZLE, WeatherRepository.qweatherDailyCondition(parsed))
        assertEquals("晴转小雨", WeatherRepository.qweatherDailyText(parsed))
    }

    @Test
    fun decimalQaqiParsesAndLocalStandardWins() {
        val air = Json.decodeFromString<QwAir>(
            """{"indexes":[{"code":"qaqi","aqi":1.4,"aqiDisplay":"1.4"},{"code":"us-epa","aqi":46,"aqiDisplay":"46"}]}""",
        )

        assertEquals(1.4, air.indexes.first().aqi!!, 0.0001)
        assertEquals("us-epa", WeatherRepository.preferredAirIndex(air.indexes)?.code)
    }
}
