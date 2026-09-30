package io.thernal.storagekit.storage.api.data

/**
 * A stored value's name and type. The type travels with the key, so a read cannot ask for the wrong one.
 *
 * Declare keys once, where they belong — as values:
 *
 * ```
 * val LastSyncedAt = longKey("last_synced_at")
 * ```
 *
 * or as an enum, when a set of keys shares a type — the constant's `name` is the stored name, so the
 * enum needs no code of its own:
 *
 * ```
 * enum class SessionKeys : StringKey { ACCESS_TOKEN, REFRESH_TOKEN }
 * ```
 *
 * Renaming a key, or an enum constant, orphans the value stored under the old name.
 */
interface Key<T> {
    /** The stored name. An enum constant's own `name` satisfies it. */
    val name: String

    val codec: KeyCodec<T>
}

/** A key of type [String]; implement it on an enum to make each constant a key. */
interface StringKey : Key<String> {
    override val codec: KeyCodec<String> get() = KeyCodec.String
}

/** A key of type [Int]; implement it on an enum to make each constant a key. */
interface IntKey : Key<Int> {
    override val codec: KeyCodec<Int> get() = KeyCodec.Int
}

/** A key of type [Long]; implement it on an enum to make each constant a key. */
interface LongKey : Key<Long> {
    override val codec: KeyCodec<Long> get() = KeyCodec.Long
}

/** A key of type [Double]; implement it on an enum to make each constant a key. */
interface DoubleKey : Key<Double> {
    override val codec: KeyCodec<Double> get() = KeyCodec.Double
}

/** A key of type [Boolean]; implement it on an enum to make each constant a key. */
interface BooleanKey : Key<Boolean> {
    override val codec: KeyCodec<Boolean> get() = KeyCodec.Boolean
}

fun stringKey(name: String): Key<String> {
    return key(name = name, codec = KeyCodec.String)
}

fun intKey(name: String): Key<Int> {
    return key(name = name, codec = KeyCodec.Int)
}

fun longKey(name: String): Key<Long> {
    return key(name = name, codec = KeyCodec.Long)
}

fun doubleKey(name: String): Key<Double> {
    return key(name = name, codec = KeyCodec.Double)
}

fun booleanKey(name: String): Key<Boolean> {
    return key(name = name, codec = KeyCodec.Boolean)
}

/** A key of any type the [codec] can write as a string. */
fun <T> key(
    name: String,
    codec: KeyCodec<T>,
): Key<T> {
    require(name.isNotBlank()) { "A key needs a name." }
    return NamedKey(name = name, codec = codec)
}

private data class NamedKey<T>(
    override val name: String,
    override val codec: KeyCodec<T>,
) : Key<T>
