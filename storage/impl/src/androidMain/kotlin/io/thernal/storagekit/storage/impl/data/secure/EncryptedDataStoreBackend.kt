package io.thernal.storagekit.storage.impl.data.secure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

/**
 * The Android [SecretBackend]: values encrypted by [cipher] into their own DataStore file, apart from
 * the plain store's. A value that no longer decrypts — its Keystore key is gone — is removed and read
 * as absent.
 */
class EncryptedDataStoreBackend(
    private val dataStore: DataStore<Preferences>,
    private val cipher: AesGcmCipher,
) : SecretBackend {
    override suspend fun read(name: String): String? {
        val stored = dataStore.data.first()[stringPreferencesKey(name)] ?: return null
        return runCatching { cipher.decrypt(stored) }.getOrElse {
            remove(name)
            null
        }
    }

    override suspend fun write(
        name: String,
        value: String,
    ) {
        val encrypted = cipher.encrypt(value)
        dataStore.edit { values -> values[stringPreferencesKey(name)] = encrypted }
    }

    override suspend fun remove(name: String) {
        dataStore.edit { values -> values.remove(stringPreferencesKey(name)) }
    }

    override suspend fun clear() {
        dataStore.edit { values -> values.clear() }
    }

    override suspend fun names(): Set<String> {
        return dataStore.data.first().asMap().keys.mapTo(mutableSetOf()) { it.name }
    }
}
