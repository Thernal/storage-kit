package io.thernal.storagekit.storage.wiring

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.thernal.storagekit.storage.api.data.SecureKeyValueStore
import io.thernal.storagekit.storage.impl.data.preferences.StorageDirectory
import io.thernal.storagekit.storage.impl.data.preferences.iosStorageDirectory
import io.thernal.storagekit.storage.impl.data.secure.iosSecureKeyValueStore

/** The iOS bindings: Application Support for files, the Keychain under `<bundle id>.secure-storage`. */
@BindingContainer
@ContributesTo(AppScope::class)
interface StorageIosWiring {
    companion object {
        @Provides
        @SingleIn(AppScope::class)
        fun provideStorageDirectory(): StorageDirectory {
            return iosStorageDirectory()
        }

        @Provides
        @SingleIn(AppScope::class)
        fun provideSecureKeyValueStore(): SecureKeyValueStore {
            return iosSecureKeyValueStore()
        }
    }
}
