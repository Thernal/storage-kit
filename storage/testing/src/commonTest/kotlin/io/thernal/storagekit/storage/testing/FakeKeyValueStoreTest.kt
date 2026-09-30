package io.thernal.storagekit.storage.testing

import io.thernal.storagekit.storage.api.data.KeyValueStore

class FakeKeyValueStoreTest : KeyValueStoreContract() {
    override fun createStore(): KeyValueStore {
        return FakeKeyValueStore()
    }
}
