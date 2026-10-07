package io.thernal.storagekit.storage.impl.data.secure

class KeychainException(
    operation: String,
    val status: Int,
) : IllegalStateException("Keychain $operation failed with OSStatus $status")
