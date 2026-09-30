package io.thernal.storagekit.storage.impl.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.thernal.storagekit.storage.api.data.Key
import io.thernal.storagekit.storage.api.data.KeyValueStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

/**
 * [KeyValueStore] over a DataStore Preferences file. Every value is kept as the string its key's codec
 * writes, so the file holds one type and `observeAll()` can show all of it.
 */
class DataStoreKeyValueStore(
    private val dataStore: DataStore<Preferences>,
) : KeyValueStore {
    // An unreadable file reads as empty rather than failing every caller; DataStore rewrites it on the
    // next edit.
    private val preferences: Flow<Preferences> = dataStore.data.catch { failure ->
        if (failure is IOException) {
            emit(emptyPreferences())
        } else {
            throw failure
        }
    }

    override suspend fun <T> get(key: Key<T>): T? {
        return preferences.first()[key.preferenceKey()]?.let(key.codec::decode)
    }

    override suspend fun <T> set(
        key: Key<T>,
        value: T,
    ) {
        dataStore.edit { values -> values[key.preferenceKey()] = key.codec.encode(value) }
    }

    override suspend fun contains(key: Key<*>): Boolean {
        return key.preferenceKey() in preferences.first()
    }

    override suspend fun remove(key: Key<*>) {
        dataStore.edit { values -> values.remove(key.preferenceKey()) }
    }

    override suspend fun clear() {
        dataStore.edit { values -> values.clear() }
    }

    override fun <T> observe(key: Key<T>): Flow<T?> {
        return preferences
            .map { values -> values[key.preferenceKey()]?.let(key.codec::decode) }
            .distinctUntilChanged()
    }

    override fun observeAll(): Flow<Map<String, String>> {
        return preferences
            .map { values -> values.asMap().entries.associate { (key, value) -> key.name to value.toString() } }
            .distinctUntilChanged()
    }

    private fun Key<*>.preferenceKey(): Preferences.Key<String> {
        return stringPreferencesKey(name)
    }
}

/**
 * The plain store an app binds: `<directory>/preferences.preferences_pb`. Create one per process —
 * DataStore allows one instance per file.
 */
fun preferencesKeyValueStore(directory: StorageDirectory): KeyValueStore {
    return DataStoreKeyValueStore(preferencesDataStore(directory.file("preferences.preferences_pb")))
}
