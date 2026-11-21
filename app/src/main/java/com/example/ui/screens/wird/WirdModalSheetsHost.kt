package com.example.ui.screens.wird

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.Composable
import com.example.data.local.ReadingStats
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdTimelineState
import com.example.ui.components.QuranStyledDialog
import com.example.ui.screens.CustomWirdPlanActions
import com.example.ui.viewmodel.WirdUiState

@Composable
internal fun WirdModalSheetsHost(
    uiState: WirdUiState,
    stats: ReadingStats,
    showNewKhatmahSheet: Boolean,
    showDuaSheet: Boolean,
    showStatsSheet: Boolean,
    showAddCustomPlanSheet: Boolean,
    editingCustomPlan: NamedWirdPlanConfig?,
    settingsTimeline: WirdTimelineState?,
    showHusunDailySheet: Boolean,
    showHusunSetupSheet: Boolean,
    pendingDeletePlan: NamedWirdPlanItem?,
    onStartNewKhatmah: (Int, Int) -> Unit,
    onSetDailyGoalPages: (Int) -> Unit = {},
    customPlanActions: CustomWirdPlanActions,
    onOpenEditMainKhatmah: () -> Unit,
    onOpenEditCustomPlan: (NamedWirdPlanConfig) -> Unit,
    onRequestDeleteCustomPlan: (NamedWirdPlanItem) -> Unit,
    onDismissNewKhatmah: () -> Unit,
    onDismissDua: () -> Unit,
    onDismissStats: () -> Unit,
    onDismissAddCustomPlan: () -> Unit,
    onDismissEditCustomPlan: () -> Unit,
    onDismissPlanSettings: () -> Unit,
    onDismissHusunDaily: () -> Unit,
    onOpenHusunSettingsFromDaily: () -> Unit,
    onDismissHusunSetup: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onCopiedDuaMessage: (String) -> Unit
) {
    val plan = uiState.plan
    if (showNewKhatmahSheet) {
        NewKhatmahSetupSheet(
            ajza = uiState.ajza,
            initialStartJuz = uiState.config.startJuz,
            initialQuartersPerDay = uiState.config.quartersPerDay,
            onConfirmPlan = onStartNewKhatmah,
            onDismiss = onDismissNewKhatmah
        )
    }
    if (showAddCustomPlanSheet) {
        AddCustomWirdPlanSheet(
            surahs = uiState.surahs,
            allAyahs = uiState.allAyahs,
            onConfirmAdd = customPlanActions.onAddPlan,
            onDismiss = onDismissAddCustomPlan
        )
    }
    if (editingCustomPlan != null) {
        AddCustomWirdPlanSheet(
            surahs = uiState.surahs,
            allAyahs = uiState.allAyahs,
            initialConfig = editingCustomPlan,
            onConfirmAdd = customPlanActions.onUpdatePlan,
            onDismiss = onDismissEditCustomPlan
        )
    }
    if (settingsTimeline != null) {
        val customItem = settingsTimeline.planId?.let { id ->
            uiState.customPlans.firstOrNull { it.config.id == id }
        }
        WirdPlanSettingsSheet(
            timeline = settingsTimeline,
            mainConfig = if (customItem == null) uiState.config else null,
            customConfig = customItem?.config,
            onToggleMainReminder = customPlanActions.onUpdateMainReminder,
            onToggleCustomReminder = customPlanActions.onTogglePlanReminder,
            onRescheduleOverdue = { customPlanActions.onRescheduleOverdue(settingsTimeline.planId) },
            onResetScheduleOverrides = { customPlanActions.onResetTimelineOverrides(settingsTimeline.planId) },
            onOpenEditPlan = {
                if (customItem != null) onOpenEditCustomPlan(customItem.config)
                else onOpenEditMainKhatmah()
            },
            onRequestDeleteCustomPlan = {
                customItem?.let { onRequestDeleteCustomPlan(it) }
            },
            onDismiss = onDismissPlanSettings
        )
    }
    pendingDeletePlan?.let { target ->
        QuranStyledDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = "حذف خطة «${target.config.title}»؟",
            subtitle = "سيتم حذف هذه الخطة الإضافية فقط ولن يتأثر وردك الأساسي.",
            icon = Icons.Rounded.DeleteOutline,
            confirmLabel = "حذف الخطة",
            confirmTestTag = "confirm_delete_custom_plan",
            onConfirm = {
                customPlanActions.onDeletePlan(target.config.id)
                onDismissDeleteDialog()
            },
            content = {}
        )
    }
    if (showDuaSheet) {
        KhatmahDuaBottomSheet(onCopiedMessage = onCopiedDuaMessage, onDismiss = onDismissDua)
    }
    if (showStatsSheet) {
        WirdStatsBottomSheet(stats = stats, activeWirdPlan = plan, onSetDailyGoalPages = onSetDailyGoalPages, onDismiss = onDismissStats)
    }
    val activeHusun = uiState.husunPlan
    if (showHusunDailySheet && activeHusun != null) {
        HusunDailySheet(
            husunPlan = activeHusun,
            onToggleFortress = customPlanActions.husun.onToggleFortress,
            onIncrementRepeat = customPlanActions.husun.onIncrementRepeat,
            onOpenReaderPage = customPlanActions.husun.onOpenReaderPage,
            onCompleteDayAndAdvance = customPlanActions.husun.onCompleteDay,
            onOpenSettings = onOpenHusunSettingsFromDaily,
            onDismiss = onDismissHusunDaily
        )
    }
    if (showHusunSetupSheet) {
        HusunSetupSheet(
            initialConfig = activeHusun?.config,
            onConfirmSetup = customPlanActions.husun.onConfigure,
            onResetHusun = customPlanActions.husun.onReset,
            onDismiss = onDismissHusunSetup
        )
    }
}
