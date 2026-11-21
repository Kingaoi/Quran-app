package com.example.data.audio

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.QuranApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class QuranPlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSession: MediaSession? = null
    private var observeJob: Job? = null
    private var isForegroundStarted = false

    private val audioPlayer: QuranAudioPlayer
        get() = (applicationContext as QuranApplication).audioPlayer

    override fun onCreate() {
        super.onCreate()
        QuranMediaNotificationHelper.ensureChannel(this)
        mediaSession = MediaSession(this, "QuranWsPlaybackSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() { audioPlayer.togglePlayPause() }
                override fun onPause() { audioPlayer.togglePlayPause() }
                override fun onSkipToNext() { audioPlayer.nextAyah() }
                override fun onSkipToPrevious() { audioPlayer.previousAyah() }
                override fun onSeekTo(pos: Long) { audioPlayer.seekTo(pos) }
                override fun onFastForward() { audioPlayer.seekRelative(10_000L) }
                override fun onRewind() { audioPlayer.seekRelative(-10_000L) }
                override fun onStop() { audioPlayer.stop() }
                override fun onCustomAction(action: String, extras: Bundle?) {
                    handleCustomAction(action)
                }
            })
            isActive = true
        }
        startObservingPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let(::handleCustomAction)
        refreshNotification(audioPlayer.state.value, audioPlayer.notificationConfig.value)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun handleCustomAction(action: String) {
        when (action) {
            QuranMediaNotificationHelper.ACTION_PLAY_PAUSE -> audioPlayer.togglePlayPause()
            QuranMediaNotificationHelper.ACTION_PREV -> audioPlayer.previousAyah()
            QuranMediaNotificationHelper.ACTION_NEXT -> audioPlayer.nextAyah()
            QuranMediaNotificationHelper.ACTION_TOGGLE_REPEAT -> audioPlayer.toggleRepeatAyah()
            QuranMediaNotificationHelper.ACTION_CYCLE_SPEED -> audioPlayer.cyclePlaybackSpeed()
            QuranMediaNotificationHelper.ACTION_SEEK_BACK -> audioPlayer.seekRelative(-10_000L)
            QuranMediaNotificationHelper.ACTION_SEEK_FORWARD -> audioPlayer.seekRelative(10_000L)
            QuranMediaNotificationHelper.ACTION_STOP -> audioPlayer.stop()
        }
    }

    private fun startObservingPlayer() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            combine(audioPlayer.state, audioPlayer.notificationConfig) { st, cfg -> st to cfg }
                .collect { (state, config) ->
                    refreshNotification(state, config)
                }
        }
    }

    private fun refreshNotification(state: AudioPlaybackState, config: PlaybackNotificationConfig) {
        val shouldKeepVisible = config.enabled && state.isActive &&
            (state.isPlaying || state.isBuffering || config.showWhenPaused)

        if (!shouldKeepVisible) {
            mediaSession?.isActive = false
            isForegroundStarted = false
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            NotificationManagerCompat.from(this).cancel(QuranMediaNotificationHelper.NOTIFICATION_ID)
            stopSelf()
            return
        }

        val session = mediaSession ?: return
        session.isActive = true
        QuranMediaNotificationHelper.updateMediaSession(session, state, config)
        val notification = QuranMediaNotificationHelper.buildNotification(this, session, state, config)

        if (!isForegroundStarted) {
            val started = runCatching {
                val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                } else {
                    0
                }
                ServiceCompat.startForeground(this, QuranMediaNotificationHelper.NOTIFICATION_ID, notification, fgsType)
            }.isSuccess
            isForegroundStarted = started
            if (!started) {
                notifySafely(notification)
            }
        } else {
            notifySafely(notification)
        }
    }

    private fun notifySafely(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        try {
            NotificationManagerCompat.from(this).notify(QuranMediaNotificationHelper.NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val st = audioPlayer.state.value
        if (!st.isPlaying) {
            audioPlayer.stop()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        observeJob?.cancel()
        serviceScope.cancel()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_SYNC = "com.example.audio.ACTION_SYNC"

        fun sync(context: Context, state: AudioPlaybackState, config: PlaybackNotificationConfig) {
            val shouldRun = config.enabled && state.isActive &&
                (state.isPlaying || state.isBuffering || config.showWhenPaused)
            val appCtx = context.applicationContext
            if (!shouldRun) {
                runCatching {
                    appCtx.stopService(Intent(appCtx, QuranPlaybackService::class.java))
                    NotificationManagerCompat.from(appCtx).cancel(QuranMediaNotificationHelper.NOTIFICATION_ID)
                }
                return
            }
            val intent = Intent(appCtx, QuranPlaybackService::class.java).apply { action = ACTION_SYNC }
            runCatching {
                if (state.isPlaying || state.isBuffering) {
                    ContextCompat.startForegroundService(appCtx, intent)
                } else {
                    appCtx.startService(intent)
                }
            }
        }
    }
}
