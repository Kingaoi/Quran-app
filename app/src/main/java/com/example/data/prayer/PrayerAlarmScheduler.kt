package com.example.data.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerSettings
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimesCalculator
import java.util.Calendar

class PrayerAlarmScheduler(private val context: Context) {

    fun syncAlarms(settings: PrayerSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        PrayerNotificationReceiver.ensureChannels(context)

        val now = System.currentTimeMillis()
        val cityTz = settings.city.resolveTimeZone()
        val today = Calendar.getInstance(cityTz)
        val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

        val todayTimes = PrayerTimesCalculator.calculateForDate(
            calendar = today,
            latitude = settings.city.latitude,
            longitude = settings.city.longitude,
            method = settings.calculationMethod,
            asrMethod = settings.asrMethod
        )
        val tomorrowTimes = PrayerTimesCalculator.calculateForDate(
            calendar = tomorrow,
            latitude = settings.city.latitude,
            longitude = settings.city.longitude,
            method = settings.calculationMethod,
            asrMethod = settings.asrMethod
        )

        val preOffsetMillis = settings.preAdhanMinutes * 60_000L

        for (prayer in PrayerName.entries) {
            if (!prayer.isMandatoryPrayer) continue
            val pending = buildPendingIntent(
                prayer = prayer,
                cityName = settings.city.nameAr,
                isPreAdhan = false,
                preMinutes = 0,
                soundMode = settings.adhanSoundMode,
                adhanVoiceId = settings.selectedAdhanVoiceId,
                cityTimeZoneId = cityTz.id
            )
            val prePending = buildPendingIntent(
                prayer = prayer,
                cityName = settings.city.nameAr,
                isPreAdhan = true,
                preMinutes = settings.preAdhanMinutes,
                soundMode = settings.adhanSoundMode,
                adhanVoiceId = settings.selectedAdhanVoiceId,
                cityTimeZoneId = cityTz.id
            )

            if (!settings.isAlertEnabled(prayer)) {
                alarmManager.cancel(pending)
                alarmManager.cancel(prePending)
                continue
            }

            val todayEntry = todayTimes.entries.firstOrNull { it.prayer == prayer }
            val tomorrowEntry = tomorrowTimes.entries.firstOrNull { it.prayer == prayer }
            val triggerAt = nextTriggerMillis(todayEntry?.timestampMillis, tomorrowEntry?.timestampMillis, now, 0L)
            if (triggerAt != null) {
                ExactAlarmCompat.scheduleExactOrAllowIdle(context, alarmManager, triggerAt, pending)
            }

            if (preOffsetMillis > 0L) {
                val preTrigger = nextTriggerMillis(
                    todayEntry?.timestampMillis,
                    tomorrowEntry?.timestampMillis,
                    now,
                    preOffsetMillis
                )
                if (preTrigger != null) {
                    ExactAlarmCompat.scheduleExactOrAllowIdle(context, alarmManager, preTrigger, prePending)
                }
            } else {
                alarmManager.cancel(prePending)
            }
        }
    }

    private fun buildPendingIntent(
        prayer: PrayerName,
        cityName: String,
        isPreAdhan: Boolean,
        preMinutes: Int,
        soundMode: AdhanSoundMode,
        adhanVoiceId: String,
        cityTimeZoneId: String
    ): PendingIntent {
        val codeBase = if (isPreAdhan) 4100 else 4000
        val actionPrefix = if (isPreAdhan) "com.example.PRE_PRAYER_ALERT_" else "com.example.PRAYER_ALERT_"
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = "$actionPrefix${prayer.id}"
            putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_ID, prayer.id)
            putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_TITLE, prayer.titleAr)
            putExtra(PrayerNotificationReceiver.EXTRA_CITY_NAME, cityName)
            putExtra(PrayerNotificationReceiver.EXTRA_IS_PRE_ADHAN, isPreAdhan)
            putExtra(PrayerNotificationReceiver.EXTRA_PRE_MINUTES, preMinutes)
            putExtra(PrayerNotificationReceiver.EXTRA_SOUND_MODE, soundMode.name)
            putExtra(PrayerNotificationReceiver.EXTRA_ADHAN_VOICE_ID, adhanVoiceId)
            putExtra(PrayerNotificationReceiver.EXTRA_CITY_TIMEZONE, cityTimeZoneId)
        }
        return PendingIntent.getBroadcast(
            context,
            prayer.ordinal + codeBase,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        fun nextTriggerMillis(
            todayPrayerMillis: Long?,
            tomorrowPrayerMillis: Long?,
            nowMillis: Long,
            preOffsetMillis: Long
        ): Long? {
            val todayCandidate = todayPrayerMillis?.minus(preOffsetMillis)
            if (todayCandidate != null && todayCandidate > nowMillis + 5_000L) {
                return todayCandidate
            }
            val tomorrowCandidate = tomorrowPrayerMillis?.minus(preOffsetMillis)
            return tomorrowCandidate?.takeIf { it > nowMillis + 5_000L }
        }
    }
}
