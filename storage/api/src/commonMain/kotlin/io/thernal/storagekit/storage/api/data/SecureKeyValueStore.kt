package io.thernal.storagekit.storage.api.data

/**
 * A [KeyValueStore] whose values are encrypted at rest: tokens, credentials, anything that must not
 * be readable from a backup or a rooted device. Android keeps them encrypted with a key held by the
 * Android Keystore; iOS keeps them in the Keychain.
 *
 * A value that can no longer be decrypted — the key was lost with an uninstall or a restore to
 * another device — is removed and read as absent.
 */
interface SecureKeyValueStore : KeyValueStore
