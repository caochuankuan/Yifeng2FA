package com.compose.yifeng2fa.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray(Charsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }

    private fun generateIv(password: String): IvParameterSpec {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = password.toByteArray(Charsets.UTF_8)
        val iv = digest.digest(bytes)
        return IvParameterSpec(iv)
    }

    fun encrypt(plainText: String, password: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = generateKey(password)
            val ivSpec = generateIv(password)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun decrypt(encryptedText: String, password: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = generateKey(password)
            val ivSpec = generateIv(password)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decoded = Base64.decode(encryptedText, Base64.DEFAULT)
            val decrypted = cipher.doFinal(decoded)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
