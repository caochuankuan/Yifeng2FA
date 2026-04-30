package com.compose.yifeng2fa.utils

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpUtils {
    fun generateTotp(
        secret: String,
        time: Long = System.currentTimeMillis(),
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30
    ): String {
        try {
            val base32 = Base32()
            val decodedKey = base32.decode(secret.uppercase().replace(" ", ""))

            val timeStep = time / (period * 1000L)
            val msg = ByteBuffer.allocate(8).putLong(timeStep).array()

            val mac = Mac.getInstance("Hmac$algorithm")
            mac.init(SecretKeySpec(decodedKey, "Hmac$algorithm"))
            val hash = mac.doFinal(msg)

            val offset = hash[hash.size - 1].toInt() and 0xF
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val otp = binary % 10.0.pow(digits.toDouble()).toInt()
            return String.format("%0${digits}d", otp)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error"
        }
    }

    fun getProgress(period: Int = 30): Float {
        val time = System.currentTimeMillis()
        val timeStep = period * 1000L
        val remaining = timeStep - (time % timeStep)
        return remaining.toFloat() / timeStep.toFloat()
    }

    fun parseOtpAuthUri(uriString: String): TotpData? {
        if (!uriString.startsWith("otpauth://totp/")) return null
        try {
            val uri = android.net.Uri.parse(uriString)
            val secret = uri.getQueryParameter("secret") ?: return null
            var issuer = uri.getQueryParameter("issuer") ?: ""
            var accountName = uri.path?.removePrefix("/") ?: ""

            if (accountName.contains(":")) {
                val parts = accountName.split(":")
                if (issuer.isEmpty()) issuer = parts[0]
                accountName = parts.drop(1).joinToString(":").trim()
            }

            val algorithm = uri.getQueryParameter("algorithm") ?: "SHA1"
            val digits = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period = uri.getQueryParameter("period")?.toIntOrNull() ?: 30

            val decodedIssuer = java.net.URLDecoder.decode(issuer, "UTF-8")
            val decodedAccountName = java.net.URLDecoder.decode(accountName, "UTF-8")

            return TotpData(
                issuer = decodedIssuer,
                accountName = decodedAccountName,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                period = period
            )
        } catch (e: Exception) {
            return null
        }
    }
}

data class TotpData(
    val issuer: String,
    val accountName: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
)
