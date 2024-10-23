package com.saadahmedev.videoplayer.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentPlayerBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.ui.home.StreamItemAdapter
import com.saadahmedev.videoplayer.util.extension.gone
import com.saadahmedev.videoplayer.util.extension.visible

class PlayerFragment :
    BaseFragment<PlayerViewModel, FragmentPlayerBinding>(FragmentPlayerBinding::inflate),
    Player.Listener {

    override val toolbarTitle: String get() = getString(R.string.media_player)
    override val viewmodel: PlayerViewModel by viewModels()

    private lateinit var player: ExoPlayer

    private val currentQueueAdapter by lazy {
        StreamItemAdapter(
            onItemClick = ::onStreamItemClicked,
            onActionClick = ::onStreamActionClicked,
            listType = ListType.QUEUE
        )
    }

    private val availableQueueAdapter by lazy {
        StreamItemAdapter(
            onItemClick = ::onStreamItemClicked,
            onActionClick = ::onStreamActionClicked,
            listType = ListType.AVAILABLE
        )
    }

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        ExoPlayer.Builder(requireContext()).build().also {
            player = it
            it.addListener(this)

            binding.apply {
                binding.exoPlayerView.player = it
                binding.exoPlayerView.clipToOutline = true

                recyclerViewCurrent.adapter = currentQueueAdapter
                recyclerViewAvailable.adapter = availableQueueAdapter
            }

            sharedViewModel.peekFirstItem()?.link?.let { link -> MediaItem.fromUri(link) }?.also { mediaItem ->
                player.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
            }
        }

        currentQueueAdapter.addItems(sharedViewModel.getCurrentQueueItems())
        availableQueueAdapter.addItems(viewmodel.getAvailableItemsExceptQueueItems(sharedViewModel.getCurrentQueueItems()))
    }

    override fun observeData() {
        //
    }

    private fun onStreamItemClicked(item: StreamItem, listType: ListType, position: Int) {
        when (listType) {
            ListType.AVAILABLE -> {
                addToQueue(item)
            }

            ListType.QUEUE -> {
                if (position == player.currentMediaItemIndex) return

                player.seekToDefaultPosition(position)
                player.play()
            }

            ListType.ALL -> {}
        }
    }

    private fun onStreamActionClicked(item: StreamItem, listType: ListType) {
        when (listType) {
            ListType.AVAILABLE -> { addToQueue(item) }

            ListType.QUEUE -> {
                if (Uri.parse(item.link) == player.currentMediaItem?.localConfiguration?.uri) {
                    "Cannot remove current video".showSnackBar()
                    return
                }

                removeFromQueue(item)
            }

            ListType.ALL -> {}
        }
    }

    private fun addToQueue(item: StreamItem) {
        sharedViewModel.addToQueue(item)
        availableQueueAdapter.removeItem(item)
        currentQueueAdapter.addItem(item)

        player.addMediaItem(MediaItem.fromUri(item.link))

        binding.apply {
            if (!tvTitleCurrent.isVisible && currentQueueAdapter.itemCount >= 1) {
                tvTitleCurrent.visible()
                recyclerViewCurrent.visible()
            }

            if (tvTitleAvailable.isVisible && availableQueueAdapter.itemCount == 0) {
                tvTitleAvailable.gone()
                recyclerViewAvailable.gone()
            }
        }
    }

    private fun removeFromQueue(item: StreamItem) {
        sharedViewModel.removeFromQueue(item)
        currentQueueAdapter.removeItem(item)
        availableQueueAdapter.addItem(item)

        for (i in 0 until player.mediaItemCount) {
            val mediaItem = player.getMediaItemAt(i)
            if (mediaItem.localConfiguration?.uri == Uri.parse(item.link)) {
                player.removeMediaItem(i)
                break
            }
        }

        binding.apply {
            if (!tvTitleAvailable.isVisible && availableQueueAdapter.itemCount >= 1) {
                tvTitleAvailable.visible()
                recyclerViewAvailable.visible()
            }

            if (tvTitleCurrent.isVisible && currentQueueAdapter.itemCount == 0) {
                tvTitleCurrent.gone()
                recyclerViewCurrent.gone()
            }
        }
    }

//    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
//            sharedViewModel.removeFirstItem()?.let {
//                availableQueueAdapter.addItem(it)
//            }
//        }
//    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                handler.post(updatePositionRunnable)

                sharedViewModel.previousPlayingItem = sharedViewModel.currentlyPlayingItem
                sharedViewModel.currentlyPlayingItem = sharedViewModel.getCurrentQueueItems().find { player.currentMediaItem?.localConfiguration?.uri == Uri.parse(it.link) }

                sharedViewModel.previousPlayingItem?.isPlaying?.set(false)
                sharedViewModel.currentlyPlayingItem?.isPlaying?.set(true)
            }

            else -> {}
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            newPosition.positionMs.also {
                saveCurrentPosition(it)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 1000

    private val updatePositionRunnable = object : Runnable {
        override fun run() {
            saveCurrentPosition(player.currentPosition)
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun saveCurrentPosition(position: Long) {
        //
    }

    override fun onStop() {
        super.onStop()
        if (::player.isInitialized) {
            player.release()
            handler.removeCallbacks(updatePositionRunnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
            handler.removeCallbacks(updatePositionRunnable)
        }
    }
}