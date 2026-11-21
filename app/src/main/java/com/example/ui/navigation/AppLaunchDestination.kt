package com.example.ui.navigation

import android.content.Intent

sealed interface AppLaunchDestination {
    data object PrayerTab : AppLaunchDestination
    data object HijriCalendar : AppLaunchDestination
    data object LastReadPage : AppLaunchDestination
    data object AdhkarTab : AppLaunchDestination
    data class OpenSurah(val surahNumber: Int) : AppLaunchDestination
    data class OpenWirdTab(
        val openAllPlans: Boolean = false,
        val customPlanId: String? = null,
        val openReaderForPlan: Boolean = false
    ) : AppLaunchDestination

    companion object {
        const val ACTION_QUICK_LAST_PAGE = "com.example.action.QUICK_LAST_PAGE"
        const val ACTION_QUICK_WIRD = "com.example.action.QUICK_WIRD"
        const val ACTION_QUICK_ADHKAR = "com.example.action.QUICK_ADHKAR"
        const val ACTION_QUICK_PRAYER = "com.example.action.QUICK_PRAYER"
        const val ACTION_OPEN_HIJRI_CALENDAR = "com.example.action.OPEN_HIJRI_CALENDAR"
        const val ACTION_OPEN_SURAH = "com.example.action.OPEN_SURAH"
        const val ACTION_OPEN_CUSTOM_PLAN = "com.example.action.OPEN_CUSTOM_PLAN"

        const val EXTRA_TARGET_SURAH = "extra_launch_target_surah"
        const val EXTRA_CUSTOM_PLAN_ID = "extra_launch_custom_plan_id"
        const val EXTRA_OPEN_PLAN_READER = "extra_launch_open_plan_reader"
        const val EXTRA_OPEN_WIRD_LEGACY = "extra_wird_tab_open"

        fun fromIntent(intent: Intent?): AppLaunchDestination? {
            if (intent == null) return null
            val action = intent.action
            if (action == ACTION_QUICK_LAST_PAGE) return LastReadPage
            if (action == ACTION_QUICK_WIRD) return OpenWirdTab()
            if (action == ACTION_QUICK_ADHKAR) return AdhkarTab
            if (action == ACTION_QUICK_PRAYER) return PrayerTab
            if (action == ACTION_OPEN_HIJRI_CALENDAR) return HijriCalendar
            if (action == ACTION_OPEN_SURAH) {
                val surah = intent.getIntExtra(EXTRA_TARGET_SURAH, 18).coerceIn(1, 114)
                return OpenSurah(surah)
            }
            if (action == ACTION_OPEN_CUSTOM_PLAN) {
                val planId = intent.getStringExtra(EXTRA_CUSTOM_PLAN_ID).orEmpty().ifBlank { null }
                val openReader = intent.getBooleanExtra(EXTRA_OPEN_PLAN_READER, false)
                return OpenWirdTab(
                    openAllPlans = planId != null && !openReader,
                    customPlanId = planId,
                    openReaderForPlan = openReader
                )
            }
            val customPlan = intent.getStringExtra("extra_custom_plan_id").orEmpty().ifBlank { null }
            if (customPlan != null) {
                return OpenWirdTab(
                    openAllPlans = true,
                    customPlanId = customPlan,
                    openReaderForPlan = intent.getBooleanExtra(EXTRA_OPEN_PLAN_READER, false)
                )
            }
            if (intent.getBooleanExtra("extra_open_wird_tab", false) ||
                intent.getBooleanExtra(EXTRA_OPEN_WIRD_LEGACY, false)
            ) {
                return OpenWirdTab()
            }
            return null
        }
    }
}
