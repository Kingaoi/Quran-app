package com.example.ui.screens.more

import androidx.compose.runtime.Composable
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.TafsirOfflineState
import com.example.domain.prayer.PrayerName
import com.example.ui.components.ComprehensiveDownloadSheet
import com.example.ui.components.ReciterPickerSheet
import com.example.ui.screens.prayer.AdhanVoicePickerSheet
import com.example.ui.screens.prayer.CityPickerBottomSheet
import com.example.ui.screens.wird.NewKhatmahSetupSheet
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState

@Composable
internal fun MoreCategoryContent(
    category: MoreCategory,
    uiState: QuranUiState,
    offlineState: OfflineDownloadState,
    adhkarState: AdhkarUiState,
    prayerState: PrayerUiState,
    wirdState: WirdUiState,
    tafsirOfflineState: TafsirOfflineState,
    offlineHubState: OfflineInventoryUiState,
    actions: MoreSettingsActions,
    onOpenReciterPicker: () -> Unit,
    onOpenComprehensiveHub: () -> Unit,
    onOpenCityPicker: () -> Unit,
    onOpenCalcMethodPicker: () -> Unit,
    onOpenAdhanVoicePicker: () -> Unit,
    onOpenNewKhatmahWizard: () -> Unit,
    onToggleMorningReminder: (Boolean, Int, Int) -> Unit,
    onToggleEveningReminder: (Boolean, Int, Int) -> Unit,
    onToggleWirdReminder: (Boolean, Int, Int) -> Unit,
    onRequestPrayerAlertToggle: (PrayerName) -> Unit,
    onRequestHijriSunnahToggle: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onOpenSources: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    when (category) {
        MoreCategory.APPEARANCE -> MoreAppearanceSection(uiState.settings, actions)
        MoreCategory.MUSHAF_READER -> MoreMushafReaderSection(
            settings = uiState.settings,
            adhkarFontSizeSp = adhkarState.fontSizeSp,
            offlineState = offlineState,
            offlineHubState = offlineHubState,
            actions = actions,
            onOpenComprehensiveHub = onOpenComprehensiveHub
        )
        MoreCategory.AUDIO_TAFSIR -> MoreAudioAndTafsirSection(
            settings = uiState.settings,
            tafsirState = tafsirOfflineState,
            actions = actions,
            onOpenReciterPicker = onOpenReciterPicker
        )
        MoreCategory.ADHKAR_WIRD -> MoreAdhkarAndWirdSection(
            settings = uiState.settings,
            reminderConfig = adhkarState.reminderConfig,
            wirdConfig = wirdState.config,
            ajza = wirdState.ajza,
            actions = actions,
            onToggleMorningReminder = onToggleMorningReminder,
            onToggleEveningReminder = onToggleEveningReminder,
            onToggleWirdReminder = onToggleWirdReminder,
            onOpenNewKhatmahWizard = onOpenNewKhatmahWizard
        )
        MoreCategory.PRAYER -> MorePrayerSection(
            prayerSettings = prayerState.settings,
            cachedAdhanVoiceIds = prayerState.cachedAdhanVoiceIds,
            actions = actions,
            onOpenCityPicker = onOpenCityPicker,
            onOpenCalcMethodPicker = onOpenCalcMethodPicker,
            onOpenAdhanVoicePicker = onOpenAdhanVoicePicker,
            onRequestPrayerAlertToggle = onRequestPrayerAlertToggle,
            onRequestHijriSunnahToggle = onRequestHijriSunnahToggle
        )
        MoreCategory.BACKUP_ABOUT -> MoreBackupAndAboutSection(
            onExportClick = onExportBackup,
            onImportClick = onImportBackup,
            onOpenSources = onOpenSources
        )
        MoreCategory.SOURCES -> MoreSourcesSection(onCopiedSource = onShowToast)
    }
}

@Composable
internal fun MorePickersSheetsHost(
    uiState: QuranUiState,
    prayerState: PrayerUiState,
    wirdState: WirdUiState,
    offlineHubState: OfflineInventoryUiState,
    actions: MoreSettingsActions,
    downloadHubActions: DownloadHubActions?,
    showReciterSheet: Boolean,
    showCityPicker: Boolean,
    showCalcMethodPicker: Boolean,
    showAdhanVoiceSheet: Boolean,
    showNewKhatmahWizard: Boolean,
    showComprehensiveHub: Boolean,
    onRequestGps: () -> Unit,
    onDismissReciter: () -> Unit,
    onDismissCity: () -> Unit,
    onDismissCalcMethod: () -> Unit,
    onDismissAdhanVoice: () -> Unit,
    onDismissNewKhatmah: () -> Unit,
    onDismissComprehensiveHub: () -> Unit
) {
    if (showReciterSheet) {
        ReciterPickerSheet(uiState.settings.selectedReciterId, uiState.settings.riwayah, actions.onSelectReciter, onDismissReciter)
    }
    if (showCityPicker) {
        CityPickerBottomSheet(prayerState.settings.city, prayerState.isLocatingGps, prayerState.settings.use24Hour, onRequestGps, actions.onSelectCity, onDismissCity)
    }
    if (showCalcMethodPicker) {
        CalculationMethodPickerSheet(prayerState.settings.calculationMethod, actions.onSelectCalculationMethod, onDismissCalcMethod)
    }
    if (showAdhanVoiceSheet) {
        AdhanVoicePickerSheet(
            selectedVoiceId = prayerState.settings.selectedAdhanVoiceId,
            cachedVoiceIds = prayerState.cachedAdhanVoiceIds,
            downloadingVoiceId = prayerState.downloadingAdhanVoiceId,
            previewingVoiceId = prayerState.previewingAdhanVoiceId,
            onSelectVoice = actions.onSelectAdhanVoice,
            onTogglePreview = actions.onTogglePreviewAdhanVoice,
            onDownloadVoice = actions.onDownloadAdhanVoice,
            onDeleteVoice = actions.onDeleteAdhanVoice,
            onStopPreview = actions.onStopAdhanPreview,
            onDismiss = onDismissAdhanVoice
        )
    }
    if (showNewKhatmahWizard) {
        NewKhatmahSetupSheet(wirdState.ajza, wirdState.config.startJuz, wirdState.config.quartersPerDay, actions.onStartNewKhatmah, onDismissNewKhatmah)
    }
    if (showComprehensiveHub && downloadHubActions != null) {
        ComprehensiveDownloadSheet(offlineHubState, uiState.settings.riwayah, uiState.settings.selectedReciterId, downloadHubActions, onDismissComprehensiveHub)
    }
}
