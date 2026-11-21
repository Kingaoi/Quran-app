package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.prayer.ExactAlarmCompat
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.HijriSunnahCalculator
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTickerState
import com.example.ui.components.OrnamentalDivider
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.screens.more.ExactAlarmWarningBanner
import com.example.ui.screens.prayer.CityPickerBottomSheet
import com.example.ui.screens.prayer.HijriCalendarSheet
import com.example.ui.screens.prayer.NightWatchCard
import com.example.ui.screens.prayer.PrayerHeroCard
import com.example.ui.screens.prayer.PrayerListItemCard
import com.example.ui.screens.prayer.PrayerScheduleSectionHeader
import com.example.ui.screens.prayer.PrayerTodaySunnahRibbon
import com.example.ui.screens.prayer.PrayerTopBar
import com.example.data.local.ReadingStats
import com.example.ui.screens.prayer.QiblaCompassCard
import com.example.ui.screens.prayer.SunnahRawatibCard
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.theme.quranColors
import com.example.ui.viewmodel.PrayerUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun PrayerTimesScreen(
    uiState: PrayerUiState,
    stats: ReadingStats = ReadingStats(),
    isTabActive: Boolean = true,
    openHijriSheetRequestCount: Int = 0,
    onShiftDay: (Int) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onDetectGpsLocation: () -> Unit,
    onTogglePrayerAlert: (PrayerName) -> Unit,
    onUpdateHijriSunnah: ((HijriSunnahConfig) -> HijriSunnahConfig) -> Unit = {},
    onToggleRawatibItem: (String) -> Unit = {},
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier,
    tickerStateFlow: StateFlow<PrayerTickerState>? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val useWestern = LocalUseWesternDigits.current
    val qc = MaterialTheme.quranColors

    var showQiblaCompass by remember { mutableStateOf(false) }
    var showCityPicker by remember { mutableStateOf(false) }
    var showHijriCalendar by remember { mutableStateOf(false) }
    var pendingAlertPrayer by remember { mutableStateOf<PrayerName?>(null) }
    var pendingEnableSunnah by remember { mutableStateOf(false) }
    LaunchedEffect(openHijriSheetRequestCount) { if (openHijriSheetRequestCount > 0) showHijriCalendar = true }
    val lifecycleOwner = LocalLifecycleOwner.current
    var canExactAlarm by remember(context) { mutableStateOf(ExactAlarmCompat.canScheduleExactAlarms(context)) }
    DisposableEffect(lifecycleOwner, context) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) canExactAlarm = ExactAlarmCompat.canScheduleExactAlarms(context)
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onDetectGpsLocation()
        else scope.launch { snackbarHostState.showQuranToast("تم رفض إذن الموقع، يمكنك اختيار مدينتك من القائمة.") }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val targetPrayer = pendingAlertPrayer
        val enableSunnah = pendingEnableSunnah
        pendingAlertPrayer = null
        pendingEnableSunnah = false
        if (granted && targetPrayer != null) {
            onTogglePrayerAlert(targetPrayer)
        } else if (granted && enableSunnah) {
            onUpdateHijriSunnah { it.copy(enabled = true) }
        } else if (!granted) {
            scope.launch { snackbarHostState.showQuranToast("يرجى السماح بالإشعارات لتفعيل التنبيهات.") }
        }
    }

    LaunchedEffect(isTabActive, uiState.statusMessage) {
        if (!isTabActive) return@LaunchedEffect
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showQuranToast(msg)
            onClearStatusMessage()
        }
    }

    fun requestCoarseLocationAndDetect() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) onDetectGpsLocation() else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun handleAlertToggle(prayer: PrayerName) {
        val isEnabling = !uiState.settings.isAlertEnabled(prayer)
        if (isEnabling && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingAlertPrayer = prayer
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        onTogglePrayerAlert(prayer)
    }

    fun handleToggleSunnah(enable: Boolean) {
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingEnableSunnah = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        onUpdateHijriSunnah { it.copy(enabled = enable) }
    }

    val nowMillis = remember(uiState.nextPrayer, uiState.dayOffset, uiState.dailyTimes) { System.currentTimeMillis() }
    val todaySunnah = remember(uiState.settings.hijriOffsetDays, useWestern, uiState.dayOffset) {
        if (uiState.dayOffset == 0) {
            HijriSunnahCalculator.inspectToday(uiState.settings.hijriOffsetDays, useWesternDigits = useWestern)
        } else null
    }
    val bottomClearance = LocalFloatingNavBottomPadding.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = qc.bg,
        snackbarHost = {
            QuranSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = bottomClearance)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp + bottomClearance),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                item(key = "top_bar") {
                    PrayerTopBar(
                        showQibla = showQiblaCompass,
                        onToggleQibla = { showQiblaCompass = !showQiblaCompass },
                        onOpenHijriCalendar = { showHijriCalendar = true }
                    )
                }

                item(key = "hero") {
                    PrayerHeroCard(
                        city = uiState.settings.city,
                        hijriDateAr = uiState.hijriDateAr,
                        gregorianDateAr = uiState.gregorianDateAr,
                        dayOffset = uiState.dayOffset,
                        nextPrayer = uiState.nextPrayer,
                        countdownText = uiState.countdownText,
                        iqamahStatusText = uiState.iqamahStatusText,
                        intervalProgress = uiState.intervalProgress,
                        qiblaBearingDegrees = uiState.dailyTimes.qiblaBearingDegrees,
                        use24Hour = uiState.settings.use24Hour,
                        methodTitleAr = uiState.settings.calculationMethod.titleAr,
                        onOpenCityPicker = { showCityPicker = true },
                        onPrevDay = { onShiftDay(-1) },
                        onToday = { onShiftDay(0) },
                        onNextDay = { onShiftDay(1) },
                        onToggleQibla = { showQiblaCompass = !showQiblaCompass },
                        tickerStateFlow = tickerStateFlow
                    )
                }

                if (todaySunnah != null) {
                    item(key = "today_sunnah") {
                        PrayerTodaySunnahRibbon(insight = todaySunnah, onClick = { showHijriCalendar = true })
                    }
                }

                if (uiState.settings.enabledAlerts.isNotEmpty() && !canExactAlarm) {
                    item(key = "exact_alarm_warning") {
                        ExactAlarmWarningBanner(
                            onOpenSettings = {
                                runCatching { context.startActivity(ExactAlarmCompat.buildExactAlarmSettingsIntent(context)) }
                            }
                        )
                    }
                }

                item(key = "qibla") {
                    AnimatedVisibility(visible = showQiblaCompass) {
                        QiblaCompassCard(
                            cityNameAr = uiState.settings.city.nameAr,
                            qiblaBearingDegrees = uiState.dailyTimes.qiblaBearingDegrees
                        )
                    }
                }

                item(key = "schedule_header") { PrayerScheduleSectionHeader() }

                items(items = uiState.dailyTimes.entries, key = { it.prayer.id }) { entry ->
                    val isNext = uiState.dayOffset == 0 && uiState.nextPrayer?.prayer == entry.prayer
                    val isPassed = uiState.dayOffset < 0 || (uiState.dayOffset == 0 && entry.timestampMillis <= nowMillis)
                    PrayerListItemCard(
                        entry = entry,
                        isNextPrayer = isNext,
                        isPassed = isPassed,
                        isAlertEnabled = uiState.settings.isAlertEnabled(entry.prayer),
                        use24Hour = uiState.settings.use24Hour,
                        onToggleAlert = ::handleAlertToggle
                    )
                }

                item(key = "night_divider") { OrnamentalDivider(color = qc.accent.copy(alpha = 0.35f)) }

                item(key = "night_watch") {
                    NightWatchCard(
                        midnightMillis = uiState.dailyTimes.midnightMillis,
                        lastThirdMillis = uiState.dailyTimes.lastThirdMillis,
                        use24Hour = uiState.settings.use24Hour,
                        timeZone = uiState.settings.city.resolveTimeZone()
                    )
                }

                if (uiState.dayOffset == 0) {
                    item(key = "sunnah_rawatib") {
                        SunnahRawatibCard(completedIdsToday = stats.rawatibCompletedIdsToday, onToggleItem = onToggleRawatibItem)
                    }
                }
            }
        }
    }

    if (showCityPicker) {
        CityPickerBottomSheet(
            currentCity = uiState.settings.city,
            isLocatingGps = uiState.isLocatingGps,
            use24Hour = uiState.settings.use24Hour,
            onRequestGpsLocation = ::requestCoarseLocationAndDetect,
            onSelectCity = onSelectCity,
            onDismiss = { showCityPicker = false }
        )
    }

    if (showHijriCalendar) {
        HijriCalendarSheet(
            hijriOffsetDays = uiState.settings.hijriOffsetDays,
            sunnahConfig = uiState.settings.hijriSunnahConfig,
            onToggleSunnahMaster = ::handleToggleSunnah,
            onDismiss = { showHijriCalendar = false }
        )
    }
}
