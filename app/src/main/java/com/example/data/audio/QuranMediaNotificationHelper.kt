package com.example.data.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import com.example.MainActivity
import com.example.R
import com.example.domain.model.toAppDigits

internal object QuranMediaNotificationHelper {
    const val CHANNEL_ID = "quran_ws_audio_playback"
    const val NOTIFICATION_ID = 7301

    const val ACTION_PLAY_PAUSE = "com.example.audio.ACTION_PLAY_PAUSE"
    const val ACTION_PREV = "com.example.audio.ACTION_PREV"
    const val ACTION_NEXT = "com.example.audio.ACTION_NEXT"
    const val ACTION_TOGGLE_REPEAT = "com.example.audio.ACTION_TOGGLE_REPEAT"
    const val ACTION_CYCLE_SPEED = "com.example.audio.ACTION_CYCLE_SPEED"
    const val ACTION_SEEK_BACK = "com.example.audio.ACTION_SEEK_BACK"
    const val ACTION_SEEK_FORWARD = "com.example.audio.ACTION_SEEK_FORWARD"
    const val ACTION_STOP = "com.example.audio.ACTION_STOP"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "مشغل التلاوة القرآنية",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "تحكم في تلاوة القرآن الكريم من شريط الإشعارات وشاشة القفل"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun updateMediaSession(
        session: MediaSession,
        state: AudioPlaybackState,
        config: PlaybackNotificationConfig
    ) {
        val title = "سورة ${state.surahNameAr} • الآية ${state.currentAyah.toAppDigits()}"
        val subtitle = buildString {
            append(state.reciter.nameAr)
            if (state.repeatAyah) append(" • تكرار")
            if (state.playbackSpeed != 1.0f) append(" • ${state.playbackSpeed}×")
        }
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, subtitle)
            .putString(MediaMetadata.METADATA_KEY_ALBUM, "المصحف الشريف")
            .putLong(MediaMetadata.METADATA_KEY_DURATION, state.durationMs.coerceAtLeast(0L))
            .build()
        session.setMetadata(metadata)

        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_PLAY_PAUSE or
            PlaybackState.ACTION_SEEK_TO or
            PlaybackState.ACTION_STOP or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_REWIND or
            PlaybackState.ACTION_FAST_FORWARD

        val pbStateCode = when {
            state.isBuffering -> PlaybackState.STATE_BUFFERING
            state.isPlaying -> PlaybackState.STATE_PLAYING
            else -> PlaybackState.STATE_PAUSED
        }

        val pbBuilder = PlaybackState.Builder()
            .setActions(actions)
            .setState(pbStateCode, state.positionMs.coerceAtLeast(0L), state.playbackSpeed)

        if (config.hasButton(PlaybackNotificationButton.REPEAT_AYAH)) {
            pbBuilder.addCustomAction(
                PlaybackState.CustomAction.Builder(
                    ACTION_TOGGLE_REPEAT,
                    if (state.repeatAyah) "إيقاف التكرار" else "تكرار الآية",
                    R.drawable.ic_notif_repeat
                ).build()
            )
        }
        if (config.hasButton(PlaybackNotificationButton.CYCLE_SPEED)) {
            pbBuilder.addCustomAction(
                PlaybackState.CustomAction.Builder(
                    ACTION_CYCLE_SPEED,
                    "السرعة ${state.playbackSpeed}×",
                    R.drawable.ic_notif_speed
                ).build()
            )
        }

        session.setPlaybackState(pbBuilder.build())
    }

    @Suppress("DEPRECATION")
    fun buildNotification(
        context: Context,
        session: MediaSession,
        state: AudioPlaybackState,
        config: PlaybackNotificationConfig
    ): Notification {
        ensureChannel(context)
        val openAppIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
                .setPriority(Notification.PRIORITY_LOW)
        }
            .setSmallIcon(R.drawable.ic_notif_quran)
            .setContentTitle("سورة ${state.surahNameAr} • الآية ${state.currentAyah.toAppDigits()}")
            .setContentText(buildSubtitle(state))
            .setContentIntent(openAppIntent)
            .setOngoing(state.isPlaying || state.isBuffering)
            .setOnlyAlertOnce(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)

        val compactIndices = mutableListOf<Int>()
        var actionIndex = 0

        fun addBtn(iconRes: Int, label: String, action: String, reqCode: Int, compact: Boolean) {
            val pending = servicePendingIntent(context, action, reqCode)
            builder.addAction(Notification.Action.Builder(iconRes, label, pending).build())
            if (compact && compactIndices.size < 3) compactIndices.add(actionIndex)
            actionIndex++
        }

        if (config.hasButton(PlaybackNotificationButton.SEEK_10S)) {
            addBtn(R.drawable.ic_notif_replay10, "-١٠ ث", ACTION_SEEK_BACK, 7311, compact = false)
        }
        if (config.hasButton(PlaybackNotificationButton.PREV_NEXT)) {
            addBtn(R.drawable.ic_notif_prev, "السابق", ACTION_PREV, 7312, compact = true)
        }
        val playPauseIcon = if (state.isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
        val playPauseLabel = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل"
        addBtn(playPauseIcon, playPauseLabel, ACTION_PLAY_PAUSE, 7313, compact = true)

        if (config.hasButton(PlaybackNotificationButton.PREV_NEXT)) {
            addBtn(R.drawable.ic_notif_next, "التالي", ACTION_NEXT, 7314, compact = true)
        }
        if (config.hasButton(PlaybackNotificationButton.SEEK_10S)) {
            addBtn(R.drawable.ic_notif_forward10, "+١٠ ث", ACTION_SEEK_FORWARD, 7315, compact = false)
        }
        if (config.hasButton(PlaybackNotificationButton.REPEAT_AYAH)) {
            val label = if (state.repeatAyah) "التكرار مفعّل" else "تكرار الآية"
            addBtn(R.drawable.ic_notif_repeat, label, ACTION_TOGGLE_REPEAT, 7316, compact = false)
        }
        if (config.hasButton(PlaybackNotificationButton.CYCLE_SPEED)) {
            addBtn(R.drawable.ic_notif_speed, "السرعة ${state.playbackSpeed}×", ACTION_CYCLE_SPEED, 7317, compact = false)
        }
        if (config.hasButton(PlaybackNotificationButton.STOP_CLOSE)) {
            addBtn(R.drawable.ic_notif_stop, "إغلاق", ACTION_STOP, 7318, compact = compactIndices.size < 2)
        }

        val mediaStyle = Notification.MediaStyle()
            .setMediaSession(session.sessionToken)
        if (compactIndices.isNotEmpty()) {
            mediaStyle.setShowActionsInCompactView(*compactIndices.toIntArray())
        }
        builder.setStyle(mediaStyle)
        return builder.build()
    }

    private fun buildSubtitle(state: AudioPlaybackState): String = buildString {
        state.playlistSession?.let { pl ->
            append("${pl.playlistTitleAr} (${(pl.currentTrackIndex + 1).toAppDigits()}/${pl.totalTracks.toAppDigits()}) • ")
        }
        append(state.reciter.nameAr)
        if (state.isBuffering) append(" • جاري التحميل...")
        else if (!state.isPlaying) append(" • متوقف مؤقتاً")
        if (state.repeatAyah) append(" • تكرار الآية")
        if (state.playbackSpeed != 1.0f) append(" • ${state.playbackSpeed}×")
    }

    private fun servicePendingIntent(context: Context, action: String, reqCode: Int): PendingIntent {
        val intent = Intent(context, QuranPlaybackService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
