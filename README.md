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

## Installing

An application takes the kit **by copy**, not as a dependency: the code is copied into the app, renamed to the app's own package, and belongs to the app from then on. Nothing is published to a Maven repository.

### With skill-manager

If you have access to the author's knowledge repository (`github.com/Thernal/knowledge`), its **skill-manager** skill does all of it — copy, rename, the skill, and later updates:

```sh
skillctl.sh kit install storage-kit --package com.example.app --module :core:storage --alias app
```

It copies the `code` parts of [`kit.yml`](kit.yml) renamed, installs the `storage-kit` skill and records the copy in `kits.lock`. `kit status` then shows what changed upstream and what the app edited; `kit update` merges the kit's changes three ways, keeping the app's edits. The install prints what the app must provide (`requires`).

### Without it

The same by hand, from a clone of this repository.

1. **Copy** the paths listed under `code` in [`kit.yml`](kit.yml) into the app, under the module path the app gives them: `storage/…` → `core/storage/…`. Note the commit you copied (`git rev-parse HEAD`) — updates start from it.
2. **Rename** in everything copied:

   | In the kit | Becomes | Where |
   |---|---|---|
   | `io.thernal.storagekit` | the app's package, e.g. `com.example.app` | sources, build files; and the directories `io/thernal/storagekit` |
   | `:storage:` and `":storage"`, `projects.storage.` | the module path, e.g. `:core:storage:`, `projects.core.storage.` | build files |
   | `libs.plugins.storagekit.` | the app's catalog alias, e.g. `libs.plugins.app.` | build files |

   ```sh
   # in the app, after copying — perl, so it runs the same on macOS and Linux
   grep -rlI -e io.thernal.storagekit -e io/thernal/storagekit -e :storage -e plugins.storagekit. core/storage \
     | xargs perl -pi -e 's/\Qio.thernal.storagekit\E/com.example.app/g; s{\Qio/thernal/storagekit\E}{com/example/app}g; s/\Q:storage:\E/:core:storage:/g; s/"\Q:storage\E"/":core:storage"/g; s/projects\.\Qstorage\E\./projects.core.storage./g; s/libs\.plugins\.\Qstoragekit\E\./libs.plugins.app./g'
   find core/storage -depth -type d -path '*/io/thernal/storagekit' | while read -r d; do
     mkdir -p "${d%/io/thernal/storagekit}/com/example" && mv "$d" "${d%/io/thernal/storagekit}/com/example/app"
   done
   find core/storage -depth -type d -empty -delete
   ```

3. **Provide** what the copy expects — the `requires` list in [`kit.yml`](kit.yml): convention plugins (build-kit's, or the ones in this repository's `build-logic/convention`), catalog entries, settings — and, where listed, platform setup.
4. **The skill** (optional): copy [`skills/storage-kit`](skills/storage-kit) into the app's skills directory (`.claude/skills/` for Claude Code), with the same renames, so an agent working in the app knows the kit.
5. **Updates** are yours to carry: `git diff <the commit you copied> <a newer one> -- <the code paths>` in the kit shows what changed; apply what you want, renamed the same way.

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
Kotlin/Native test runner does not provide; it reports that and checks nothing there. The backend's
round trip was verified against the macOS login Keychain through the same Security API — see
[`docs/todos/keychain-access-groups.md`](docs/todos/keychain-access-groups.md).

## Static analysis

`config/detekt/detekt.yml` and the rules in `build-logic/detekt-rules`; the code is clean, with no
baseline and no `// TODO: Detekt` markers.
