package io.thernal.storagekit.storage.impl.data.preferences

import android.content.Context

/** `<files dir>/storage`: private to the app, kept across updates, removed with the app. */
fun androidStorageDirectory(context: Context): StorageDirectory {
    return StorageDirectory(context.filesDir.resolve("storage").absolutePath)
}
