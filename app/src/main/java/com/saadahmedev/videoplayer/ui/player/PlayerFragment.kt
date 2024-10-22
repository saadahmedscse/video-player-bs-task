package com.saadahmedev.videoplayer.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
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
            onActionClick = ::onStreamActionClicked,
            listType = ListType.QUEUE
        )
    }

    private val availableQueueAdapter by lazy {
        StreamItemAdapter(
            onActionClick = ::onStreamActionClicked,
            listType = ListType.AVAILABLE
        )
    }

    override fun onFragmentCreate(savedInstanceState: Bundle?) {
        player = ExoPlayer.Builder(requireContext()).build()
        player.addListener(this)
        binding.apply {
            binding.exoPlayerView.player = player
            binding.exoPlayerView.clipToOutline = true

            recyclerViewCurrent.adapter = currentQueueAdapter
            recyclerViewAvailable.adapter = availableQueueAdapter
        }

        sharedViewModel.peekFirstItem()?.link?.let {
            loadMediaPreparePlay(it)
        }

        currentQueueAdapter.addItems(sharedViewModel.getCurrentQueueItemExceptSelf())
        availableQueueAdapter.addItems(viewmodel.getAvailableItemsExceptQueueItems(sharedViewModel.currentQueue))
    }

    override fun observeData() {
        //
    }

    private fun onStreamActionClicked(item: StreamItem, listType: ListType) {
        when (listType) {
            ListType.AVAILABLE -> {
                availableQueueAdapter.removeItem(item)
                currentQueueAdapter.addItem(item)
                sharedViewModel.addToQueue(item)

                MediaItem.fromUri(item.link).also {
                    player.addMediaItem(it)
                }

                binding.apply {
                    if (!tvTitleCurrent.isVisible && currentQueueAdapter.itemCount >= 1) {
                        tvTitleCurrent.visible()
                        recyclerViewCurrent.visible()
                    }
                }
            }

            ListType.QUEUE -> {
                currentQueueAdapter.removeItem(item)
                availableQueueAdapter.addItem(item)
                sharedViewModel.removeFromQueue(item)

                for (i in 0 until player.mediaItemCount) {
                    val mediaItem = player.getMediaItemAt(i)
                    if (mediaItem.localConfiguration?.uri == Uri.parse(item.link)) {
                        player.removeMediaItem(i)
                        break
                    }
                }

                if (currentQueueAdapter.itemCount == 0) {
                    binding.apply {
                        tvTitleCurrent.gone()
                        recyclerViewCurrent.gone()
                    }
                }
            }

            ListType.ALL -> {}
        }
    }

    private fun loadMediaPreparePlay(link: String) {
        MediaItem.fromUri(link).also {
            player.apply {
                setMediaItem(it)
                prepare()
                play()
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            sharedViewModel.removeFirstItem()?.let {
                availableQueueAdapter.addItem(it)
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                handler.post(updatePositionRunnable)
                if (currentQueueAdapter.itemCount > 0) currentQueueAdapter.removeItem(0)

                viewmodel.getAvailableItemsExceptQueueItems(sharedViewModel.currentQueue)
                    .find { player.currentMediaItem?.localConfiguration?.uri == Uri.parse(it.link) }
                    ?.also {
                        availableQueueAdapter.removeItem(it)
                    }

                binding.apply {
                    if (tvTitleCurrent.isVisible && currentQueueAdapter.itemCount <= 0) {
                        tvTitleCurrent.gone()
                        recyclerViewCurrent.gone()
                    }
                }
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