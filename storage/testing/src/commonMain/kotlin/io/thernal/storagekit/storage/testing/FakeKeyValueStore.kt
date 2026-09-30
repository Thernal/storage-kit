package io.thernal.storagekit.storage.testing

import io.thernal.storagekit.storage.api.data.Key
import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * An in-memory [KeyValueStore] for tests: the same contract as the real stores
 * ([KeyValueStoreContract] runs against both), no files, no Keystore. [stored] exposes what was
 * written, as the strings a real store would keep.
 */
open class FakeKeyValueStore(
    initial: Map<String, String> = emptyMap(),
) : KeyValueStore {
    private val values = MutableStateFlow(initial)

    val stored: StateFlow<Map<String, String>> get() = values

    override suspend fun <T> get(key: Key<T>): T? {
        return values.value[key.name]?.let(key.codec::decode)
    }

    override suspend fun <T> set(
        key: Key<T>,
        value: T,
    ) {
        values.update { it + (key.name to key.codec.encode(value)) }
    }

    override suspend fun contains(key: Key<*>): Boolean {
        return key.name in values.value
    }

    override suspend fun remove(key: Key<*>) {
        values.update { it - key.name }
    }

    override suspend fun clear() {
        values.value = emptyMap()
    }

    override fun <T> observe(key: Key<T>): Flow<T?> {
        return values.map { it[key.name]?.let(key.codec::decode) }.distinctUntilChanged()
    }

    override fun observeAll(): Flow<Map<String, String>> {
        return values
    }
}

/** The in-memory stand-in for the secure store; nothing is encrypted — there is nothing to protect. */
class FakeSecureKeyValueStore(
    initial: Map<String, String> = emptyMap(),
) : FakeKeyValueStore(initial),
    SecureKeyValueStore
