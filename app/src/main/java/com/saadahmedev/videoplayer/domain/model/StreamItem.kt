package com.saadahmedev.videoplayer.domain.model

import androidx.databinding.ObservableField

data class StreamItem(
    val id: Long,
    val name: String,
    val link: String? = null,
    val filePath: String? = null,
    val type: VideoType,
    val isPlaying: ObservableField<Boolean> = ObservableField(false)
)
