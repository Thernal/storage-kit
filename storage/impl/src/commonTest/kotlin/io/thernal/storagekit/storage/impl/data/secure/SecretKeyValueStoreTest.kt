package io.thernal.storagekit.storage.impl.data.secure

import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.testing.KeyValueStoreContract

class SecretKeyValueStoreTest : KeyValueStoreContract() {
    override fun createStore(): KeyValueStore {
        return SecretKeyValueStore(InMemorySecretBackend())
    }
}
