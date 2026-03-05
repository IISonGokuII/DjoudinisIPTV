package com.djoudini.iptv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.djoudini.iptv.domain.model.BufferSize
import com.djoudini.iptv.domain.model.VideoDecoder
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class PlayerManager @Inject constructor(
    private val context: Context
) {
    fun createPlayer(
        bufferSize: BufferSize,
        customBufferMs: Int,
        decoder: VideoDecoder,
        userAgent: String = "IPTVSmartersPro"
    ): ExoPlayer {
        
        // 1. Buffer & Load Control
        val minBuffer = if (bufferSize == BufferSize.CUSTOM) customBufferMs else bufferSize.minBufferMs
        val maxBuffer = if (bufferSize == BufferSize.CUSTOM) customBufferMs * 2 else bufferSize.maxBufferMs

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                minBuffer,
                maxBuffer,
                if (minBuffer < 1000) minBuffer else 1000,
                minBuffer
            )
            .build()

        // 2. Renderers Factory (Hardware vs Software)
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(
                if (decoder == VideoDecoder.HARDWARE) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
            )
            // Enable deinterlacing if needed (simplified here)
        }

        // 3. MediaSource Factory with Spoofed User-Agent
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)

        return ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setLoadControl(loadControl)
            .build()
    }
}
