package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import com.example.data.prayer.PrayerAlertTestHelper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.audio.AppAudioCoordinator
import com.example.data.audio.AppAudioOwner
import com.example.data.local.AdhanSoundMode
import com.example.data.local.PrayerPreferencesRepository
import com.example.data.local.PrayerSettings
import com.example.data.prayer.AdhanAudioStorage
import com.example.data.prayer.AdhanPlaybackService
import com.example.data.prayer.HijriSunnahScheduler
import com.example.data.prayer.PrayerAlarmScheduler
import com.example.data.prayer.PrayerLocationResolver
import com.example.data.prayer.PrayerNotificationReceiver
import com.example.data.widget.QuranSummaryWidgetProvider
import com.example.domain.prayer.AdhanVoice
import com.example.domain.prayer.AdhanVoices
import com.example.domain.prayer.AsrJuristicMethod
import com.example.domain.prayer.CalculationMethod
import com.example.domain.prayer.CityLocation
import com.example.domain.prayer.HijriSunnahConfig
import com.example.domain.prayer.DailyPrayerTimes
import com.example.domain.prayer.PrayerName
import com.example.domain.prayer.PrayerTimeEntry
import com.example.domain.prayer.PrayerTickerState
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.prayer.PresetCities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Immutable
data class PrayerUiState(
    val settings: PrayerSettings = PrayerSettings(),
    val dayOffset: Int = 0,
    val hijriDateAr: String = "",
    val gregorianDateAr: String = "",
    val dailyTimes: DailyPrayerTimes = PrayerTimesCalculator.calculateForDate(
        calendar = Calendar.getInstance(PresetCities.defaultCity.resolveTimeZone()),
        latitude = PresetCities.defaultCity.latitude,
        longitude = PresetCities.defaultCity.longitude,
        method = CalculationMethod.UMM_AL_QURA,
        asrMethod = AsrJuristicMethod.SHAFII
    ),
    val nextPrayer: PrayerTimeEntry? = null,
    val countdownText: String = "٠٠:٠٠:٠٠",
    val iqamahStatusText: String? = null,
    val intervalProgress: Float = 0f,
    val isLocatingGps: Boolean = false,
    val cachedAdhanVoiceIds: Set<String> = emptySet(),
    val downloadingAdhanVoiceId: String? = null,
    val previewingAdhanVoiceId: String? = null,
    val statusMessage: String? = null
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PrayerPreferencesRepository(application)
    private val scheduler = PrayerAlarmScheduler(application)
    private val sunnahScheduler = HijriSunnahScheduler(application)
    private val adhanStorage = AdhanAudioStorage(application)
    private var previewPlayer: ExoPlayer? = null
    private var previewTimeoutJob: Job? = null

    private val _uiState = MutableStateFlow(PrayerUiState(cachedAdhanVoiceIds = adhanStorage.cachedVoiceIds()))
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()
    private val _tickerState = MutableStateFlow(PrayerTickerState())
    val tickerState: StateFlow<PrayerTickerState> = _tickerState.asStateFlow()

    init {
        AppAudioCoordinator.register(AppAudioOwner.ADHAN_PREVIEW) { stopAdhanPreview() }
        observeSettings(); startTicker()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            prefs.settingsFlow.collect { settings ->
                scheduler.syncAlarms(settings)
                sunnahScheduler.syncReminders(settings)
                QuranSummaryWidgetProvider.savePrayerSnapshot(
                    context = getApplication(),
                    cityName = settings.city.nameAr,
                    lat = settings.city.latitude,
                    lon = settings.city.longitude,
                    cityTz = settings.city.resolveTimeZone().id,
                    method = settings.calculationMethod,
                    asr = settings.asrMethod,
                    use24Hour = settings.use24Hour,
                    hijriOffsetDays = settings.hijriOffsetDays
                )
                recomputeForSettingsAndDay(settings, _uiState.value.dayOffset)
            }
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (isActive) {
                refreshCountdown(_uiState.value, forceCommitUi = false)
                delay(1_000L)
            }
        }
    }

    fun shiftDay(delta: Int) {
        val nextOffset = if (delta == 0) 0 else (_uiState.value.dayOffset + delta).coerceIn(-365, 365)
        recomputeForSettingsAndDay(_uiState.value.settings, nextOffset)
    }

    fun selectCity(city: CityLocation) { viewModelScope.launch { prefs.setCity(city) } }
    fun setCalculationMethod(method: CalculationMethod) { viewModelScope.launch { prefs.setCalculationMethod(method) } }
    fun setAsrMethod(method: AsrJuristicMethod) { viewModelScope.launch { prefs.setAsrMethod(method) } }
    fun setUse24Hour(use24Hour: Boolean) { viewModelScope.launch { prefs.setUse24Hour(use24Hour) } }
    fun setPreAdhanMinutes(minutes: Int) { viewModelScope.launch { prefs.setPreAdhanMinutes(minutes) } }
    fun setAdhanSoundMode(mode: AdhanSoundMode) { viewModelScope.launch { prefs.setAdhanSoundMode(mode) } }
    fun setAdhanVoiceId(voiceId: String) { viewModelScope.launch { prefs.setAdhanVoiceId(voiceId) } }
    fun setHijriOffsetDays(offset: Int) { viewModelScope.launch { prefs.setHijriOffsetDays(offset) } }
    fun updateHijriSunnah(transform: (HijriSunnahConfig) -> HijriSunnahConfig) {
        viewModelScope.launch { prefs.updateHijriSunnahConfig(transform) }
    }

    fun togglePrayerAlert(prayer: PrayerName) {
        viewModelScope.launch {
            val enabled = prefs.togglePrayerAlert(prayer)
            val msg = if (enabled) "تم تفعيل تنبيه صلاة ${prayer.titleAr}" else "تم إيقاف تنبيه صلاة ${prayer.titleAr}"
            _uiState.update { it.copy(statusMessage = msg) }
        }
    }

    fun downloadAdhanVoice(voice: AdhanVoice) {
        if (_uiState.value.downloadingAdhanVoiceId != null) return
        _uiState.update { it.copy(downloadingAdhanVoiceId = voice.id) }
        viewModelScope.launch {
            val res = adhanStorage.downloadVoice(voice)
            _uiState.update {
                it.copy(
                    downloadingAdhanVoiceId = null,
                    cachedAdhanVoiceIds = adhanStorage.cachedVoiceIds(),
                    statusMessage = if (res.isSuccess) "تم حفظ ${voice.nameAr} للعمل بدون إنترنت" else "تعذّر تنزيل صوت الأذان، تحقق من الاتصال"
                )
            }
        }
    }

    fun deleteAdhanVoice(voice: AdhanVoice) {
        viewModelScope.launch(Dispatchers.IO) {
            adhanStorage.deleteCachedVoice(voice.id)
            _uiState.update { it.copy(cachedAdhanVoiceIds = adhanStorage.cachedVoiceIds(), statusMessage = "تم حذف الملف المحفوظ لـ ${voice.nameAr}") }
        }
    }

    fun togglePreviewAdhanVoice(voice: AdhanVoice) {
        if (_uiState.value.previewingAdhanVoiceId == voice.id) { stopAdhanPreview(); return }
        stopAdhanPreview()
        AppAudioCoordinator.claimFocus(AppAudioOwner.ADHAN_PREVIEW)
        val app = getApplication<Application>()
        runCatching {
            val mediaItem = adhanStorage.buildMediaItem(voice)
            val player = ExoPlayer.Builder(app).setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true
            ).build()
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) { if (playbackState == Player.STATE_ENDED) stopAdhanPreview() }
                override fun onPlayerError(error: PlaybackException) {
                    stopAdhanPreview()
                    _uiState.update { it.copy(statusMessage = "تعذّر تشغيل معاينة الأذان، تأكد من الاتصال") }
                }
            })
            previewPlayer = player
            _uiState.update { it.copy(previewingAdhanVoiceId = voice.id) }
            player.setMediaItem(mediaItem); player.prepare(); player.play()
            voice.maxDurationMs?.let { maxMs ->
                previewTimeoutJob = viewModelScope.launch { delay(maxMs + 1_200L); if (_uiState.value.previewingAdhanVoiceId == voice.id) stopAdhanPreview() }
            }
        }.onFailure { stopAdhanPreview(); _uiState.update { it.copy(statusMessage = "تعذّر تشغيل صوت الأذان") } }
    }

    fun stopAdhanPreview() {
        previewTimeoutJob?.cancel(); previewTimeoutJob = null
        runCatching { previewPlayer?.release() }
        previewPlayer = null
        _uiState.update { it.copy(previewingAdhanVoiceId = null) }
    }

    fun triggerTestPrayerAlert() {
        val app = getApplication<Application>()
        val s = _uiState.value.settings
        val prayer = _tickerState.value.nextPrayer?.prayer ?: _uiState.value.nextPrayer?.prayer ?: PrayerName.DHUHR
        if (s.adhanSoundMode == AdhanSoundMode.FULL_ADHAN && _uiState.value.previewingAdhanVoiceId != null) {
            stopAdhanPreview()
            _uiState.update { it.copy(statusMessage = "تم إيقاف تجربة الأذان") }
            return
        }
        runCatching {
            PrayerNotificationReceiver.ensureChannels(app)
            when (s.adhanSoundMode) {
                AdhanSoundMode.FULL_ADHAN -> togglePreviewAdhanVoice(AdhanVoices.byId(s.selectedAdhanVoiceId))
                AdhanSoundMode.DEFAULT, AdhanSoundMode.SOFT -> PrayerAlertTestHelper.playShortTestTone(app, s.adhanSoundMode)
                AdhanSoundMode.SILENT_VIBRATE -> PrayerAlertTestHelper.triggerTestVibration(app)
            }
            val hasPerm = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                PrayerNotificationReceiver.postFallbackNotification(
                    context = app,
                    prayerId = "test_${prayer.id}",
                    prayerTitle = prayer.titleAr,
                    cityName = s.city.nameAr,
                    isPreAdhan = false,
                    preMinutes = 0,
                    soundMode = s.adhanSoundMode
                )
                _uiState.update {
                    it.copy(
                        statusMessage = if (s.adhanSoundMode == AdhanSoundMode.FULL_ADHAN) {
                            "تم تشغيل أذان ${prayer.titleAr} وإرسال إشعار تجريبي — اضغط مجدداً للإيقاف"
                        } else {
                            "تم إرسال إشعار تجريبي واختبار نغمة صلاة ${prayer.titleAr}"
                        }
                    )
                }
            } else {
                _uiState.update { it.copy(statusMessage = "تم تشغيل الصوت التجريبي — يرجى منح إذن الإشعارات لعرض التنبيه") }
            }
        }.onFailure { ex ->
            _uiState.update { it.copy(statusMessage = "تعذّر إجراء الاختبار: ${ex.message ?: "خطأ غير متوقع"}") }
        }
    }

    fun clearStatusMessage() { _uiState.update { it.copy(statusMessage = null) } }

    fun detectCoarseLocation() {
        if (_uiState.value.isLocatingGps) return
        _uiState.update { it.copy(isLocatingGps = true, statusMessage = null) }
        val app = getApplication<Application>()
        viewModelScope.launch {
            val location = withContext(Dispatchers.IO) { PrayerLocationResolver.readBestLastLocation(app) }
            if (location == null) {
                _uiState.update {
                    it.copy(
                        isLocatingGps = false,
                        statusMessage = "تعذّر الحصول على الموقع تلقائياً، يمكنك اختيار المدينة من القائمة."
                    )
                }
                return@launch
            }
            val city = withContext(Dispatchers.IO) {
                PrayerLocationResolver.resolveCityFromCoordinates(app, location.latitude, location.longitude)
            }
            prefs.setCity(city)
            _uiState.update { it.copy(isLocatingGps = false, statusMessage = "تم تحديث الموقع إلى ${city.nameAr}") }
        }
    }

    private fun recomputeForSettingsAndDay(settings: PrayerSettings, dayOffset: Int) {
        val cityTz = settings.city.resolveTimeZone()
        val cal = Calendar.getInstance(cityTz).apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
        val daily = PrayerTimesCalculator.calculateForDate(cal, settings.city.latitude, settings.city.longitude, settings.calculationMethod, settings.asrMethod)
        val draft = _uiState.value.copy(
            settings = settings, dayOffset = dayOffset,
            hijriDateAr = PrayerTimesCalculator.formatHijriDateAr(cal, settings.hijriOffsetDays),
            gregorianDateAr = PrayerTimesCalculator.formatGregorianDateAr(cal), dailyTimes = daily
        )
        refreshCountdown(draft, forceCommitUi = true)
    }

    private fun refreshCountdown(state: PrayerUiState, forceCommitUi: Boolean = false) {
        val now = System.currentTimeMillis()
        val entries = state.dailyTimes.entries
        val upcoming = entries.firstOrNull { it.timestampMillis > now } ?: run {
            val cal = Calendar.getInstance(state.settings.city.resolveTimeZone()).apply { add(Calendar.DAY_OF_YEAR, 1) }
            PrayerTimesCalculator.calculateForDate(cal, state.settings.city.latitude, state.settings.city.longitude, state.settings.calculationMethod, state.settings.asrMethod).entries.firstOrNull()
        }
        val lastPassedMandatory = entries.lastOrNull { it.prayer.isMandatoryPrayer && it.timestampMillis <= now }
        val iqamahText = lastPassedMandatory?.let { passed ->
            val remain = passed.timestampMillis + 15 * 60_000L - now
            if (remain in 1_000L..900_000L) {
                "إقامة ${passed.prayer.titleAr} خلال ${(remain / 60_000L).toInt().toString().padStart(2, '0')}:${((remain % 60_000L) / 1_000L).toInt().toString().padStart(2, '0')}"
            } else null
        }
        val prevTime = entries.lastOrNull { it.timestampMillis <= now }?.timestampMillis ?: (upcoming?.timestampMillis?.minus(6 * 3_600_000L) ?: now)
        val nextTime = upcoming?.timestampMillis ?: (now + 1L)
        val totalSpan = (nextTime - prevTime).coerceAtLeast(1L)
        val countdown = PrayerTimesCalculator.formatCountdown(nextTime - now)
        val progress = ((now - prevTime).coerceIn(0L, totalSpan).toFloat() / totalSpan.toFloat()).coerceIn(0f, 1f)
        _tickerState.value = PrayerTickerState(upcoming, countdown, iqamahText, progress)
        if (forceCommitUi || state.nextPrayer?.prayer != upcoming?.prayer) {
            _uiState.value = state.copy(nextPrayer = upcoming, countdownText = countdown, iqamahStatusText = iqamahText, intervalProgress = progress)
        }
    }

    override fun onCleared() {
        AppAudioCoordinator.unregister(AppAudioOwner.ADHAN_PREVIEW)
        stopAdhanPreview()
        super.onCleared()
    }
}
