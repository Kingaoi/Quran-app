package com.example.data.prayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.MainActivity
import com.example.R
import com.example.data.audio.AppAudioCoordinator
import com.example.data.audio.AppAudioOwner
import com.example.domain.prayer.AdhanVoices

class AdhanPlaybackService : Service() {

    private var player: ExoPlayer? = null
    private var fallbackRingtone: Ringtone? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stopTimeoutRunnable = Runnable { stopPlaybackAndSelf() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ADHAN) {
            stopPlaybackAndSelf()
            return START_NOT_STICKY
        }

        val prayerTitle = intent?.getStringExtra(PrayerNotificationReceiver.EXTRA_PRAYER_TITLE) ?: "الصلاة"
        val cityName = intent?.getStringExtra(PrayerNotificationReceiver.EXTRA_CITY_NAME) ?: ""
        val voiceId = intent?.getStringExtra(PrayerNotificationReceiver.EXTRA_ADHAN_VOICE_ID)
        val voice = AdhanVoices.byId(voiceId)

        PrayerNotificationReceiver.ensureChannels(this)
        val notification = buildOngoingNotification(prayerTitle, cityName, voice.nameAr)
        val started = runCatching {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
            ServiceCompat.startForeground(this, ADHAN_SERVICE_NOTIFICATION_ID, notification, fgsType)
            true
        }.getOrDefault(false)

        if (!started) {
            stopSelf()
            return START_NOT_STICKY
        }

        val timeoutMs = voice.maxDurationMs?.plus(1_500L)?.coerceAtMost(MAX_PLAYBACK_DURATION_MS)
            ?: MAX_PLAYBACK_DURATION_MS
        acquireWakeLock(timeoutMs)
        mainHandler.removeCallbacks(stopTimeoutRunnable)
        mainHandler.postDelayed(stopTimeoutRunnable, timeoutMs)
        startAdhanAudio(voice)
        return START_NOT_STICKY
    }

    private fun startAdhanAudio(voice: com.example.domain.prayer.AdhanVoice) {
        AppAudioCoordinator.claimFocus(AppAudioOwner.ADHAN_SERVICE)
        releaseAudioOnly()
        runCatching {
            val mediaItem = AdhanAudioStorage(this).buildMediaItem(voice)

            val exo = ExoPlayer.Builder(this)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_ALARM)
                        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                        .build(),
                    true
                )
                .build()

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        stopPlaybackAndSelf()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    playSystemRingtoneFallback()
                }
            })

            player = exo
            exo.setMediaItem(mediaItem)
            exo.prepare()
            exo.play()
        }.onFailure {
            playSystemRingtoneFallback()
        }
    }

    private fun playSystemRingtoneFallback() {
        player?.release()
        player = null
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (uri == null) {
            stopPlaybackAndSelf()
            return
        }
        runCatching {
            val ringtone = RingtoneManager.getRingtone(this, uri)
            fallbackRingtone = ringtone
            ringtone?.play()
            mainHandler.removeCallbacks(stopTimeoutRunnable)
            mainHandler.postDelayed(stopTimeoutRunnable, 12_000L)
        }.onFailure {
            stopPlaybackAndSelf()
        }
    }

    private fun buildOngoingNotification(prayerTitle: String, cityName: String, voiceName: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            ADHAN_SERVICE_NOTIFICATION_ID,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            ADHAN_SERVICE_NOTIFICATION_ID + 1,
            Intent(this, AdhanPlaybackService::class.java).apply { action = ACTION_STOP_ADHAN },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val subtitle = buildString {
            append(voiceName)
            if (cityName.isNotBlank()) append(" • $cityName")
        }
        return NotificationCompat.Builder(this, PrayerNotificationReceiver.CHANNEL_ADHAN_ONGOING)
            .setSmallIcon(R.drawable.ic_notif_adhan)
            .setContentTitle("حان الآن موعد أذان $prayerTitle")
            .setContentText(subtitle)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_notif_stop, "إيقاف الأذان", stopIntent)
            .build()
    }

    private fun acquireWakeLock(timeoutMs: Long = MAX_PLAYBACK_DURATION_MS) {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuranWs:AdhanPlaybackWakeLock").apply {
            acquire(timeoutMs)
        }
    }

    private fun releaseAudioOnly() {
        player?.release()
        player = null
        runCatching { fallbackRingtone?.stop() }
        fallbackRingtone = null
    }

    private fun stopPlaybackAndSelf() {
        mainHandler.removeCallbacks(stopTimeoutRunnable)
        releaseAudioOnly()
        runCatching {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        }
        wakeLock = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(stopTimeoutRunnable)
        releaseAudioOnly()
        runCatching {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        }
        wakeLock = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_START_ADHAN = "com.example.ACTION_START_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        private const val ADHAN_SERVICE_NOTIFICATION_ID = 4910
        private const val MAX_PLAYBACK_DURATION_MS = 5 * 60_000L

        fun buildStartIntent(
            context: Context,
            prayerTitle: String,
            cityName: String,
            voiceId: String
        ): Intent = Intent(context, AdhanPlaybackService::class.java).apply {
            action = ACTION_START_ADHAN
            putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_TITLE, prayerTitle)
            putExtra(PrayerNotificationReceiver.EXTRA_CITY_NAME, cityName)
            putExtra(PrayerNotificationReceiver.EXTRA_ADHAN_VOICE_ID, voiceId)
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, AdhanPlaybackService::class.java))
            }
        }
    }
}
