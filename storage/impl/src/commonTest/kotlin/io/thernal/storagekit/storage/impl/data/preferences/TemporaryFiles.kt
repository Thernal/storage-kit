package io.thernal.storagekit.storage.impl.data.preferences

import okio.Path
import kotlin.random.Random

/** A fresh directory under the system temp directory, for one test. */
internal fun temporaryDirectory(): Path {
    val directory = okio.FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "storage-kit-test-${Random.nextLong().toULong()}"
    systemFileSystem.createDirectories(directory)
    return directory
}

internal fun Path.deleteRecursively() {
    systemFileSystem.deleteRecursively(this)
}
