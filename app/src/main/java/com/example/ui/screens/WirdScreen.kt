package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.audio.AudioPlaybackState
import com.example.data.local.ReadingStats
import com.example.domain.model.toAppDigits
import com.example.domain.wird.CustomWirdFormatters
import com.example.domain.wird.HusunActions
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.NamedWirdPlanItem
import com.example.domain.wird.WirdPlanCalculator
import com.example.domain.wird.WirdPlanSummary
import com.example.domain.wird.WirdPortion
import com.example.domain.wird.WirdTimelineCalculator
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.screens.wird.AllCustomPlansScreen
import com.example.ui.screens.wird.WirdCompletedHeroCard
import com.example.ui.screens.wird.WirdHeroCard
import com.example.ui.screens.wird.WirdModalSheetsHost
import com.example.ui.screens.wird.WirdProgressCard
import com.example.ui.screens.wird.WirdTimelineScreen
import com.example.ui.screens.wird.WirdTopBar
import com.example.ui.screens.wird.WirdWelcomeSetupCard
import com.example.ui.navigation.LocalAnimatedVisibilityScope
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.viewmodel.WirdUiState
import kotlinx.coroutines.launch

private enum class WirdSubView { FEED, ALL_PLANS, TIMELINE }

@Composable
fun WirdScreen(
    uiState: WirdUiState,
    stats: ReadingStats = ReadingStats(),
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    isTabActive: Boolean = true,
    openAllPlansRequestCount: Int = 0,
    onStartNewKhatmah: (startJuz: Int, quartersPerDay: Int) -> Unit,
    onCompleteCurrentWird: () -> Unit,
    onUndoCompleteWird: () -> Unit,
    onSetCompletedCount: (Int) -> Unit,
    onOpenPageInReader: (Int, Int) -> Unit,
    onTogglePlayWird: (WirdPortion) -> Unit = {},
    onSetDailyGoalPages: (Int) -> Unit = {},
    onClearStatusMessage: () -> Unit,
    customPlanActions: CustomWirdPlanActions = CustomWirdPlanActions(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showNewKhatmahSheet by remember { mutableStateOf(false) }
    var showDuaSheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var subView by rememberSaveable { mutableStateOf(WirdSubView.FEED) }
    var timelinePlanId by rememberSaveable { mutableStateOf<String?>(null) }
    var timelineFromAllPlans by rememberSaveable { mutableStateOf(false) }
    var showAddCustomPlanSheet by remember { mutableStateOf(false) }
    var editingCustomPlan by remember { mutableStateOf<NamedWirdPlanConfig?>(null) }
    var showPlanSettingsSheet by remember { mutableStateOf(false) }
    var showHusunDailySheet by remember { mutableStateOf(false) }
    var showHusunSetupSheet by remember { mutableStateOf(false) }
    var pendingDeletePlan by remember { mutableStateOf<NamedWirdPlanItem?>(null) }
    var pendingToggleReminderPlanId by remember { mutableStateOf<String?>(null) }
    var pendingAddCustomPlan by remember { mutableStateOf<NamedWirdPlanConfig?>(null) }

    LaunchedEffect(openAllPlansRequestCount) { if (openAllPlansRequestCount > 0) subView = WirdSubView.ALL_PLANS }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val toggleId = pendingToggleReminderPlanId; val addPlan = pendingAddCustomPlan
        pendingToggleReminderPlanId = null; pendingAddCustomPlan = null
        if (granted) {
            if (toggleId != null) customPlanActions.onTogglePlanReminder(toggleId)
            if (addPlan != null) customPlanActions.onAddPlan(addPlan)
        } else {
            if (addPlan != null) customPlanActions.onAddPlan(addPlan.copy(reminderEnabled = false))
            scope.launch { snackbarHostState.showQuranToast("يرجى السماح بالإشعارات لتفعيل تنبيهات الجلسات.") }
        }
    }

    fun hasNotifPerm(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun handleTogglePlanReminder(planId: String) {
        val enabling = uiState.customPlans.firstOrNull { it.config.id == planId }?.config?.reminderEnabled == false
        if (enabling && !hasNotifPerm() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingToggleReminderPlanId = planId
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        customPlanActions.onTogglePlanReminder(planId)
    }

    fun handleAddPlan(planConfig: NamedWirdPlanConfig) {
        if (planConfig.reminderEnabled && !hasNotifPerm() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingAddCustomPlan = planConfig
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        customPlanActions.onAddPlan(planConfig)
    }

    fun navigateBackSubView() {
        subView = if (subView == WirdSubView.TIMELINE && timelineFromAllPlans) WirdSubView.ALL_PLANS else WirdSubView.FEED
    }

    val subPredictiveState = rememberPredictiveBackState(isTabActive && subView != WirdSubView.FEED) { navigateBackSubView() }

    LaunchedEffect(isTabActive, uiState.statusMessage, uiState.lastCompletedBeforeUndo) {
        if (!isTabActive) return@LaunchedEffect
        val msg = uiState.statusMessage ?: return@LaunchedEffect
        val canUndo = uiState.lastCompletedBeforeUndo != null
        val res = snackbarHostState.showQuranToast(
            message = msg, actionLabel = if (canUndo) "تراجع" else null,
            duration = if (canUndo) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (res == SnackbarResult.ActionPerformed && canUndo) onUndoCompleteWird()
        onClearStatusMessage()
    }

    val plan = uiState.plan
    val todayFallback = remember { CustomWirdFormatters.currentEpochDay() }
    val activeTimeline = remember(timelinePlanId, uiState.mainTimeline, uiState.customTimelines, plan, uiState.config, uiState.customPlans) {
        val pid = timelinePlanId
        if (pid == null) {
            uiState.mainTimeline ?: plan?.let { WirdTimelineCalculator.buildForMainKhatmah(uiState.config, it, todayFallback) }
        } else {
            uiState.customTimelines[pid] ?: uiState.customPlans.firstOrNull { it.config.id == pid }?.let {
                WirdTimelineCalculator.buildForCustomPlan(it, todayFallback)
            }
        }
    }
    LaunchedEffect(subView, activeTimeline) {
        if (subView == WirdSubView.TIMELINE && activeTimeline == null) navigateBackSubView()
    }

    val bottomClearance = LocalFloatingNavBottomPadding.current
    val subtitleText = when {
        plan == null -> "نظّم ختمتك وتابع وردك القرآني يومياً"
        plan.isCompleted -> "ما شاء الله • أتممت الختمة الحالية"
        else -> "وردك اليوم: ${WirdPlanCalculator.formatDailyPortionSummaryAr(uiState.config.quartersPerDay)}"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { QuranSnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = bottomClearance)) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            AnimatedContent(
                targetState = subView,
                transitionSpec = QuranMotionSpecs.subScreenTransition { initial, target -> target.ordinal >= initial.ordinal },
                label = "wird_sub_view_switch"
            ) { activeSub ->
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                    when (activeSub) {
                        WirdSubView.TIMELINE -> if (activeTimeline != null) {
                            WirdTimelineScreen(
                                timeline = activeTimeline,
                                bottomClearance = bottomClearance,
                                onBack = ::navigateBackSubView,
                                onOpenEdit = {
                                    val pid = activeTimeline.planId
                                    if (pid == null) showNewKhatmahSheet = true
                                    else editingCustomPlan = uiState.customPlans.firstOrNull { it.config.id == pid }?.config
                                },
                                onOpenSettings = { showPlanSettingsSheet = true },
                                onToggleSession = { idx -> customPlanActions.onToggleTimelinePortion(activeTimeline.planId, idx) },
                                onOpenSessionReader = { session ->
                                    val pid = activeTimeline.planId
                                    if (pid == null) onOpenPageInReader(session.portion.startPage, session.portion.index)
                                    else customPlanActions.onOpenPlanPage(session.portion.startPage, session.portion.index, pid)
                                },
                                onRescheduleOverdue = { customPlanActions.onRescheduleOverdue(activeTimeline.planId) },
                                onDeferToday = { customPlanActions.onDeferToday(activeTimeline.planId) },
                                modifier = Modifier.predictiveBackTransform(subPredictiveState)
                            )
                        }
                        WirdSubView.ALL_PLANS -> AllCustomPlansScreen(
                            customPlans = uiState.customPlans,
                            husunPlan = uiState.husunPlan,
                            bottomClearance = bottomClearance,
                            onBack = { subView = WirdSubView.FEED },
                            onAddPlan = { showAddCustomPlanSheet = true },
                            onOpenPlanReader = customPlanActions.onOpenPlanPage,
                            onCompletePlanWird = customPlanActions.onCompletePlanWird,
                            onTogglePlanReminder = ::handleTogglePlanReminder,
                            onOpenPlanSchedule = { pid -> timelinePlanId = pid; timelineFromAllPlans = true; subView = WirdSubView.TIMELINE },
                            onRequestDeletePlan = { pendingDeletePlan = it },
                            onOpenHusunSetup = { showHusunSetupSheet = true },
                            onOpenHusunDaily = { showHusunDailySheet = true },
                            modifier = Modifier.predictiveBackTransform(subPredictiveState)
                        )
                        WirdSubView.FEED -> LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp + bottomClearance),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.widthIn(max = 760.dp).fillMaxSize().statusBarsPadding()
                        ) {
                            item(key = "top_bar") {
                                WirdTopBar(
                                    hasActivePlan = plan != null,
                                    customPlansCount = uiState.customPlans.size,
                                    subtitleText = subtitleText,
                                    onOpenAllPlans = { subView = WirdSubView.ALL_PLANS },
                                    onOpenStats = { showStatsSheet = true },
                                    onOpenDua = { showDuaSheet = true },
                                    onOpenSchedule = { timelinePlanId = null; timelineFromAllPlans = false; subView = WirdSubView.TIMELINE },
                                    onShareProgress = { plan?.let { shareWirdPlanProgress(context, it) } }
                                )
                            }
                            if (plan == null) {
                                item(key = "welcome_setup") {
                                    WirdWelcomeSetupCard(onQuickStartPlan = onStartNewKhatmah, onOpenCustomWizard = { showNewKhatmahSheet = true })
                                }
                            } else {
                                item(key = "active_hero") {
                                    val currentWird = plan.currentWird
                                    if (plan.isCompleted || currentWird == null) {
                                        WirdCompletedHeroCard(
                                            onOpenDua = { showDuaSheet = true },
                                            onShareCompletion = { shareWirdPlanProgress(context, plan) },
                                            onStartNewKhatmah = { showNewKhatmahSheet = true }
                                        )
                                    } else {
                                        WirdHeroCard(
                                            wird = currentWird, totalWirds = plan.totalWirds,
                                            hasStartedCurrentWird = plan.hasStartedCurrentWird, resumePage = plan.resumePage,
                                            playbackState = playbackState,
                                            onOpenReader = { onOpenPageInReader(plan.resumePage, currentWird.index) },
                                            onToggleListenWird = { onTogglePlayWird(currentWird) },
                                            onCompleteWird = onCompleteCurrentWird
                                        )
                                    }
                                }
                                item(key = "progress_summary") {
                                    WirdProgressCard(
                                        plan = plan, config = uiState.config, ajza = uiState.ajza,
                                        onOpenSchedule = { timelinePlanId = null; timelineFromAllPlans = false; subView = WirdSubView.TIMELINE }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    WirdModalSheetsHost(
        uiState = uiState, stats = stats,
        showNewKhatmahSheet = showNewKhatmahSheet, showDuaSheet = showDuaSheet, showStatsSheet = showStatsSheet,
        showAddCustomPlanSheet = showAddCustomPlanSheet, editingCustomPlan = editingCustomPlan,
        settingsTimeline = if (showPlanSettingsSheet) activeTimeline else null,
        showHusunDailySheet = showHusunDailySheet, showHusunSetupSheet = showHusunSetupSheet, pendingDeletePlan = pendingDeletePlan,
        onStartNewKhatmah = onStartNewKhatmah, onSetDailyGoalPages = onSetDailyGoalPages,
        customPlanActions = customPlanActions.copy(onAddPlan = ::handleAddPlan, onTogglePlanReminder = ::handleTogglePlanReminder),
        onOpenEditMainKhatmah = { showNewKhatmahSheet = true },
        onOpenEditCustomPlan = { editingCustomPlan = it },
        onRequestDeleteCustomPlan = { pendingDeletePlan = it },
        onDismissNewKhatmah = { showNewKhatmahSheet = false },
        onDismissDua = { showDuaSheet = false }, onDismissStats = { showStatsSheet = false },
        onDismissAddCustomPlan = { showAddCustomPlanSheet = false },
        onDismissEditCustomPlan = { editingCustomPlan = null },
        onDismissPlanSettings = { showPlanSettingsSheet = false },
        onDismissHusunDaily = { showHusunDailySheet = false },
        onOpenHusunSettingsFromDaily = { showHusunDailySheet = false; showHusunSetupSheet = true },
        onDismissHusunSetup = { showHusunSetupSheet = false },
        onDismissDeleteDialog = { pendingDeletePlan = null },
        onCopiedDuaMessage = { msg -> scope.launch { snackbarHostState.showQuranToast(msg) } }
    )
}
