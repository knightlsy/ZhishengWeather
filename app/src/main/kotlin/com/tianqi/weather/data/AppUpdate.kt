package com.tianqi.weather.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.tianqi.weather.BuildConfig
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

data class AppUpdateInfo(
    val versionCode: Int? = null,
    val versionName: String,
    val apkUrl: String,
    val sha256: String? = null,
    val notes: String = "",
    val pageUrl: String = AppUpdate.RELEASES_PAGE,
)

sealed class AppUpdateCheck {
    data class Available(val info: AppUpdateInfo) : AppUpdateCheck()
    data object UpToDate : AppUpdateCheck()
    data class Failed(val message: String) : AppUpdateCheck()
}

object AppUpdate {
    const val RELEASES_PAGE = "https://github.com/knightlsy/ZhishengWeather/releases"
    private const val MANIFEST_PRIMARY =
        "https://raw.githubusercontent.com/knightlsy/ZhishengWeather/main/update.json"
    private const val MANIFEST_MIRROR =
        "https://cdn.jsdelivr.net/gh/knightlsy/ZhishengWeather@main/update.json"
    private const val GITHUB_LATEST =
        "https://api.github.com/repos/knightlsy/ZhishengWeather/releases/latest"

    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    // 镜像加速：直连 GitHub 经常超时，按顺序尝试镜像。
    private val APK_MIRRORS = listOf(
        "https://ghfast.top/{owner}/{repo}",
        "https://ghproxy.net/{owner}/{repo}",
        "https://gh.jesd.top/{owner}/{repo}",
        "https://gh.horsey.top/{owner}/{repo}",
        "https://gh.chenx264.top/{owner}/{repo}",
    )
    private val APK_URL_MIRROR_REGEX = Regex("""^https://github\.com/([^/]+)/([^/]+)/(.+)$""")

    private val downloadHttp = http.newBuilder()
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun check(): AppUpdateCheck = withContext(Dispatchers.IO) {
        val info = fetchManifest()
            ?: fetchGithubLatest()
            ?: return@withContext AppUpdateCheck.Failed("暂时连不上更新源，可稍后再试或打开 GitHub。")
        if (isNewer(info, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)) {
            AppUpdateCheck.Available(info)
        } else {
            AppUpdateCheck.UpToDate
        }
    }

