package io.thernal.storagekit.storage.impl.data.secure

import io.thernal.storagekit.storage.api.data.stringKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import platform.Security.errSecNotAvailable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The iOS secure store against a real Keychain. The Kotlin/Native test runner starts the test binary
 * on the simulator outside any app, where there is no Keychain (`errSecNotAvailable`); the test then
 * says so and checks nothing. Run inside an app — the scratch app, or an XCTest host — it runs in full.
 */
class KeychainStoreTest {
    private val backend = KeychainBackend(service = "storage-kit-test-${Random.nextLong().toULong()}")
    private val store = SecretKeyValueStore(backend)

    @Test
    fun `values round trip through the keychain`() {
        runTest {
            val token = stringKey("token")
            try {
                store.set(token, "hunter2")
            } catch (unavailable: KeychainException) {
                if (unavailable.status != errSecNotAvailable) {
                    throw unavailable
                }
                println("KeychainStoreTest: no Keychain in this test runner (errSecNotAvailable); not checked here")
                return@runTest
            }

            assertEquals(expected = "hunter2", actual = store.get(token))
            store.set(token, "rotated")
            assertEquals(expected = "rotated", actual = store.get(token))
            assertEquals(expected = mapOf("token" to "rotated"), actual = store.observeAll().first())

            store.remove(token)
            assertNull(store.get(token))
            store.set(token, "again")
            store.clear()
            assertEquals(expected = emptyMap(), actual = store.observeAll().first())
        }
    }
}
