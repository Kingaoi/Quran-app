package com.example.data.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.local.PrayerSettings
import com.example.domain.prayer.HijriSunnahCalculator
import com.example.domain.prayer.HijriSunnahCategory
import com.example.domain.prayer.HijriSunnahNotice

class HijriSunnahScheduler(private val context: Context) {

    fun syncReminders(settings: PrayerSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        HijriSunnahNotificationReceiver.ensureChannel(context)

        cancelAll(alarmManager)
        val config = settings.hijriSunnahConfig
        if (!config.enabled) return

        val notices = HijriSunnahCalculator.computeNextNotices(
            config = config,
            nowMillis = System.currentTimeMillis(),
            hijriOffsetDays = settings.hijriOffsetDays
        )
        notices.forEach { notice ->
            val pending = buildPendingIntent(notice)
            ExactAlarmCompat.scheduleExactOrAllowIdle(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = notice.triggerAtMillis,
                pendingIntent = pending
            )
        }
    }

    private fun cancelAll(alarmManager: AlarmManager) {
        HijriSunnahCategory.entries.forEach { category ->
            val intent = Intent(context, HijriSunnahNotificationReceiver::class.java).apply {
                action = ACTION_HIJRI_SUNNAH_ALERT
            }
            val pending = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + category.ordinal,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }

    private fun buildPendingIntent(notice: HijriSunnahNotice): PendingIntent {
        val intent = Intent(context, HijriSunnahNotificationReceiver::class.java).apply {
            action = ACTION_HIJRI_SUNNAH_ALERT
            putExtra(HijriSunnahNotificationReceiver.EXTRA_CATEGORY_KEY, notice.category.key)
            putExtra(HijriSunnahNotificationReceiver.EXTRA_TITLE, notice.titleAr)
            putExtra(HijriSunnahNotificationReceiver.EXTRA_BODY, notice.bodyAr)
            putExtra(HijriSunnahNotificationReceiver.EXTRA_OPEN_KAHF, notice.openKahfOnTap)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + notice.category.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_HIJRI_SUNNAH_ALERT = "com.example.HIJRI_SUNNAH_ALERT"
        private const val REQUEST_CODE_BASE = 8400
    }
}
