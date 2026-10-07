package io.thernal.storagekit.storage.testing

import io.thernal.storagekit.storage.api.data.SecureKeyValueStore

/** The in-memory stand-in for the secure store; nothing is encrypted — there is nothing to protect. */
class FakeSecureKeyValueStore(
    initial: Map<String, String> = emptyMap(),
) : FakeKeyValueStore(initial),
    SecureKeyValueStore
