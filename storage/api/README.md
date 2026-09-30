# storage/api

Typed local key-value storage — a plain store and a secure one behind the same contract. Package
`io.thernal.storagekit.storage.api.data`.

## Contents

| Type | Is |
|---|---|
| `Key<T>` | a stored value's `name` and `codec`; `StringKey`, `IntKey`, `LongKey`, `DoubleKey`, `BooleanKey` for enums |
| `stringKey(name)` … `booleanKey(name)`, `key(name, codec)` | keys as values |
| `KeyCodec<T>` | value ⇄ stored string; `KeyCodec.String/Int/Long/Double/Boolean`, `KeyCodec.codec(encode, decode)` |
| `KeyValueStore` | `get`, `set`, `contains`, `remove`, `clear` (suspend); `observe(key)`, `observeAll()` (`Flow`) |
| `SecureKeyValueStore` | the same contract, encrypted at rest — tokens, credentials |

## Which store

| Keep | In |
|---|---|
| settings, flags, counters, identifiers that are not secret | `KeyValueStore` |
| access and refresh tokens, credentials, anything a backup or a rooted device must not reveal | `SecureKeyValueStore` |
| large or structured data, lists, anything queried | not here — a database |

The two are separate stores: `clear()` on one leaves the other.

## Installing

### Modules

| Module | Who depends on it | What it holds |
|---|---|---|
| `:storage:api` | every module that reads or writes a value | the contracts in this document |
| `:storage:impl` | the module that builds the graph (or wires by hand) | the stores, the Android and iOS backends |
| `:storage:wiring` | the module that declares the [Metro](https://github.com/ZacSweers/metro) graph | `StorageWiring`, `StorageAndroidWiring`, `StorageIosWiring` |
| `:storage:testing` | test source sets | `FakeKeyValueStore`, `FakeSecureKeyValueStore`, `KeyValueStoreContract` |

```kotlin
commonMain.dependencies {
    implementation(projects.storage.api)
    implementation(projects.storage.impl)
    implementation(projects.storage.wiring)            // leave out when wiring by hand
    implementation(libs.kotlinx.coroutines.core)
}
commonTest.dependencies {
    implementation(projects.storage.testing)
}
```

### Dependencies you declare

`api(...)` is not used in this repository: nothing is re-exported, so a module that uses a type from
these libraries declares it itself, as `implementation`.

| Library (catalog entry) | Declare it where the module uses |
|---|---|
| `kotlinx-coroutines-core` | `Flow` from `observe` / `observeAll`, and to call the suspend functions |

### With Metro

The wiring contributes to `AppScope`. The plain store, the directory and the secure store are app-scoped
singletons — DataStore allows one instance per file. **On Android the graph must provide the application
`Context`** (the storage directory and the Keystore alias come from it):

```kotlin
@DependencyGraph(AppScope::class)
interface AppGraph {
    val keyValueStore: KeyValueStore
    val secureKeyValueStore: SecureKeyValueStore

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AppGraph   // Android; the iOS graph takes nothing
    }
}
```

### Without a DI framework

Create each store once and keep it:

```kotlin
// Android
val directory = androidStorageDirectory(context)
val store = preferencesKeyValueStore(directory)
val secure = androidSecureKeyValueStore(context, directory)

// iOS
val directory = iosStorageDirectory()
val store = preferencesKeyValueStore(directory)
val secure = iosSecureKeyValueStore()                       // service "<bundle id>.secure-storage"
```

Files go to `<files dir>/storage/` on Android and `Application Support/storage/` on iOS.

## Keys

As values, when each key has its own type:

```kotlin
val LastSyncedAt = longKey("last_synced_at")
val OnboardingSeen = booleanKey("onboarding_seen")
```

As an enum, when a set of keys shares a type — each constant's `name` is its stored name:

```kotlin
enum class SessionKeys : StringKey { ACCESS_TOKEN, REFRESH_TOKEN, PROFILE_ID }
enum class Counters : IntKey { LAUNCHES, RATING_PROMPTS }
```

Renaming a constant or a key orphans what was stored under the old name. A value whose name must survive
refactors is a `val` with an explicit name.

Any other type goes through a codec — an enum value, a value class, JSON through the app's serializer:

```kotlin
val ThemeKey = key("theme", KeyCodec.codec<Theme>(encode = { it.name }, decode = { s -> Theme.entries.find { it.name == s } }))
```

A codec's `decode` returns null for a string it cannot read; the store then reports the key as absent.

## KeyValueStore

```kotlin
store.set(OnboardingSeen, true)
val seen: Boolean = store.get(OnboardingSeen) ?: false
store.contains(SessionKeys.ACCESS_TOKEN)
store.remove(LastSyncedAt)
store.clear()                                           // this store only

store.observe(OnboardingSeen)                           // Flow<Boolean?>: current value, then changes, no repeats
store.observeAll()                                      // Flow<Map<String, String>>: every stored name and string
```

Everything suspends; call it from a coroutine (a ViewModel's scope, a use case). For state a screen shows,
observe rather than reading once.

## SecureKeyValueStore

The same calls. Android encrypts each value with AES/GCM under a key the Android Keystore holds, in a file
of its own; iOS keeps each value as a Keychain item, `AfterFirstUnlockThisDeviceOnly` (readable in the
background after the first unlock, never moved to another device by a backup).

A value that can no longer be decrypted — its key went with an uninstall, or the data was restored to
another device — is removed and read as absent. Treat an absent token as signed out.

Observation is driven by the store itself (neither platform reports changes): keep **one instance per app**
— which the wiring does — or a write through another instance goes unseen until the next write here.

## observeAll() and a debug console

`observeAll()` is what a debug console or diagnostics screen needs; on the secure store it **decrypts every
value**. The kit shows nothing itself — wire it into the app's console, and only in non-production builds:

```kotlin
combine(store.observeAll(), secure.observeAll()) { plain, secret ->
    plain.mapKeys { "prefs.${it.key}" } + secret.mapKeys { "secure.${it.key}" }
}
```

## Testing

In tests, inject the fakes — the same contract, in memory:

```kotlin
val store = FakeKeyValueStore()
val secure = FakeSecureKeyValueStore(initial = mapOf("ACCESS_TOKEN" to "t"))
store.stored.value                                      // what was written, as stored strings
```

An app that writes its own store proves it against the kit's suite:

```kotlin
class MyStoreTest : KeyValueStoreContract() {
    override fun createStore(): KeyValueStore {
        return MyStore()
    }
}
```
