package io.thernal.storagekit.storage.impl.data.preferences

/**
 * The private directory the stores keep their files in: on Android the app's files directory, on iOS
 * Application Support — neither is visible to the user, both survive updates. Built by
 * `androidStorageDirectory(context)` or `iosStorageDirectory()`, or from any path an app prefers.
 */
class StorageDirectory(
    val path: String,
) {
    fun file(name: String): String {
        return "${path.trimEnd('/')}/$name"
    }
}