    /** True only when the GitHub public APK can safely replace this installed build. */
    fun canSelfUpdate(): Boolean = BuildConfig.CAN_SELF_UPDATE

    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    fun installPermissionIntent(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:${context.packageName}"))

    fun apkFile(context: Context): File =
        File(File(context.cacheDir, "updates").apply { mkdirs() }, "TianQiWeather-update.apk")

    suspend fun download(
        context: Context,
        info: AppUpdateInfo,
        onProgress: (Float?) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val dest = apkFile(context)
        var lastError: String? = null
        for (url in downloadCandidates(info.apkUrl)) {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent())
                .build()
            val resp = downloadHttp.newCall(request).execute()
            if (!resp.isSuccessful) {
                resp.close()
                lastError = "HTTP ${resp.code}"
                continue
            }
            val body = resp.body ?: run { resp.close(); null }
            if (body == null) continue
            val total = body.contentLength()
            dest.outputStream().use { out ->
                body.byteStream().use { input ->
                    val buf = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        out.write(buf, 0, n)
                        read += n
                        if (total > 0) {
                            withContext(Dispatchers.Main.immediate) {
                                onProgress(read.toFloat() / total.toFloat())
                            }
                        }
                    }
                }
            }
            resp.close()
            val shaOk = info.sha256?.trim()?.takeIf(String::isNotEmpty)?.let { expected ->
                val actual = sha256Hex(dest)
                if (!actual.equals(expected, ignoreCase = true)) {
                    dest.delete()
                    lastError = "SHA256 校验失败"
                    return@let false
                }
                true
            } ?: true
            if (!shaOk) continue
            return@withContext dest
        }
        error("下载失败：所有镜像均不可用（$lastError）")
    }

    private fun downloadCandidates(rawUrl: String): List<String> {
        val list = mutableListOf(rawUrl)
        val m = APK_URL_MIRROR_REGEX.find(rawUrl) ?: return list
        val (owner, repo, path) = m.destructured
        for (mirror in APK_MIRRORS) {
            list.add(mirror.replace("{owner}", owner).replace("{repo}", repo) + path)
        }
        return list
    }

    fun install(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    internal fun parseManifest(raw: String): AppUpdateInfo {
        val dto = json.decodeFromString<UpdateManifestDto>(raw)
        val name = dto.versionName.trim()
        require(name.isNotEmpty() && dto.apkUrl.isNotBlank()) { "更新清单缺少版本或下载地址" }
        return AppUpdateInfo(
            versionCode = dto.versionCode,
            versionName = name.removePrefix("v"),
            apkUrl = dto.apkUrl.trim(),
            sha256 = dto.sha256?.trim()?.takeIf { it.isNotEmpty() },
            notes = dto.notes.orEmpty().trim(),
            pageUrl = dto.pageUrl?.trim()?.ifBlank { null } ?: RELEASES_PAGE,
        )
    }

    internal fun parseGithubLatest(raw: String): AppUpdateInfo? {
        val release = json.decodeFromString<GithubReleaseDto>(raw)
        if (release.draft || release.prerelease) return null
        val asset = pickPublicApk(release.assets) ?: return null
        val name = release.tagName.removePrefix("v").trim()
        if (name.isEmpty()) return null
        val digest = asset.digest
            ?.removePrefix("sha256:")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        return AppUpdateInfo(
            versionName = name,
            apkUrl = asset.browserDownloadUrl,
            sha256 = digest,
            notes = release.body.orEmpty().trim().lineSequence()
                .take(8)
                .joinToString("\n")
                .take(400),
            pageUrl = release.htmlUrl.ifBlank { RELEASES_PAGE },
        )
    }

    internal fun isNewer(
        info: AppUpdateInfo,
        localCode: Int,
        localName: String,
    ): Boolean {
        val remoteCode = info.versionCode
        if (remoteCode != null && remoteCode > 0) return remoteCode > localCode
        return compareSemver(info.versionName, localName) > 0
    }

    internal fun pickPublicApk(assets: List<GithubAssetDto>): GithubAssetDto? {
        val apk = assets.filter { it.state == "uploaded" && it.name.endsWith(".apk", ignoreCase = true) }
        val public = apk.filter { isPublicApkName(it.name) }
        return public.firstOrNull { it.name.contains("-public", ignoreCase = true) }
            ?: public.firstOrNull()
    }

    internal fun isPublicApkName(name: String): Boolean {
        val lower = name.lowercase()
        if (!lower.endsWith(".apk")) return false
        if (listOf("private", "parallel", "preview", "full", "owner").any { it in lower }) return false
        return lower.contains("tianqi-weather") ||
            lower.contains("tianqiweather") ||
            lower.contains("-public")
    }

    internal fun compareSemver(remote: String, local: String): Int {
        val r = parseSemver(remote)
        val l = parseSemver(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv != lv) return rv.compareTo(lv)
        }
        return 0
    }

    private fun parseSemver(value: String): List<Int> =
        value.removePrefix("v")
            .takeWhile { it.isDigit() || it == '.' }
            .split('.')
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }

    private fun userAgent(): String = "TianQiWeather/${BuildConfig.VERSION_NAME}"

    private fun fetchManifest(): AppUpdateInfo? {
        for (url in listOf(MANIFEST_PRIMARY, MANIFEST_MIRROR)) {
            val raw = getText(url) ?: continue
            runCatching { parseManifest(raw) }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun fetchGithubLatest(): AppUpdateInfo? {
        val raw = getText(GITHUB_LATEST, accept = "application/vnd.github+json") ?: return null
        return runCatching { parseGithubLatest(raw) }.getOrNull()
    }

    private fun getText(url: String, accept: String = "application/json"): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent())
            .header("Accept", accept)
            .build()
        return runCatching {
            http.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                resp.body?.string()
            }
        }.getOrNull()
    }

    private fun sha256Hex(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val n = input.read(buf)
                if (n <= 0) break
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { b -> "%02x".format(b) }
    }
}

@Serializable
internal data class UpdateManifestDto(
    val versionCode: Int? = null,
    val versionName: String,
    val apkUrl: String,
    val sha256: String? = null,
    val notes: String? = null,
    val pageUrl: String? = null,
)

@Serializable
internal data class GithubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val body: String? = null,
    @SerialName("html_url") val htmlUrl: String = AppUpdate.RELEASES_PAGE,
    val assets: List<GithubAssetDto> = emptyList(),
)

@Serializable
internal data class GithubAssetDto(
    val name: String,
    val state: String = "uploaded",
    val digest: String? = null,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)
