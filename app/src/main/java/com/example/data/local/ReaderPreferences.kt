package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.audio.PlaybackNotificationButton
import com.example.data.audio.PlaybackNotificationConfig
import com.example.domain.model.AppDigitConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_ws_prefs")

class ReaderPreferencesRepository(private val context: Context) {
    private object Keys {
        val LAST_PAGE = intPreferencesKey("last_page")
        val LAST_SURAH = intPreferencesKey("last_surah")
        val LAST_AYAH = intPreferencesKey("last_ayah")
        val RIWAYAH_SLUG = stringPreferencesKey("riwayah_slug")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val QURAN_FONT = stringPreferencesKey("quran_font")
        val APP_THEME = stringPreferencesKey("app_theme")
        val AYAH_MARKER_COLOR = stringPreferencesKey("ayah_marker_color")
        val AYAH_MARKER_TARGET = stringPreferencesKey("ayah_marker_target")
        val SURAH_BANNER_ENABLED = booleanPreferencesKey("surah_banner_enabled")
        val LEGACY_PAPER_THEME = stringPreferencesKey("paper_theme")
        val LEGACY_APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val RECITER_ID = stringPreferencesKey("reciter_id")
        val BOOKMARKED_PAGES = stringSetPreferencesKey("bookmarked_pages")
        val USE_WESTERN_DIGITS = booleanPreferencesKey("use_western_digits")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val READER_DIMMER_ALPHA = floatPreferencesKey("reader_dimmer_alpha")
        val TWO_PAGE_SPREAD = booleanPreferencesKey("two_page_spread_tablet")
        val AUTO_ADVANCE_DHIKR = booleanPreferencesKey("auto_advance_dhikr")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val AYAH_HIGHLIGHT_COLOR = stringPreferencesKey("ayah_highlight_color")
        val AYAH_HIGHLIGHT_STYLE = stringPreferencesKey("ayah_highlight_style")
        val AYAH_HIGHLIGHT_ALPHA = floatPreferencesKey("ayah_highlight_alpha")
        val WIRD_PURE_READING_MODE = booleanPreferencesKey("wird_pure_reading_mode")
        val HIDE_WIRD_BANNER_OUTSIDE_WIRD = booleanPreferencesKey("hide_wird_banner_outside_wird")
        val PLAYBACK_NOTIF_ENABLED = booleanPreferencesKey("playback_notif_enabled")
        val PLAYBACK_NOTIF_BUTTONS = stringSetPreferencesKey("playback_notif_buttons")
        val PLAYBACK_NOTIF_SHOW_PAUSED = booleanPreferencesKey("playback_notif_show_paused")
    }

