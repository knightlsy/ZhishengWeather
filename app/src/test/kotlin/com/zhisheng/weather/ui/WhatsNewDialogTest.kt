package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewDialogTest {
    @Test
    fun `versioned update guide explains the bugfix release and can be reopened`() {
        assertEquals("0.1.3Preview", WhatsNewVersion)

        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val dialog = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/WhatsNewDialog.kt").readText()
        val activity = File(projectDir, "src/main/kotlin/com/zhisheng/weather/MainActivity.kt").readText()
        val settings = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/SettingsScreen.kt").readText()

        assertTrue(dialog.contains("从 0.1.0 升级"))
        assertTrue(dialog.contains("横屏待机"))
        assertTrue(dialog.contains("逐时与逐日修复"))
        assertTrue(dialog.contains("短时降水重做"))
        assertTrue(dialog.contains("和风接入更可靠"))
        assertTrue(dialog.contains("透明小组件"))
        assertTrue(dialog.contains("真正铺满屏幕"))
        assertTrue(!dialog.contains("QQ"))
        assertTrue(!dialog.contains("群号"))
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
