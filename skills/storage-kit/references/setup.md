# Setup

Installed as a kit: `skillctl.sh kit install storage-kit --package <app package> --module <module path>
--alias <plugin alias>` — then include the four modules (`storage/api`, `impl`, `wiring`, `testing`, at
the chosen module path) and provide what `kit.yml`'s `requires` lists: the `<alias>.kmp.library` and
`<alias>.injection` conventions, the catalog entries (kotlinx-coroutines, datastore-preferences-core, okio,
metro-runtime, kotlin-test, kotlin-test-junit, kotlinx-coroutines-test), `TYPESAFE_PROJECT_ACCESSORS`.

Without skill-manager, the kit's `README.md` → Installing → *Without it* does the same by hand (copy, rename, provide).

## Dependencies

```kotlin
commonMain.dependencies {
    implementation(projects.storage.api)             // every module that reads or writes
    implementation(libs.kotlinx.coroutines.core)     // Flow, suspend — not re-exported
}
// the module that builds the graph
commonMain.dependencies {
    implementation(projects.storage.impl)
    implementation(projects.storage.wiring)
}
commonTest.dependencies { implementation(projects.storage.testing) }
```

No `api(...)`: each module declares what it uses.

## Metro

`StorageProvidersModule` (common), `StorageAndroidProvidersModule`, `StorageIosProvidersModule` contribute to `AppScope`:
`KeyValueStore`, `SecureKeyValueStore`, `StorageDirectory`, all singletons. On Android the graph must
provide the application `Context`:

```kotlin
@DependencyGraph(AppScope::class)
interface AppGraph {
    val keyValueStore: KeyValueStore
    @DependencyGraph.Factory
    fun interface Factory { fun create(@Provides context: Context): AppGraph }
}
```

Missing binding for `Context` at compile time → the Android graph has no factory parameter for it.

## By hand

```kotlin
val directory = androidStorageDirectory(context)              // iOS: iosStorageDirectory()
val store: KeyValueStore = preferencesKeyValueStore(directory)
val secure: SecureKeyValueStore = androidSecureKeyValueStore(context, directory)   // iOS: iosSecureKeyValueStore()
```

Create each once per process and keep it.

## Where data lives

| | Android | iOS |
|---|---|---|
| plain | `<files dir>/storage/preferences.preferences_pb` | `Application Support/storage/preferences.preferences_pb` |
| secure | `<files dir>/storage/secure.preferences_pb`, AES/GCM, key alias `<package>.secure-storage` in the Android Keystore | Keychain, service `<bundle id>.secure-storage`, `AfterFirstUnlockThisDeviceOnly` |

Uninstall removes all of it on Android; iOS Keychain items can outlive an uninstall — clear the secure store
on first launch after install if that matters to the app.
