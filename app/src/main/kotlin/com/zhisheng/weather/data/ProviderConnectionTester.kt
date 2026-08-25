package com.zhisheng.weather.data

import java.net.IDN
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

enum class ProviderTestStage(val label: String) {
    VALIDATE("校验参数"),
    SIGN("签发 JWT"),
    CONNECT("请求服务"),
    VERIFY("核对响应"),
}

data class ProviderConnectionResult(
    val ok: Boolean,
    val title: String,
    val detail: String,
    val normalizedHost: String? = null,
)

data class QweatherHostResult(
    val value: String? = null,
    val error: String? = null,
) {
    val ok: Boolean get() = value != null
}

/** 对尚未保存的候选凭据做一次真实请求；本类不读写 SecretStore，也不记录凭据。 */
object ProviderConnectionTester {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun normalizeQweatherHost(raw: String): QweatherHostResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return QweatherHostResult(error = "请填写 API Host")
        return try {
            val withScheme = if (trimmed.contains("://")) trimmed else "https://$trimmed"
            val uri = URI(withScheme)
            if (!uri.scheme.equals("https", ignoreCase = true)) {
                return QweatherHostResult(error = "API Host 必须使用 HTTPS")
            }
            if (uri.userInfo != null || uri.query != null || uri.fragment != null) {
                return QweatherHostResult(error = "只粘贴主机地址，不要带账号、参数或片段")
            }
            if (!uri.path.isNullOrEmpty() && uri.path != "/") {
                return QweatherHostResult(error = "API Host 不应包含接口路径")
            }
            val asciiHost = uri.host?.let { IDN.toASCII(it).lowercase() }
                ?: return QweatherHostResult(error = "无法识别这个主机地址")
            if (asciiHost != "qweatherapi.com" && !asciiHost.endsWith(".qweatherapi.com")) {
                return QweatherHostResult(error = "请使用控制台提供的 qweatherapi.com 主机")
            }
            if (uri.port !in listOf(-1, 443)) {
                return QweatherHostResult(error = "API Host 只允许标准 HTTPS 端口")
            }
            QweatherHostResult(value = "https://$asciiHost")
        } catch (_: Exception) {
            QweatherHostResult(error = "API Host 格式不正确")
        }
    }

    suspend fun testQweather(
        candidate: QwRuntimeCreds,
        onStage: (ProviderTestStage) -> Unit,
    ): ProviderConnectionResult {
        onStage(ProviderTestStage.VALIDATE)
        val host = normalizeQweatherHost(candidate.host)
        if (!host.ok) return failure("参数未通过校验", host.error.orEmpty())
        val normalized = host.value!!
        val resolved = QwResolved(
            host = normalized,
            projectId = candidate.projectId.trim(),
            kid = candidate.kid.trim(),
            privateKey = candidate.privateKey.trim(),
            apiKey = candidate.apiKey.trim(),
        )
        if (!resolved.jwtReady && !resolved.keyReady) {
            return failure("凭据还不完整", "请补齐当前认证方式需要的字段")
        }

        val authHeader: Pair<String, String> = if (resolved.jwtReady) {
            onStage(ProviderTestStage.SIGN)
            val token = QwAuth.tokenFor(resolved)
                ?: return failure("JWT 签发失败", "请重新生成密钥，并核对项目 ID 与凭据 ID")
            "Authorization" to "Bearer $token"
        } else {
            "X-QW-Api-Key" to resolved.apiKey
        }

        return try {
            val client = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header(authHeader.first, authHeader.second)
                            .build(),
                    )
                })
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()
            val service = Retrofit.Builder()
                .baseUrl("$normalized/")
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(QWeatherService::class.java)

            onStage(ProviderTestStage.CONNECT)
            val body = service.cityLookup("北京", 1)
            onStage(ProviderTestStage.VERIFY)
            val city = body.location.firstOrNull()?.name?.trim()
            if (city.isNullOrEmpty()) {
                failure("服务已响应，但数据无效", "没有返回城市结果，请核对项目权限与 API Host")
            } else {
                ProviderConnectionResult(
                    ok = true,
                    title = "和风链路已建立",
                    detail = "已通过 $normalized 返回「$city」城市数据",
                    normalizedHost = normalized,
                )
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            mapFailure(t, provider = "和风天气")
        }
    }

    suspend fun testCaiyun(
        token: String,
        onStage: (ProviderTestStage) -> Unit,
    ): ProviderConnectionResult {
        onStage(ProviderTestStage.VALIDATE)
        val candidate = token.trim()
        if (candidate.isEmpty()) return failure("凭据还不完整", "请填写彩云 Token")
        return try {
            onStage(ProviderTestStage.CONNECT)
            val body = CaiyunApi.service.weather(candidate, "116.4074", "39.9042")
            onStage(ProviderTestStage.VERIFY)
            if (body.status.equals("ok", true) && body.result?.realtime != null) {
                ProviderConnectionResult(
                    ok = true,
                    title = "彩云链路已建立",
                    detail = "已返回北京实况天气数据",
                )
            } else {
                failure("服务已响应，但鉴权未通过", "请回开放平台核对 Token 和应用状态")
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            mapFailure(t, provider = "彩云天气")
        }
    }

    private fun mapFailure(t: Throwable, provider: String): ProviderConnectionResult = when (t) {
        is HttpException -> when (t.code()) {
            401 -> failure("鉴权被拒绝 · 401", "请核对凭据内容和认证方式")
            403 -> failure("当前凭据无权限 · 403", "请确认项目已启用天气服务")
            404 -> failure("接口未找到 · 404", "请核对 API Host 或应用接口版本")
            429 -> failure("请求频率受限 · 429", "稍后再试，并检查账户额度")
            else -> failure("$provider 返回 ${t.code()}", "服务已连通，但没有完成验证")
        }
        is UnknownHostException -> failure("无法解析服务主机", "请检查网络与主机地址")
        is SocketTimeoutException -> failure("连接超时", "请检查网络后重试")
        is SSLException -> failure("安全连接失败", "请确认系统时间和网络证书环境正常")
        is SerializationException -> failure("响应格式无法识别", "服务已响应，但内容与当前版本不兼容")
        else -> failure("连接未完成", "请检查网络、凭据和服务状态后重试")
    }

    private fun failure(title: String, detail: String) =
        ProviderConnectionResult(ok = false, title = title, detail = detail)
}
