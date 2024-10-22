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

    fun getHLSVideos(): List<StreamItem> {
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
                link = "https://test-streams.mux.dev/tears-of-steel/playlist.m3u8",
                type = VideoType.HLS
            ),
            StreamItem(
                id = 3,
                name = "NYC Live Stream",
                link = "https://video-weaver.jfk02.hls.ttvnw.net/v1/playlist/CpcEYQ9bwrdbHEEYH-wuSg.vwp-b3oUvbzMdAQdBTV3XUbc9n9Ibpkh13GYOT_giYq_lMoOhNrJfHX2RyejWwqEMTO62oKcMWlTkzDXWxuW6dyQUdW6IagkwF8i7BDgeFsbVtN7SpEpOHuJhGUkah1EsA6HE.rM9_NNinZV5S28IVNe5Qo2KnAe7M7PhfbgLeT4lRO7YexdgztMr7A.m3u8",
                type = VideoType.HLS
            ),
            StreamItem(
                id = 4,
                name = "Apple Advanced Stream",
                link = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
                type = VideoType.HLS
            )
        )
    }

    fun getDASHVideos(): List<StreamItem> {
        return listOf(
            StreamItem(
                id = 1,
                name = "Big Buck Bunny",
                link = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd",
                type = VideoType.DASH
            ),
            StreamItem(
                id = 2,
                name = "Tears of Steel",
                link = "https://dash.akamaized.net/akamai/elephants_dream/elephants_dream_4k_30fps.mpd",
                type = VideoType.DASH
            ),
            StreamItem(
                id = 3,
                name = "Envivio",
                link = "https://dash.akamaized.net/envivio/EnvivioDash3/manifest.mpd",
                type = VideoType.DASH
            ),
            StreamItem(
                id = 4,
                name = "Live Stream Test",
                link = "https://test-streams.mux.dev/pts_shift/master.mpd",
                type = VideoType.DASH
            )
        )
    }

    fun getAvailableItemsExceptQueueItems(queue: List<StreamItem>): List<StreamItem> {
        val list = mutableListOf<StreamItem>()

        getHLSVideos().forEach { item ->
            if (!queue.contains(item)) {
                list.add(item)
            }
        }

        getDASHVideos().forEach { item ->
            if (!queue.contains(item)) {
                list.add(item)
            }
        }

        return list
    }
}