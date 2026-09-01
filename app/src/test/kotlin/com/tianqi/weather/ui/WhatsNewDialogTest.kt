package com.tianqi.weather.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNewDialogTest {
    @Test
    fun `versioned update guide explains the bugfix release and can be reopened`() {
        assertEquals("1.0.0", WhatsNewVersion)

        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val dialog = File(projectDir, "src/main/kotlin/com/tianqi/weather/ui/WhatsNewDialog.kt").readText()
        val activity = File(projectDir, "src/main/kotlin/com/tianqi/weather/MainActivity.kt").readText()
        val settings = File(projectDir, "src/main/kotlin/com/tianqi/weather/ui/SettingsScreen.kt").readText()

        assertTrue(dialog.contains("天气体验升级"))
        assertFalse(dialog.contains("0.1.0"))
        assertFalse(dialog.contains("Preview"))
        assertTrue(dialog.contains("横屏待机"))
        assertTrue(dialog.contains("逐时与逐日修复"))
        assertTrue(dialog.contains("短时降水重做"))
        assertTrue(dialog.contains("同一时刻对齐"))
        assertTrue(dialog.contains("和风接入更可靠"))
        assertTrue(dialog.contains("套餐能力不浪费"))
        assertTrue(dialog.contains("生活指数更整齐"))
        assertTrue(dialog.contains("透明小组件"))
        assertTrue(dialog.contains("真正铺满屏幕"))
        assertTrue(!dialog.contains("QQ"))
        assertTrue(!dialog.contains("群号"))
        assertTrue(!dialog.contains("本机凭据"))
        assertTrue(!dialog.contains("冷启动"))
        assertTrue(!dialog.contains("缺路"))
        assertTrue(!dialog.contains("公共源补齐"))
        assertTrue(!dialog.contains("阻断构建"))
        assertTrue(!dialog.contains("实验室"))
        assertTrue(dialog.contains("\"更新说明\""))
        assertTrue(!dialog.contains("0.0.8"))
        assertTrue(!dialog.contains("这次不只是修补"))
        assertTrue(activity.contains("shouldShowWhatsNew()"))
        assertTrue(activity.contains("markWhatsNewSeen()"))
        assertTrue(!activity.contains("AppUpdate.check"))
        assertTrue(settings.contains("v\${com.tianqi.weather.BuildConfig.VERSION_NAME} · 更新说明"))
        assertTrue(settings.contains("检查更新"))
        assertTrue(settings.contains("有新版本时再下载，不自动提醒"))
        assertTrue(settings.contains("AppUpdateDialog"))
    }
}
