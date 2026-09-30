package io.thernal.storagekit.storage.impl.data.secure

import io.thernal.storagekit.storage.api.data.Key
import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * [SecureKeyValueStore] over a platform [SecretBackend].
 *
 * The Keychain reports no changes, so observation is driven here: every write through this store
 * bumps a revision, and observers re-read. A change made outside this instance — another process, or
 * a second instance — is seen on the next write; bind one instance per app.
 */
class SecretKeyValueStore(
    private val backend: SecretBackend,
) : SecureKeyValueStore {
    private val revision = MutableStateFlow(0L)

    override suspend fun <T> get(key: Key<T>): T? {
        return backend.read(key.name)?.let(key.codec::decode)
    }

    override suspend fun <T> set(
        key: Key<T>,
        value: T,
    ) {
        backend.write(name = key.name, value = key.codec.encode(value))
        changed()
    }

    override suspend fun contains(key: Key<*>): Boolean {
        return backend.read(key.name) != null
    }

    override suspend fun remove(key: Key<*>) {
        backend.remove(key.name)
        changed()
    }

    override suspend fun clear() {
        backend.clear()
        changed()
    }

    override fun <T> observe(key: Key<T>): Flow<T?> {
        return revision.map { get(key) }.distinctUntilChanged()
    }

    override fun observeAll(): Flow<Map<String, String>> {
        return revision
            .map {
                backend.names()
                    .mapNotNull { name -> backend.read(name)?.let { value -> name to value } }
                    .toMap()
            }
            .distinctUntilChanged()
    }

    private fun changed() {
        revision.update { it + 1 }
    }
}
