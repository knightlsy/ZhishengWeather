package com.zhisheng.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.WeatherData

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

    @Test
    fun openMeteoSupplementRestoresMissingXiaomiTelemetryWithoutOverwritingProviderValues() {
        val source = WeatherData(
            current = CurrentWeather(
                visibility = 18.0,
                dewPoint = null,
                cloudCover = null,
                windGust = null,
            ),
            dataSource = "XIAOMI",
        )
        val merged = WeatherRepository.mergeCurrentSupplement(
            source,
            OpenMeteoResult(
                current = OpenMeteoCurrent(
                    visibility = 9_000.0,
                    dew_point_2m = 7.5,
                    cloud_cover = 62.0,
                    wind_gusts_10m = 28.0,
                ),
            ),
        )

        assertEquals(18.0, merged.current?.visibility)
        assertEquals(7.5, merged.current?.dewPoint)
        assertEquals(62.0, merged.current?.cloudCover)
        assertEquals(28.0, merged.current?.windGust)
        assertEquals("OPEN-METEO", merged.blockSources["current-supplement"])
    }

    @Test
    fun onlyAutoAndXiaomiMaySupplementFromPublicSource() {
        assertEquals(true, WeatherRepository.shouldSupplementWithOpenMeteo(SourcePref.AUTO))
        assertEquals(false, WeatherRepository.shouldSupplementWithOpenMeteo(SourcePref.QWEATHER))
        assertEquals(false, WeatherRepository.shouldSupplementWithOpenMeteo(SourcePref.CAIYUN))
        assertEquals(true, WeatherRepository.shouldSupplementWithOpenMeteo(SourcePref.XIAOMI))
        assertEquals(false, WeatherRepository.shouldSupplementWithOpenMeteo(SourcePref.OPEN_METEO))
    }
}
