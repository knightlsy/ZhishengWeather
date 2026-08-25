package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityGroupTest {
    @Test
    fun `community contact is release injected and qr stays out of git`() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val settings = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/SettingsScreen.kt").readText()
        val dialog = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/CommunityGroupDialog.kt").readText()
        val build = File(projectDir, "build.gradle.kts").readText()
        val ignore = File(projectDir.parentFile, ".gitignore").readText()

        assertTrue(settings.contains("用户交流 QQ 群"))
        assertTrue(settings.contains("CommunityQqGroup.isNotBlank()"))
        assertTrue(settings.contains("CommunityGroupDialog"))
        assertTrue(dialog.contains("getIdentifier(\"qq_group_qr\""))
        assertTrue(build.contains("COMMUNITY_QQ_GROUP"))
        assertTrue(ignore.contains("app/src/main/res/drawable-nodpi/qq_group_qr.jpg"))
        assertFalse(Regex("\\d{9,12}").containsMatchIn(dialog))
    }
}
