package com.saadahmedev.videoplayer.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saadahmedev.videoplayer.util.SingleLiveEvent

abstract class BaseViewModel : ViewModel() {
    private val _onSwipeRefresh = MutableLiveData<SingleLiveEvent<Boolean>>()
    val onSwipeRefresh: LiveData<SingleLiveEvent<Boolean>> get() = _onSwipeRefresh

    fun onSwipeRefreshed() {
        _onSwipeRefresh.value = SingleLiveEvent(true)
    }
}