package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewDialogTest {
    @Test
    fun `versioned update guide explains the bugfix release and can be reopened`() {
        assertEquals("0.1.2.1Preview", WhatsNewVersion)

        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val dialog = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/WhatsNewDialog.kt").readText()
        val activity = File(projectDir, "src/main/kotlin/com/zhisheng/weather/MainActivity.kt").readText()
        val settings = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/SettingsScreen.kt").readText()

        assertTrue(dialog.contains("预报阅读优化"))
        assertTrue(dialog.contains("逐日增加日期"))
        assertTrue(dialog.contains("短时降水重排"))
        assertTrue(dialog.contains("和风保持单一来源"))
        assertTrue(dialog.contains("来源与更新时间"))
        assertTrue(!dialog.contains("本机凭据"))
        assertTrue(!dialog.contains("冷启动"))
        assertTrue(!dialog.contains("缺路"))
        assertTrue(!dialog.contains("公共源补齐"))
        assertTrue(!dialog.contains("阻断构建"))
        assertTrue(dialog.contains("\"更新说明\""))
        assertTrue(!dialog.contains("0.0.8"))
        assertTrue(!dialog.contains("这次不只是修补"))
        assertTrue(activity.contains("shouldShowWhatsNew()"))
        assertTrue(activity.contains("markWhatsNewSeen()"))
        assertTrue(settings.contains("v\${com.zhisheng.weather.BuildConfig.VERSION_NAME} · 查看更新"))
    }
}
