package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.NatureCalmApp
import com.example.R

class SoundscapePlaybackService : Service() {

    private val binder = LocalBinder()
    private var isForeground = false

    inner class LocalBinder : Binder() {
        fun getService(): SoundscapePlaybackService = this@SoundscapePlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                val soundName = intent.getStringExtra(EXTRA_SOUND_NAME) ?: "Nature Calm"
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                startForegroundWithNotification(soundName, isPlaying)
            }
            ACTION_UPDATE -> {
                val soundName = intent.getStringExtra(EXTRA_SOUND_NAME) ?: "Nature Calm"
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                updateNotification(soundName, isPlaying)
            }
            ACTION_STOP_FOREGROUND -> {
                stopForegroundService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(soundName: String, isPlaying: Boolean) {
        val notification = buildNotification(soundName, isPlaying)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isForeground = true
    }

    private fun updateNotification(soundName: String, isPlaying: Boolean) {
        if (!isForeground) {
            startForegroundWithNotification(soundName, isPlaying)
            return
        }
        val notification = buildNotification(soundName, isPlaying)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(soundName: String, isPlaying: Boolean): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent(ACTION_TOGGLE_PLAYBACK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent(ACTION_STOP_SERVICE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                getString(R.string.action_pause),
                playPauseIntent
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                getString(R.string.action_play),
                playPauseIntent
            ).build()
        }

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(R.string.action_stop),
            stopIntent
        ).build()

        return NotificationCompat.Builder(this, NatureCalmApp.CHANNEL_ID)
            .setContentTitle("Nature Calm")
            .setContentText(soundName)
            .setSubText(if (isPlaying) getString(R.string.notification_playing) else getString(R.string.notification_paused))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(soundName)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun stopForegroundService() {
        isForeground = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_FOREGROUND = "com.example.naturecalm.ACTION_START"
        const val ACTION_UPDATE = "com.example.naturecalm.ACTION_UPDATE"
        const val ACTION_STOP_FOREGROUND = "com.example.naturecalm.ACTION_STOP_FOREGROUND"

        const val ACTION_TOGGLE_PLAYBACK = "com.example.naturecalm.ACTION_TOGGLE"
        const val ACTION_STOP_SERVICE = "com.example.naturecalm.ACTION_STOP"

        const val EXTRA_SOUND_NAME = "extra_sound_name"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun start(context: Context, soundName: String, isPlaying: Boolean) {
            val intent = Intent(context, SoundscapePlaybackService::class.java).apply {
                action = ACTION_START_FOREGROUND
                putExtra(EXTRA_SOUND_NAME, soundName)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, soundName: String, isPlaying: Boolean) {
            val intent = Intent(context, SoundscapePlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_SOUND_NAME, soundName)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // ignore
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SoundscapePlaybackService::class.java).apply {
                action = ACTION_STOP_FOREGROUND
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
