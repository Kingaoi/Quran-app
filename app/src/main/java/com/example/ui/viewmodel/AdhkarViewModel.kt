package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.adhkar.AdhkarAudioController
import com.example.data.adhkar.AdhkarCatalog
import com.example.data.adhkar.AdhkarPreferencesRepository
import com.example.data.adhkar.AdhkarReminderConfig
import com.example.data.adhkar.AdhkarReminderScheduler
import com.example.data.adhkar.DhikrAudioState
import com.example.data.adhkar.HisnMuslimRepository
import com.example.data.local.ReadingStatsRepository
import com.example.data.repository.QuranRepository
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.domain.model.DhikrSubChapterGroup
import com.example.domain.model.groupBySubChapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

private val Context.adhkarDataStore by preferencesDataStore(name = "adhkar_daily_progress")

@Immutable
data class AdhkarUiState(
    val selectedCategory: DhikrCategory = DhikrCategory.MORNING,
    val countsById: Map<String, Int> = emptyMap(),
    val fontSizeSp: Float = 21f,
    val reminderConfig: AdhkarReminderConfig = AdhkarReminderConfig(),
    val itemsByCategory: Map<DhikrCategory, List<DhikrItem>> = AdhkarCatalog.defaultMap(),
    val favoriteIds: Set<String> = emptySet(),
    val customDuas: List<DhikrItem> = emptyList(),
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false,
    val audioState: DhikrAudioState = DhikrAudioState(),
    val isHisnSynced: Boolean = false,
    val isSyncingSource: Boolean = false,
    val syncStatusMessage: String? = null
) {
    fun itemsForCategory(category: DhikrCategory): List<DhikrItem> {
        val base = itemsByCategory[category] ?: AdhkarCatalog.byCategory(category)
        return if (category == DhikrCategory.PROPHETIC_DUAS && customDuas.isNotEmpty()) customDuas + base else base
    }

    fun subChaptersForCategory(category: DhikrCategory): List<DhikrSubChapterGroup> =
        itemsForCategory(category).groupBySubChapter()

    fun shouldOpenDirectlyInReader(category: DhikrCategory): Boolean =
        category.isSequentialLitany || subChaptersForCategory(category).size <= 1

    fun firstIncompleteIndex(category: DhikrCategory): Int {
        val list = itemsForCategory(category)
        return list.indexOfFirst { (countsById[it.id] ?: 0) < it.targetCount }
            .takeIf { it >= 0 } ?: 0
    }

    fun itemsForScope(category: DhikrCategory, chapterTitle: String?): List<DhikrItem> {
        val all = itemsForCategory(category)
        val clean = chapterTitle?.trim().orEmpty()
        if (clean.isBlank()) return all
        return all.filter { it.resolvedChapterTitle() == clean }.ifEmpty { all }
    }

    val allItems: List<DhikrItem>
        get() = customDuas + DhikrCategory.entries.flatMap { itemsByCategory[it] ?: AdhkarCatalog.byCategory(it) }

    val favoriteItems: List<DhikrItem>
        get() = allItems.distinctBy { it.id }.filter { it.id in favoriteIds }

    val searchResults: List<DhikrItem>
        get() {
            val q = QuranRepository.normalizeArabic(searchQuery.trim())
            if (q.length < 2) return emptyList()
            return allItems.distinctBy { it.id }.filter { item ->
                QuranRepository.normalizeArabic(item.title).contains(q) ||
                    QuranRepository.normalizeArabic(item.arabicText).contains(q) ||
                    QuranRepository.normalizeArabic(item.virtueOrNote).contains(q)
            }.take(40)
        }

    val activeItems: List<DhikrItem> get() = itemsForCategory(selectedCategory)
    val completedInCategory: Int get() = activeItems.count { (countsById[it.id] ?: 0) >= it.targetCount }
    val totalInCategory: Int get() = activeItems.size
}

class AdhkarViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.adhkarDataStore
    private val reminderPrefs = AdhkarPreferencesRepository(application)
    private val reminderScheduler = AdhkarReminderScheduler(application)
    private val hisnRepository = HisnMuslimRepository(application)
    private val statsRepository = ReadingStatsRepository(application)
    private val audioController = AdhkarAudioController(application)
    private val dateKeyPref = stringPreferencesKey("adhkar_progress_day")
    private val fontSizePref = floatPreferencesKey("adhkar_font_size_sp")

    private val _uiState = MutableStateFlow(AdhkarUiState())
    val uiState: StateFlow<AdhkarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val today = currentDayStamp()
            val prefs = dataStore.data.first()
            val savedFont = (prefs[fontSizePref] ?: 21f).coerceIn(17f, 29f)
            val savedDay = prefs[dateKeyPref]
            if (savedDay != today) {
                dataStore.edit { mutable ->
                    mutable.clear(); mutable[dateKeyPref] = today; mutable[fontSizePref] = savedFont
                }
                _uiState.update { it.copy(fontSizeSp = savedFont) }
            } else {
                val restored = buildMap {
                    for ((key, value) in prefs.asMap()) {
                        if (key.name.startsWith("cnt_") && value is Int && value > 0) {
                            put(key.name.removePrefix("cnt_"), value)
                        }
                    }
                }
                _uiState.update { it.copy(countsById = restored, fontSizeSp = savedFont) }
            }
            val cached = hisnRepository.loadCachedCatalog()
            if (cached != null) _uiState.update { it.copy(itemsByCategory = cached.itemsByCategory, isHisnSynced = true) }
            val isStale = cached == null || (System.currentTimeMillis() - cached.cachedAtMillis) > HisnMuslimRepository.CACHE_MAX_AGE_MS
            if (isStale) syncFromHisnApi(showStatusOnResult = false)
        }
        viewModelScope.launch {
            reminderPrefs.configFlow.collect { config ->
                reminderScheduler.syncReminders(config)
                _uiState.update { it.copy(reminderConfig = config) }
            }
        }
        viewModelScope.launch { reminderPrefs.favoriteIdsFlow.collect { ids -> _uiState.update { it.copy(favoriteIds = ids) } } }
        viewModelScope.launch { reminderPrefs.customDuasFlow.collect { list -> _uiState.update { it.copy(customDuas = list) } } }
        viewModelScope.launch { audioController.state.collect { audio -> _uiState.update { it.copy(audioState = audio) } } }
    }

    fun refreshFromHisnMuslim() {
        if (_uiState.value.isSyncingSource) return
        viewModelScope.launch { syncFromHisnApi(showStatusOnResult = true) }
    }

    fun clearSyncStatusMessage() = _uiState.update { it.copy(syncStatusMessage = null) }
    fun updateSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun toggleFavoritesOnly() = _uiState.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    fun toggleFavorite(id: String) { viewModelScope.launch { reminderPrefs.toggleFavorite(id) } }
    fun addCustomDua(title: String, arabicText: String, targetCount: Int, note: String) {
        viewModelScope.launch { reminderPrefs.addCustomDua(title, arabicText, targetCount, note) }
    }
    fun deleteCustomDua(id: String) { viewModelScope.launch { reminderPrefs.deleteCustomDua(id) } }
    fun togglePlayDhikrAudio(item: DhikrItem) = audioController.togglePlay(item)
    fun stopDhikrAudio() = audioController.stop()

    private suspend fun syncFromHisnApi(showStatusOnResult: Boolean) {
        _uiState.update { it.copy(isSyncingSource = true, syncStatusMessage = null) }
        val result = hisnRepository.fetchAndCacheFromApi()
        _uiState.update { st ->
            result.fold(
                onSuccess = { loadedMap ->
                    st.copy(
                        itemsByCategory = loadedMap,
                        isHisnSynced = true,
                        isSyncingSource = false,
                        syncStatusMessage = if (showStatusOnResult) "تم تحديث الأذكار من حصن المسلم" else null
                    )
                },
                onFailure = { err ->
                    st.copy(
                        isSyncingSource = false,
                        syncStatusMessage = if (showStatusOnResult) (err.localizedMessage ?: "تعذّر تحديث الأذكار") else null
                    )
                }
            )
        }
    }

    fun selectCategory(category: DhikrCategory) { _uiState.update { it.copy(selectedCategory = category) } }
    fun adjustFontSize(delta: Float) = setFontSize(_uiState.value.fontSizeSp + delta)
    fun setFontSize(sizeSp: Float) {
        val clamped = sizeSp.coerceIn(17f, 29f)
        _uiState.update { it.copy(fontSizeSp = clamped) }
        viewModelScope.launch { dataStore.edit { prefs -> prefs[fontSizePref] = clamped } }
    }
    fun resetFontSize() = setFontSize(21f)
    fun updateMorningReminder(enabled: Boolean, hour: Int, minute: Int) { viewModelScope.launch { reminderPrefs.setMorningReminder(enabled, hour, minute) } }
    fun updateEveningReminder(enabled: Boolean, hour: Int, minute: Int) { viewModelScope.launch { reminderPrefs.setEveningReminder(enabled, hour, minute) } }

    fun incrementDhikr(item: DhikrItem): Boolean {
        val current = _uiState.value.countsById[item.id] ?: 0
        if (current >= item.targetCount) return false
        val next = (current + 1).coerceAtMost(item.targetCount)
        val completedNow = next >= item.targetCount
        _uiState.update { state -> state.copy(countsById = state.countsById + (item.id to next)) }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[dateKeyPref] = currentDayStamp()
                prefs[intPreferencesKey("cnt_${item.id}")] = next
            }
            if (completedNow) statsRepository.recordAdhkarCompleted()
        }
        return completedNow
    }

    fun resetDhikr(dhikrId: String) {
        _uiState.update { state -> state.copy(countsById = state.countsById - dhikrId) }
        viewModelScope.launch { dataStore.edit { prefs -> prefs.remove(intPreferencesKey("cnt_$dhikrId")) } }
    }

    fun resetActiveCategory() = resetCategory(_uiState.value.selectedCategory)
    fun resetCategory(category: DhikrCategory) {
        resetItems(_uiState.value.itemsForCategory(category).map { it.id })
    }

    fun resetItems(ids: List<String>) {
        if (ids.isEmpty()) return
        _uiState.update { state -> state.copy(countsById = state.countsById - ids.toSet()) }
        viewModelScope.launch { dataStore.edit { prefs -> ids.forEach { prefs.remove(intPreferencesKey("cnt_$it")) } } }
    }

    override fun onCleared() {
        super.onCleared()
        audioController.release()
    }

    private fun currentDayStamp(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }
}
