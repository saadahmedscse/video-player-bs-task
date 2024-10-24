package com.saadahmedev.videoplayer.ui

import com.saadahmedev.videoplayer.base.BaseViewModel
import com.saadahmedev.videoplayer.domain.model.PlayerMode
import com.saadahmedev.videoplayer.domain.model.StreamItem
import java.util.LinkedList

class MainActivityViewModel : BaseViewModel() {
    private val currentQueue: LinkedList<StreamItem> = LinkedList()
    var playerMode: PlayerMode = PlayerMode.ONLINE

    var previousPlayingItem: StreamItem? = null
    var currentlyPlayingItem: StreamItem? = null

    fun clearQueue() {
        currentQueue.clear()
    }

    fun addToQueue(item: StreamItem) {
        currentQueue.add(item)
    }

    fun removeFromQueue(item: StreamItem) {
        currentQueue.remove(item)
    }

    fun peekFirstItem(): StreamItem? {
        return currentQueue.peekFirst()
    }

    fun removeFirstItem(): StreamItem? {
        return currentQueue.pollFirst()
    }

    fun getCurrentQueueItems(): List<StreamItem> {
        // Passing another list to avoid reflection in the adapter while modifying the main list
        return arrayListOf<StreamItem>().also { it.addAll(currentQueue) }
    }
}