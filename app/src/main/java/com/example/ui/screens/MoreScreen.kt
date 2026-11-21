package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.BackupManager
import com.example.domain.model.DownloadHubActions
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.OfflineInventoryUiState
import com.example.domain.model.TafsirOfflineState
import com.example.domain.prayer.PrayerName
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.screens.more.MoreAboutAppCard
import com.example.ui.screens.more.MoreCategory
import com.example.ui.screens.more.MoreCategoryContent
import com.example.ui.screens.more.MoreCategoryDetailHeader
import com.example.ui.screens.more.moreCategoryHubItems
import com.example.ui.screens.more.MorePickersSheetsHost
import com.example.ui.screens.more.MoreQuickSummaryCard
import com.example.ui.screens.more.MoreSearchField
import com.example.ui.screens.more.MoreSettingsActions
import com.example.ui.screens.more.MoreTopBar
import com.example.ui.navigation.LocalAnimatedVisibilityScope
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.theme.quranColors
import com.example.ui.viewmodel.AdhkarUiState
import com.example.ui.viewmodel.OfflineDownloadState
import com.example.ui.viewmodel.PrayerUiState
import com.example.ui.viewmodel.QuranUiState
import com.example.ui.viewmodel.WirdUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface PendingNotificationAction {
    data class PrayerAlert(val prayer: PrayerName) : PendingNotificationAction
    data class WirdReminder(val hour: Int, val minute: Int) : PendingNotificationAction
    data class MorningAdhkar(val hour: Int, val minute: Int) : PendingNotificationAction
    data class EveningAdhkar(val hour: Int, val minute: Int) : PendingNotificationAction
    data object HijriSunnah : PendingNotificationAction
}

