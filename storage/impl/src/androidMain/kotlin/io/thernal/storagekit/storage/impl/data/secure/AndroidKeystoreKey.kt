package io.thernal.storagekit.storage.impl.data.secure

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

/**
 * An AES key held by the Android Keystore under [alias]: generated on first use, never leaving the
 * secure hardware where the device has it. It does not survive an uninstall or a restore to another
 * device — values encrypted with it then read as absent.
 */
class AndroidKeystoreKey(
    private val alias: String,
) {
    private val lock = Any()

    fun get(): SecretKey {
        return synchronized(lock) {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            keyStore.getKey(alias, null) as? SecretKey ?: generate()
        }
    }

    private fun generate(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply { init(spec) }
            .generateKey()
    }
}
