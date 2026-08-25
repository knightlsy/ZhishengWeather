package com.zhisheng.weather.ui

import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherIntensity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmosphereLabTest {
    @Test
    fun everyKnownWeatherConditionCanBePreviewed() {
        val previewed = atmosphereScenarios.map { it.condition }.toSet()
        val expected = WeatherCondition.entries.filterNot { it == WeatherCondition.UNKNOWN }.toSet()
        assertEquals(expected, previewed)
    }

    @Test
    fun simulationBuildsSelfContainedWeatherWithoutRealProviderIdentity() {
        atmosphereScenarios.forEach { scenario ->
            val data = simulatedWeather(scenario, WeatherIntensity.HEAVY)
            assertEquals("SIMULATION", data.dataSource)
            assertEquals(scenario.condition, data.current?.condition)
            assertNotNull(data.current?.profile)
            assertTrue(data.hourly.isNotEmpty())
            assertTrue(data.daily.isNotEmpty())
        }
    }
}
