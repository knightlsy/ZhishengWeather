package com.zhisheng.weather.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenMeteoSourceTest {
    @Test
    fun carbonMonoxideIsConvertedFromMicrogramsToMilligrams() {
        assertEquals("0.14", OpenMeteoSource.fmtCoMg(142.0))
        assertEquals("0.3", OpenMeteoSource.fmtCoMg(300.0))
    }

    @Test
    fun currentResponseAcceptsSurfacePressure() {
        val decoded = Json.decodeFromString<OmFull>(
            """{"current":{"surface_pressure":849.2,"pressure_msl":1015.4}}""",
        )

        assertEquals(849.2, decoded.current?.surface_pressure)
        assertEquals(1015.4, decoded.current?.pressure_msl)
    }
}
