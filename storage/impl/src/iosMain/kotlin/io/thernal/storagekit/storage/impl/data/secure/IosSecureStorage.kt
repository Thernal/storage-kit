package io.thernal.storagekit.storage.impl.data.secure

import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import platform.Foundation.NSBundle

/** The iOS [SecureKeyValueStore]: Keychain items under `<bundle id>.secure-storage`. Create one per process. */
fun iosSecureKeyValueStore(service: String = defaultService()): SecureKeyValueStore {
    return SecretKeyValueStore(KeychainBackend(service))
}

private fun defaultService(): String {
    return (NSBundle.mainBundle.bundleIdentifier ?: "storage") + ".secure-storage"
}
