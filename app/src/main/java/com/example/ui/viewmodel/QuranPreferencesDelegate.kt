package com.example.ui.viewmodel

import com.example.data.audio.PlaybackNotificationButton
import com.example.data.local.AppTheme
import com.example.data.local.AyahHighlightColorPreset
import com.example.data.local.AyahHighlightStyle
import com.example.data.local.AyahMarkerColorPreset
import com.example.data.local.AyahMarkerColorTarget
import com.example.data.local.QuranFontOption
import com.example.data.local.ReaderPreferencesRepository
import com.example.data.local.RiwayahEdition
import com.example.di.QuranSystemBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class QuranPreferencesDelegate(
    private val scope: CoroutineScope,
    private val prefsRepository: ReaderPreferencesRepository,
    private val systemBridge: QuranSystemBridge,
    private val playbackDelegate: QuranPlaybackDelegate
) {
    fun updateLastPosition(page: Int, surahNumber: Int, ayahNumber: Int) {
        scope.launch { prefsRepository.updateLastPosition(page, surahNumber, ayahNumber) }
    }

    fun toggleBookmark(page: Int) { scope.launch { prefsRepository.toggleBookmark(page) } }
    fun setRiwayah(edition: RiwayahEdition) { scope.launch { prefsRepository.setRiwayah(edition) } }
    fun setAppTheme(theme: AppTheme) { scope.launch { prefsRepository.setAppTheme(theme) } }
    fun setAyahMarkerColor(preset: AyahMarkerColorPreset) { scope.launch { prefsRepository.setAyahMarkerColor(preset) } }
    fun setAyahMarkerColorTarget(target: AyahMarkerColorTarget) { scope.launch { prefsRepository.setAyahMarkerColorTarget(target) } }
    fun setAyahHighlightColor(preset: AyahHighlightColorPreset) { scope.launch { prefsRepository.setAyahHighlightColor(preset) } }
    fun setAyahHighlightStyle(style: AyahHighlightStyle) { scope.launch { prefsRepository.setAyahHighlightStyle(style) } }
    fun setAyahHighlightAlpha(alpha: Float) { scope.launch { prefsRepository.setAyahHighlightAlpha(alpha) } }
    fun setSurahBannerEnabled(enabled: Boolean) { scope.launch { prefsRepository.setSurahBannerEnabled(enabled) } }
    fun setFontSize(sizeSp: Float) { scope.launch { prefsRepository.setFontSize(sizeSp) } }
    fun setQuranFont(option: QuranFontOption) { scope.launch { prefsRepository.setQuranFont(option) } }
    fun resetFontSettings() { scope.launch { prefsRepository.resetFontSettings() } }

    fun setUseWesternDigits(useWestern: Boolean) {
        scope.launch {
            prefsRepository.setUseWesternDigits(useWestern)
            systemBridge.notifyWidgetsChanged()
        }
    }

    fun setKeepScreenOn(enabled: Boolean) { scope.launch { prefsRepository.setKeepScreenOn(enabled) } }
    fun setReaderDimmerAlpha(alpha: Float) { scope.launch { prefsRepository.setReaderDimmerAlpha(alpha) } }
    fun setTwoPageSpreadOnTablet(enabled: Boolean) { scope.launch { prefsRepository.setTwoPageSpreadOnTablet(enabled) } }
    fun setAutoAdvanceDhikr(enabled: Boolean) { scope.launch { prefsRepository.setAutoAdvanceDhikr(enabled) } }
    fun setHapticFeedbackEnabled(enabled: Boolean) { scope.launch { prefsRepository.setHapticFeedbackEnabled(enabled) } }
    fun setWirdPureReadingMode(enabled: Boolean) { scope.launch { prefsRepository.setWirdPureReadingMode(enabled) } }
    fun setHideWirdBannerOutsideWird(enabled: Boolean) { scope.launch { prefsRepository.setHideWirdBannerOutsideWird(enabled) } }

    fun setPlaybackNotifEnabled(enabled: Boolean) {
        scope.launch { prefsRepository.updatePlaybackNotifConfig { it.copy(enabled = enabled) } }
    }

    fun togglePlaybackNotifButton(button: PlaybackNotificationButton) {
        scope.launch { prefsRepository.updatePlaybackNotifConfig { it.toggledButton(button) } }
    }

    fun setPlaybackNotifShowWhenPaused(show: Boolean) {
        scope.launch { prefsRepository.updatePlaybackNotifConfig { it.copy(showWhenPaused = show) } }
    }

    fun setReciter(reciterId: String) {
        scope.launch {
            prefsRepository.setReciter(reciterId)
            playbackDelegate.setDefaultReciter(reciterId)
        }
    }
}
