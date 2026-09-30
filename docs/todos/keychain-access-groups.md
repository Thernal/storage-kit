# Keychain items shared with app extensions

**Status:** open

`KeychainBackend` keeps its items in the app's own Keychain access group, so an app extension (a
notification service extension, a widget) cannot read them. An app that needs that adds a
`kSecAttrAccessGroup` parameter to `iosSecureKeyValueStore` and the Keychain Sharing entitlement on both
targets. Add it when an app asks.

The backend itself is verified: on 2026-09-30 the full round trip — add, read, overwrite, list, delete,
clear — ran against the login Keychain through the same Security API (`KeychainBackend` built for
macosArm64, run from a Terminal session: "KEYCHAIN OK"). The Kotlin/Native test runner cannot do this
(`errSecNotAvailable` outside an app), so `KeychainStoreTest` stays a compile check.
