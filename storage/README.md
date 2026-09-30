# storage — design

Why the contracts have the shape they have. What they are and how to use them is in
[`api/README.md`](api/README.md).

The kit is ArenaGo's `core/storage` (Android-only) ported to Compose Multiplatform, with the choices where
Act2Act solved the same thing differently decided explicitly (epic decisions D30–D33).

## One reactive contract, no synchronous one

ArenaGo had two call shapes over the same files — a synchronous `KeyValueStore` and a reactive
`SharedPrefs` — because `SharedPreferences` reads synchronously. DataStore and the Keychain do not: a
synchronous read would block whichever thread asked, the main one included. So there is one contract,
`suspend` for reads and writes and `Flow` to observe, and the plain and secure stores share it —
`SecureKeyValueStore` adds nothing but the promise of encryption, so code that takes a `KeyValueStore`
takes either.

## Typed keys that enums already satisfy

A key carries its name and a codec, so a read cannot ask for the wrong type, and the type is written once,
where the key is declared. `Key.name` is deliberately called `name`: an enum constant's own `name`
implements it, so `enum class SessionKeys : StringKey { ACCESS_TOKEN }` is a set of keys with no code of
its own — the shape Act2Act used, without its `KClass` parameters and context-parameter extensions.

Renaming a key renames what is stored — an enum constant too. That is the cost of deriving the stored name
from the constant; a key whose stored name must outlive refactors is a `val` with an explicit name.

## Everything is stored as a string

Codecs write strings and both stores keep strings. That makes the secure store possible at all (the
Keychain and AES take bytes, and a string is the one encoding every type shares), keeps the DataStore
file to one preference type, and lets `observeAll()` show every value. A string that no longer decodes —
an older version wrote something else under the key — reads as absent instead of throwing, the same as a
missing value.

## Plain store: DataStore directly

DataStore Preferences is multiplatform, writes atomically, and publishes changes as a `Flow`. Act2Act
reached it through multiplatform-settings; that layer is where its `MutablePreferences cannot be cast to
Unit` crash and its per-type read fallbacks came from, and it added nothing a string-only store needs.
DataStore allows one instance per file per process, so every store is a singleton in the wiring.

## Secure store: own code over each platform's keystore

No third-party library (Act2Act used KSafe, whose reads can miss during the Keystore's cold start — its own
note says so). The platform part is small and auditable:

- **Android** — ArenaGo's AES/GCM with a key the Android Keystore generates and holds, but the ciphertext
  goes into its own DataStore file instead of `SharedPreferences`. A value that fails to decrypt (the key
  went with an uninstall, or the data was restored to another device) is removed and read as absent, as
  ArenaGo did: a stale token is worth less than a clean sign-in.
- **iOS** — one generic-password Keychain item per value, under `<bundle id>.secure-storage`,
  `AfterFirstUnlockThisDeviceOnly`: readable in the background once the device has been unlocked, never
  migrated to another device by a backup.

Neither platform reports Keychain or keystore changes, so `SecretKeyValueStore` drives observation itself:
each write through it bumps a revision and observers re-read. That is why one instance per app matters —
a write through a second instance is not seen until the first writes.

## `observeAll()` and the debug console

Both apps fed a debug console with every stored value. A kit cannot depend on the console (no kit depends
on another), so the stores expose `observeAll()` — the secure one decrypting — and the application decides
what to show and in which builds. ArenaGo's `AppDetailsSource` contribution is the application's to write.

## `testing`

Fakes are not enough on their own: a fake that disagrees with the real store makes tests pass that
production fails. `KeyValueStoreContract` is one suite that every store — real or fake — runs, so the fakes
are held to the same behaviour, and an application that writes its own store can prove it against the same
tests.
