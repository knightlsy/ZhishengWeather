package com.tianqi.weather.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateTest {
    @Test
    fun manifestPrefersVersionCodeAndKeepsPublicApkUrl() {
        val info = AppUpdate.parseManifest(
            """
            {
              "versionCode": 20260901,
              "versionName": "v0.1.4",
              "apkUrl": "https://example.test/TianQiWeather-v0.1.4-public.apk",
              "sha256": "abc",
              "notes": "修复小组件",
              "pageUrl": "https://github.com/tianqiplus/TianQiWeather/releases/tag/v0.1.4"
            }
            """.trimIndent(),
        )
        assertEquals(20260901, info.versionCode)
        assertEquals("1.0.1", info.versionName)
        assertEquals("https://example.test/TianQiWeather-v0.1.4-public.apk", info.apkUrl)
        assertEquals("abc", info.sha256)
        assertTrue(AppUpdate.isNewer(info, 1000000, "1.0.0"))
        assertFalse(AppUpdate.isNewer(info, 20260901, "1.0.1"))
    }

    @Test
    fun sameOrOlderVersionIsNotAnUpdate() {
        val same = AppUpdateInfo(versionCode = 1000000, versionName = "1.0.0", apkUrl = "https://example.test/app.apk")
        val olderName = AppUpdateInfo(versionName = "0.1.0", apkUrl = "https://example.test/app.apk")
        assertFalse(AppUpdate.isNewer(same, 1000000, "1.0.0"))
        assertFalse(AppUpdate.isNewer(olderName, 1000000, "1.0.0"))
        assertTrue(AppUpdate.isNewer(AppUpdateInfo(versionName = "1.0.1", apkUrl = "https://example.test/app.apk"), 1000000, "1.0.0"))
    }

    @Test
    fun githubLatestIgnoresPrivateAndPreviewApks() {
        val info = AppUpdate.parseGithubLatest(
            """
            {
              "tag_name": "v1.0.0",
              "draft": false,
              "prerelease": false,
              "html_url": "https://github.com/tianqiplus/TianQiWeather/releases/tag/v1.0.0",
              "body": "正式版",
              "assets": [
                {"name": "TianQiWeather-v1.0.0-full-private.apk", "state": "uploaded", "browser_download_url": "https://example.test/private.apk"},
                {"name": "TianQiWeather-v1.0.0-public.apk", "state": "uploaded", "digest": "sha256:deadbeef", "browser_download_url": "https://example.test/public.apk"}
              ]
            }
            """.trimIndent(),
        )
        assertEquals("1.0.0", info?.versionName)
        assertEquals("https://example.test/public.apk", info?.apkUrl)
        assertEquals("deadbeef", info?.sha256)
    }

    @Test
    fun githubLatestSkipsDraftPrereleaseAndUnknownAssets() {
        assertNull(
            AppUpdate.parseGithubLatest(
                """{"tag_name":"v0.1.4","draft":true,"prerelease":false,"html_url":"https://example.test","assets":[{"name":"TianQiWeather-v0.1.4-public.apk","state":"uploaded","browser_download_url":"https://example.test/public.apk"}]}""",
            ),
        )
        assertNull(
            AppUpdate.parseGithubLatest(
                """{"tag_name":"v0.1.4-preview","draft":false,"prerelease":true,"html_url":"https://example.test","assets":[{"name":"tianqi-weather-v0.1.4.apk","state":"uploaded","browser_download_url":"https://example.test/public.apk"}]}""",
            ),
        )
        assertNull(
            AppUpdate.parseGithubLatest(
                """{"tag_name":"v0.1.4","draft":false,"prerelease":false,"html_url":"https://example.test","assets":[{"name":"notes.txt","state":"uploaded","browser_download_url":"https://example.test/notes.txt"}]}""",
            ),
        )
    }

    @Test
    fun publicApkNameAcceptsLegacyAndCurrentReleaseFiles() {
        assertTrue(AppUpdate.isPublicApkName("tianqi-weather-v0.1.0.apk"))
        assertTrue(AppUpdate.isPublicApkName("TianQiWeather-v1.0.0-public.apk"))
        assertFalse(AppUpdate.isPublicApkName("TianQiWeather-v1.0.0-full-private.apk"))
        assertFalse(AppUpdate.isPublicApkName("TianQiWeather-v1.0.0-public-parallel.apk"))
        assertFalse(AppUpdate.isPublicApkName("TianQiWeather-v1.0.0-owner-upgrade-private.apk"))
    }

    @Test
    fun checkedInManifestMatchesCurrentPublicRelease() {
        val manifest = sequenceOf(
            File("update.json"),
            File("../update.json"),
        ).first { it.isFile }.readText()
        val info = AppUpdate.parseManifest(manifest)
        assertEquals(1000000, info.versionCode)
        assertEquals("1.0.0", info.versionName)
        assertTrue(info.apkUrl.endsWith("TianQiWeather-v1.0.0-public.apk"))
        assertTrue(info.sha256?.matches(Regex("[0-9a-fA-F]{64}")) == true)
        assertFalse(AppUpdate.isNewer(info, 1000000, "1.0.0"))
    }

    @Test
    fun updateEntryIsManualOnly() {
        val settings = sequenceOf(
            File("src/main/kotlin/com/tianqi/weather/ui/SettingsScreen.kt"),
            File("app/src/main/kotlin/com/tianqi/weather/ui/SettingsScreen.kt"),
        ).first { it.isFile }.readText()
        val activity = sequenceOf(
            File("src/main/kotlin/com/tianqi/weather/MainActivity.kt"),
            File("app/src/main/kotlin/com/tianqi/weather/MainActivity.kt"),
        ).first { it.isFile }.readText()
        val dialog = sequenceOf(
            File("src/main/kotlin/com/tianqi/weather/ui/AppUpdateDialog.kt"),
            File("app/src/main/kotlin/com/tianqi/weather/ui/AppUpdateDialog.kt"),
        ).first { it.isFile }.readText()
        val manifest = sequenceOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml"),
        ).first { it.isFile }.readText()

        assertTrue(settings.contains("检查更新"))
        assertTrue(dialog.contains("不会自动下载"))
        assertTrue(!activity.contains("AppUpdate.check"))
        assertTrue(manifest.contains("REQUEST_INSTALL_PACKAGES"))
        assertTrue(manifest.contains("androidx.core.content.FileProvider"))
        assertTrue(manifest.contains("@xml/file_provider_paths"))
        assertTrue(dialog.contains("AppUpdate.canSelfUpdate()"))
        assertTrue(dialog.contains("不能由公共版直接覆盖"))
    }

    @Test
    fun buildTypesOnlyAllowTheFormalPublicPackageToSelfUpdate() {
        val gradle = sequenceOf(
            File("build.gradle.kts"),
            File("app/build.gradle.kts"),
        ).first { it.isFile }.readText()

        assertTrue(gradle.contains("buildConfigField(\"boolean\", \"CAN_SELF_UPDATE\", \"true\")"))
        assertTrue(gradle.contains("buildConfigField(\"boolean\", \"CAN_SELF_UPDATE\", \"false\")"))
    }
}
