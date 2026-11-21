package com.example.data.prayer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.PrayerPreferencesRepository
import com.example.domain.prayer.HijriSunnahCategory
import com.example.ui.navigation.AppLaunchDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HijriSunnahNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val categoryKey = intent.getStringExtra(EXTRA_CATEGORY_KEY) ?: HijriSunnahCategory.OCCASIONS.key
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "تذكير بسنّة اليوم" }
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty().ifBlank { "اغتنم سنن اليوم والمناسبات المباركة." }
        val openKahf = intent.getBooleanExtra(EXTRA_OPEN_KAHF, false)

        ensureChannel(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = PrayerPreferencesRepository(context).settingsFlow.first()
                HijriSunnahScheduler(context).syncReminders(settings)
            } finally {
                pendingResult.finish()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notifId = NOTIF_ID_BASE + categoryKey.hashCode().and(0xFF)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (openKahf) {
                action = AppLaunchDestination.ACTION_OPEN_SURAH
                putExtra(AppLaunchDestination.EXTRA_TARGET_SURAH, 18)
            } else {
                action = AppLaunchDestination.ACTION_OPEN_HIJRI_CALENDAR
            }
        }
        val tapPending = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_adhan)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapPending)

        if (openKahf) {
            builder.addAction(0, "قراءة سورة الكهف", tapPending)
        }

        runCatching {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        }
    }

    companion object {
        const val CHANNEL_ID = "quran_ws_hijri_sunnah_alerts"
        const val EXTRA_CATEGORY_KEY = "extra_sunnah_category_key"
        const val EXTRA_TITLE = "extra_sunnah_title"
        const val EXTRA_BODY = "extra_sunnah_body"
        const val EXTRA_OPEN_KAHF = "extra_sunnah_open_kahf"
        private const val NOTIF_ID_BASE = 8500

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "سنن ومناسبات التقويم الهجري",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات سورة الكهف يوم الجمعة، صيام الاثنين والخميس والأيام البيض، والمناسبات الهجرية"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
