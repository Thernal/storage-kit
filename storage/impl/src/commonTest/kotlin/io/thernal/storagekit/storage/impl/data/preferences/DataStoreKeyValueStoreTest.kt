package io.thernal.storagekit.storage.impl.data.preferences

import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.testing.KeyValueStoreContract

class DataStoreKeyValueStoreTest : KeyValueStoreContract() {
    private val directory = temporaryDirectory()

    override fun createStore(): KeyValueStore {
        val file = StorageDirectory(directory.toString()).file("test.preferences_pb")
        return DataStoreKeyValueStore(preferencesDataStore(file))
    }

    override fun disposeStore() {
        directory.deleteRecursively()
    }
}
