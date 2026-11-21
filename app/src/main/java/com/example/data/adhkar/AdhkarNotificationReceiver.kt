package com.example.data.adhkar

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

class AdhkarNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getStringExtra(EXTRA_SLOT) ?: AdhkarReminderScheduler.SLOT_MORNING
        val hour = intent.getIntExtra(EXTRA_HOUR, if (slot == AdhkarReminderScheduler.SLOT_MORNING) 6 else 16)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        AdhkarReminderScheduler(context).scheduleNextSlot(slot, hour, minute)
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val isMorning = slot == AdhkarReminderScheduler.SLOT_MORNING
        val notifId = if (isMorning) 6041 else 6042
        val title = if (isMorning) "أذكار الصباح" else "أذكار المساء"
        val body = if (isMorning) {
            "استفتح يومك بذكر الله وتحصينه بأذكار الصباح المباركة."
        } else {
            "حصّن مساءك بذكر الله، لا تنسَ أذكار المساء."
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    companion object {
        const val CHANNEL_ID = "quran_ws_adhkar_alerts"
        const val EXTRA_SLOT = "extra_adhkar_slot"
        const val EXTRA_HOUR = "extra_adhkar_hour"
        const val EXTRA_MINUTE = "extra_adhkar_minute"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "تنبيهات الأذكار",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تذكير يومي بأذكار الصباح والمساء"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
