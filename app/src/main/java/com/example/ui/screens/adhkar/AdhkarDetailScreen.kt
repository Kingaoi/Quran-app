package com.example.ui.screens.adhkar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.theme.LocalAppTheme
import com.example.ui.util.ShareCardGenerator
import com.example.ui.util.copyTextToClipboard
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.AdhkarUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdhkarDetailScreen(
    category: DhikrCategory,
    uiState: AdhkarUiState,
    chapterTitle: String = "",
    initialIndex: Int = 0,
    autoAdvanceDhikr: Boolean = false,
    hapticEnabled: Boolean = true,
    onIncrementDhikr: (DhikrItem) -> Boolean,
    onResetDhikr: (String) -> Unit,
    onResetCategory: (DhikrCategory) -> Unit,
    onResetItems: (List<String>) -> Unit = { onResetCategory(category) },
    onAdjustFontSize: (Float) -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onTogglePlayAudio: (DhikrItem) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cleanChapter = chapterTitle.trim()
    val items = remember(category, cleanChapter, uiState.itemsByCategory, uiState.customDuas) {
        uiState.itemsForScope(category, cleanChapter)
    }
    val safeInitialPage = remember(items.size, initialIndex) {
        initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    }
    val totalPagerPages = if (items.isEmpty()) 1 else items.size + 1
    val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { totalPagerPages })
    val currentItem = items.getOrNull(pagerState.currentPage)
    val activeTheme = LocalAppTheme.current
    val displayTitle = cleanChapter.ifBlank { category.titleAr }
    val completedItemsCount = remember(items, uiState.countsById) {
        items.count { (uiState.countsById[it.id] ?: 0) >= it.targetCount }
    }
    val firstIncompleteIndex = remember(items, uiState.countsById) {
        items.indexOfFirst { (uiState.countsById[it.id] ?: 0) < it.targetCount }
    }

    var topExpanded by remember { mutableStateOf(true) }
    var bottomExpanded by remember { mutableStateOf(true) }

    val statusTopInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var topExpandedHeightPx by remember { mutableIntStateOf(0) }
    var bottomExpandedHeightPx by remember { mutableIntStateOf(0) }

    val topClearance by animateDpAsState(
        targetValue = if (topExpandedHeightPx > 0) {
            with(density) { topExpandedHeightPx.toDp() } + 12.dp
        } else {
            statusTopInset + 124.dp
        },
        label = "adhkar_top_clearance"
    )
    val bottomClearance by animateDpAsState(
        targetValue = if (items.isEmpty()) {
            navBottomInset + 16.dp
        } else if (bottomExpandedHeightPx > 0) {
            with(density) { bottomExpandedHeightPx.toDp() } + 12.dp
        } else {
            navBottomInset + 148.dp
        },
        label = "adhkar_bottom_clearance"
    )

    LaunchedEffect(pagerState.currentPage) {
        topExpanded = true
        bottomExpanded = true
    }

    DisposableEffect(Unit) {
        onDispose { onStopAudio() }
    }

    fun handleIncrement(item: DhikrItem): Boolean {
        val completed = onIncrementDhikr(item)
        if (completed && autoAdvanceDhikr && pagerState.currentPage < items.size) {
            scope.launch {
                delay(320L)
                val target = (pagerState.currentPage + 1).coerceAtMost(items.size)
                pagerState.animateScrollToPage(target)
            }
        }
        return completed
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .align(Alignment.Center)
        ) { pageIndex ->
            val pagePadding = PaddingValues(start = 16.dp, end = 16.dp, top = topClearance, bottom = bottomClearance)
            if (pageIndex < items.size) {
                val item = items[pageIndex]
                val cardSharedModifier = if (pageIndex == pagerState.currentPage) {
                    Modifier.quranSharedBounds(QuranSharedKey.AdhkarItemCard(item.id))
                } else Modifier
                AdhkarDetailPageContent(
                    item = item,
                    currentCount = uiState.countsById[item.id] ?: 0,
                    fontSizeSp = uiState.fontSizeSp,
                    isCurrentPage = pageIndex == pagerState.currentPage,
                    isImmersiveCollapsed = !topExpanded && !bottomExpanded,
                    hapticEnabled = hapticEnabled,
                    onIncrement = { handleIncrement(item) },
                    onImmersiveChange = { collapsed ->
                        topExpanded = !collapsed
                        bottomExpanded = !collapsed
                    },
                    contentPadding = pagePadding,
                    cardModifier = cardSharedModifier
                )
            } else if (items.isNotEmpty()) {
                AdhkarThankYouPageContent(
                    sectionTitle = displayTitle,
                    completedCount = completedItemsCount,
                    totalItemsCount = items.size,
                    hasIncompleteItems = firstIncompleteIndex >= 0,
                    onJumpToIncomplete = {
                        scope.launch { pagerState.animateScrollToPage(firstIncompleteIndex) }
                    },
                    onRestartSection = {
                        if (cleanChapter.isNotBlank()) onResetItems(items.map { it.id }) else onResetCategory(category)
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                            snackbarHostState.showQuranToast(context.getString(R.string.adhkar_section_reset_toast, displayTitle))
                        }
                    },
                    contentPadding = pagePadding
                )
            }
        }

        AdhkarDetailTopCard(
            title = displayTitle,
            currentIndex = pagerState.currentPage.coerceAtMost((items.size - 1).coerceAtLeast(0)),
            totalCount = items.size,
            fontSizeSp = uiState.fontSizeSp,
            expanded = topExpanded,
            onExpandedChange = { topExpanded = it },
            onBack = onBack,
            onAdjustFontSize = onAdjustFontSize,
            onResetCategory = {
                if (cleanChapter.isNotBlank()) onResetItems(items.map { it.id }) else onResetCategory(category)
                scope.launch { snackbarHostState.showQuranToast(context.getString(R.string.adhkar_section_reset_toast, displayTitle)) }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { if (topExpanded) topExpandedHeightPx = it.height }
        )

        if (currentItem != null) {
            AdhkarDetailBottomBar(
                item = currentItem,
                currentCount = uiState.countsById[currentItem.id] ?: 0,
                hasPrevious = pagerState.currentPage > 0,
                isLastPage = false,
                expanded = bottomExpanded,
                onExpandedChange = { bottomExpanded = it },
                isFavorite = currentItem.id in uiState.favoriteIds,
                audioState = uiState.audioState,
                hapticEnabled = hapticEnabled,
                onToggleFavorite = { onToggleFavorite(currentItem.id) },
                onTogglePlayAudio = { onTogglePlayAudio(currentItem) },
                onIncrement = { handleIncrement(currentItem) },
                onReset = { onResetDhikr(currentItem.id) },
                onCopy = {
                    copyTextToClipboard(context, currentItem.title, "${currentItem.arabicText}\n\n(${currentItem.reference})")
                    scope.launch { snackbarHostState.showQuranToast(context.getString(R.string.adhkar_copied_toast)) }
                },
                onShare = {
                    sharePlainText(
                        context = context,
                        chooserTitle = context.getString(R.string.adhkar_share_cd),
                        text = "${currentItem.title}\n\n${currentItem.arabicText}\n\n${currentItem.virtueOrNote} (${currentItem.reference})"
                    )
                },
                onShareImage = {
                    ShareCardGenerator.shareStyledCard(
                        context = context,
                        headerLabel = currentItem.title,
                        bodyText = currentItem.arabicText,
                        footerLabel = currentItem.reference,
                        appTheme = activeTheme
                    )
                },
                onPrevious = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                onNextOrFinish = {
                    scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(items.size)) }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { if (bottomExpanded) bottomExpandedHeightPx = it.height }
            )
        } else if (items.isNotEmpty()) {
            AdhkarThankYouBottomBar(
                onPrevious = {
                    scope.launch { pagerState.animateScrollToPage((items.size - 1).coerceAtLeast(0)) }
                },
                onFinish = onBack,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { if (bottomExpanded) bottomExpandedHeightPx = it.height }
            )
        }

        QuranSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomClearance)
        )
    }
}
