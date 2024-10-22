package com.saadahmedev.videoplayer.util.extension

import android.os.Handler
import android.os.Looper

inline fun delayedAction(wait: Long, crossinline action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed({
        action.invoke()
    }, wait)
}

inline fun Boolean.delay(wait: Long, crossinline action: () -> Unit) {
    if (this) {
        delayedAction(wait, action)
    }
}