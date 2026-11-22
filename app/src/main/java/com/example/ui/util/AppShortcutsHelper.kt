package com.example.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.toAppDigits
import com.example.ui.navigation.AppLaunchDestination

object AppShortcutsHelper {
    private const val DYNAMIC_LAST_PAGE_ID = "shortcut_continue_reading"

    fun syncDynamicShortcuts(
        context: Context,
        lastReadPage: Int,
        surahNameAr: String?,
        useWesternDigits: Boolean
    ) {
        runCatching {
            val cleanPage = lastReadPage.coerceIn(1, 604)
            val pageText = cleanPage.toAppDigits(useWesternDigits)
            val shortLabel = if (!surahNameAr.isNullOrBlank()) {
                context.getString(R.string.shortcut_continue_surah_short, surahNameAr)
            } else {
                context.getString(R.string.shortcut_continue_page_short, pageText)
            }
            val longLabel = if (!surahNameAr.isNullOrBlank()) {
                context.getString(R.string.shortcut_continue_surah_long, surahNameAr, pageText)
            } else {
                context.getString(R.string.shortcut_continue_page_long, pageText)
            }
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = AppLaunchDestination.ACTION_QUICK_LAST_PAGE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val continueShortcut = ShortcutInfoCompat.Builder(context, DYNAMIC_LAST_PAGE_ID)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_read))
                .setIntent(launchIntent)
                .setRank(0)
                .build()

            // 3 static shortcuts in res/xml/shortcuts.xml + 1 dynamic shortcut = 4 (within launcher max)
            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(continueShortcut))
        }
    }
}
