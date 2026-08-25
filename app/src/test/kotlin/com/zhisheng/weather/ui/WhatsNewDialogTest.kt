package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewDialogTest {
    @Test
    fun `versioned update guide explains the city deck and can be reopened`() {
        assertEquals("0.1.0", WhatsNewVersion)

        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val dialog = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/WhatsNewDialog.kt").readText()
        val activity = File(projectDir, "src/main/kotlin/com/zhisheng/weather/MainActivity.kt").readText()
        val settings = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/SettingsScreen.kt").readText()

        assertTrue(dialog.contains("按住屏幕底部的呼吸光"))
        assertTrue(dialog.contains("第二次震动代表卡组已固定"))
        assertTrue(dialog.contains("\"更新说明\""))
        assertTrue(!dialog.contains("0.0.8"))
        assertTrue(!dialog.contains("这次不只是修补"))
        assertTrue(dialog.contains("模块顺序"))
        assertTrue(dialog.contains("全天气氛围"))
        assertTrue(dialog.contains("五种终端小组件"))
        assertTrue(activity.contains("shouldShowWhatsNew()"))
        assertTrue(activity.contains("markWhatsNewSeen()"))
        assertTrue(settings.contains("v\${com.zhisheng.weather.BuildConfig.VERSION_NAME} · 查看更新"))
    }
}
