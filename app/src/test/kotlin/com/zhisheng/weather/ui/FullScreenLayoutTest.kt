package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FullScreenLayoutTest {
    @Test
    fun homeSurfaceExtendsBehindGestureNavigationArea() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val activity = File(projectDir, "src/main/kotlin/com/zhisheng/weather/MainActivity.kt").readText()
        val theme = File(projectDir, "src/main/res/values/themes.xml").readText()
        val home = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/home/HomeScreen.kt").readText()

        assertTrue(activity.contains("WindowCompat.setDecorFitsSystemWindows(window, false)"))
        assertTrue(activity.contains("window.navigationBarColor = android.graphics.Color.TRANSPARENT"))
        assertTrue(activity.contains("window.isNavigationBarContrastEnforced = false"))
        assertTrue(theme.contains("<item name=\"android:navigationBarColor\">@android:color/transparent</item>"))
        assertTrue(home.contains("CLEAR SIGNAL"))
    }
}
