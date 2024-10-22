package com.saadahmedev.videoplayer.ui

import com.saadahmedev.videoplayer.base.BaseViewModel
import com.saadahmedev.videoplayer.domain.model.StreamItem
import java.util.LinkedList

class MainActivityViewModel : BaseViewModel() {
    val currentQueue: LinkedList<StreamItem> = LinkedList()

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

    fun getCurrentQueueItemExceptSelf(): List<StreamItem> {
        val list = mutableListOf<StreamItem>()

        val currentItem = peekFirstItem()
        currentItem?.let {
            currentQueue.forEach { item ->
                if (item != it) {
                    list.add(item)
                }
            }
        }

        return list
    }
}