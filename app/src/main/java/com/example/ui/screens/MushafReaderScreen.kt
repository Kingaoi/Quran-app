package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.example.QuranApplication
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.audio.PlaybackNotifActions
import com.example.data.audio.SurahAudioDownloadState
import com.example.data.local.AppTheme
import com.example.data.local.AyahBookmark
import com.example.data.local.MushafOfflineStorage
import com.example.data.local.MushafRasterCache
import com.example.domain.model.AyahItem
import com.example.domain.model.QuranReciters
import com.example.domain.model.ReciterOption
import com.example.domain.model.TafsirEdition
import com.example.domain.model.toAppDigits
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.domain.wird.WirdPlanSummary
import com.example.ui.components.MushafExpandedAudioSheet
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.screens.reader.AyahDetailCardType
import com.example.ui.screens.reader.MemorizationMaskState
import com.example.ui.screens.reader.MemorizationMaskStyle
import com.example.ui.screens.reader.MushafPagerViewport
import com.example.ui.screens.reader.MushafPredictivePrefetchEffect
import com.example.ui.screens.reader.MushafReaderDialogsHost
import com.example.ui.screens.reader.ReaderAutoScrollHost
import com.example.ui.screens.reader.ReaderAyahQuickBarHost
import com.example.ui.screens.reader.ReaderBottomChromeHost
import com.example.ui.screens.reader.ReaderTopChromeHost
import com.example.ui.screens.reader.rememberReaderAutoScrollState
import com.example.ui.screens.reader.resolveWirdReaderScope
import com.example.ui.theme.CreamOnBackgroundDark
import com.example.ui.theme.InkOnBackgroundLight
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MushafPaperNight
import com.example.ui.theme.MushafPaperSepia
import com.example.ui.viewmodel.AudioPlaylistActions
import com.example.ui.viewmodel.AyahTafsirUiState
import com.example.ui.viewmodel.QuranUiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MushafReaderScreen(
    uiState: QuranUiState, currentPage: Int, playbackState: AudioPlaybackState, offlineStorage: MushafOfflineStorage,
    bookmarks: List<AyahBookmark> = emptyList(), tafsirState: AyahTafsirUiState = AyahTafsirUiState(),
    surahAudioState: SurahAudioDownloadState = SurahAudioDownloadState(), highlightedSearchAyah: Pair<Int, Int>? = null,
    reciterCatalog: List<ReciterOption> = QuranReciters.combinedCatalog(), activeWirdPlan: WirdPlanSummary? = null,
    playlists: List<AudioPlaylist> = emptyList(), playlistActions: AudioPlaylistActions = AudioPlaylistActions(),
    openedFromWird: Boolean = false, targetWirdIndex: Int = -1,
    getAyahsForPage: (Int) -> List<AyahItem>, getPageForAyah: (Int, Int) -> Int? = { _, _ -> null },
    getMutashabihatCount: (Int, Int) -> Int = { _, _ -> 0 }, findMutashabihat: (AyahItem) -> List<MutashabihMatch> = { emptyList() },
    compareAyahWords: (AyahItem, AyahItem) -> List<AyahWordDiffSegment> = { _, _ -> emptyList() },
    getSvgModel: (Int) -> Any, getRawGithubSvgUrl: (Int) -> String,
    onPageScrolling: (Int) -> Unit, onPageSettled: (Int) -> Unit, onSelectSurah: (Int) -> Unit,
    onJumpToAyah: (Int, Int, Int) -> Unit = { page, _, _ -> onPageSettled(page) }, onToggleBookmark: (Int) -> Unit,
    onSaveAyahBookmark: (AyahBookmark) -> Unit = {}, onDeleteAyahBookmark: (String) -> Unit = {},
    onLoadTafsir: (Int, Int, TafsirEdition) -> Unit = { _, _, _ -> }, onCompleteActiveWird: () -> Unit = {}, onCompleteWirdAtIndex: (Int) -> Unit = {},
    onSetDimmerAlpha: (Float) -> Unit = {}, onSetKeepScreenOn: (Boolean) -> Unit = {},
    onPlayAyah: (Int, Int) -> Unit, onPlayPageFirstAyah: () -> Unit, onTogglePlayPause: () -> Unit,
    onNextAyah: () -> Unit, onPreviousAyah: () -> Unit, onToggleRepeat: () -> Unit,
    onCycleSpeed: () -> Unit = {}, onSetSleepTimerMinutes: (Int?) -> Unit = {}, onToggleStopAfterSurah: () -> Unit = {},
    onStartMemorizationLoop: (MemorizationLoopConfig) -> Unit = {}, onClearMemorizationLoop: () -> Unit = {},
    onDownloadSurahAudio: () -> Unit = {}, onCancelSurahAudio: () -> Unit = {}, onDeleteSurahAudio: () -> Unit = {},
    onSeekAudio: (Long) -> Unit, onStopAudio: () -> Unit, onSelectReciter: (String) -> Unit,
    playbackNotifActions: PlaybackNotifActions = PlaybackNotifActions(), onBack: (() -> Unit)? = null, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appImageLoader = context.imageLoader
    val rasterCache = remember(context) { (context.applicationContext as? QuranApplication)?.mushafRasterCache ?: MushafRasterCache(context) }

    var showControls by remember { mutableStateOf(true) }; var showAudioSheet by remember { mutableStateOf(false) }
    var showQuickJumpSheet by remember { mutableStateOf(false) }; var showPageJumpDialog by remember { mutableStateOf(false) }
    var showPageAyahsSheet by remember { mutableStateOf(false) }; var showDimmerDialog by remember { mutableStateOf(false) }
    var memLoopSeedAyah by remember { mutableStateOf<Pair<Int, Int>?>(null) }; var activeAyahCard by remember { mutableStateOf<Pair<AyahItem, AyahDetailCardType>?>(null) }
    var quickActionAyah by remember { mutableStateOf<AyahItem?>(null) }; var quickAddPlaylistAyah by remember { mutableStateOf<AyahItem?>(null) }
    var memHideMode by remember { mutableStateOf(false) }; var memMaskStyle by remember { mutableStateOf(MemorizationMaskStyle.FULL_BLANK) }
    var revealedAyahs by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }
    val autoScrollState = rememberReaderAutoScrollState()
    val isOverlayBlockingAutoScroll = memHideMode || showAudioSheet || showQuickJumpSheet || showPageJumpDialog || playbackState.isPlaying ||
        showPageAyahsSheet || showDimmerDialog || memLoopSeedAyah != null || activeAyahCard != null || quickActionAyah != null || quickAddPlaylistAyah != null
    val keepScreenOnEffective = uiState.settings.keepScreenOn || (autoScrollState.enabled && !autoScrollState.userPaused && !isOverlayBlockingAutoScroll)
    DisposableEffect(view, keepScreenOnEffective) { val prev = view.keepScreenOn; view.keepScreenOn = keepScreenOnEffective; onDispose { view.keepScreenOn = prev } }
    val overlayPredictiveState = rememberPredictiveBackState(enabled = showAudioSheet || quickActionAyah != null || quickAddPlaylistAyah != null) {
        when { quickAddPlaylistAyah != null -> quickAddPlaylistAyah = null; showAudioSheet -> showAudioSheet = false; else -> quickActionAyah = null }
    }
    val maskState = remember(memHideMode, memMaskStyle, revealedAyahs) { MemorizationMaskState(memHideMode, memMaskStyle, revealedAyahs) }
    val isPureWirdMode = openedFromWird && uiState.settings.wirdPureReadingMode
    var activeScopedWirdIndex by rememberSaveable(openedFromWird, targetWirdIndex) { mutableIntStateOf(targetWirdIndex) }
    val wirdScope = remember(openedFromWird, activeWirdPlan, activeScopedWirdIndex) { resolveWirdReaderScope(openedFromWird, activeWirdPlan, activeScopedWirdIndex) }
    val allowWirdBanner = com.example.ui.screens.reader.shouldShowWirdBannerInReader(openedFromWird, uiState.settings.hideWirdBannerOutsideWird)
    val visibleWirdPlan = if (allowWirdBanner) activeWirdPlan else null; val visibleWirdTarget = if (allowWirdBanner) wirdScope.boundaryPortion else null
    val showBottomWirdBanner = visibleWirdPlan != null && visibleWirdTarget != null && (!visibleWirdPlan.isCompleted || visibleWirdTarget.index < visibleWirdPlan.completedCount)
    LaunchedEffect(currentPage) { if (quickActionAyah != null && quickActionAyah?.pageNumber != currentPage) quickActionAyah = null }
    LaunchedEffect(isPureWirdMode) { if (isPureWirdMode) { quickActionAyah = null; quickAddPlaylistAyah = null; showAudioSheet = false; activeAyahCard = null } }
    val isCurrentPageBookmarked = uiState.settings.bookmarkedPages.contains(currentPage)
    val activePageAyahs = remember(currentPage, uiState.currentPageAyahs) { if (currentPage == uiState.currentPage) uiState.currentPageAyahs else getAyahsForPage(currentPage) }
    val revealedCountOnPage = remember(activePageAyahs, revealedAyahs) { activePageAyahs.count { (it.surahNumber to it.ayahNumber) in revealedAyahs } }
    val firstAyahOnCurrentPage = activePageAyahs.firstOrNull(); val currentSurahLabel = firstAyahOnCurrentPage?.surahNameAr ?: uiState.currentPageSurah?.nameAr ?: "الفاتحة"
    val isNightPaper = LocalAppTheme.current == AppTheme.DARK; val paperBg = if (isNightPaper) MushafPaperNight else MushafPaperSepia; val paperInk = if (isNightPaper) CreamOnBackgroundDark else InkOnBackgroundLight
    val markerColorKey = uiState.settings.mushafMarkerColorKey(isNightPaper)
    val markerAccentColor = if (isNightPaper) uiState.settings.ayahMarkerColor.nightPreviewColor else uiState.settings.ayahMarkerColor.lightPreviewColor
    val highlightAccentColor = uiState.settings.resolveAyahHighlightColor(isNightPaper, markerAccentColor)
    val cornerShiftProgress by animateFloatAsState(targetValue = if (showControls || showAudioSheet) 1f else 0f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 700f), label = "corner_shift")
    val toastBottomPadding by animateDpAsState(targetValue = (when { showAudioSheet && !isPureWirdMode -> 16.dp; showControls && isPureWirdMode && showBottomWirdBanner -> 98.dp; showControls && !isPureWirdMode && showBottomWirdBanner -> 196.dp; showControls && !isPureWirdMode -> 114.dp; else -> 16.dp }) + if (autoScrollState.enabled && !(showAudioSheet && !isPureWirdMode)) 72.dp else 0.dp, label = "reader_toast_offset")
    val surahsByNumber = remember(uiState.surahs) { uiState.surahs.associateBy { it.number } }
    val highlightedAyah = remember(playbackState.isActive, playbackState.currentSurah, playbackState.currentAyah, quickActionAyah, highlightedSearchAyah) {
        if (playbackState.isActive) playbackState.currentSurah to playbackState.currentAyah else quickActionAyah?.let { it.surahNumber to it.ayahNumber } ?: highlightedSearchAyah
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(paperBg)) {
        val isTwoPageSpread = uiState.settings.twoPageSpreadOnTablet && maxWidth >= 700.dp
        val pageCount = wirdScope.pageCount(isTwoPageSpread)
        val pagerState = rememberPagerState(initialPage = wirdScope.pageToIndex(currentPage, isTwoPageSpread), pageCount = { pageCount })
        val latestCurrentPage by rememberUpdatedState(currentPage); val latestWirdScope by rememberUpdatedState(wirdScope)
        val latestOnPageScrolling by rememberUpdatedState(onPageScrolling); val latestOnPageSettled by rememberUpdatedState(onPageSettled)

        LaunchedEffect(currentPage, isTwoPageSpread, wirdScope.startPage, wirdScope.endPage) {
            val idx = wirdScope.pageToIndex(currentPage, isTwoPageSpread)
            if (pagerState.currentPage != idx && !pagerState.isScrollInProgress) pagerState.scrollToPage(idx)
        }
        LaunchedEffect(playbackState.isActive, playbackState.currentSurah, playbackState.currentAyah, isTwoPageSpread, wirdScope.startPage, wirdScope.endPage) {
            if (playbackState.isActive) {
                val p = getPageForAyah(playbackState.currentSurah, playbackState.currentAyah)
                if (p != null && p in wirdScope.startPage..wirdScope.endPage) {
                    val idx = wirdScope.pageToIndex(p, isTwoPageSpread)
                    if (pagerState.currentPage != idx && !pagerState.isScrollInProgress) pagerState.animateScrollToPage(idx)
                }
            }
        }
        LaunchedEffect(pagerState, isTwoPageSpread, wirdScope.startPage, wirdScope.endPage) {
            launch { snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { idx ->
                val logical = latestWirdScope.indexToPage(idx, isTwoPageSpread)
                if (logical != latestCurrentPage && (!isTwoPageSpread || latestCurrentPage !in logical..(logical + 1))) latestOnPageScrolling(logical)
            } }
            snapshotFlow { pagerState.settledPage }.distinctUntilChanged().collect { idx ->
                val logical = latestWirdScope.indexToPage(idx, isTwoPageSpread)
                if (!isTwoPageSpread || latestCurrentPage !in logical..(logical + 1)) latestOnPageSettled(logical)
            }
        }

        val density = LocalDensity.current
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val (targetWidthPx, targetHeightPx) = remember(maxWidth, maxHeight, statusBarTop, navBarBottom, density) {
            val rawW = with(density) { maxWidth.roundToPx() }.coerceAtLeast(360)
            val rawH = (with(density) { maxHeight.roundToPx() } - with(density) { (statusBarTop + navBarBottom + 48.dp).roundToPx() }).coerceAtLeast(520)
            val scale = minOf(1f, 1800f / maxOf(rawW, rawH))
            (rawW * scale).toInt().coerceAtLeast(360) to (rawH * scale).toInt().coerceAtLeast(520)
        }

        MushafPredictivePrefetchEffect(
            activePageNum = currentPage, riwayah = uiState.settings.riwayah, isTwoPageSpread = isTwoPageSpread, svgVersion = uiState.svgVersion,
            markerColorKey = markerColorKey, ornamentHex = uiState.settings.resolveSvgOrnamentHex(isNightPaper),
            numberHex = uiState.settings.resolveSvgNumberHex(isNightPaper), surahBannerHex = uiState.settings.resolveSvgSurahBannerHex(isNightPaper),
            targetWidthPx = targetWidthPx, targetHeightPx = targetHeightPx, context = context, imageLoader = appImageLoader,
            rasterCache = rasterCache, offlineStorage = offlineStorage, getSvgModel = getSvgModel, getRawGithubSvgUrl = getRawGithubSvgUrl,
            scopedStartPage = wirdScope.startPage, scopedEndPage = wirdScope.endPage
        )
        MushafPagerViewport(
            pagerState = pagerState, isTwoPageSpread = isTwoPageSpread, isNightPaper = isNightPaper, riwayah = uiState.settings.riwayah,
            markerColorKey = markerColorKey, ornamentHex = uiState.settings.resolveSvgOrnamentHex(isNightPaper),
            numberHex = uiState.settings.resolveSvgNumberHex(isNightPaper), surahBannerHex = uiState.settings.resolveSvgSurahBannerHex(isNightPaper),
            markerAccentColor = markerAccentColor, fontSizeSp = uiState.settings.fontSizeSp, svgImageLoader = appImageLoader, rasterCache = rasterCache,
            offlineStorage = offlineStorage, targetWidthPx = targetWidthPx, targetHeightPx = targetHeightPx, paperBg = paperBg,
            paperInk = paperInk, subtleCornerInk = paperInk.copy(alpha = 0.56f), cornerShiftProgress = { cornerShiftProgress },
            bottomCornerShiftProgress = { if (!isPureWirdMode || showBottomWirdBanner) cornerShiftProgress else 0f }, highlightedAyah = highlightedAyah,
            highlightColor = highlightAccentColor, highlightStyle = uiState.settings.ayahHighlightStyle, highlightAlpha = uiState.settings.ayahHighlightAlpha,
            maskState = maskState, scopedStartPage = wirdScope.startPage, scopedEndPage = wirdScope.endPage, wirdBoundaryPortion = visibleWirdTarget,
            onToggleRevealAyah = { key -> revealedAyahs = if (key in revealedAyahs) revealedAyahs - key else revealedAyahs + key },
            getAyahsForPage = getAyahsForPage, getSvgModel = getSvgModel, getRawGithubSvgUrl = getRawGithubSvgUrl,
            onTapAyah = { tapped -> if (showAudioSheet) showAudioSheet = false else if (isPureWirdMode) showControls = !showControls else { activeAyahCard = null; quickActionAyah = if (quickActionAyah?.surahNumber == tapped.surahNumber && quickActionAyah?.ayahNumber == tapped.ayahNumber) null else tapped } },
            onLongPressAyah = { tapped -> if (!isPureWirdMode) { showAudioSheet = false; activeAyahCard = null; quickActionAyah = tapped } },
            onSingleTap = { if (showAudioSheet) showAudioSheet = false else if (quickActionAyah != null) quickActionAyah = null else showControls = !showControls }
        )

        if (uiState.settings.readerDimmerAlpha > 0.01f) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = uiState.settings.readerDimmerAlpha)))

        AnimatedVisibility(visible = showControls || (showAudioSheet && !isPureWirdMode), enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut(), modifier = Modifier.align(Alignment.TopCenter)) {
            ReaderTopChromeHost(
                isPureWirdMode = isPureWirdMode, activeWirdPlan = activeWirdPlan, targetWird = wirdScope.boundaryPortion,
                showAudioSheet = showAudioSheet, isCurrentPageBookmarked = isCurrentPageBookmarked,
                isDimmerActive = uiState.settings.readerDimmerAlpha > 0.02f, isMemorizationHideMode = memHideMode,
                memorizationMaskStyle = memMaskStyle, revealedCountOnPage = revealedCountOnPage, totalCountOnPage = activePageAyahs.size,
                onBack = onBack, onOpenDimmer = { showDimmerDialog = true },
                onToggleMemorizationHideMode = { memHideMode = !memHideMode; quickActionAyah = null; activeAyahCard = null; showAudioSheet = false; if (!memHideMode) revealedAyahs = emptySet() },
                onToggleMemorizationMaskStyle = { memMaskStyle = if (memMaskStyle == MemorizationMaskStyle.FULL_BLANK) MemorizationMaskStyle.FIRST_WORD_HINT else MemorizationMaskStyle.FULL_BLANK },
                onRevealNextMemorization = { activePageAyahs.firstOrNull { (it.surahNumber to it.ayahNumber) !in revealedAyahs }?.let { revealedAyahs = revealedAyahs + (it.surahNumber to it.ayahNumber) } },
                onToggleAudioSheet = { quickActionAyah = null; activeAyahCard = null; showAudioSheet = !showAudioSheet },
                onToggleBookmark = { onToggleBookmark(currentPage); scope.launch { snackbarHostState.showQuranToast(if (isCurrentPageBookmarked) "تمت إزالة العلامة المرجعية" else "تم حفظ الصفحة ${currentPage.toAppDigits()} في العلامات") } },
                onRevealAllMemorization = { revealedAyahs = revealedAyahs + activePageAyahs.map { it.surahNumber to it.ayahNumber } },
                onHideAllMemorization = { revealedAyahs = revealedAyahs - activePageAyahs.map { it.surahNumber to it.ayahNumber }.toSet() }
            )
        }

        AnimatedVisibility(visible = showControls && (!showAudioSheet || isPureWirdMode) && (!isPureWirdMode || showBottomWirdBanner), enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(), modifier = Modifier.align(Alignment.BottomCenter)) {
            ReaderBottomChromeHost(
                isPureWirdMode = isPureWirdMode, currentPage = currentPage, currentSurahLabel = currentSurahLabel,
                firstAyahNumber = firstAyahOnCurrentPage?.ayahNumber ?: 1, minPage = wirdScope.startPage, maxPage = wirdScope.endPage,
                activeWirdPlan = visibleWirdPlan, targetWird = visibleWirdTarget,
                onCompleteWird = {
                    val target = wirdScope.boundaryPortion
                    if (target == null || target.index == activeWirdPlan?.currentWird?.index) onCompleteActiveWird() else onCompleteWirdAtIndex(target.index)
                    val finishedAll = target != null && activeWirdPlan != null && (target.index + 1) >= activeWirdPlan.totalWirds
                    scope.launch { snackbarHostState.showQuranToast(if (finishedAll) "ما شاء الله! أتممت الختمة المباركة بحمد الله" else "تقبل الله! تم إتمام وردك الحالي") }
                },
                onAdvanceToNextWird = {
                    val nextWird = wirdScope.boundaryPortion?.let { activeWirdPlan?.allWirds?.getOrNull(it.index + 1) } ?: activeWirdPlan?.currentWird
                    if (nextWird != null) { activeScopedWirdIndex = nextWird.index; onPageSettled(nextWird.startPage); scope.launch { pagerState.scrollToPage(0) } }
                },
                onOpenPageAyahs = { showPageAyahsSheet = true }, onOpenQuickJump = { showQuickJumpSheet = true }, onOpenPageJumpDialog = { showPageJumpDialog = true },
                onStepPreviousPage = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                onStepNextPage = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(pageCount - 1)) } },
                onScrubToPage = { target -> onPageSettled(target); scope.launch { pagerState.scrollToPage(wirdScope.pageToIndex(target, isTwoPageSpread)) } },
                autoScrollEnabled = autoScrollState.enabled, onToggleAutoScroll = autoScrollState::toggle
            )
        }

        ReaderAutoScrollHost(
            autoScrollState = autoScrollState, isBlockedByOverlay = isOverlayBlockingAutoScroll, isTwoPageSpread = isTwoPageSpread,
            pagerState = pagerState, pageCount = pageCount, showControls = showControls, isPureWirdMode = isPureWirdMode,
            showBottomWirdBanner = showBottomWirdBanner, showAudioSheet = showAudioSheet,
            onReachedEnd = { scope.launch { snackbarHostState.showQuranToast("وصلتَ إلى نهاية النطاق الحالي") } },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        ReaderAyahQuickBarHost(
            ayah = quickActionAyah, visible = !isPureWirdMode && quickActionAyah != null && !showAudioSheet && activeAyahCard == null,
            accentColor = highlightAccentColor, controlsVisible = showControls, hasBottomWirdBanner = showBottomWirdBanner, playbackState = playbackState,
            bookmarks = bookmarks, getMutashabihatCount = getMutashabihatCount, context = context, scope = scope,
            snackbarHostState = snackbarHostState, onPlayAyah = onPlayAyah, onTogglePlayPause = onTogglePlayPause, onSaveBookmark = onSaveAyahBookmark,
            onDeleteBookmark = onDeleteAyahBookmark, onOpenMemorizationLoop = { quickActionAyah = null; memLoopSeedAyah = it.surahNumber to it.ayahNumber },
            onAddToPlaylist = { quickActionAyah = null; quickAddPlaylistAyah = it },
            onOpenDedicatedCard = { ayah, type -> activeAyahCard = ayah to type }, onDismiss = { quickActionAyah = null },
            modifier = Modifier.align(Alignment.BottomCenter).predictiveBackTransform(overlayPredictiveState, maxCornerRadius = 22.dp, maxScaleDrop = 0.04f, maxShiftDp = 12.dp)
        )
        QuranSnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = toastBottomPadding))
        MushafExpandedAudioSheet(
            visible = !isPureWirdMode && showAudioSheet, playbackState = playbackState, currentSurahName = currentSurahLabel, currentPage = currentPage,
            activeRiwayah = uiState.settings.riwayah, reciterCatalog = reciterCatalog, surahAudioState = surahAudioState,
            onTogglePlayPause = onTogglePlayPause, onPlayPageFirstAyah = onPlayPageFirstAyah, onNextAyah = onNextAyah,
            onPreviousAyah = onPreviousAyah, onToggleRepeat = onToggleRepeat, onCycleSpeed = onCycleSpeed,
            onSetSleepTimerMinutes = onSetSleepTimerMinutes, onToggleStopAfterSurah = onToggleStopAfterSurah,
            onOpenMemorizationLoop = { memLoopSeedAyah = if (playbackState.isActive) playbackState.currentSurah to playbackState.currentAyah else (firstAyahOnCurrentPage?.surahNumber ?: 1) to (firstAyahOnCurrentPage?.ayahNumber ?: 1) },
            onDownloadSurahAudio = onDownloadSurahAudio, onCancelSurahAudio = onCancelSurahAudio, onDeleteSurahAudio = onDeleteSurahAudio,
            onSeek = onSeekAudio, onStopAudio = onStopAudio, onSelectReciter = onSelectReciter,
            playlists = playlists, surahsByNumber = surahsByNumber, playlistActions = playlistActions,
            playbackNotifConfig = uiState.settings.playbackNotificationConfig, playbackNotifActions = playbackNotifActions,
            modifier = Modifier.align(Alignment.BottomCenter).predictiveBackTransform(overlayPredictiveState, maxCornerRadius = 24.dp, maxScaleDrop = 0.04f, maxShiftDp = 12.dp)
        )
        MushafReaderDialogsHost(
            uiState = uiState, currentPage = currentPage, currentSurahLabel = currentSurahLabel, playbackState = playbackState, activePageAyahs = activePageAyahs,
            bookmarks = bookmarks, tafsirState = tafsirState, currentLoop = playbackState.memorizationLoop, totalAyahsFallback = playbackState.totalAyahsInSurah,
            markerAccentColor = markerAccentColor, showQuickJumpSheet = showQuickJumpSheet, showPageAyahsSheet = showPageAyahsSheet,
            showPageJumpDialog = showPageJumpDialog, showDimmerDialog = showDimmerDialog, memLoopSeedAyah = memLoopSeedAyah, activeAyahCard = activeAyahCard,
            context = context, scope = scope, snackbarHostState = snackbarHostState, getMutashabihatCount = getMutashabihatCount,
            findMutashabihat = findMutashabihat, compareAyahWords = compareAyahWords, onSelectSurah = onSelectSurah,
            onJumpToPage = { page -> val p = page.coerceIn(wirdScope.startPage, wirdScope.endPage); onPageSettled(p); scope.launch { pagerState.scrollToPage(wirdScope.pageToIndex(p, isTwoPageSpread)) } },
            onJumpToAyah = { page, surah, ayahNum -> val p = page.coerceIn(wirdScope.startPage, wirdScope.endPage); onJumpToAyah(p, surah, ayahNum); quickActionAyah = null; scope.launch { pagerState.scrollToPage(wirdScope.pageToIndex(p, isTwoPageSpread)) } },
            onPlayAyah = onPlayAyah, onTogglePlayPause = onTogglePlayPause, onLoadTafsir = onLoadTafsir, onSaveBookmark = onSaveAyahBookmark, onDeleteBookmark = onDeleteAyahBookmark,
            onSetDimmerAlpha = onSetDimmerAlpha, onSetKeepScreenOn = onSetKeepScreenOn, onStartMemorizationLoop = onStartMemorizationLoop,
            onClearMemorizationLoop = onClearMemorizationLoop, onSelectAyahInQuickBar = { quickActionAyah = it },
            onOpenPageWaqfGuide = { activePageAyahs.firstOrNull()?.let { anchor -> activeAyahCard = anchor to AyahDetailCardType.WAQF_PAGE_GUIDE } },
            onDismissAyahCard = { activeAyahCard = null }, onDismissQuickJump = { showQuickJumpSheet = false },
            onDismissPageAyahs = { showPageAyahsSheet = false }, onDismissPageJumpDialog = { showPageJumpDialog = false },
            onDismissDimmerDialog = { showDimmerDialog = false }, onDismissMemLoopDialog = { memLoopSeedAyah = null },
            playlists = playlists, surahsByNumber = surahsByNumber, playlistActions = playlistActions,
            quickAddAyahToPlaylist = quickAddPlaylistAyah, onDismissQuickAddPlaylist = { quickAddPlaylistAyah = null }
        )
    }
}
