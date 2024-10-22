package com.saadahmedev.videoplayer.domain.model

data class StreamItem(
    val id: Long,
    val name: String,
    val link: String,
    val type: VideoType
)
