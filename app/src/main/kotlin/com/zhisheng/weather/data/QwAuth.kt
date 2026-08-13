package com.zhisheng.weather.data

import android.util.Base64
import com.zhisheng.weather.BuildConfig
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.json.JSONObject
import java.nio.charset.StandardCharsets

// 和风天气 JWT 认证（Ed25519 / EdDSA，凭据私钥签名，1 小时有效，过期前自动重签）
object QwAuth {

    private var cached: String? = null
    private var cachedExp: Long = 0
    private var lastFailAt: Long = 0

    // 签名失败负缓存：私钥格式错误时 5 分钟内不再重试解码+签名，
    // 此前每个请求都会失败一次并打一条日志（v0.0.4）
    private const val NEGATIVE_CACHE_MS = 5 * 60_000L

    // 7 路请求在 OkHttp 线程池并发取 token：加锁保证可见性 + 只签一次（v0.0.1）
    @Synchronized
    fun token(): String? {
        val now = System.currentTimeMillis() / 1000
        cached?.let { if (now < cachedExp - 120) return it }
        if (lastFailAt > 0 && System.currentTimeMillis() - lastFailAt < NEGATIVE_CACHE_MS) return null
        val t = sign(now)
        if (t == null) {
            lastFailAt = System.currentTimeMillis()
            android.util.Log.e("ZhishengWeather", "QwAuth 签名失败，和风请求将无 token（检查 qw.private_key 配置）")
            return null
        }
        lastFailAt = 0
        cached = t
        cachedExp = now + 3600
        return t
    }

    // 服务端 401 时作废旧 token，下次调用立即重签
    @Synchronized
    fun invalidate() {
        cached = null
        cachedExp = 0
    }

    private fun sign(now: Long): String? = try {
        val der = Base64.decode(BuildConfig.QW_PRIVATE_KEY, Base64.DEFAULT)
        require(der.size >= 32) { "bad private key" }
        val seed = der.copyOfRange(der.size - 32, der.size)
        val signer = Ed25519Signer()
        signer.init(true, Ed25519PrivateKeyParameters(seed, 0))

        val header = JSONObject()
            .put("alg", "EdDSA")
            .put("kid", BuildConfig.QW_KID)
            .put("typ", "JWT")
        val payload = JSONObject()
            .put("sub", BuildConfig.QW_PROJECT_ID)
            .put("iat", now - 30)
            .put("exp", now + 3600)
        val signingInput = b64url(header.toString().toByteArray(StandardCharsets.US_ASCII)) +
            "." + b64url(payload.toString().toByteArray(StandardCharsets.US_ASCII))
        val data = signingInput.toByteArray(StandardCharsets.US_ASCII)
        signer.update(data, 0, data.size)
        signingInput + "." + b64url(signer.generateSignature())
    } catch (e: Exception) {
        null
    }

    private fun b64url(b: ByteArray): String =
        android.util.Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
