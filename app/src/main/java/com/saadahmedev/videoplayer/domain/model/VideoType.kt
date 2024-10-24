package com.saadahmedev.videoplayer.domain.model

enum class VideoType(val value: String) {
    HLS("HLS Streamable Videos"), DASH("DASH Streamable Videos"), LOCAL("Local Videos")
}