package com.saadahmedev.videoplayer.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.domain.model.VideoType
import com.saadahmedev.videoplayer.util.SingleLiveEvent

abstract class BaseViewModel : ViewModel() {
    private val _onSwipeRefresh = MutableLiveData<SingleLiveEvent<Boolean>>()
    val onSwipeRefresh: LiveData<SingleLiveEvent<Boolean>> get() = _onSwipeRefresh

    fun onSwipeRefreshed() {
        _onSwipeRefresh.value = SingleLiveEvent(true)
    }

    private fun streamableVideos(): List<StreamItem> {
        return listOf(
            StreamItem(
                id = 1,
                name = "Big Buck Bunny",
                link = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                type = VideoType.HLS
            ),
            StreamItem(
                id = 2,
                name = "Tears of Steel",
                link = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                type = VideoType.HLS
            ),
            StreamItem(
                id = 3,
                name = "Apple Advanced Stream",
                link = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
                type = VideoType.HLS
            ),
            StreamItem(
                id = 4,
                name = "Envivio",
                link = "https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd",
                type = VideoType.DASH
            ),
            StreamItem(
                id = 5,
                name = "Big Buck Bunny",
                link = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd",
                type = VideoType.DASH
            ),
            StreamItem(
                id = 6,
                name = "Art of Motion",
                link = "https://cdn.bitmovin.com/content/assets/art-of-motion-dash-hls-progressive/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
                type = VideoType.DASH
            )
        )
    }

    fun getHLSVideos(): List<StreamItem> {
        return streamableVideos().filter { it.type == VideoType.HLS }
    }

    fun getDASHVideos(): List<StreamItem> {
        return streamableVideos().filter { it.type == VideoType.DASH }
    }

    fun getAvailableItemsExceptQueueItems(queue: List<StreamItem>): List<StreamItem> {
        val list = mutableListOf<StreamItem>()

        streamableVideos().forEach {
            queue.forEach { item ->
                if (it.id != item.id) list.add(it)
            }
        }

        return list
    }
}