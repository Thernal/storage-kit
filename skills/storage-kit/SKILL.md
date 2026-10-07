---
name: storage-kit
description: Stores and reads local key-value data in Compose Multiplatform apps that use storage-kit (packages io.thernal.storagekit.storage.*; Key, StringKey, IntKey, LongKey, DoubleKey, BooleanKey, stringKey, KeyCodec, KeyValueStore, SecureKeyValueStore, FakeKeyValueStore, KeyValueStoreContract, StorageProvidersModule). Use it for any local persistence of settings, flags, counters, identifiers, tokens or credentials in such a project, even when storage-kit is not named - installing the kit, declaring keys as values or enums, reading, writing and observing values, keeping tokens encrypted (Android Keystore, iOS Keychain), sign-out, feeding a debug console, and testing code that stores things. Not for databases, files, caches of large data, or projects without storage-kit.
---

# storage-kit

storage-kit is typed local key-value storage for Compose Multiplatform (Android, iosArm64,
iosSimulatorArm64): a plain store over DataStore and a secure one (Android Keystore AES/GCM, iOS
Keychain) behind one contract — suspend reads and writes, `Flow` to observe. Source and the complete
guide: https://github.com/Thernal/storage-kit — `storage/api/README.md`.

## 1. Orient first

```sh
grep -rn --include=*.kt -e "StorageProvidersModule" -e "preferencesKeyValueStore(" -e "SecureKeyValueStore(" .   # installed? how?
grep -rn --include=*.kt -e ": StringKey" -e ": IntKey" -e ": LongKey" -e ": BooleanKey" -e ": DoubleKey" .  # enum key sets
grep -rn --include=*.kt -e "stringKey(\"" -e "longKey(\"" -e "intKey(\"" -e "booleanKey(\"" -e "key(\"" .   # value keys — names taken
grep -rn --include=*.kt -e "observeAll()" .                                                                 # console wiring
```

Nothing installed → [references/setup.md](references/setup.md) first. **Taken as a kit?** A `kits.lock`
naming `storage-kit` means the modules were copied renamed with skill-manager — this skill with them, so the
names here are the project's. `skillctl.sh kit status storage-kit` says what moved upstream; offer
`kit update storage-kit` rather than editing the copy towards it by hand.

## 2. The model

- A **key** is a name plus a codec. Declare it once: `val LastSyncedAt = longKey("last_synced_at")`, or an
  enum whose constants share a type: `enum class SessionKeys : StringKey { ACCESS_TOKEN }` — the constant's
  `name` is the stored name.
- **`KeyValueStore`** for anything not secret; **`SecureKeyValueStore`** for tokens and credentials. Same
  calls: `get`, `set`, `contains`, `remove`, `clear` (suspend), `observe(key)`, `observeAll()` (Flow).
- Values are stored as strings; one that no longer decodes, or no longer decrypts, reads as **absent**.
- One instance of each store per app (the wiring binds singletons).

## 3. Tasks

| Task | Read |
|---|---|
| install, bind (Metro needs an Android `Context` in the graph), or build by hand | [setup.md](references/setup.md) |
| declare keys, custom types, read/write/observe, tokens and sign-out, a debug console, tests | [usage.md](references/usage.md) |

## 4. Rules

- Never put a secret in `KeyValueStore`.
- Never create a second store for the same file or Keychain service — DataStore fails, and observers of the
  first stop seeing changes.
- Never rename a key or an enum constant that already shipped without migrating its value: the stored name
  changes with it.
- Read state a screen shows by `observe`, not by a one-off `get` in `init`.
- Absent is a normal answer: an undecryptable token reads as null — treat it as signed out.
- In tests use `FakeKeyValueStore` / `FakeSecureKeyValueStore`; a custom store extends `KeyValueStoreContract`.

## 5. Verify

```sh
./gradlew build     # compiles every target, runs tests on the JVM host and the iOS simulator
```
