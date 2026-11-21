package com.example.ui.screens.more

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.example.data.audio.PlaybackNotificationButton
import com.example.data.local.AdhanSoundMode
import com.example.data.local.AppTheme
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.AyahMarkerColorPreset
import com.example.data.local.AyahMarkerColorTarget
import com.example.data.local.QuranFontOption
import com.example.data.local.RiwayahEdition
import com.example.domain.model.TafsirEdition
import com.example.domain.prayer.AdhanVoice
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.PrayerName
import com.example.ui.viewmodel.AdhkarViewModel
import com.example.ui.viewmodel.PrayerViewModel
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.WirdViewModel

@Immutable
data class MoreSettingsActions(
    val onSelectAppTheme: (AppTheme) -> Unit,
    val onSetUseWesternDigits: (Boolean) -> Unit,
    val onSetKeepScreenOn: (Boolean) -> Unit,
    val onSetReaderDimmerAlpha: (Float) -> Unit,
    val onSetTwoPageSpreadOnTablet: (Boolean) -> Unit,
    val onSetAutoAdvanceDhikr: (Boolean) -> Unit,
    val onSetHapticFeedbackEnabled: (Boolean) -> Unit,
    val onSelectAyahMarkerColor: (AyahMarkerColorPreset) -> Unit,
    val onSelectAyahMarkerColorTarget: (AyahMarkerColorTarget) -> Unit,
    val onSelectAyahHighlightColor: (AyahHighlightColorPreset) -> Unit,
    val onSelectAyahHighlightStyle: (AyahHighlightStyle) -> Unit,
    val onSetAyahHighlightAlpha: (Float) -> Unit,
    val onToggleSurahBanner: (Boolean) -> Unit,
    val onSelectRiwayah: (RiwayahEdition) -> Unit,
    val onSelectQuranFont: (QuranFontOption) -> Unit,
    val onQuranFontSizeChange: (Float) -> Unit,
    val onResetQuranFontSettings: () -> Unit,
    val onAdhkarFontSizeChange: (Float) -> Unit,
    val onResetAdhkarFontSize: () -> Unit,
    val onSelectReciter: (String) -> Unit,
    val onSetPlaybackNotifEnabled: (Boolean) -> Unit,
    val onTogglePlaybackNotifButton: (PlaybackNotificationButton) -> Unit,
    val onSetPlaybackNotifShowWhenPaused: (Boolean) -> Unit,
    val onStartFullDownload: () -> Unit,
    val onCancelFullDownload: () -> Unit,
    val onDeleteFullDownload: () -> Unit,
    val onSelectTafsirEdition: (TafsirEdition) -> Unit,
    val onDownloadTafsir: () -> Unit,
    val onCancelTafsir: () -> Unit,
    val onDeleteTafsir: () -> Unit,
    val onUpdateMorningAdhkarReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    val onUpdateEveningAdhkarReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    val onSelectCity: (CityLocation) -> Unit,
    val onDetectGpsLocation: () -> Unit,
    val onSelectCalculationMethod: (CalculationMethod) -> Unit,
    val onSelectAsrMethod: (AsrJuristicMethod) -> Unit,
    val onSetUse24Hour: (Boolean) -> Unit,
    val onSetPreAdhanMinutes: (Int) -> Unit,
    val onSetAdhanSoundMode: (AdhanSoundMode) -> Unit,
    val onSelectAdhanVoice: (String) -> Unit,
    val onDownloadAdhanVoice: (AdhanVoice) -> Unit,
    val onDeleteAdhanVoice: (AdhanVoice) -> Unit,
    val onTogglePreviewAdhanVoice: (AdhanVoice) -> Unit,
    val onStopAdhanPreview: () -> Unit,
    val onTestPrayerAlert: () -> Unit,
    val onTogglePrayerAlert: (PrayerName) -> Unit,
    val onSetHijriOffsetDays: (Int) -> Unit,
    val onUpdateHijriSunnah: ((HijriSunnahConfig) -> HijriSunnahConfig) -> Unit = {},
    val onClearPrayerStatus: () -> Unit,
    val onUpdateWirdReminder: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    val onSetWirdPureReadingMode: (Boolean) -> Unit,
    val onSetHideWirdBannerOutsideWird: (Boolean) -> Unit,
    val onStartNewKhatmah: (startJuz: Int, quartersPerDay: Int) -> Unit,
    val onClearWirdStatus: () -> Unit
)

