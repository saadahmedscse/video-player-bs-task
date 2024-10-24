package com.saadahmedev.videoplayer.util.extension

import android.content.Context
import com.saadahmedev.videoplayer.util.DataStoreManager
import com.saadahmedev.videoplayer.util.extension.DataStoreExtension.PREFERENCES_KEY_VIDEO_NAME_LIST

object DataStoreExtension {
    const val PREFERENCES_KEY_VIDEO_NAME_LIST = "PREFERENCES_KEY_VIDEO_NAME_LIST"
    const val PREFERENCES_KEY_VIDEO_POSITION = "PREFERENCES_KEY_VIDEO_POSITION"
}

suspend fun Context.saveLocalVideoNames(list: List<String>) {
    val dataStore = DataStoreManager(this)
    dataStore.saveStringList(PREFERENCES_KEY_VIDEO_NAME_LIST, list)
}

fun Context.getLocalVideoNames(): List<String> {
    val dataStore = DataStoreManager(this)
    return dataStore.getStringList(PREFERENCES_KEY_VIDEO_NAME_LIST)
}

suspend fun Context.saveCurrentVideoPosition(uri: String?, position: Long) {
    val dataStore = DataStoreManager(this)
    dataStore.saveLong(uri, position)
}

fun Context.getCurrentVideoPosition(uri: String): Long {
    val dataStore = DataStoreManager(this)
    return dataStore.getLong(uri)
}