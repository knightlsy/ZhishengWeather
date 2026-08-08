package com.zhisheng.weather.data

import java.time.Instant
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MoonCalcTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun rememberTimeZone() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun identifiesKnownAugust2024PhaseDates() {
        assertEquals("new-moon", phaseAtNoon("2024-08-04"))
        assertEquals("first-quarter", phaseAtNoon("2024-08-12"))
        assertEquals("full-moon", phaseAtNoon("2024-08-19"))
        assertEquals("last-quarter", phaseAtNoon("2024-08-26"))
    }

    @Test
    fun identifiesPeriodsBetweenMajorPhases() {
        assertEquals("waxing-crescent", phaseAtNoon("2024-08-08"))
        assertEquals("waxing-gibbous", phaseAtNoon("2024-08-16"))
        assertEquals("waning-gibbous", phaseAtNoon("2024-08-22"))
        assertEquals("waning-crescent", phaseAtNoon("2024-08-29"))
    }

    @Test
    fun usesTheDeviceLocalDayForPhaseEvents() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
        val afternoonUtc = Instant.parse("2024-08-04T12:00:00Z").toEpochMilli()

        assertEquals("new-moon", MoonCalc.phaseKey(afternoonUtc))
    }

    private fun phaseAtNoon(date: String): String =
        MoonCalc.phaseKey(Instant.parse("${date}T12:00:00Z").toEpochMilli())
}
