package io.thernal.storagekit.storage.api.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class KeyTest {
    private enum class SessionKeys : StringKey { ACCESS_TOKEN, REFRESH_TOKEN }

    private enum class Flags : BooleanKey { ONBOARDING_SEEN }

    @Test
    fun `an enum constant is a key named after itself`() {
        val key: Key<String> = SessionKeys.ACCESS_TOKEN
        assertEquals("ACCESS_TOKEN", key.name)
        assertSame(KeyCodec.String, key.codec)
        assertSame(KeyCodec.Boolean, Flags.ONBOARDING_SEEN.codec)
    }

    @Test
    fun `keys built from values compare by name and codec`() {
        assertEquals(stringKey("a"), stringKey("a"))
        assertFailsWith<IllegalArgumentException> { stringKey(" ") }
    }

    @Test
    fun `codecs round trip and reject foreign strings`() {
        assertEquals(42, KeyCodec.Int.decode(KeyCodec.Int.encode(42)))
        assertEquals(Long.MAX_VALUE, KeyCodec.Long.decode(KeyCodec.Long.encode(Long.MAX_VALUE)))
        assertEquals(0.25, KeyCodec.Double.decode(KeyCodec.Double.encode(0.25)))
        assertEquals(true, KeyCodec.Boolean.decode(KeyCodec.Boolean.encode(true)))
        assertNull(KeyCodec.Int.decode("forty-two"))
        assertNull(KeyCodec.Boolean.decode("yes"))
    }

    @Test
    fun `a custom codec carries an enum value`() {
        val themeCodec = KeyCodec.codec<Theme>(
            encode = { it.name },
            decode = { stored -> Theme.entries.find { it.name == stored } },
        )
        val theme = key(name = "theme", codec = themeCodec)
        assertEquals(Theme.DARK, theme.codec.decode(theme.codec.encode(Theme.DARK)))
        assertNull(theme.codec.decode("SEPIA"))
    }

    private enum class Theme { LIGHT, DARK }
}
