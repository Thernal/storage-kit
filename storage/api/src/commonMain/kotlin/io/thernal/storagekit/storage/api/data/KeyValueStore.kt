package io.thernal.storagekit.storage.api.data

import kotlinx.coroutines.flow.Flow

/**
 * Local key-value storage that survives the process: settings, flags, cached identifiers.
 *
 * Every call suspends — the stores read files or the Keychain, and a synchronous read would block
 * whatever thread asked. To react to a value changing, [observe] it; a one-off read is [get].
 *
 * Nothing secret goes here: use [SecureKeyValueStore], which encrypts at rest.
 */
interface KeyValueStore {
    /** The stored value, or null when there is none or it cannot be decoded as [key]'s type. */
    suspend fun <T> get(key: Key<T>): T?

    suspend fun <T> set(
        key: Key<T>,
        value: T,
    )

    suspend fun contains(key: Key<*>): Boolean

    suspend fun remove(key: Key<*>)

    /** Removes every value in this store — and only this store: the plain and the secure one are separate. */
    suspend fun clear()

    /** The current value, then every change to it; repeats are not emitted. */
    fun <T> observe(key: Key<T>): Flow<T?>

    /**
     * Every stored name and its stored string, then every change — for a debug console or a
     * diagnostics screen. On [SecureKeyValueStore] this decrypts every value: what to show, and in
     * which builds, is the caller's decision.
     */
    fun observeAll(): Flow<Map<String, String>>
}

/**
 * A [KeyValueStore] whose values are encrypted at rest: tokens, credentials, anything that must not
 * be readable from a backup or a rooted device. Android keeps them encrypted with a key held by the
 * Android Keystore; iOS keeps them in the Keychain.
 *
 * A value that can no longer be decrypted — the key was lost with an uninstall or a restore to
 * another device — is removed and read as absent.
 */
interface SecureKeyValueStore : KeyValueStore
