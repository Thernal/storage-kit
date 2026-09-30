package io.thernal.storagekit.storage.impl.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * A DataStore over the file at [path], creating its directory. DataStore allows one instance per file
 * in a process — a second one fails on its first read — so whoever calls this keeps the result as a
 * singleton (the wiring binds it `@SingleIn(AppScope::class)`).
 */
fun preferencesDataStore(path: String): DataStore<Preferences> {
    val file = path.toPath()
    file.parent?.let { directory -> systemFileSystem.createDirectories(directory) }
    return PreferenceDataStoreFactory.createWithPath(produceFile = { file })
}
