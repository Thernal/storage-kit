package io.thernal.storagekit.storage.impl.data.secure

/** A [SecretBackend] in a map, to test [SecretKeyValueStore] apart from any platform. */
internal class InMemorySecretBackend : SecretBackend {
    val values = mutableMapOf<String, String>()

    override suspend fun read(name: String): String? {
        return values[name]
    }

    override suspend fun write(
        name: String,
        value: String,
    ) {
        values[name] = value
    }

    override suspend fun remove(name: String) {
        values.remove(name)
    }

    override suspend fun clear() {
        values.clear()
    }

    override suspend fun names(): Set<String> {
        return values.keys.toSet()
    }
}
