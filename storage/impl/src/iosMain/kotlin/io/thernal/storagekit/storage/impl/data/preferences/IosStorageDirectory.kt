package io.thernal.storagekit.storage.impl.data.preferences

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** `Application Support/storage`: private to the app, not user-visible, kept across updates. */
@OptIn(ExperimentalForeignApi::class)
fun iosStorageDirectory(): StorageDirectory {
    val support = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = checkNotNull(support?.path) { "No Application Support directory." }
    return StorageDirectory("$path/storage")
}
