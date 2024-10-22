package com.saadahmedev.videoplayer.util.extension

import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.enable(alpha: Float = 1F) {
    this.isEnabled = true
    this.alpha = alpha
}

fun View.disable(alpha: Float = 0.5F) {
    this.isEnabled = false
    this.alpha = alpha
}