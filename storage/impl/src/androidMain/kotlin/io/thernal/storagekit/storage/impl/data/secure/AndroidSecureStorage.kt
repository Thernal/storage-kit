package io.thernal.storagekit.storage.impl.data.secure

import android.content.Context
import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import io.thernal.storagekit.storage.impl.data.preferences.StorageDirectory
import io.thernal.storagekit.storage.impl.data.preferences.preferencesDataStore

/**
 * The Android [SecureKeyValueStore]: a Keystore key named after the app, values in
 * `<directory>/secure.preferences_pb`. Create one per process.
 */
fun androidSecureKeyValueStore(
    context: Context,
    directory: StorageDirectory,
): SecureKeyValueStore {
    val key = AndroidKeystoreKey(alias = "${context.packageName}.secure-storage")
    val backend = EncryptedDataStoreBackend(
        dataStore = preferencesDataStore(directory.file("secure.preferences_pb")),
        cipher = AesGcmCipher(key::get),
    )
    return SecretKeyValueStore(backend)
}
