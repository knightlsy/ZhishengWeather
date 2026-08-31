package com.tianqi.weather.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AlertLevelTest {

    @Test
    fun xiaomiChineseLevelNames() {
        assertEquals(AlertLevel.RED, alertLevelOf("红色预警"))
        assertEquals(AlertLevel.ORANGE, alertLevelOf("橙色预警"))
        assertEquals(AlertLevel.YELLOW, alertLevelOf("黄色预警"))
        assertEquals(AlertLevel.BLUE, alertLevelOf("蓝色预警"))
    }

    @Test
    fun englishColorNamesIgnoreCase() {
        assertEquals(AlertLevel.RED, alertLevelOf("red"))
        assertEquals(AlertLevel.RED, alertLevelOf("RED"))
        assertEquals(AlertLevel.ORANGE, alertLevelOf("Orange"))
        assertEquals(AlertLevel.YELLOW, alertLevelOf("Yellow"))
        assertEquals(AlertLevel.BLUE, alertLevelOf("blue"))
    }

    @Test
    fun qWeatherSeverityEnum() {
        assertEquals(AlertLevel.RED, alertLevelOf("extreme"))
        assertEquals(AlertLevel.ORANGE, alertLevelOf("severe"))
        assertEquals(AlertLevel.ORANGE, alertLevelOf("major"))
        assertEquals(AlertLevel.YELLOW, alertLevelOf("moderate"))
        assertEquals(AlertLevel.BLUE, alertLevelOf("minor"))
        assertEquals(AlertLevel.BLUE, alertLevelOf("standard"))
        assertEquals(AlertLevel.ORANGE, alertLevelOf("Severe"))
        assertEquals(AlertLevel.BLUE, alertLevelOf("Minor"))
    }

    @Test
    fun unknownAndBlankFallBack() {
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf("unknown"))
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf("garbage"))
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf(""))
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf("   "))
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf(null))
    }

    @Test
    fun surroundingWhitespaceIsTrimmed() {
        assertEquals(AlertLevel.RED, alertLevelOf(" red "))
        assertEquals(AlertLevel.BLUE, alertLevelOf(" 蓝色预警 "))
    }

    // 彩云 v2.6 预警 code：4 位字符串，前 2 位类型、后 2 位级别（0.0.9-debug 接入）
    @Test
    fun caiyunFourDigitCodes() {
        assertEquals(AlertLevel.BLUE, alertLevelOf("0501"))   // 大风蓝色
        assertEquals(AlertLevel.YELLOW, alertLevelOf("0902")) // 雷电黄色
        assertEquals(AlertLevel.ORANGE, alertLevelOf("0103")) // 台风橙色
        assertEquals(AlertLevel.RED, alertLevelOf("0204"))    // 暴雨红色
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf("1300")) // 白色（国标四档外）
        assertEquals(AlertLevel.UNKNOWN, alertLevelOf("0299")) // 非法级别
    }
}
