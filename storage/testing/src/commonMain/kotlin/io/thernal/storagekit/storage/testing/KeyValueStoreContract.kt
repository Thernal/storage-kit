package io.thernal.storagekit.storage.testing

import io.thernal.storagekit.storage.api.data.IntKey
import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.api.data.StringKey
import io.thernal.storagekit.storage.api.data.booleanKey
import io.thernal.storagekit.storage.api.data.intKey
import io.thernal.storagekit.storage.api.data.stringKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val LAUNCHES = 3
private const val OTHER_LAUNCHES = 7
private const val OBSERVED_EMISSIONS = 3
private const val MAX_WAITS = 2_000

/**
 * What every [KeyValueStore] must do. Extend it in a test source set with [createStore] returning the
 * store under test — a real one over a temporary file, or a replacement an app writes — and every
 * test here runs against it.
 */
abstract class KeyValueStoreContract {
    private enum class Session : StringKey { ACCESS_TOKEN, REFRESH_TOKEN }

    private enum class Counters : IntKey { LAUNCHES }

    private val store: KeyValueStore by lazy { createStore() }

    /** A new, empty store. Called at most once per test. */
    protected abstract fun createStore(): KeyValueStore

    /** Releases whatever [createStore] made; runs after each test. */
    protected open fun disposeStore() {
        // Nothing to release by default.
    }

    @AfterTest
    fun dispose() {
        disposeStore()
    }

    @Test
    fun anAbsentValueReadsAsNull() {
        runTest {
            assertNull(store.get(stringKey("missing")))
            assertFalse(store.contains(stringKey("missing")))
        }
    }

    @Test
    fun valuesRoundTripWithTheirTypes() {
        runTest {
            store.set(key = Session.ACCESS_TOKEN, value = "a.b.c")
            store.set(key = Counters.LAUNCHES, value = LAUNCHES)
            store.set(key = booleanKey("seen"), value = true)

            assertEquals(expected = "a.b.c", actual = store.get(Session.ACCESS_TOKEN))
            assertEquals(expected = LAUNCHES, actual = store.get(Counters.LAUNCHES))
            assertEquals(expected = true, actual = store.get(booleanKey("seen")))
            assertTrue(store.contains(Session.ACCESS_TOKEN))
        }
    }

    @Test
    fun aValueTheKeysTypeCannotReadIsAbsent() {
        runTest {
            store.set(key = stringKey("count"), value = "not a number")

            assertNull(store.get(intKey("count")))
        }
    }

    @Test
    fun removeAndClearDropValues() {
        runTest {
            store.set(key = Session.ACCESS_TOKEN, value = "a")
            store.set(key = Session.REFRESH_TOKEN, value = "r")

            store.remove(Session.ACCESS_TOKEN)
            assertNull(store.get(Session.ACCESS_TOKEN))
            assertEquals(expected = "r", actual = store.get(Session.REFRESH_TOKEN))

            store.clear()
            assertNull(store.get(Session.REFRESH_TOKEN))
            assertEquals(expected = emptyMap(), actual = store.observeAll().first())
        }
    }

    @Test
    fun observeEmitsTheCurrentValueThenChangesWithoutRepeats() {
        runTest {
            store.set(key = Session.ACCESS_TOKEN, value = "first")
            val seen = mutableListOf<String?>()
            val collecting = launch { store.observe(Session.ACCESS_TOKEN).take(OBSERVED_EMISSIONS).toList(seen) }
            waitUntil { seen.size == 1 }

            store.set(key = Session.ACCESS_TOKEN, value = "second")
            waitUntil { seen.size == 2 }
            store.set(key = Session.ACCESS_TOKEN, value = "second")
            store.remove(Session.ACCESS_TOKEN)
            collecting.join()

            assertEquals(expected = listOf("first", "second", null), actual = seen)
        }
    }

    @Test
    fun observeAllShowsEveryStoredString() {
        runTest {
            store.set(key = Session.ACCESS_TOKEN, value = "a")
            store.set(key = Counters.LAUNCHES, value = OTHER_LAUNCHES)

            assertEquals(
                expected = mapOf("ACCESS_TOKEN" to "a", "LAUNCHES" to OTHER_LAUNCHES.toString()),
                actual = store.observeAll().first(),
            )
        }
    }

    // Real stores deliver changes on their own dispatchers, on real time a test clock does not advance.
    private suspend fun waitUntil(condition: () -> Boolean) {
        repeat(MAX_WAITS) {
            if (condition()) {
                return
            }
            delay(1)
        }
    }
}
