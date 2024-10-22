package com.saadahmedev.videoplayer.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomToolbarViewModel : ViewModel() {
    private val _title = MutableLiveData<String?>()
    val title: LiveData<String?>
        get() = _title

    fun setTitle(title: String?) {
        _title.postValue(title)
    }
}