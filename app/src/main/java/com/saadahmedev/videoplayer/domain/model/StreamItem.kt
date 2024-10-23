package com.saadahmedev.videoplayer.domain.model

import androidx.databinding.ObservableField

data class StreamItem(
    val id: Long,
    val name: String,
    val link: String,
    val type: VideoType,
    val isPlaying: ObservableField<Boolean> = ObservableField(false)
)
