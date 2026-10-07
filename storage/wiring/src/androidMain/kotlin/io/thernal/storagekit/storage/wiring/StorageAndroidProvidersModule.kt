package io.thernal.storagekit.storage.wiring

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import io.thernal.storagekit.storage.impl.data.preferences.StorageDirectory
import io.thernal.storagekit.storage.impl.data.preferences.androidStorageDirectory
import io.thernal.storagekit.storage.impl.data.secure.androidSecureKeyValueStore

/** The Android bindings. The graph must provide the application `Context`. */
@BindingContainer
@ContributesTo(AppScope::class)
interface StorageAndroidWiring {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideStorageDirectory(context: Context): StorageDirectory {
            return androidStorageDirectory(context)
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideSecureKeyValueStore(
            context: Context,
            directory: StorageDirectory,
        ): SecureKeyValueStore {
            return androidSecureKeyValueStore(context = context, directory = directory)
        }
    }
}
