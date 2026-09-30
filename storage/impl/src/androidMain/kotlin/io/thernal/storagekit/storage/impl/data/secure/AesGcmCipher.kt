package io.thernal.storagekit.storage.impl.data.secure

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH_BITS = 128
private const val SEPARATOR = ':'

/**
 * AES/GCM over strings: a fresh IV per value, stored as `base64(iv):base64(ciphertext+tag)`. GCM
 * authenticates, so a tampered or foreign value fails to decrypt instead of decrypting to garbage.
 * The key comes from [key] on every call, so a Keystore key is created lazily, on first use.
 */
class AesGcmCipher(
    private val key: () -> SecretKey,
) {
    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plain.encodeToByteArray())
        return Base64.encode(cipher.iv) + SEPARATOR + Base64.encode(encrypted)
    }

    /** Throws for anything [encrypt] did not produce with the same key. */
    fun decrypt(stored: String): String {
        val iv = stored.substringBefore(delimiter = SEPARATOR, missingDelimiterValue = "")
        val encrypted = stored.substringAfter(delimiter = SEPARATOR, missingDelimiterValue = "")
        require(iv.isNotEmpty() && encrypted.isNotEmpty()) { "Not a value this cipher wrote." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val parameters = GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(iv))
        cipher.init(Cipher.DECRYPT_MODE, key(), parameters)
        return cipher.doFinal(Base64.decode(encrypted)).decodeToString()
    }
}
