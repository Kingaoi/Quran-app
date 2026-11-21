package com.example.data.prayer

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.local.AdhanSoundMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PrayerAlertTestHelper {

    fun playShortTestTone(context: Context, mode: AdhanSoundMode) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val ringType = if (mode == AdhanSoundMode.DEFAULT) {
                    RingtoneManager.TYPE_ALARM
                } else {
                    RingtoneManager.TYPE_NOTIFICATION
                }
                val uri = RingtoneManager.getDefaultUri(ringType)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = uri?.let { RingtoneManager.getRingtone(context, it) }
                ringtone?.play()
                delay(3500L)
                ringtone?.stop()
            }
        }
    }

    fun triggerTestVibration(context: Context) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 300, 150, 300), -1)
            }
        }
    }
}
