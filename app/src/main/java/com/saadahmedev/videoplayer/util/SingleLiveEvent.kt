package com.saadahmedev.videoplayer.util

class SingleLiveEvent<out T>(private val content: T?) {

    private var hasBeenHandled = false

    val unhandledContent: T?
        get() =
            if (hasBeenHandled) null
            else {
                hasBeenHandled = true
                content
            }

    val peekContent: T? get() = content
}