@Composable
internal fun rememberMoreSettingsActions(
    quranViewModel: QuranViewModel,
    adhkarViewModel: AdhkarViewModel,
    prayerViewModel: PrayerViewModel,
    wirdViewModel: WirdViewModel
): MoreSettingsActions = remember(quranViewModel, adhkarViewModel, prayerViewModel, wirdViewModel) {
    MoreSettingsActions(
        onSelectAppTheme = quranViewModel::setAppTheme,
        onSetUseWesternDigits = quranViewModel::setUseWesternDigits,
        onSetKeepScreenOn = quranViewModel::setKeepScreenOn,
        onSetReaderDimmerAlpha = quranViewModel::setReaderDimmerAlpha,
        onSetTwoPageSpreadOnTablet = quranViewModel::setTwoPageSpreadOnTablet,
        onSetAutoAdvanceDhikr = quranViewModel::setAutoAdvanceDhikr,
        onSetHapticFeedbackEnabled = quranViewModel::setHapticFeedbackEnabled,
        onSelectAyahMarkerColor = quranViewModel::setAyahMarkerColor,
        onSelectAyahMarkerColorTarget = quranViewModel::setAyahMarkerColorTarget,
        onSelectAyahHighlightColor = quranViewModel::setAyahHighlightColor,
        onSelectAyahHighlightStyle = quranViewModel::setAyahHighlightStyle,
        onSetAyahHighlightAlpha = quranViewModel::setAyahHighlightAlpha,
        onToggleSurahBanner = quranViewModel::setSurahBannerEnabled,
        onSelectRiwayah = quranViewModel::setRiwayah,
        onSelectQuranFont = quranViewModel::setQuranFont,
        onQuranFontSizeChange = quranViewModel::setFontSize,
        onResetQuranFontSettings = quranViewModel::resetFontSettings,
        onAdhkarFontSizeChange = adhkarViewModel::setFontSize,
        onResetAdhkarFontSize = adhkarViewModel::resetFontSize,
        onSelectReciter = quranViewModel::setReciter,
        onSetPlaybackNotifEnabled = quranViewModel::setPlaybackNotifEnabled,
        onTogglePlaybackNotifButton = quranViewModel::togglePlaybackNotifButton,
        onSetPlaybackNotifShowWhenPaused = quranViewModel::setPlaybackNotifShowWhenPaused,
        onStartFullDownload = quranViewModel::startFullMushafDownload,
        onCancelFullDownload = quranViewModel::cancelFullMushafDownload,
        onDeleteFullDownload = quranViewModel::deleteDownloadedMushaf,
        onSelectTafsirEdition = quranViewModel.extras::selectTafsirOfflineEdition,
        onDownloadTafsir = { quranViewModel.extras.downloadFullTafsir() },
        onCancelTafsir = quranViewModel.extras::cancelTafsirDownload,
        onDeleteTafsir = { quranViewModel.extras.deleteTafsirEdition() },
        onUpdateMorningAdhkarReminder = adhkarViewModel::updateMorningReminder,
        onUpdateEveningAdhkarReminder = adhkarViewModel::updateEveningReminder,
        onSelectCity = prayerViewModel::selectCity,
        onDetectGpsLocation = prayerViewModel::detectCoarseLocation,
        onSelectCalculationMethod = prayerViewModel::setCalculationMethod,
        onSelectAsrMethod = prayerViewModel::setAsrMethod,
        onSetUse24Hour = prayerViewModel::setUse24Hour,
        onSetPreAdhanMinutes = prayerViewModel::setPreAdhanMinutes,
        onSetAdhanSoundMode = prayerViewModel::setAdhanSoundMode,
        onSelectAdhanVoice = prayerViewModel::setAdhanVoiceId,
        onDownloadAdhanVoice = prayerViewModel::downloadAdhanVoice,
        onDeleteAdhanVoice = prayerViewModel::deleteAdhanVoice,
        onTogglePreviewAdhanVoice = prayerViewModel::togglePreviewAdhanVoice,
        onStopAdhanPreview = prayerViewModel::stopAdhanPreview,
        onTestPrayerAlert = prayerViewModel::triggerTestPrayerAlert,
        onTogglePrayerAlert = prayerViewModel::togglePrayerAlert,
        onSetHijriOffsetDays = prayerViewModel::setHijriOffsetDays,
        onUpdateHijriSunnah = prayerViewModel::updateHijriSunnah,
        onClearPrayerStatus = prayerViewModel::clearStatusMessage,
        onUpdateWirdReminder = wirdViewModel::updateReminder,
        onSetWirdPureReadingMode = quranViewModel::setWirdPureReadingMode,
        onSetHideWirdBannerOutsideWird = quranViewModel::setHideWirdBannerOutsideWird,
        onStartNewKhatmah = wirdViewModel::startNewKhatmah,
        onClearWirdStatus = wirdViewModel::clearStatusMessage
    )
}
