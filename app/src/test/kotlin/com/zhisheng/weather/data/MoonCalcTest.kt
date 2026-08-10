package com.zhisheng.weather.data

import java.time.Instant
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test
    fun calculatesMoonriseAndMoonsetForARegularLatitude() {
        // 2026-08-10 00:00 at Beijing (UTC+8).
        val beijingDayStart = Instant.parse("2026-08-09T16:00:00Z").toEpochMilli()
        val times = MoonCalc.riseSet(beijingDayStart, 39.90, 116.41)

        requireNotNull(times.rise)
        requireNotNull(times.set)
        assertTrue(times.rise.matches(Regex("[0-2]\\d:[0-5]\\d")))
        assertTrue(times.set.matches(Regex("[0-2]\\d:[0-5]\\d")))
        // 公历天文表约为 01:50 / 17:43；本地轻量算法控制在 20 分钟内。
        assertWithinMinutes("01:50", times.rise, 20)
        assertWithinMinutes("17:43", times.set, 20)
    }

    @Test
    fun rejectsInvalidCoordinatesWithoutCrashing() {
        val dayStart = Instant.parse("2026-08-10T00:00:00Z").toEpochMilli()
        assertEquals(MoonCalc.MoonTimes(null, null), MoonCalc.riseSet(dayStart, 91.0, 0.0))
        assertEquals(MoonCalc.MoonTimes(null, null), MoonCalc.riseSet(dayStart, Double.NaN, 0.0))
    }

    private fun phaseAtNoon(date: String): String =
        MoonCalc.phaseKey(Instant.parse("${date}T12:00:00Z").toEpochMilli())

    private fun assertWithinMinutes(expected: String, actual: String, tolerance: Int) {
        fun minutes(value: String): Int =
            value.substringBefore(':').toInt() * 60 + value.substringAfter(':').toInt()
        assertTrue("Expected $actual to be within $tolerance minutes of $expected", kotlin.math.abs(minutes(actual) - minutes(expected)) <= tolerance)
    }
}
