package com.saadahmedev.videoplayer.ui.player

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore.Audio.Media
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.exoplayer.ExoPlayer
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.base.BaseFragment
import com.saadahmedev.videoplayer.databinding.FragmentPlayerBinding
import com.saadahmedev.videoplayer.domain.model.ListType
import com.saadahmedev.videoplayer.domain.model.PlayerMode
import com.saadahmedev.videoplayer.domain.model.StreamItem
import com.saadahmedev.videoplayer.domain.model.VideoType
import com.saadahmedev.videoplayer.service.VideoPlayerService
import com.saadahmedev.videoplayer.ui.home.StreamItemAdapter
import com.saadahmedev.videoplayer.util.DownloadDialog
import com.saadahmedev.videoplayer.util.extension.gone
import com.saadahmedev.videoplayer.util.extension.visible
import java.io.File


class PlayerFragment :
    BaseFragment<PlayerViewModel, FragmentPlayerBinding>(FragmentPlayerBinding::inflate),
    Player.Listener {

    override val toolbarTitle: String get() = getString(R.string.media_player)
    override val viewmodel: PlayerViewModel by viewModels()

    private var videoPlayerService: VideoPlayerService? = null
    private var downloadDialog: DownloadDialog? = null
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
                exoPlayerView.player = it
                exoPlayerView.clipToOutline = true

                recyclerViewCurrent.adapter = currentQueueAdapter
                recyclerViewAvailable.adapter = availableQueueAdapter
            }

            sharedViewModel.peekFirstItem()?.let { streamItem -> mediaItem(streamItem) }?.also { mediaItem ->
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
        availableQueueAdapter.addItems(
            if (sharedViewModel.playerMode == PlayerMode.ONLINE) viewmodel.getAvailableItemsExceptQueueItems(sharedViewModel.getCurrentQueueItems())
            else viewmodel.getOfflineAvailableItemsExceptQueueItems(sharedViewModel.getCurrentQueueItems())
        )
    }

    override fun initView() {
        if (sharedViewModel.playerMode == PlayerMode.ONLINE) {
            binding.btnDownload.visible()
        } else {
            binding.btnDownload.gone()
        }
    }

    override fun observeData() {
        //
    }

    override fun clickListeners() {
        binding.apply {
            btnDownload.setOnClickListener {
                checkAndRequestPermissions()
            }
        }
    }

    private fun mediaItem(streamItem: StreamItem): MediaItem? {
        return if (streamItem.link == null && streamItem.filePath != null) MediaItem.fromUri(
            streamItem.filePath
        ) else if (streamItem.link != null) MediaItem.fromUri(streamItem.link) else null
    }

    private fun uri(streamItem: StreamItem): Uri? {
        return if (streamItem.link == null && streamItem.filePath != null) Uri.parse(streamItem.filePath) else if (streamItem.link != null) Uri.parse(
            streamItem.link
        ) else null
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
                if (uri(item) == player.currentMediaItem?.localConfiguration?.uri) {
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

        mediaItem(item)?.let {
            player.addMediaItem(it)
        }

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
            if (mediaItem.localConfiguration?.uri == uri(item)) {
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
                sharedViewModel.currentlyPlayingItem = sharedViewModel.getCurrentQueueItems().find { player.currentMediaItem?.localConfiguration?.uri == uri(it) }

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

    private val storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startDownloading()
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) startDownloading()
            else requestManageExternalStoragePermission()
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startDownloading()
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 999)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 999 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownloading()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageExternalStoragePermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            storagePermissionLauncher.launch(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            storagePermissionLauncher.launch(intent)
        }
    }

    private fun startDownloading() {
        val fileName = "${sharedViewModel.currentlyPlayingItem?.name?.split(" ")?.joinToString("-")}.mp4"
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()

        val outputFile = File(outputDir, fileName)
        if (outputFile.exists()) {
            showPopupDialog(
                icon = R.drawable.ic_error,
                title = "Already Exists!",
                message = "The video you are trying to download is already exists in the local storage. Do you want to download it again?",
                negativeButtonText = "No",
                positiveButtonText = "Yes",
                cancelable = false,
                positiveButtonAction = {
                    if (outputFile.delete()) {
                        downloadStreamAsMP4(sharedViewModel.currentlyPlayingItem?.link, outputFile)
                    }
                }
            )
        } else {
            downloadStreamAsMP4(sharedViewModel.currentlyPlayingItem?.link, outputFile)
        }
    }

    private fun downloadStreamAsMP4(url: String?, outputFile: File) {
        if (url.isNullOrBlank()) {
            "Downloadable url cannot be blank".showSnackBar()
            return
        }

        downloadDialog = DownloadDialog.getInstance(requireContext()).also {
            it.show {
                FFmpeg.cancel()
                if (outputFile.exists()) outputFile.delete()
            }
        }

        val command = "-y -i $url -c:v mpeg4 -c:a aac -strict experimental -b:a 192k ${outputFile.absolutePath}"

        Log.d("player_debug", "downloadHLSStreamAsMP4: $command")

        FFmpeg.executeAsync(command) { _, returnCode ->
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    "Download successful".showSnackBar()
                    downloadDialog?.dismiss()
                }
                Config.RETURN_CODE_CANCEL -> {
                    "Download cancelled".showSnackBar()
                    downloadDialog?.dismiss()
                }
                else -> {
                    "Download failed".showSnackBar()
                    downloadDialog?.dismiss()
                }
            }
        }
    }
}