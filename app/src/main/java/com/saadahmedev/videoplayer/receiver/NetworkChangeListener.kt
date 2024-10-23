package com.saadahmedev.videoplayer.receiver

interface NetworkChangeListener {
    fun onNetworkAvailable()
    fun onNetworkLost()
}