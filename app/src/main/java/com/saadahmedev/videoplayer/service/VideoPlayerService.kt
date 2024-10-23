package com.saadahmedev.videoplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.saadahmedev.videoplayer.R
import com.saadahmedev.videoplayer.util.NotificationOverlay

class VideoPlayerService : Service() {
    companion object {
        const val CHANNEL_ID = "VideoPlayerChannel"
        const val NOTIFICATION_ID = 1
    }

    private var currentProgress = 0

    inner class VideoPlayerBinder : Binder() {
        fun getService(): VideoPlayerService = this@VideoPlayerService
    }

    private val binder: IBinder = VideoPlayerBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val name = "Stream Lab"
        val descriptionText = "Notifications for video playback"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stream Lab")
            .setContentText("Stream is playing...")
            .setSmallIcon(R.drawable.ic_video)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(true)
            .setProgress(100, currentProgress, false)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun updateNotification(progress: Int) {
        val intent = Intent(this, NotificationOverlay::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use FLAG_IMMUTABLE if targeting Android 12 or higher
        )

        currentProgress = progress

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stream Lab")
            .setContentText("Stream is playing...")
            .setSmallIcon(R.drawable.ic_video)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(true)
            .setProgress(100, currentProgress, false)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}