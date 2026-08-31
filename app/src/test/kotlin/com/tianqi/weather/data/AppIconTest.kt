package com.tianqi.weather.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIconTest {
    @Test
    fun characterIconIsTheDefaultPreference() {
        assertEquals(AppIconStyle.CHARACTER, AppIconStyle.from(null))
        assertEquals(AppIconStyle.CHARACTER, AppIconStyle.from("unknown"))
        assertEquals(AppIconStyle.CLASSIC, AppIconStyle.from("classic"))
    }

    @Test
    fun launcherAliasesAreMutuallyConfiguredAndSettingsExposeBothChoices() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val manifest = File(projectDir, "src/main/AndroidManifest.xml").readText()
        val settings = File(
            projectDir,
            "src/main/kotlin/com/tianqi/weather/ui/SettingsScreen.kt",
        ).readText()

        assertTrue(manifest.contains("android:name=\".IconCharacter\""))
        assertTrue(manifest.contains("android:name=\".IconClassic\""))
        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher_character\""))
        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue(manifest.contains("android:targetActivity=\".MainActivity\""))
        assertFalse(
            Regex(
                "<activity\\s[\\s\\S]*?android:name=\\\"\\.MainActivity\\\"[\\s\\S]*?" +
                    "android.intent.category.LAUNCHER[\\s\\S]*?</activity>",
            ).containsMatchIn(manifest),
        )
        assertTrue(settings.contains("\"应用图标\""))
        assertTrue(settings.contains("\"天气娘\" to \"character\""))
        assertTrue(settings.contains("\"经典\" to \"classic\""))
        assertTrue(settings.contains("AppIconManager.apply(context, selected)"))
    }
}
