package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.Immutable
import com.example.domain.model.toAppDigits
import com.example.domain.wird.HusunActions
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdPlanSummary
import com.example.ui.util.sharePlainText

@Immutable
data class CustomWirdPlanActions(
    val onAddPlan: (NamedWirdPlanConfig) -> Unit = {},
    val onUpdatePlan: (NamedWirdPlanConfig) -> Unit = {},
    val onCompletePlanWird: (String) -> Unit = {},
    val onSetPlanCompletedCount: (String, Int) -> Unit = { _, _ -> },
    val onDeletePlan: (String) -> Unit = {},
    val onTogglePlanReminder: (String) -> Unit = {},
    val onOpenPlanPage: (page: Int, wirdIndex: Int, planId: String) -> Unit = { _, _, _ -> },
    val onToggleTimelinePortion: (planId: String?, portionIndex: Int) -> Unit = { _, _ -> },
    val onRescheduleOverdue: (planId: String?) -> Unit = {},
    val onDeferToday: (planId: String?) -> Unit = {},
    val onResetTimelineOverrides: (planId: String?) -> Unit = {},
    val onUpdateMainReminder: (Boolean, Int, Int) -> Unit = { _, _, _ -> },
    val husun: HusunActions = HusunActions()
)

internal fun shareWirdPlanProgress(context: Context, plan: WirdPlanSummary) {
    val text = if (plan.isCompleted) {
        "بحمد الله أتممت ختمة القرآن الكريم (${plan.totalWirds.toAppDigits()} ورداً). نسأل الله القبول."
    } else {
        val current = plan.currentWird
        val percent = (plan.progressFraction * 100f).toInt()
        val range = current?.let {
            "من سورة ${it.startSurahNameAr} آية ${it.startAyahNumber.toAppDigits()} إلى سورة ${it.endSurahNameAr} آية ${it.endAyahNumber.toAppDigits()}"
        }.orEmpty()
        "وردي القرآني اليوم (${percent.toAppDigits()}٪ من الختمة): $range"
    }
    sharePlainText(context, "مشاركة ورد القرآن الكريم", text)
}
