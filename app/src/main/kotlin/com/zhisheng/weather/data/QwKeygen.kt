package com.zhisheng.weather.data

import android.util.Base64
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory
import java.security.SecureRandom

data class QwGeneratedKeys(
    val publicPem: String,
    val privateDerB64: String,
)

object QwKeygen {
    fun generate(): QwGeneratedKeys {
        val gen = Ed25519KeyPairGenerator()
        gen.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val pair = gen.generateKeyPair()
        val pub = pair.public as Ed25519PublicKeyParameters
        val priv = pair.private as Ed25519PrivateKeyParameters
        val spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pub).encoded
        val b64 = Base64.encodeToString(spki, Base64.NO_WRAP)
        val body = b64.chunked(64).joinToString("\n")
        val pem = "-----BEGIN PUBLIC KEY-----\n$body\n-----END PUBLIC KEY-----"
        return QwGeneratedKeys(
            publicPem = pem,
            privateDerB64 = Base64.encodeToString(priv.encoded, Base64.NO_WRAP),
        )
    }
}

object QwPing {
    suspend fun test(): String {
        if (!QWeatherApi.enabled) return "还没有填完凭据"
        return try {
            val r = QWeatherApi.service.cityLookup("北京")
            val name = r.location.firstOrNull()?.name
            if (name.isNullOrBlank()) "已连通，但没有返回城市，请核对 Host 与凭据"
            else "连接成功，和风已返回「$name」"
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (_: Exception) {
            "连接失败，请核对 API Host 与凭据"
        }
    }
}
