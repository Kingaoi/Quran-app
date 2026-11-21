package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.QuranApplication
import com.example.data.local.HusunPreferencesRepository
import com.example.data.local.ReadingStatsRepository
import com.example.data.local.WirdPreferencesRepository
import com.example.data.repository.QuranRepository
import com.example.data.widget.QuranSummaryWidgetProvider
import com.example.data.wird.WirdReminderScheduler
import com.example.domain.model.AyahItem
import com.example.domain.model.JuzInfo
import com.example.domain.model.SurahInfo
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.HusunCalculator
import com.example.domain.wird.HusunDailyPlanUiState
import com.example.domain.wird.HusunFortressType
import com.example.domain.wird.HusunNewRate
import com.example.domain.wird.KhatmahPlanConfig
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdDirection
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdTimelineCalculator
import com.example.domain.wird.WirdTimelineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WirdUiState(
    val isLoading: Boolean = true,
    val config: KhatmahPlanConfig = KhatmahPlanConfig(),
    val plan: WirdPlanSummary? = null,
    val mainTimeline: WirdTimelineState? = null,
    val customPlans: List<NamedWirdPlanItem> = emptyList(),
    val customTimelines: Map<String, WirdTimelineState> = emptyMap(),
    val husunPlan: HusunDailyPlanUiState? = null,
    val ajza: List<JuzInfo> = emptyList(),
    val surahs: List<SurahInfo> = emptyList(),
    val allAyahs: List<AyahItem> = emptyList(),
    val statusMessage: String? = null,
    val lastCompletedBeforeUndo: Int? = null,
    val lastUndoPlanId: String? = null
)

class WirdViewModel(application: Application) : AndroidViewModel(application) {
    private val quranRepository =
        (application as? QuranApplication)?.quranRepository ?: QuranRepository(application)
    private val prefsRepository = WirdPreferencesRepository(application)
    private val husunPrefs = HusunPreferencesRepository(application)
    private val statsRepository = ReadingStatsRepository(application)
    private val reminderScheduler = WirdReminderScheduler(application)
    private val timelineDelegate = WirdTimelineDelegate(prefsRepository, statsRepository)

