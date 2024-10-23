package com.saadahmedev.videoplayer.ui.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.exoplayer.ExoPlayer
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentPlayerBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.service.VideoPlayerService
import com.saadahmedev.videoplayer.ui.home.StreamItemAdapter
import com.saadahmedev.videoplayer.util.extension.gone
import com.saadahmedev.videoplayer.util.extension.visible

class PlayerFragment :
    BaseFragment<PlayerViewModel, FragmentPlayerBinding>(FragmentPlayerBinding::inflate),
    Player.Listener {

    override val toolbarTitle: String get() = getString(R.string.media_player)
    override val viewmodel: PlayerViewModel by viewModels()

    private var videoPlayerService: VideoPlayerService? = null
    private var isBound = false
    private lateinit var player: ExoPlayer

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as VideoPlayerService.VideoPlayerBinder
            videoPlayerService = binder.getService()
            videoPlayerService?.showNotification()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            videoPlayerService = null
        }
    }

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

        val intent = Intent(requireContext(), VideoPlayerService::class.java)
        requireContext().startService(intent)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        isBound = true

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

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        showPopupDialog(
            title = "Playback Error!",
            message = if (error.cause?.javaClass == HttpDataSourceException::class.java) "Unable to load video from remote, please check your internet connection" else error.message ?: "Unable to play current video, please try another",
            icon = if (error.cause?.javaClass == HttpDataSourceException::class.java) R.drawable.ic_no_internet else R.drawable.ic_error,
            showNegativeButton = false,
            cancelable = true,
            positiveButtonText = "Dismiss"
        )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                handler.removeCallbacks(updatePositionRunnable)
                handler.post(updatePositionRunnable)

                sharedViewModel.previousPlayingItem = sharedViewModel.currentlyPlayingItem
                sharedViewModel.currentlyPlayingItem = sharedViewModel.getCurrentQueueItems().find { player.currentMediaItem?.localConfiguration?.uri == Uri.parse(it.link) }

                sharedViewModel.previousPlayingItem?.isPlaying?.set(false)
                sharedViewModel.currentlyPlayingItem?.isPlaying?.set(true)
            }

            Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_ENDED -> {
                handler.removeCallbacks(updatePositionRunnable)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 1000

    private val updatePositionRunnable = object : Runnable {
        override fun run() {
            val currentPosition = if (player.currentPosition < player.duration) player.currentPosition else player.duration
            saveCurrentPosition(if (currentPosition == player.duration) 0 else currentPosition)

            val progress = ((currentPosition * 100).toFloat() / player.duration.toFloat()).toInt()
            videoPlayerService?.updateNotification(progress)

            if (currentPosition == player.duration) {
                handler.removeCallbacks(this)
                return
            }
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun saveCurrentPosition(position: Long) {
        //
    }

    override fun onNetworkAvailable() {
        super.onNetworkAvailable()
        if (::player.isInitialized) {
            player.prepare()
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isBound) {
            requireContext().unbindService(connection)
            isBound = false
        }
        videoPlayerService?.stopSelf()

        if (::player.isInitialized) {
            player.release()
            handler.removeCallbacks(updatePositionRunnable)
        }
    }
}