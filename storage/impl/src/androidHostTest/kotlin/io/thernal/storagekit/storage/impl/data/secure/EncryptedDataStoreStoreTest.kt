package io.thernal.storagekit.storage.impl.data.secure

import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.api.data.stringKey
import io.thernal.storagekit.storage.impl.data.preferences.StorageDirectory
import io.thernal.storagekit.storage.impl.data.preferences.deleteRecursively
import io.thernal.storagekit.storage.impl.data.preferences.preferencesDataStore
import io.thernal.storagekit.storage.impl.data.preferences.temporaryDirectory
import io.thernal.storagekit.storage.testing.KeyValueStoreContract
import kotlinx.coroutines.test.runTest
import java.io.File
import javax.crypto.SecretKey
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** The Android secure store end to end, a software key standing in for the Keystore's. */
class EncryptedDataStoreStoreTest : KeyValueStoreContract() {
    private val directory = temporaryDirectory()
    private val file = StorageDirectory(directory.toString()).file("secure.preferences_pb")
    private var key: SecretKey = softwareKey()
    private val dataStore by lazy { preferencesDataStore(file) }

    override fun createStore(): KeyValueStore {
        return SecretKeyValueStore(EncryptedDataStoreBackend(dataStore, AesGcmCipher { key }))
    }

    override fun disposeStore() {
        directory.deleteRecursively()
    }

    @Test
    fun `the file holds no plain text`() {
        runTest {
            createStore().set(stringKey("token"), "hunter2")

            assertFalse("hunter2" in File(file).readBytes().decodeToString())
        }
    }

    @Test
    fun `a value whose key is gone is removed and read as absent`() {
        runTest {
            val store = createStore()
            store.set(stringKey("token"), "hunter2")

            key = softwareKey()

            assertNull(store.get(stringKey("token")))
            assertFalse(store.contains(stringKey("token")))
        }
    }
}
