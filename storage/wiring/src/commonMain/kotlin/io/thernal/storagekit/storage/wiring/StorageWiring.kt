package io.thernal.storagekit.storage.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.storagekit.storage.api.data.KeyValueStore
import io.thernal.storagekit.storage.impl.data.preferences.StorageDirectory
import io.thernal.storagekit.storage.impl.data.preferences.preferencesKeyValueStore

/**
 * Binds the plain store into an application graph. The directory and the secure store are
 * platform bindings — `StorageAndroidWiring` (which needs an Android `Context` in the graph) and
 * `StorageIosWiring`. Every binding is a singleton: DataStore allows one instance per file.
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface StorageWiring {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideKeyValueStore(directory: StorageDirectory): KeyValueStore {
            return preferencesKeyValueStore(directory)
        }
    }
}
