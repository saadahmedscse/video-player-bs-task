package com.saadahmedev.videoplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saadahmedev.videoplayer.util.isNetworkAvailable

class NetworkChangeReceiver(private val listener: NetworkChangeListener) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (it.isNetworkAvailable()) listener.onNetworkAvailable()
            else listener.onNetworkLost()
        }
    }
}