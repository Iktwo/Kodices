package com.iktwo.kodex.viewer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewerArgsTest {
    private fun parse(
        vararg args: String,
        env: Map<String, String> = emptyMap(),
    ) = parseViewerArgs(args.toList(), env::get)

    private fun config(
        vararg args: String,
        env: Map<String, String> = emptyMap(),
    ) = assertIs<ViewerArgs.Start>(parse(*args, env = env)).config

    @Test
    fun withNothingItOpensTheFormOnTheDefaultAddress() {
        assertEquals(ViewerConfig(serverUrl = DEFAULT_URL, token = "", autoConnect = false), config())
    }

    @Test
    fun anAddressAndTokenConnectAtOnce() {
        val expected = ViewerConfig("http://127.0.0.1:8089", "s3cret", autoConnect = true)

        assertEquals(expected, config("--url", "http://127.0.0.1:8089", "--token", "s3cret"))
        assertEquals(expected, config("--url=http://127.0.0.1:8089", "--token=s3cret"))
        assertEquals(expected, config(env = mapOf(ENV_URL to "http://127.0.0.1:8089", ENV_TOKEN to " s3cret ")))
    }

    @Test
    fun argumentsWinOverTheEnvironment() {
        val env = mapOf(ENV_URL to "http://env:1", ENV_TOKEN to "from-env")

        assertEquals(ViewerConfig("http://arg:2", "from-env", autoConnect = true), config("--url", "http://arg:2", env = env))
        assertEquals(ViewerConfig("http://env:1", "from-arg", autoConnect = true), config("--token", "from-arg", env = env))
    }

    @Test
    fun aTokenAloneDoesNotConnectAnywhere() {
        assertEquals(ViewerConfig(DEFAULT_URL, "s3cret", autoConnect = false), config("--token", "s3cret"))
        assertEquals(ViewerConfig(DEFAULT_URL, "", autoConnect = false), config(env = mapOf(ENV_URL to "  ")))
    }

    @Test
    fun aTokenWithAnEqualsSignSurvives() {
        assertEquals("abc=def==", config("--token=abc=def==").token)
        assertEquals("abc=def==", config("--token", "abc=def==").token)
    }

    @Test
    fun helpAndMistakesAreReported() {
        assertEquals(ViewerArgs.Help, parse("--help"))
        assertEquals(ViewerArgs.Help, parse("--url", "http://x", "-h"))
        assertEquals("--url needs a value", assertIs<ViewerArgs.Invalid>(parse("--url")).message)
        assertEquals("Unknown argument: --port", assertIs<ViewerArgs.Invalid>(parse("--port", "1")).message)
    }

    @Test
    fun theTokenStaysOutOfWhatGetsPrinted() {
        assertTrue("s3cret" !in config("--token", "s3cret").toString())
        assertTrue(ENV_TOKEN in USAGE && ENV_URL in USAGE)
    }

    @Test
    fun renderSizesAreWidthByHeight() {
        assertEquals(1280 to 900, parseSize("1280x900"))
        assertEquals(400 to 2400, parseSize("400X2400"))
        assertNull(parseSize("1280"))
        assertNull(parseSize("0x900"))
        assertNull(parseSize("wide"))
    }
}
