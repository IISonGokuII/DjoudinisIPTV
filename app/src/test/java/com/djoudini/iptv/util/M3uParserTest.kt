package com.djoudini.iptv.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class M3uParserTest {

    @Test
    fun `parse valid m3u returns correct streams and categories`() = runBlocking {
        val m3uContent = """
            #EXTM3U
            #EXTINF:-1 tvg-id="CNN.us" tvg-name="CNN" tvg-logo="http://logo.com/cnn.png" group-title="News",CNN News
            http://stream.com/cnn.m3u8
            #EXTINF:-1 tvg-id="HBO.us" tvg-name="HBO" group-title="Movies",HBO HD
            http://stream.com/hbo.m3u8
        """.trimIndent()

        val inputStream = ByteArrayInputStream(m3uContent.toByteArray())
        val (categories, streams) = M3uParser.parse(inputStream, 1L)

        assertEquals(2, categories.size)
        assertEquals(2, streams.size)
        
        assertEquals("News", categories[0].name)
        assertEquals("Movies", categories[1].name)
        
        assertEquals("CNN", streams[0].name)
        assertEquals("http://logo.com/cnn.png", streams[0].streamIcon)
        assertEquals("http://stream.com/cnn.m3u8", streams[0].url)
    }

    @Test
    fun `parse empty stream returns empty lists`() = runBlocking {
        val inputStream = ByteArrayInputStream("".toByteArray())
        val (categories, streams) = M3uParser.parse(inputStream, 1L)

        assertTrue(categories.isEmpty())
        assertTrue(streams.isEmpty())
    }
}
