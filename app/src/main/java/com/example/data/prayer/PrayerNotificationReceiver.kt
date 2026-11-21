package com.example.data.prayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerPreferencesRepository
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.AdhanVoices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: return
        val prayerTitle = intent.getStringExtra(EXTRA_PRAYER_TITLE) ?: "الصلاة"
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
        val isPreAdhan = intent.getBooleanExtra(EXTRA_IS_PRE_ADHAN, false)
        val preMinutes = intent.getIntExtra(EXTRA_PRE_MINUTES, 10)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)
        val voiceId = intent.getStringExtra(EXTRA_ADHAN_VOICE_ID) ?: AdhanVoices.defaultVoice.id
        val soundMode = runCatching {
            AdhanSoundMode.valueOf(intent.getStringExtra(EXTRA_SOUND_MODE) ?: AdhanSoundMode.FULL_ADHAN.name)
        }.getOrDefault(AdhanSoundMode.FULL_ADHAN)

        ensureChannels(context)

        if (!isTest) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = PrayerPreferencesRepository(context).settingsFlow.first()
                    PrayerAlarmScheduler(context).syncAlarms(settings)
                } finally {
                    pendingResult.finish()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        if (!isPreAdhan && soundMode == AdhanSoundMode.FULL_ADHAN) {
            val startedFgs = runCatching {
                val serviceIntent = AdhanPlaybackService.buildStartIntent(
                    context = context,
                    prayerTitle = prayerTitle,
                    cityName = cityName,
                    voiceId = voiceId
                )
                ContextCompat.startForegroundService(context, serviceIntent)
            }.isSuccess
            if (startedFgs) return
        }

        postFallbackNotification(
            context = context,
            prayerId = prayerId,
            prayerTitle = prayerTitle,
            cityName = cityName,
            isPreAdhan = isPreAdhan,
            preMinutes = preMinutes,
            soundMode = soundMode
        )
    }

    companion object {
        const val CHANNEL_ID = "quran_ws_prayer_alerts"
        const val CHANNEL_SOFT = "quran_ws_prayer_alerts_soft"
        const val CHANNEL_SILENT_VIBRATE = "quran_ws_prayer_alerts_vibrate"
        const val CHANNEL_PRE_ADHAN = "quran_ws_pre_adhan_alerts"
        const val CHANNEL_ADHAN_ONGOING = "quran_ws_adhan_playback"

        const val EXTRA_PRAYER_ID = "extra_prayer_id"
        const val EXTRA_PRAYER_TITLE = "extra_prayer_title"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val EXTRA_IS_PRE_ADHAN = "extra_is_pre_adhan"
        const val EXTRA_PRE_MINUTES = "extra_pre_minutes"
        const val EXTRA_SOUND_MODE = "extra_sound_mode"
        const val EXTRA_ADHAN_VOICE_ID = "extra_adhan_voice_id"
        const val EXTRA_CITY_TIMEZONE = "extra_city_timezone"
        const val EXTRA_IS_TEST = "extra_is_test_alert"

        fun postFallbackNotification(
            context: Context,
            prayerId: String,
            prayerTitle: String,
            cityName: String,
            isPreAdhan: Boolean,
            preMinutes: Int,
            soundMode: AdhanSoundMode
        ) {
            val notifId = prayerId.hashCode() + if (isPreAdhan) 991 else 0
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (isPreAdhan) "اقتربت صلاة $prayerTitle" else "موعد صلاة $prayerTitle"
            val bodyText = if (isPreAdhan) {
                "بقي ${preMinutes.toAppDigits()} دقائق على أذان $prayerTitle${if (cityName.isNotBlank()) " في $cityName" else ""}"
            } else if (cityName.isNotBlank()) {
                "حان الآن موعد أذان $prayerTitle في $cityName"
            } else {
                "حان الآن موعد أذان $prayerTitle"
            }

            val channelId = if (isPreAdhan) {
                CHANNEL_PRE_ADHAN
            } else {
                when (soundMode) {
                    AdhanSoundMode.FULL_ADHAN, AdhanSoundMode.DEFAULT -> CHANNEL_ID
                    AdhanSoundMode.SOFT -> CHANNEL_SOFT
                    AdhanSoundMode.SILENT_VIBRATE -> CHANNEL_SILENT_VIBRATE
                }
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notif_adhan)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setPriority(
                    if (soundMode == AdhanSoundMode.SILENT_VIBRATE) NotificationCompat.PRIORITY_DEFAULT
                    else NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (soundMode == AdhanSoundMode.SILENT_VIBRATE) {
                builder.setSilent(true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            runCatching {
                NotificationManagerCompat.from(context).notify(notifId, builder.build())
            }
        }

        fun ensureChannel(context: Context) = ensureChannels(context)

        fun ensureChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val main = NotificationChannel(CHANNEL_ID, "تنبيهات أوقات الصلاة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات مواقيت الصلوات الخمس"
            }
            val soft = NotificationChannel(CHANNEL_SOFT, "تنبيهات الصلاة (هادئ)", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "إشعارات الصلاة بنغمة تنبيه هادئة"
                val ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(ringUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            }
            val vibrate = NotificationChannel(CHANNEL_SILENT_VIBRATE, "تنبيهات الصلاة (صامت + اهتزاز)", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تنبيهات الصلاة بالاهتزاز فقط دون صوت"
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 260, 140, 260)
            }
            val preAdhan = NotificationChannel(CHANNEL_PRE_ADHAN, "تنبيه ما قبل الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تذكير مسبق قبل دخول وقت الصلاة"
            }
            val adhanOngoing = NotificationChannel(CHANNEL_ADHAN_ONGOING, "تشغيل صوت الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعار تشغيل صوت المؤذن الكامل عند دخول الوقت"
                setSound(null, null)
                enableVibration(true)
            }
            manager.createNotificationChannels(listOf(main, soft, vibrate, preAdhan, adhanOngoing))
        }
    }
}
