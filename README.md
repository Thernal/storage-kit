# storage-kit

Local key-value storage for Compose Multiplatform (android, iosArm64, iosSimulatorArm64): typed keys,
a plain store over DataStore, and a secure one — encrypted with an Android Keystore key on Android, in
the Keychain on iOS — behind one suspend-and-`Flow` contract. Keys are values or enums:

```kotlin
enum class SessionKeys : StringKey { ACCESS_TOKEN, REFRESH_TOKEN }
val LastSyncedAt = longKey("last_synced_at")

secureStore.set(SessionKeys.ACCESS_TOKEN, token)
val token: String? = secureStore.get(SessionKeys.ACCESS_TOKEN)
store.observe(LastSyncedAt).collect { … }
```

## Documentation

| Read | For |
|---|---|
| this file | what is here and how it is built |
| [`storage/api/README.md`](storage/api/README.md) | every contract, and how to install and use it — task by task |
| [`storage/README.md`](storage/README.md) | why the contracts have their shape, and what changed from the apps it came from |
| [`skills/storage-kit`](skills/storage-kit/SKILL.md) | the same for an agent working in an app that uses the kit |
| [`docs/todos/`](docs/todos) | open questions |

## For AI agents

An application takes the kit by copy: `skillctl.sh kit install storage-kit --package <its package>
--module <its module path> --alias <its plugin alias>` copies `storage/api`, `impl`, `wiring` and
`testing` renamed, installs the `storage-kit` skill, and records both in `kits.lock`, so every later
change here can be merged into the copy. `kit.yml` lists what the copied modules expect from the
application's build.

## Layout

| Module | Holds | Depends on |
|---|---|---|
| `storage/api` | `Key`, `KeyCodec`, `StringKey`/`IntKey`/…, `KeyValueStore`, `SecureKeyValueStore` | coroutines |
| `storage/impl` | `DataStoreKeyValueStore`; `SecretKeyValueStore` over a platform `SecretBackend` — `EncryptedDataStoreBackend` + `AesGcmCipher` + `AndroidKeystoreKey` on Android, `KeychainBackend` on iOS; the storage directory | api, DataStore, Okio |
| `storage/wiring` | Metro bindings: `StorageWiring`, `StorageAndroidWiring`, `StorageIosWiring` | api, impl |
| `storage/testing` | `FakeKeyValueStore`, `FakeSecureKeyValueStore`, and `KeyValueStoreContract` — the test suite every store passes | api, kotlin-test |

## Targets

`android`, `iosArm64`, `iosSimulatorArm64` — the same set as nav-kit and paging-kit, so the kits sit in
one application.

## Building

```sh
./gradlew build
```

Compiles every target, runs the tests on the JVM host and the iOS simulator, and runs Detekt, which
fails on any finding. The Gradle daemon runs on JDK 21 (Metro's Gradle plugin), provisioned from
`gradle/gradle-daemon-jvm.properties`.

The same `KeyValueStoreContract` runs against the fakes, the DataStore store, the secure store over an
in-memory backend, and the Android secure store end to end (a software AES key standing in for the
Keystore, which the JVM host does not have). The iOS Keychain test needs a Keychain, which the
Kotlin/Native test runner does not provide; it reports that and checks nothing there — see
[`docs/todos/keychain-in-an-app.md`](docs/todos/keychain-in-an-app.md).

## Static analysis

`config/detekt/detekt.yml` and the rules in `build-logic/detekt-rules`; the code is clean, with no
baseline and no `// TODO: Detekt` markers.
