package com.zhisheng.weather.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FmtTest {

    @Test
    fun temperatureHandlesUnitsRoundingAndMissingValues() {
        assertEquals("24", Fmt.temp(23.6, "c"))
        assertEquals("74", Fmt.temp(23.6, "f"))
        assertEquals("°C", Fmt.unitSuffix("c"))
        assertEquals("°F", Fmt.unitSuffix("f"))
        assertNull(Fmt.temp(null, "c"))
    }

    @Test
    fun windConvertsToMetersPerSecond() {
        assertEquals("1.0 m/s", Fmt.wind(3.6, "ms"))
        assertEquals("1.0", Fmt.windValue(3.6, "ms"))
        assertNull(Fmt.wind(null, "ms"))
    }

    @Test
    fun beaufortBoundariesDoNotSlipByOneLevel() {
        val cases = listOf(
            0.0 to "0",
            0.99 to "0",
            1.0 to "1",
            5.99 to "1",
            6.0 to "2",
            11.99 to "2",
            12.0 to "3",
            28.99 to "4",
            29.0 to "5",
            49.99 to "6",
            50.0 to "7",
            74.99 to "8",
            75.0 to "9",
            102.99 to "10",
            103.0 to "11",
            117.99 to "11",
            118.0 to "12",
        )

        cases.forEach { (kmh, expected) ->
            assertEquals("Unexpected Beaufort value at $kmh km/h", expected, Fmt.windValue(kmh, "bft"))
        }
    }

    @Test
    fun pressureUsesStableDisplayPrecision() {
        assertEquals("1013 hPa", Fmt.pressure(1013.25, "hpa"))
        assertEquals("760 mmHg", Fmt.pressure(1013.25, "mmhg"))
        assertEquals("29.92 inHg", Fmt.pressure(1013.25, "inhg"))
        assertNull(Fmt.pressure(null, "hpa"))
    }

    @Test
    fun moonPhaseNamesAcceptProviderFormattingVariants() {
        assertEquals("满月", Fmt.moonPhaseZh("FULL MOON"))
        assertEquals("上弦月", Fmt.moonPhaseZh("first-quarter"))
        assertEquals("未知相位", Fmt.moonPhaseZh("未知相位"))
        assertNull(Fmt.moonPhaseZh(null))
    }
}
