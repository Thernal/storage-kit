package io.thernal.storagekit.storage.api.data

/**
 * Turns a value into the string a store keeps, and back. Every store keeps strings, so one codec
 * serves the plain and the secure store alike, and `observeAll()` can show any value.
 *
 * [decode] returns null for a string it cannot read — a value written by an older version of the
 * app under the same key, say — and the store then reports the key as absent rather than failing.
 */
interface KeyCodec<T> {
    fun encode(value: T): String

    fun decode(stored: String): T?

    companion object {
        val String: KeyCodec<String> = codec(encode = { it }, decode = { it })
        val Int: KeyCodec<Int> = codec(encode = { it.toString() }, decode = { it.toIntOrNull() })
        val Long: KeyCodec<Long> = codec(encode = { it.toString() }, decode = { it.toLongOrNull() })
        val Double: KeyCodec<Double> = codec(encode = { it.toString() }, decode = { it.toDoubleOrNull() })
        val Boolean: KeyCodec<Boolean> = codec(encode = { it.toString() }, decode = { it.toBooleanStrictOrNull() })

        /** A codec from two functions — for an enum, a value class, or JSON through the app's serializer. */
        fun <T> codec(
            encode: (T) -> String,
            decode: (String) -> T?,
        ): KeyCodec<T> {
            return object : KeyCodec<T> {
                override fun encode(value: T): String {
                    return encode(value)
                }

                override fun decode(stored: String): T? {
                    return decode(stored)
                }
            }
        }
    }
}