    private val _uiState = MutableStateFlow(WirdUiState())
    val uiState: StateFlow<WirdUiState> = _uiState.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            quranRepository.ensureLoaded()
            val ajza = quranRepository.getAjza()
            val surahs = quranRepository.getSurahs()
            val quarters = quranRepository.getAllHizbQuarters()
            val allAyahs = quranRepository.getAllAyahs()
            combine(prefsRepository.configFlow, prefsRepository.customPlansFlow, husunPrefs.configFlow) { config, customConfigs, husunConfig ->
                Triple(config, customConfigs, husunConfig)
            }.collect { (config, customConfigs, husunConfig) ->
                reminderScheduler.syncReminder(config)
                val today = WirdPreferencesRepository.currentLocalEpochDay()
                val summary = WirdPlanCalculator.buildPlan(config, quarters, allAyahs, ajza, today)
                val mainTimeline = summary?.let { WirdTimelineCalculator.buildForMainKhatmah(config, it, today) }
                val customItems = customConfigs.mapNotNull { c ->
                    val planSummary = if (c.isV2Plan) {
                        CustomWirdPlanCalculator.buildPlan(c, allAyahs, ajza, today)
                    } else {
                        WirdPlanCalculator.buildPlan(c.toPlanConfig(), quarters, allAyahs, ajza, today)
                    }
                    planSummary?.let { NamedWirdPlanItem(config = c, summary = it) }
                }
                val customTimelines = customItems.associate { item ->
                    item.config.id to WirdTimelineCalculator.buildForCustomPlan(item, today)
                }
                reminderScheduler.syncCustomPlanItems(customItems)
                val husunState = HusunCalculator.buildDailyPlan(husunConfig, allAyahs, today)
                val cur = summary?.currentWird
                val widgetSummary = if (!config.isActive || cur == null) {
                    "لا توجد ختمة نشطة — ابدأ وردك اليومي"
                } else {
                    "الورد الحالي: ${cur.startSurahNameAr} (${cur.startAyahNumber.toArabicIndicDigits()}) - ص ${cur.startPage.toArabicIndicDigits()}..${cur.endPage.toArabicIndicDigits()}"
                }
                QuranSummaryWidgetProvider.saveWirdSnapshot(getApplication(), widgetSummary)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        config = config,
                        plan = summary,
                        mainTimeline = mainTimeline,
                        customPlans = customItems,
                        customTimelines = customTimelines,
                        husunPlan = husunState,
                        ajza = ajza,
                        surahs = surahs,
                        allAyahs = allAyahs
                    )
                }
            }
        }
    }

    fun startNewKhatmah(startJuz: Int, quartersPerDay: Int) {
        viewModelScope.launch {
            prefsRepository.startNewPlan(startJuz, quartersPerDay)
            val startLabel = WirdPlanCalculator.formatStartPointAr(startJuz, _uiState.value.ajza)
            val portionLabel = WirdPlanCalculator.formatDailyPortionSummaryAr(quartersPerDay)
            _uiState.update {
                it.copy(statusMessage = "تم بدء ختمة جديدة من $startLabel ($portionLabel يومياً)", lastCompletedBeforeUndo = null)
            }
        }
    }

    fun completeCurrentWird(emitStatusMessage: Boolean = true) {
        val state = _uiState.value
        viewModelScope.launch {
            val res = timelineDelegate.completeMainPortion(state) ?: return@launch
            if (emitStatusMessage) {
                _uiState.update { it.copy(lastCompletedBeforeUndo = res.first, lastUndoPlanId = null, statusMessage = res.second) }
            } else {
                _uiState.update { it.copy(statusMessage = null, lastCompletedBeforeUndo = null, lastUndoPlanId = null) }
            }
        }
    }

    fun undoLastWirdCompletion() {
        val rollback = _uiState.value.lastCompletedBeforeUndo ?: return
        val undoPlanId = _uiState.value.lastUndoPlanId
        viewModelScope.launch {
            if (undoPlanId != null) prefsRepository.setCustomPlanCompletedCount(undoPlanId, rollback)
            else prefsRepository.setCompletedWirdsCount(rollback)
            _uiState.update {
                it.copy(lastCompletedBeforeUndo = null, lastUndoPlanId = null, statusMessage = "تم التراجع عن إتمام الورد")
            }
        }
    }

    fun setCompletedWirdsCount(targetCompletedCount: Int, emitStatusMessage: Boolean = true) {
        val total = _uiState.value.plan?.totalWirds ?: return
        val clamped = targetCompletedCount.coerceIn(0, total)
        viewModelScope.launch {
            prefsRepository.setCompletedWirdsCount(clamped)
            if (emitStatusMessage) {
                _uiState.update {
                    it.copy(lastCompletedBeforeUndo = null, lastUndoPlanId = null, statusMessage = "تم تحديث موضع الورد الحالي في الختمة")
                }
            } else {
                _uiState.update { it.copy(statusMessage = null, lastCompletedBeforeUndo = null, lastUndoPlanId = null) }
            }
        }
    }

    fun addCustomPlan(planConfig: NamedWirdPlanConfig) {
        val cleanTitle = planConfig.title.trim().ifBlank { "خطة ورد جديدة" }
        val normalized = planConfig.copy(title = cleanTitle, isV2Plan = true, iconKey = planConfig.planType.key)
        viewModelScope.launch {
            prefsRepository.addCustomPlan(normalized)
            val metrics = CustomWirdPlanCalculator.calculateMetrics(normalized, _uiState.value.allAyahs, _uiState.value.surahs)
            val dailyLabel = CustomWirdPlanCalculator.formatAmountAr(metrics.dailyInUnit, normalized.amountUnit)
            _uiState.update {
                it.copy(lastCompletedBeforeUndo = null, lastUndoPlanId = null, statusMessage = "تم إنشاء خطة «$cleanTitle» ($dailyLabel يومياً)")
            }
        }
    }

    fun updateCustomPlan(planConfig: NamedWirdPlanConfig) {
        val cleanTitle = planConfig.title.trim().ifBlank { "خطة ورد جديدة" }
        val normalized = planConfig.copy(title = cleanTitle, isV2Plan = true, iconKey = planConfig.planType.key)
        viewModelScope.launch {
            prefsRepository.updateCustomPlan(normalized)
            _uiState.update { it.copy(statusMessage = "تم تحديث إعدادات خطة «$cleanTitle»") }
        }
    }

    fun completeCustomPlanWird(planId: String, emitStatusMessage: Boolean = true) {
        val state = _uiState.value
        viewModelScope.launch {
            val res = timelineDelegate.completeCustomPortion(state, planId) ?: return@launch
            if (emitStatusMessage) {
                _uiState.update { it.copy(lastCompletedBeforeUndo = res.first, lastUndoPlanId = planId, statusMessage = res.second) }
            }
        }
    }

    fun setCustomPlanCompletedCount(planId: String, targetCompletedCount: Int, emitStatusMessage: Boolean = true) {
        val item = _uiState.value.customPlans.firstOrNull { it.config.id == planId } ?: return
        val clamped = targetCompletedCount.coerceIn(0, item.summary.totalWirds)
        viewModelScope.launch {
            prefsRepository.setCustomPlanCompletedCount(planId, clamped)
            if (emitStatusMessage) {
                _uiState.update {
                    it.copy(lastCompletedBeforeUndo = null, lastUndoPlanId = null, statusMessage = "تم تحديث موضع خطة «${item.config.title}»")
                }
            }
        }
    }

    fun toggleTimelinePortion(planId: String?, portionIndex: Int) {
        viewModelScope.launch {
            val msg = timelineDelegate.togglePortionCompleted(_uiState.value, planId, portionIndex)
            if (msg != null) _uiState.update { it.copy(statusMessage = msg, lastCompletedBeforeUndo = null) }
        }
    }

    fun rescheduleOverdueSessions(planId: String?) {
        viewModelScope.launch {
            val msg = timelineDelegate.rescheduleOverdue(_uiState.value, planId)
            if (msg != null) _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun deferTodaySessions(planId: String?) {
        viewModelScope.launch {
            val msg = timelineDelegate.deferTodaySessions(_uiState.value, planId)
            if (msg != null) _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun resetTimelineOverrides(planId: String?) {
        viewModelScope.launch {
            val msg = timelineDelegate.resetOverrides(planId)
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun deleteCustomPlan(planId: String) {
        val title = _uiState.value.customPlans.firstOrNull { it.config.id == planId }?.config?.title ?: return
        viewModelScope.launch {
            reminderScheduler.cancelCustomPlanReminders(planId)
            prefsRepository.deleteCustomPlan(planId)
            _uiState.update {
                it.copy(lastCompletedBeforeUndo = null, lastUndoPlanId = null, statusMessage = "تم حذف خطة «$title»")
            }
        }
    }

    fun onReaderPageSettled(page: Int, planId: String? = null) {
        if (!planId.isNullOrBlank()) {
            val item = _uiState.value.customPlans.firstOrNull { it.config.id == planId } ?: return
            val cur = item.summary.currentWird ?: return
            val lo = minOf(cur.startPage, cur.endPage)
            val hi = maxOf(cur.startPage, cur.endPage)
            if (page in lo..hi && page != item.config.lastProgressPage) {
                viewModelScope.launch { prefsRepository.setCustomPlanLastProgressPage(planId, page) }
            }
            return
        }
        val current = _uiState.value.plan?.currentWird ?: return
        if (page in current.startPage..current.endPage && page != _uiState.value.config.lastProgressPage) {
            viewModelScope.launch { prefsRepository.setLastProgressPage(page) }
        }
    }

    fun toggleCustomPlanReminder(planId: String) {
        val item = _uiState.value.customPlans.firstOrNull { it.config.id == planId } ?: return
        val next = !item.config.reminderEnabled
        viewModelScope.launch {
            prefsRepository.setCustomPlanReminderEnabled(planId, next)
            val msg = if (next) "تم تفعيل تذكير جلسات خطة «${item.config.title}»" else "تم إيقاف تذكير جلسات خطة «${item.config.title}»"
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun updateReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefsRepository.setReminder(enabled, hour, minute)
            val msg = if (enabled) "تم تفعيل تذكير الورد اليومي" else "تم إيقاف تذكير الورد اليومي"
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun configureHusun(dir: WirdDirection, startPages: Int, rate: HusunNewRate, contPages: Int, farDivs: Int) {
        viewModelScope.launch {
            husunPrefs.configureAndStart(dir, startPages, rate, contPages, farDivs, WirdPreferencesRepository.currentLocalEpochDay())
            _uiState.update { it.copy(statusMessage = "تم تفعيل نظام الحصون الخمسة بنجاح") }
        }
    }

    fun toggleHusunFortress(type: HusunFortressType) {
        viewModelScope.launch { husunPrefs.toggleFortressCompleted(type, WirdPreferencesRepository.currentLocalEpochDay()) }
    }

    fun incrementHusunRepeat() {
        viewModelScope.launch { husunPrefs.incrementTodayRepeat(WirdPreferencesRepository.currentLocalEpochDay()) }
    }

    fun completeHusunDay() {
        viewModelScope.launch {
            husunPrefs.advanceToNextDay(WirdPreferencesRepository.currentLocalEpochDay())
            _uiState.update { it.copy(statusMessage = "بارك الله فيك! تم تثبيت إنجاز اليوم والانتقال للورد التالي") }
        }
    }

    fun resetHusun() {
        viewModelScope.launch {
            husunPrefs.deactivateAndReset()
            _uiState.update { it.copy(statusMessage = "تم إيقاف خطة الحصون الخمسة") }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, lastCompletedBeforeUndo = null, lastUndoPlanId = null) }
    }
}
