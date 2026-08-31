package com.tianqi.weather.data

import android.util.Base64
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.json.JSONObject
import java.nio.charset.StandardCharsets

// 和风天气 JWT 认证（Ed25519 / EdDSA）。运行时凭据优先，其次构建期 local.properties。
object QwAuth {

    private var cached: String? = null
    private var cachedExp: Long = 0
    private var lastFailAt: Long = 0

    private const val NEGATIVE_CACHE_MS = 5 * 60_000L

    @Synchronized
    fun token(): String? {
        val creds = SecretStore.resolvedQw()
        if (!creds.jwtReady) return null
        val now = System.currentTimeMillis() / 1000
        cached?.let { if (now < cachedExp - 120) return it }
        if (lastFailAt > 0 && System.currentTimeMillis() - lastFailAt < NEGATIVE_CACHE_MS) return null
        val t = sign(now, creds)
        if (t == null) {
            lastFailAt = System.currentTimeMillis()
            android.util.Log.e("TianQiWeather", "QwAuth 签名失败，和风请求将无 token")
            return null
        }
        lastFailAt = 0
        cached = t
        cachedExp = now + 3600
        return t
    }

    @Synchronized
    fun invalidate() {
        cached = null
        cachedExp = 0
        lastFailAt = 0
    }

    /** 为尚未落盘的候选凭据签发一次 JWT；连接验证成功前不触碰全局缓存。 */
    internal fun tokenFor(creds: QwResolved, now: Long = System.currentTimeMillis() / 1000): String? {
        if (!creds.jwtReady) return null
        return sign(now, creds)
    }

    private fun sign(now: Long, creds: QwResolved): String? = try {
        val der = Base64.decode(normalizeKey(creds.privateKey), Base64.DEFAULT)
        require(der.size >= 32) { "bad private key" }
        val seed = der.copyOfRange(der.size - 32, der.size)
        val signer = Ed25519Signer()
        signer.init(true, Ed25519PrivateKeyParameters(seed, 0))

        val header = JSONObject()
            .put("alg", "EdDSA")
            .put("kid", creds.kid)
            .put("typ", "JWT")
        val payload = JSONObject()
            .put("sub", creds.projectId)
            .put("iat", now - 30)
            .put("exp", now + 3600)
        val signingInput = b64url(header.toString().toByteArray(StandardCharsets.US_ASCII)) +
            "." + b64url(payload.toString().toByteArray(StandardCharsets.US_ASCII))
        val data = signingInput.toByteArray(StandardCharsets.US_ASCII)
        signer.update(data, 0, data.size)
        signingInput + "." + b64url(signer.generateSignature())
    } catch (_: Exception) {
        null
    }

    internal fun normalizeKey(raw: String): String =
        raw.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN ED25519 PRIVATE KEY-----", "")
            .replace("-----END ED25519 PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

    private fun b64url(b: ByteArray): String =
        android.util.Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
