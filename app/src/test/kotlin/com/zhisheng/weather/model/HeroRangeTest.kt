package com.zhisheng.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class HeroRangeTest {

    private val zone = ZoneId.of("Asia/Shanghai")
    private val today = DailyWeather(dateMillis = 1L, high = 24.0, low = 8.0)
    private val tomorrow = DailyWeather(dateMillis = 2L, high = 18.0, low = 6.0)
    private val yesterday = YesterdayInfo(high = 22.0, low = 5.0)

    private fun at(hour: Int): Long =
        LocalDateTime.of(2026, 8, 21, hour, 0).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun morningUsesYesterdayLowAndTodayHigh() {
        val r = HeroTemps.range(listOf(today, tomorrow), yesterday, at(7), zone)
        assertEquals("昨低", r.leftLabel)
        assertEquals(5.0, r.left)
        assertEquals("今高", r.rightLabel)
        assertEquals(24.0, r.right)
    }

    @Test
    fun missingYesterdayLeavesMorningLowEmpty() {
        val r = HeroTemps.range(listOf(today, tomorrow), null, at(7), zone)
        assertEquals("昨低", r.leftLabel)
        assertEquals(null, r.left)
        assertEquals(24.0, r.right)
    }

    @Test
    fun daytimeUsesTodayHighLow() {
        val r = HeroTemps.range(listOf(today, tomorrow), yesterday, at(14), zone)
        assertEquals("高", r.leftLabel)
        assertEquals(24.0, r.left)
        assertEquals("低", r.rightLabel)
        assertEquals(8.0, r.lowOrRight())
    }

    @Test
    fun eveningUsesTonightLowAndTomorrowHigh() {
        // 0.0.9-debug：夜低取明日 low（今夜最低通常落在明晨），今日 low 仅为缺数兜底
        val r = HeroTemps.range(listOf(today, tomorrow), yesterday, at(21), zone)
        assertEquals("夜低", r.leftLabel)
        assertEquals(6.0, r.left)
        assertEquals("明高", r.rightLabel)
        assertEquals(18.0, r.right)
    }

    @Test
    fun eveningFallsBackToTodayLowWhenTomorrowMissing() {
        val r = HeroTemps.range(listOf(today), yesterday, at(21), zone)
        assertEquals(8.0, r.left)
        assertEquals(24.0, r.right)
    }

    @Test
    fun feelsLikeHidesWhenClose() {
        assertFalse(HeroTemps.showFeelsLike(20.0, 20.4))
        assertTrue(HeroTemps.showFeelsLike(20.0, 22.0))
        assertFalse(HeroTemps.showFeelsLike(20.0, null))
    }

    private fun HeroRange.lowOrRight() = right
}
