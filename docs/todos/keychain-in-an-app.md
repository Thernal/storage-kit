# Verify the Keychain backend inside an app

**Status:** open

`KeychainBackend` compiles for both iOS targets, and its delete path ran against the macOS login Keychain (same Security API), but
its add / read / list paths have not run against one yet: the Kotlin/Native test runner starts the test
binary on the simulator outside any app (`errSecNotAvailable`), and the macOS check binary
(`KeychainBackend` built for macosArm64) could not add an item from a non-interactive session
(`errSecInteractionNotAllowed`).

To close this: run `KeychainStoreTest`'s round trip inside an app — the first application that installs
the kit, or an XCTest host — and record the result here. If items must be shared with an app extension,
add a `kSecAttrAccessGroup` parameter to `iosSecureKeyValueStore`.