    val settingsFlow: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        mapPrefs(prefs).also { AppDigitConfig.useWesternDigits = it.useWesternDigits }
    }

    private fun mapPrefs(prefs: Preferences): ReaderSettings {
        val resolvedTheme = prefs[Keys.APP_THEME]?.let { stored ->
            AppTheme.fromStoredName(stored)
        } ?: migrateLegacyTheme(
            legacyPaper = prefs[Keys.LEGACY_PAPER_THEME],
            legacyAppMode = prefs[Keys.LEGACY_APP_THEME_MODE]
        )
        return ReaderSettings(
            lastPage = (prefs[Keys.LAST_PAGE] ?: 1).coerceIn(1, 604),
            lastSurah = (prefs[Keys.LAST_SURAH] ?: 1).coerceIn(1, 114),
            lastAyah = (prefs[Keys.LAST_AYAH] ?: 1).coerceAtLeast(1),
            riwayah = RiwayahEdition.fromSlug(prefs[Keys.RIWAYAH_SLUG] ?: RiwayahEdition.HAFS.slug),
            fontSizeSp = (prefs[Keys.FONT_SIZE_SP] ?: 26f).coerceIn(18f, 38f),
            quranFont = runCatching {
                QuranFontOption.valueOf(prefs[Keys.QURAN_FONT] ?: QuranFontOption.ALYAMAMA.name)
            }.getOrDefault(QuranFontOption.ALYAMAMA),
            appTheme = resolvedTheme,
            ayahMarkerColor = runCatching {
                AyahMarkerColorPreset.valueOf(prefs[Keys.AYAH_MARKER_COLOR] ?: AyahMarkerColorPreset.EMERALD.name)
            }.getOrDefault(AyahMarkerColorPreset.EMERALD),
            ayahMarkerColorTarget = runCatching {
                AyahMarkerColorTarget.valueOf(
                    prefs[Keys.AYAH_MARKER_TARGET] ?: AyahMarkerColorTarget.NUMBER_AND_ORNAMENT.name
                )
            }.getOrDefault(AyahMarkerColorTarget.NUMBER_AND_ORNAMENT),
            ayahHighlightColor = runCatching {
                AyahHighlightColorPreset.valueOf(
                    prefs[Keys.AYAH_HIGHLIGHT_COLOR] ?: AyahHighlightColorPreset.FOLLOW_MARKER.name
                )
            }.getOrDefault(AyahHighlightColorPreset.FOLLOW_MARKER),
            ayahHighlightStyle = runCatching {
                AyahHighlightStyle.valueOf(
                    prefs[Keys.AYAH_HIGHLIGHT_STYLE] ?: AyahHighlightStyle.FILL_AND_BORDER.name
                )
            }.getOrDefault(AyahHighlightStyle.FILL_AND_BORDER),
            ayahHighlightAlpha = (prefs[Keys.AYAH_HIGHLIGHT_ALPHA] ?: 0.22f).coerceIn(0.10f, 0.42f),
            surahBannerEnabled = prefs[Keys.SURAH_BANNER_ENABLED] ?: true,
            selectedReciterId = prefs[Keys.RECITER_ID] ?: "Abdul_Basit_Murattal_192kbps",
            bookmarkedPages = prefs[Keys.BOOKMARKED_PAGES]
                ?.mapNotNull { it.toIntOrNull()?.takeIf { p -> p in 1..604 } }
                ?.toSet()
                ?: emptySet(),
            useWesternDigits = prefs[Keys.USE_WESTERN_DIGITS] ?: false,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            readerDimmerAlpha = (prefs[Keys.READER_DIMMER_ALPHA] ?: 0f).coerceIn(0f, 0.65f),
            twoPageSpreadOnTablet = prefs[Keys.TWO_PAGE_SPREAD] ?: true,
            autoAdvanceDhikr = prefs[Keys.AUTO_ADVANCE_DHIKR] ?: false,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK_ENABLED] ?: true,
            wirdPureReadingMode = prefs[Keys.WIRD_PURE_READING_MODE] ?: true,
            hideWirdBannerOutsideWird = prefs[Keys.HIDE_WIRD_BANNER_OUTSIDE_WIRD] ?: false,
            playbackNotificationConfig = PlaybackNotificationConfig(
                enabled = prefs[Keys.PLAYBACK_NOTIF_ENABLED] ?: true,
                buttons = PlaybackNotificationButton.fromIds(prefs[Keys.PLAYBACK_NOTIF_BUTTONS]),
                showWhenPaused = prefs[Keys.PLAYBACK_NOTIF_SHOW_PAUSED] ?: true
            )
        )
    }

    private fun migrateLegacyTheme(legacyPaper: String?, legacyAppMode: String?): AppTheme = when {
        legacyAppMode == "DARK" || legacyPaper == "NIGHT" -> AppTheme.DARK
        legacyPaper == "SEPIA" || legacyAppMode == "LIGHT" -> AppTheme.SEPIA
        else -> AppTheme.SYSTEM
    }

    suspend fun updateLastPosition(page: Int, surah: Int, ayah: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_PAGE] = page.coerceIn(1, 604)
            prefs[Keys.LAST_SURAH] = surah.coerceIn(1, 114)
            prefs[Keys.LAST_AYAH] = ayah.coerceAtLeast(1)
        }
    }

    suspend fun setRiwayah(edition: RiwayahEdition) {
        context.dataStore.edit { it[Keys.RIWAYAH_SLUG] = edition.slug }
    }

    suspend fun setFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE_SP] = sizeSp.coerceIn(18f, 38f) }
    }

    suspend fun setQuranFont(option: QuranFontOption) {
        context.dataStore.edit { it[Keys.QURAN_FONT] = option.name }
    }

    suspend fun resetFontSettings() {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE_SP] = 26f
            prefs[Keys.QURAN_FONT] = QuranFontOption.ALYAMAMA.name
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme.name
            prefs.remove(Keys.LEGACY_PAPER_THEME)
            prefs.remove(Keys.LEGACY_APP_THEME_MODE)
        }
    }

    suspend fun setAyahMarkerColor(preset: AyahMarkerColorPreset) {
        context.dataStore.edit { it[Keys.AYAH_MARKER_COLOR] = preset.name }
    }

    suspend fun setAyahMarkerColorTarget(target: AyahMarkerColorTarget) {
        context.dataStore.edit { it[Keys.AYAH_MARKER_TARGET] = target.name }
    }

    suspend fun setSurahBannerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SURAH_BANNER_ENABLED] = enabled }
    }

    suspend fun setReciter(reciterId: String) {
        context.dataStore.edit { it[Keys.RECITER_ID] = reciterId }
    }

    suspend fun setUseWesternDigits(useWestern: Boolean) {
        AppDigitConfig.useWesternDigits = useWestern
        context.dataStore.edit { it[Keys.USE_WESTERN_DIGITS] = useWestern }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setReaderDimmerAlpha(alpha: Float) {
        context.dataStore.edit { it[Keys.READER_DIMMER_ALPHA] = alpha.coerceIn(0f, 0.65f) }
    }

    suspend fun setTwoPageSpreadOnTablet(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TWO_PAGE_SPREAD] = enabled }
    }

    suspend fun setAutoAdvanceDhikr(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_ADVANCE_DHIKR] = enabled }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setAyahHighlightColor(preset: AyahHighlightColorPreset) {
        context.dataStore.edit { it[Keys.AYAH_HIGHLIGHT_COLOR] = preset.name }
    }

    suspend fun setAyahHighlightStyle(style: AyahHighlightStyle) {
        context.dataStore.edit { it[Keys.AYAH_HIGHLIGHT_STYLE] = style.name }
    }

    suspend fun setAyahHighlightAlpha(alpha: Float) {
        context.dataStore.edit { it[Keys.AYAH_HIGHLIGHT_ALPHA] = alpha.coerceIn(0.10f, 0.42f) }
    }

    suspend fun setWirdPureReadingMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIRD_PURE_READING_MODE] = enabled }
    }

    suspend fun setHideWirdBannerOutsideWird(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_WIRD_BANNER_OUTSIDE_WIRD] = enabled }
    }

    suspend fun updatePlaybackNotifConfig(transform: (PlaybackNotificationConfig) -> PlaybackNotificationConfig) {
        context.dataStore.edit { prefs ->
            val cur = PlaybackNotificationConfig(
                enabled = prefs[Keys.PLAYBACK_NOTIF_ENABLED] ?: true,
                buttons = PlaybackNotificationButton.fromIds(prefs[Keys.PLAYBACK_NOTIF_BUTTONS]),
                showWhenPaused = prefs[Keys.PLAYBACK_NOTIF_SHOW_PAUSED] ?: true
            )
            val next = transform(cur)
            prefs[Keys.PLAYBACK_NOTIF_ENABLED] = next.enabled
            prefs[Keys.PLAYBACK_NOTIF_BUTTONS] = next.buttonIds()
            prefs[Keys.PLAYBACK_NOTIF_SHOW_PAUSED] = next.showWhenPaused
        }
    }

    suspend fun toggleBookmark(page: Int) {
        val clamped = page.coerceIn(1, 604).toString()
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BOOKMARKED_PAGES]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(clamped)) current.remove(clamped)
            prefs[Keys.BOOKMARKED_PAGES] = current
        }
    }

    suspend fun exportJson(): JSONObject {
        val s = mapPrefs(context.dataStore.data.first())
        return JSONObject().apply {
            put("lastPage", s.lastPage)
            put("lastSurah", s.lastSurah)
            put("lastAyah", s.lastAyah)
            put("riwayah", s.riwayah.slug)
            put("fontSizeSp", s.fontSizeSp.toDouble())
            put("quranFont", s.quranFont.name)
            put("appTheme", s.appTheme.name)
            put("markerColor", s.ayahMarkerColor.name)
            put("markerTarget", s.ayahMarkerColorTarget.name)
            put("surahBanner", s.surahBannerEnabled)
            put("reciter", s.selectedReciterId)
            put("westernDigits", s.useWesternDigits)
            put("keepScreenOn", s.keepScreenOn)
            put("dimmer", s.readerDimmerAlpha.toDouble())
            put("twoPage", s.twoPageSpreadOnTablet)
            put("autoAdvanceDhikr", s.autoAdvanceDhikr)
            put("hapticFeedback", s.hapticFeedbackEnabled)
            put("highlightColor", s.ayahHighlightColor.name)
            put("highlightStyle", s.ayahHighlightStyle.name)
            put("highlightAlpha", s.ayahHighlightAlpha.toDouble())
            put("wirdPureReadingMode", s.wirdPureReadingMode)
            put("hideWirdBannerOutsideWird", s.hideWirdBannerOutsideWird)
            put("playbackNotifEnabled", s.playbackNotificationConfig.enabled)
            put("playbackNotifShowPaused", s.playbackNotificationConfig.showWhenPaused)
            put("playbackNotifButtons", JSONArray().apply { s.playbackNotificationConfig.buttonIds().forEach(::put) })
            put("bookmarkedPages", JSONArray().apply { s.bookmarkedPages.forEach(::put) })
        }
    }

    suspend fun importJson(obj: JSONObject) {
        val western = obj.optBoolean("westernDigits", false)
        AppDigitConfig.useWesternDigits = western
        val pagesArr = obj.optJSONArray("bookmarkedPages")
        val pagesSet = buildSet {
            if (pagesArr != null) {
                for (i in 0 until pagesArr.length()) {
                    val p = pagesArr.optInt(i, -1)
                    if (p in 1..604) add(p.toString())
                }
            }
        }
        val notifButtonsArr = obj.optJSONArray("playbackNotifButtons")
        val notifButtonsSet = if (notifButtonsArr != null) {
            buildSet {
                for (i in 0 until notifButtonsArr.length()) {
                    notifButtonsArr.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        } else {
            PlaybackNotificationButton.defaultButtons.mapTo(linkedSetOf()) { it.id }
        }
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_PAGE] = obj.optInt("lastPage", 1).coerceIn(1, 604)
            prefs[Keys.LAST_SURAH] = obj.optInt("lastSurah", 1).coerceIn(1, 114)
            prefs[Keys.LAST_AYAH] = obj.optInt("lastAyah", 1).coerceAtLeast(1)
            prefs[Keys.RIWAYAH_SLUG] = obj.optString("riwayah", RiwayahEdition.HAFS.slug)
            prefs[Keys.FONT_SIZE_SP] = obj.optDouble("fontSizeSp", 26.0).toFloat().coerceIn(18f, 38f)
            prefs[Keys.QURAN_FONT] = obj.optString("quranFont", QuranFontOption.ALYAMAMA.name)
            prefs[Keys.APP_THEME] = AppTheme.fromStoredName(obj.optString("appTheme", AppTheme.SYSTEM.name)).name
            prefs[Keys.AYAH_MARKER_COLOR] = obj.optString("markerColor", AyahMarkerColorPreset.EMERALD.name)
            prefs[Keys.AYAH_MARKER_TARGET] = obj.optString("markerTarget", AyahMarkerColorTarget.NUMBER_AND_ORNAMENT.name)
            prefs[Keys.AYAH_HIGHLIGHT_COLOR] = obj.optString("highlightColor", AyahHighlightColorPreset.FOLLOW_MARKER.name)
            prefs[Keys.AYAH_HIGHLIGHT_STYLE] = obj.optString("highlightStyle", AyahHighlightStyle.FILL_AND_BORDER.name)
            prefs[Keys.AYAH_HIGHLIGHT_ALPHA] = obj.optDouble("highlightAlpha", 0.22).toFloat().coerceIn(0.10f, 0.42f)
            prefs[Keys.SURAH_BANNER_ENABLED] = obj.optBoolean("surahBanner", true)
            prefs[Keys.RECITER_ID] = obj.optString("reciter", "Abdul_Basit_Murattal_192kbps")
            prefs[Keys.USE_WESTERN_DIGITS] = western
            prefs[Keys.KEEP_SCREEN_ON] = obj.optBoolean("keepScreenOn", true)
            prefs[Keys.READER_DIMMER_ALPHA] = obj.optDouble("dimmer", 0.0).toFloat().coerceIn(0f, 0.65f)
            prefs[Keys.TWO_PAGE_SPREAD] = obj.optBoolean("twoPage", true)
            prefs[Keys.AUTO_ADVANCE_DHIKR] = obj.optBoolean("autoAdvanceDhikr", false)
            prefs[Keys.HAPTIC_FEEDBACK_ENABLED] = obj.optBoolean("hapticFeedback", true)
            prefs[Keys.WIRD_PURE_READING_MODE] = obj.optBoolean("wirdPureReadingMode", true)
            prefs[Keys.HIDE_WIRD_BANNER_OUTSIDE_WIRD] = obj.optBoolean("hideWirdBannerOutsideWird", false)
            prefs[Keys.PLAYBACK_NOTIF_ENABLED] = obj.optBoolean("playbackNotifEnabled", true)
            prefs[Keys.PLAYBACK_NOTIF_SHOW_PAUSED] = obj.optBoolean("playbackNotifShowPaused", true)
            prefs[Keys.PLAYBACK_NOTIF_BUTTONS] = notifButtonsSet
            prefs[Keys.BOOKMARKED_PAGES] = pagesSet
        }
    }
}
