package io.thernal.storagekit.storage.impl.data.secure

/**
 * Where [SecretKeyValueStore] keeps its strings, by name — the platform part of secure storage. The
 * Android backend encrypts with a Keystore key into a DataStore file; the iOS one uses the Keychain.
 *
 * [read] returns null for a value it can no longer decrypt, having removed it.
 */
interface SecretBackend {
    suspend fun read(name: String): String?

    suspend fun write(
        name: String,
        value: String,
    )

    suspend fun remove(name: String)

    suspend fun clear()

    suspend fun names(): Set<String>
}
