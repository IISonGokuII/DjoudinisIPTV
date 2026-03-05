package com.djoudini.iptv.domain.model

enum class BufferSize(val minBufferMs: Int, val maxBufferMs: Int) {
    SMALL(1000, 5000),
    NORMAL(15000, 30000),
    LARGE(30000, 60000),
    CUSTOM(0, 0) // Will be handled separately if CUSTOM
}

enum class VideoDecoder {
    HARDWARE,
    SOFTWARE
}

enum class AspectRatio {
    FIT,
    FILL,
    RATIO_16_9,
    RATIO_4_3
}

enum class EpgUpdateInterval {
    ON_START,
    EVERY_12H,
    EVERY_24H,
    MANUAL
}

enum class ProviderType {
    XTREAM,
    M3U
}
