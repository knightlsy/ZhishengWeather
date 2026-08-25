package com.zhisheng.weather.ui.components

import com.zhisheng.weather.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbienceKindTest {

    // 0.0.9 的验收线：每一种天气都要有自己的一套，没有"落到默认分支"这回事。
    @Test
    fun everyConditionHasItsOwnAmbience() {
        WeatherCondition.entries.forEach { c ->
            if (c == WeatherCondition.UNKNOWN) return@forEach
            assertNotEquals("$c 没有对应的氛围动效", AmbienceKind.NONE, ambienceKindOf(c, night = false))
            assertNotEquals("$c 夜间没有对应的氛围动效", AmbienceKind.NONE, ambienceKindOf(c, night = true))
        }
    }

    @Test
    fun noConditionYieldsNothingToDraw() {
        assertEquals(AmbienceKind.NONE, ambienceKindOf(null))
        assertEquals(AmbienceKind.NONE, ambienceKindOf(WeatherCondition.UNKNOWN))
    }

    @Test
    fun onlyPhysicalMotionRunsAFrameLoop() {
        assertTrue(AmbienceKind.CLEAR_DAY.isDynamic())
        assertTrue(AmbienceKind.STARFIELD.isDynamic())
        assertTrue(AmbienceKind.PARTLY_CLOUDY.isDynamic())
        assertTrue(AmbienceKind.OVERCAST.isDynamic())
        assertTrue(AmbienceKind.FOG.isDynamic())
        assertTrue(AmbienceKind.HAZE.isDynamic())
        assertTrue(AmbienceKind.RAIN.isDynamic())
        assertTrue(AmbienceKind.SNOW.isDynamic())
        assertTrue(AmbienceKind.HAIL.isDynamic())
    }

    // 上一轮被点名的问题：沙尘、霾、雾挤在同一个分支里，看起来是同一种天气。
    @Test
    fun sandHazeAndFogAreThreeDifferentThings() {
        val fog = ambienceKindOf(WeatherCondition.FOG)
        val haze = ambienceKindOf(WeatherCondition.HAZE)
        val sand = ambienceKindOf(WeatherCondition.SAND)
        assertNotEquals(fog, haze)
        assertNotEquals(fog, sand)
        assertNotEquals(haze, sand)
    }

    // 风是横的、雨是竖的，两者不能共用一套；小雨也不是雨调淡。
    @Test
    fun windDrizzleAndRainAreDistinct() {
        val wind = ambienceKindOf(WeatherCondition.WIND)
        val rain = ambienceKindOf(WeatherCondition.RAIN)
        val drizzle = ambienceKindOf(WeatherCondition.DRIZZLE)
        assertNotEquals(wind, rain)
        assertNotEquals(rain, drizzle)
        assertNotEquals(wind, drizzle)
    }

    // 多云和阴共用同一套结构（更密更慢更暗），但仍是两个档，不是同一个 kind。
    @Test
    fun cloudyAndOvercastStaySeparate() {
        assertEquals(AmbienceKind.PARTLY_CLOUDY, ambienceKindOf(WeatherCondition.PARTLY_CLOUDY))
        assertEquals(AmbienceKind.PARTLY_CLOUDY, ambienceKindOf(WeatherCondition.PARTLY_CLOUDY_NIGHT))
        assertEquals(AmbienceKind.OVERCAST, ambienceKindOf(WeatherCondition.CLOUDY))
        assertEquals(AmbienceKind.OVERCAST, ambienceKindOf(WeatherCondition.OVERCAST))
    }

    // 国标现象码没有昼夜变体：夜里的"晴"必须靠日出日落判出来，不能走白天那套。
    @Test
    fun clearSwitchesToStarfieldAtNight() {
        assertEquals(AmbienceKind.CLEAR_DAY, ambienceKindOf(WeatherCondition.CLEAR, night = false))
        assertEquals(AmbienceKind.STARFIELD, ambienceKindOf(WeatherCondition.CLEAR, night = true))
        // 源已经给了夜间码时，白天判断不该把它抢回去
        assertEquals(AmbienceKind.STARFIELD, ambienceKindOf(WeatherCondition.CLEAR_NIGHT, night = false))
    }

    @Test
    fun nightIsDecidedBySunriseAndSunset() {
        assertTrue(isNightAt("06:12", "19:40", 5 * 60))        // 日出前
        assertFalse(isNightAt("06:12", "19:40", 12 * 60))      // 正午
        assertTrue(isNightAt("06:12", "19:40", 21 * 60))       // 日落后
        assertFalse(isNightAt("06:12", "19:40", 6 * 60 + 12))  // 日出那一分钟算白天
        assertTrue(isNightAt("06:12", "19:40", 19 * 60 + 40))  // 日落那一分钟算夜里
    }

    @Test
    fun nightFallsBackToFixedHoursWithoutAstronomy() {
        // 缺日出日落（Open-Meteo 之外的源不一定给）就退回 06:00 / 19:00，不去猜
        assertTrue(isNightAt(null, null, 3 * 60))
        assertFalse(isNightAt(null, "19:40", 12 * 60))
        assertTrue(isNightAt("06:12", null, 23 * 60))
        assertFalse(isNightAt("", "", 10 * 60))
        // 乱码和极昼极夜（日出不早于日落）同样退回固定档
        assertFalse(isNightAt("--", "??", 10 * 60))
        assertTrue(isNightAt("20:00", "05:00", 2 * 60))
    }

    @Test
    fun nightAcceptsTheClockFormatsTheSourcesActuallyReturn() {
        // 小米/和风给 "06:12"，Open-Meteo 给整串 ISO，都要认
        assertFalse(isNightAt("2026-08-25T06:12", "2026-08-25T19:40", 12 * 60))
        assertTrue(isNightAt("2026-08-25T06:12+08:00", "2026-08-25T19:40+08:00", 22 * 60))
        assertFalse(isNightAt("6:12", "19:40", 9 * 60))
    }
}
