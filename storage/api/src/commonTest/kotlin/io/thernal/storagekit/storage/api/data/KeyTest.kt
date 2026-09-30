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
    fun anEnumConstantIsAKeyNamedAfterItself() {
        val key: Key<String> = SessionKeys.ACCESS_TOKEN
        assertEquals("ACCESS_TOKEN", key.name)
        assertSame(KeyCodec.String, key.codec)
        assertSame(KeyCodec.Boolean, Flags.ONBOARDING_SEEN.codec)
    }

    @Test
    fun keysBuiltFromValuesCompareByNameAndCodec() {
        assertEquals(stringKey("a"), stringKey("a"))
        assertFailsWith<IllegalArgumentException> { stringKey(" ") }
    }

    @Test
    fun codecsRoundTripAndRejectForeignStrings() {
        assertEquals(42, KeyCodec.Int.decode(KeyCodec.Int.encode(42)))
        assertEquals(Long.MAX_VALUE, KeyCodec.Long.decode(KeyCodec.Long.encode(Long.MAX_VALUE)))
        assertEquals(0.25, KeyCodec.Double.decode(KeyCodec.Double.encode(0.25)))
        assertEquals(true, KeyCodec.Boolean.decode(KeyCodec.Boolean.encode(true)))
        assertNull(KeyCodec.Int.decode("forty-two"))
        assertNull(KeyCodec.Boolean.decode("yes"))
    }

    @Test
    fun aCustomCodecCarriesAnEnumValue() {
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
