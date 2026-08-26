package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HourlyLayoutTest {
    @Test
    fun missingWeatherIconStillKeepsFixedSlotForContinuousCurve() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val home = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/home/HomeScreen.kt").readText()

        assertTrue(home.contains("Box(Modifier.size(24.dp), contentAlignment = Alignment.Center)"))
        assertTrue(home.contains("WeatherIcon(h.condition, Modifier.fillMaxSize())"))
    }
}