@Composable
fun MoreScreen(
    uiState: QuranUiState,
    offlineState: OfflineDownloadState,
    adhkarState: AdhkarUiState,
    prayerState: PrayerUiState,
    wirdState: WirdUiState,
    actions: MoreSettingsActions,
    isTabActive: Boolean = true,
    tafsirOfflineState: TafsirOfflineState = TafsirOfflineState(),
    offlineHubState: OfflineInventoryUiState = OfflineInventoryUiState(),
    downloadHubActions: DownloadHubActions? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val backupManager by remember(context) { lazy { BackupManager(context) } }
    val useWestern = LocalUseWesternDigits.current

    var searchQuery by remember { mutableStateOf("") }
    var activeCategory by rememberSaveable { mutableStateOf(MoreCategory.APPEARANCE) }
    var selectedPhoneCategory by rememberSaveable { mutableStateOf<MoreCategory?>(null) }
    var showReciterSheet by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showCalcMethodPicker by remember { mutableStateOf(false) }
    var showAdhanVoiceSheet by remember { mutableStateOf(false) }
    var showNewKhatmahWizard by remember { mutableStateOf(false) }
    var showComprehensiveHub by remember { mutableStateOf(false) }
    var pendingNotificationAction by remember { mutableStateOf<PendingNotificationAction?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = withContext(Dispatchers.IO) { backupManager.exportAllJson() }
                withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } }
            }.onSuccess { snackbarHostState.showQuranToast("تم تصدير النسخة الاحتياطية بنجاح") }
                .onFailure { snackbarHostState.showQuranToast("تعذّر تصدير النسخة الاحتياطية") }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                runCatching {
                    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
                    backupManager.importAllJson(raw).getOrThrow()
                }
            }
            if (res.isSuccess) snackbarHostState.showQuranToast("تمت استعادة الإعدادات والسجلات بنجاح")
            else snackbarHostState.showQuranToast("ملف النسخة الاحتياطية غير صالح")
        }
    }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) actions.onDetectGpsLocation()
        else scope.launch { snackbarHostState.showQuranToast("تم رفض إذن الموقع، يمكنك اختيار مدينتك من القائمة.") }
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val pending = pendingNotificationAction
        pendingNotificationAction = null
        if (granted && pending != null) {
            when (pending) {
                is PendingNotificationAction.PrayerAlert -> actions.onTogglePrayerAlert(pending.prayer)
                is PendingNotificationAction.WirdReminder -> actions.onUpdateWirdReminder(true, pending.hour, pending.minute)
                is PendingNotificationAction.MorningAdhkar -> actions.onUpdateMorningAdhkarReminder(true, pending.hour, pending.minute)
                is PendingNotificationAction.EveningAdhkar -> actions.onUpdateEveningAdhkarReminder(true, pending.hour, pending.minute)
                PendingNotificationAction.HijriSunnah -> actions.onUpdateHijriSunnah { it.copy(enabled = true) }
            }
        } else if (!granted) scope.launch { snackbarHostState.showQuranToast("يرجى السماح بالإشعارات لتفعيل التنبيهات.") }
    }

    LaunchedEffect(isTabActive, prayerState.statusMessage) {
        if (!isTabActive) return@LaunchedEffect
        prayerState.statusMessage?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showQuranToast(it); actions.onClearPrayerStatus() }
    }
    LaunchedEffect(isTabActive, wirdState.statusMessage) {
        if (!isTabActive) return@LaunchedEffect
        wirdState.statusMessage?.takeIf { it.isNotBlank() }?.let { snackbarHostState.showQuranToast(it); actions.onClearWirdStatus() }
    }

    fun hasNotificationPerm() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    fun requestGpsLocation() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) actions.onDetectGpsLocation() else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    fun guardNotification(enabled: Boolean, pending: PendingNotificationAction, onAllowed: () -> Unit) {
        if (enabled && !hasNotificationPerm() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingNotificationAction = pending; notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else onAllowed()
    }

    val morePredictiveState = rememberPredictiveBackState(
        enabled = isTabActive && (searchQuery.isNotBlank() || selectedPhoneCategory != null)
    ) {
        if (searchQuery.isNotBlank()) searchQuery = "" else selectedPhoneCategory = null
    }
    val visibleCategories = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) MoreCategory.entries else MoreCategory.entries.filter { it.titleAr.contains(q, true) || it.keywords.contains(q, true) }
    }
    val bottomClearance = LocalFloatingNavBottomPadding.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { QuranSnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = bottomClearance)) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isTablet = maxWidth >= 720.dp && searchQuery.isBlank()
            val renderCat: @Composable (MoreCategory) -> Unit = { cat ->
                MoreCategoryContent(
                    category = cat, uiState = uiState, offlineState = offlineState, adhkarState = adhkarState,
                    prayerState = prayerState, wirdState = wirdState, tafsirOfflineState = tafsirOfflineState,
                    offlineHubState = offlineHubState, actions = actions,
                    onOpenReciterPicker = { showReciterSheet = true }, onOpenComprehensiveHub = { showComprehensiveHub = true },
                    onOpenCityPicker = { showCityPicker = true }, onOpenCalcMethodPicker = { showCalcMethodPicker = true },
                    onOpenAdhanVoicePicker = { showAdhanVoiceSheet = true }, onOpenNewKhatmahWizard = { showNewKhatmahWizard = true },
                    onToggleMorningReminder = { en, h, m -> guardNotification(en, PendingNotificationAction.MorningAdhkar(h, m)) { actions.onUpdateMorningAdhkarReminder(en, h, m) } },
                    onToggleEveningReminder = { en, h, m -> guardNotification(en, PendingNotificationAction.EveningAdhkar(h, m)) { actions.onUpdateEveningAdhkarReminder(en, h, m) } },
                    onToggleWirdReminder = { en, h, m -> guardNotification(en, PendingNotificationAction.WirdReminder(h, m)) { actions.onUpdateWirdReminder(en, h, m) } },
                    onRequestPrayerAlertToggle = { prayer -> guardNotification(!prayerState.settings.isAlertEnabled(prayer), PendingNotificationAction.PrayerAlert(prayer)) { actions.onTogglePrayerAlert(prayer) } },
                    onRequestHijriSunnahToggle = { en -> guardNotification(en, PendingNotificationAction.HijriSunnah) { actions.onUpdateHijriSunnah { it.copy(enabled = en) } } },
                    onExportBackup = { exportLauncher.launch("quran_app_backup.json") },
                    onImportBackup = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                    onOpenSources = {
                        activeCategory = MoreCategory.SOURCES
                        selectedPhoneCategory = MoreCategory.SOURCES
                    },
                    onShowToast = { msg -> scope.launch { snackbarHostState.showQuranToast(msg) } }
                )
            }

            if (isTablet) {
                Row(
                    modifier = Modifier.widthIn(max = 1040.dp).fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.width(260.dp).fillMaxHeight().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoreCategory.entries.forEach { cat ->
                            val selected = activeCategory == cat
                            val qc = MaterialTheme.quranColors
                            Surface(
                                onClick = { activeCategory = cat }, shape = RoundedCornerShape(16.dp),
                                color = if (selected) qc.selected else qc.surface,
                                border = BorderStroke(1.dp, if (selected) qc.selected else qc.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(cat.icon, contentDescription = null, tint = if (selected) qc.onSelected else qc.primary, modifier = Modifier.size(18.dp))
                                    Text(cat.titleAr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (selected) qc.onSelected else qc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp + bottomClearance), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
                        item(key = "top_bar") { MoreTopBar() }
                        item(key = "search_field") { MoreSearchField(searchQuery, { searchQuery = it }) }
                        item(key = "quick_summary") { MoreQuickSummaryCard(uiState.settings, prayerState.settings, wirdState.config, offlineHubState) { showComprehensiveHub = true } }
                        item(key = activeCategory.name) { renderCat(activeCategory) }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = if (searchQuery.isNotBlank()) null to true else selectedPhoneCategory to false,
                    transitionSpec = QuranMotionSpecs.subScreenTransition { prev, next ->
                        (next.first != null && prev.first == null) || (next.second && !prev.second)
                    },
                    label = "more_phone_navigation"
                ) { (pickedCat, isSearching) ->
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedContent) {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp + bottomClearance),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .widthIn(max = 760.dp)
                                .fillMaxSize()
                                .predictiveBackTransform(morePredictiveState, maxCornerRadius = 26.dp, maxScaleDrop = 0.06f, maxShiftDp = 18.dp)
                                .testTag("more_lazy_list")
                        ) {
                            if (isSearching) {
                                item(key = "top_bar") { MoreTopBar() }
                                item(key = "search_field") { MoreSearchField(searchQuery, { searchQuery = it }) }
                                if (visibleCategories.isEmpty()) item(key = "empty_search") { Text("لا توجد إعدادات مطابقة لـ «$searchQuery»", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
                                else visibleCategories.forEach { cat -> item(key = cat.name) { renderCat(cat) } }
                            } else if (pickedCat == null) {
                                item(key = "top_bar") { MoreTopBar() }
                                item(key = "search_field") { MoreSearchField(searchQuery, { searchQuery = it }) }
                                item(key = "quick_summary") { MoreQuickSummaryCard(uiState.settings, prayerState.settings, wirdState.config, offlineHubState) { showComprehensiveHub = true } }
                                moreCategoryHubItems(uiState.settings, prayerState.settings, wirdState.config, adhkarState.reminderConfig, tafsirOfflineState, useWestern) { selectedPhoneCategory = it }
                                item(key = "about_footer") { MoreAboutAppCard(onOpenSources = { selectedPhoneCategory = MoreCategory.SOURCES }) }
                            } else {
                                item(key = "cat_header") { MoreCategoryDetailHeader(pickedCat, onBack = { selectedPhoneCategory = null }, onSelectCategory = { selectedPhoneCategory = it }) }
                                item(key = pickedCat.name) { renderCat(pickedCat) }
                            }
                        }
                    }
                }
            }
        }
    }

    MorePickersSheetsHost(
        uiState = uiState, prayerState = prayerState, wirdState = wirdState, offlineHubState = offlineHubState,
        actions = actions, downloadHubActions = downloadHubActions, showReciterSheet = showReciterSheet,
        showCityPicker = showCityPicker, showCalcMethodPicker = showCalcMethodPicker, showAdhanVoiceSheet = showAdhanVoiceSheet,
        showNewKhatmahWizard = showNewKhatmahWizard, showComprehensiveHub = showComprehensiveHub,
        onRequestGps = ::requestGpsLocation, onDismissReciter = { showReciterSheet = false },
        onDismissCity = { showCityPicker = false }, onDismissCalcMethod = { showCalcMethodPicker = false },
        onDismissAdhanVoice = { showAdhanVoiceSheet = false }, onDismissNewKhatmah = { showNewKhatmahWizard = false },
        onDismissComprehensiveHub = { showComprehensiveHub = false }
    )
}
