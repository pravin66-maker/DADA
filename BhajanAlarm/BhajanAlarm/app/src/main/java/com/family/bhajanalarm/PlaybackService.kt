package com.family.bhajanalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PlaybackService : Service() {

    companion object {
        const val ACTION_PLAY = "com.family.bhajanalarm.action.PLAY"
        const val ACTION_PAUSE = "com.family.bhajanalarm.action.PAUSE"
        const val ACTION_RESUME = "com.family.bhajanalarm.action.RESUME"
        const val ACTION_STOP = "com.family.bhajanalarm.action.STOP"

        const val EXTRA_URI = "extra_uri"
        const val EXTRA_NAME = "extra_name"

        private const val CHANNEL_ID = "bhajan_playback_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentSongName: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val uri = intent.getStringExtra(EXTRA_URI)
                val name = intent.getStringExtra(EXTRA_NAME) ?: ""
                if (uri != null) startPlayback(uri, name)
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    private fun startPlayback(uriString: String, name: String) {
        stopMediaPlayerOnly()
        currentSongName = name
        createChannelIfNeeded()
        startForeground(NOTIFICATION_ID, buildNotification(playing = true))

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(this@PlaybackService, Uri.parse(uriString))
                setOnCompletionListener {
                    // Song finished naturally -> switch off, as requested.
                    stopPlayback()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    true
                }
                prepare()
                start()
            }
            PlaybackStatus.update(PlaybackState.PLAYING, currentSongName)
        } catch (e: Exception) {
            stopPlayback()
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                PlaybackStatus.update(PlaybackState.PAUSED, currentSongName)
                updateNotification(playing = false)
            }
        }
    }

    private fun resumePlayback() {
        mediaPlayer?.let {
            it.start()
            PlaybackStatus.update(PlaybackState.PLAYING, currentSongName)
            updateNotification(playing = true)
        }
    }

    private fun stopPlayback() {
        stopMediaPlayerOnly()
        PlaybackStatus.update(PlaybackState.STOPPED, currentSongName)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopMediaPlayerOnly() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: Exception) {
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopMediaPlayerOnly()
        super.onDestroy()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun actionIntent(action: String): PendingIntent {
        val intent = Intent(this, PlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(playing: Boolean): android.app.Notification {
        val openAppIntent = Intent(this, NowPlayingActivity::class.java).apply {
            putExtra(EXTRA_NAME, currentSongName)
        }
        val contentPI = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(currentSongName.ifEmpty { getString(R.string.notification_playing_text) })
            .setContentText(getString(R.string.notification_playing_text))
            .setContentIntent(contentPI)
            .setOngoing(playing)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (playing) {
            builder.addAction(android.R.drawable.ic_media_pause, getString(R.string.pause), actionIntent(ACTION_PAUSE))
        } else {
            builder.addAction(android.R.drawable.ic_media_play, getString(R.string.play), actionIntent(ACTION_RESUME))
        }
        builder.addAction(android.R.drawable.ic_media_stop, getString(R.string.stop), actionIntent(ACTION_STOP))

        return builder.build()
    }

    private fun updateNotification(playing: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(playing))
    }
}
