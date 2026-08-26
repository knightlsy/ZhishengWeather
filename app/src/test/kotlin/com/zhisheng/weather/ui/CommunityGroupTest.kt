package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityGroupTest {
    @Test
    fun `community contact is built into every distribution`() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val settings = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/SettingsScreen.kt").readText()
        val dialog = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/CommunityGroupDialog.kt").readText()
        val build = File(projectDir, "build.gradle.kts").readText()
        assertTrue(settings.contains("用户交流 QQ 群"))
        assertFalse(settings.contains("CommunityQqGroup.isNotBlank()"))
        assertTrue(settings.contains("CommunityGroupDialog"))
        assertTrue(dialog.contains("R.drawable.qq_group_qr"))
        assertTrue(build.contains("COMMUNITY_QQ_GROUP"))
        assertTrue(build.contains("1106284779"))
        assertFalse(Regex("\\d{9,12}").containsMatchIn(dialog))
    }
}
