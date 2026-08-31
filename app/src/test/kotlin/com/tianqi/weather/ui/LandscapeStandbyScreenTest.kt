package com.tianqi.weather.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LandscapeStandbyScreenTest {
    @Test
    fun landscapeUsesDedicatedStandbyScreenAndCanBeDisabled() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val activity = File(projectDir, "src/main/kotlin/com/tianqi/weather/MainActivity.kt").readText()
        val screen = File(projectDir, "src/main/kotlin/com/tianqi/weather/ui/LandscapeStandbyScreen.kt").readText()
        val settings = File(projectDir, "src/main/kotlin/com/tianqi/weather/ui/SettingsScreen.kt").readText()

        assertTrue(activity.contains("LandscapeStandbyScreen"))
        assertTrue(activity.contains("SCREEN_ORIENTATION_PORTRAIT"))
        assertTrue(activity.contains("SCREEN_ORIENTATION_SENSOR"))
        assertTrue(screen.contains("TIANQI AMBIENT TERMINAL"))
        assertTrue(screen.contains("todayDaily(nowMillis)"))
        assertTrue(settings.contains("横屏待机界面"))
    }
}
