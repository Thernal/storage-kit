package io.thernal.storagekit.storage.impl.data.secure

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertFalse

// The Android Keystore does not exist on the JVM host; a software AES key exercises the same cipher.
internal fun softwareKey(): SecretKey {
    return KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
}

class AesGcmCipherTest {
    private val key = softwareKey()
    private val cipher = AesGcmCipher { key }

    @Test
    fun `round trips and never repeats a ciphertext`() {
        val first = cipher.encrypt("secret")
        val second = cipher.encrypt("secret")

        assertEquals("secret", cipher.decrypt(first))
        assertNotEquals(first, second)
        assertFalse("secret" in first)
    }

    @Test
    fun `rejects tampered foreign and malformed values`() {
        val stored = cipher.encrypt("secret")
        val flipped = if (stored.last() == 'A') {
            "BA"
        } else {
            "AA"
        }
        val tampered = stored.dropLast(2) + flipped

        assertFailsWith<Exception> { cipher.decrypt(tampered) }
        assertFailsWith<Exception> { AesGcmCipher { softwareKey() }.decrypt(stored) }
        assertFailsWith<IllegalArgumentException> { cipher.decrypt("plain text") }
    }
}
