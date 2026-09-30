# Usage

## Keys

```kotlin
val LastSyncedAt = longKey("last_synced_at")                     // value keys: one type each
enum class SessionKeys : StringKey { ACCESS_TOKEN, REFRESH_TOKEN }  // enum keys: constant name = stored name
enum class Flags : BooleanKey { ONBOARDING_SEEN }
val ThemeKey = key("theme", KeyCodec.codec<Theme>(encode = { it.name }, decode = { s -> Theme.entries.find { it.name == s } }))
```

Keys live with the feature that owns the value — in its `data` layer — not in one global list. Two features
must not use the same name in the same store.

## Calls

```kotlin
store.set(Flags.ONBOARDING_SEEN, true)
store.get(Flags.ONBOARDING_SEEN)          // Boolean? — null if absent or undecodable
store.contains(LastSyncedAt)
store.remove(LastSyncedAt)
store.clear()                             // this store only; plain and secure are separate
store.observe(Flags.ONBOARDING_SEEN)      // Flow<Boolean?> — current value, then changes, no repeats
store.observeAll()                        // Flow<Map<String, String>>
```

## Tokens and sign-out

```kotlin
class SessionRepository(private val secure: SecureKeyValueStore) {
    suspend fun save(tokens: Tokens) {
        secure.set(SessionKeys.ACCESS_TOKEN, tokens.access)
        secure.set(SessionKeys.REFRESH_TOKEN, tokens.refresh)
    }
    fun isSignedIn(): Flow<Boolean> {
        return secure.observe(SessionKeys.ACCESS_TOKEN).map { it != null }
    }
    suspend fun signOut() {
        secure.clear()
    }
}
```

A token that cannot be decrypted (reinstall, restore to another device) is removed and reads as null: the
user is signed out, which is the right outcome.

## A debug console

`observeAll()` gives every stored name and string; the secure store decrypts. Show it only in
non-production builds:

```kotlin
combine(store.observeAll(), secure.observeAll()) { plain, secret ->
    plain.mapKeys { "prefs.${it.key}" } + secret.mapKeys { "secure.${it.key}" }
}
```

## Testing

```kotlin
val store = FakeKeyValueStore()                                  // or FakeSecureKeyValueStore()
val repository = SessionRepository(FakeSecureKeyValueStore())
store.stored.value                                               // Map<String, String> written so far
```

A store written in the app proves itself against the kit's suite:
`class MyStoreTest : KeyValueStoreContract() { override fun createStore(): KeyValueStore { return MyStore() } }`.

## Troubleshooting

| Symptom | Cause |
|---|---|
| `There are multiple DataStores active for the same file` | a second store for the same file — bind one |
| a value reads null right after an app update | the key or enum constant was renamed, or its codec changed |
| an observer misses a secure write | the write went through another `SecureKeyValueStore` instance |
| `KeychainException … -34018` on iOS | missing Keychain entitlement in the app target |
| `No binding for Context` (Android graph) | the graph factory does not take `@Provides context: Context` |
