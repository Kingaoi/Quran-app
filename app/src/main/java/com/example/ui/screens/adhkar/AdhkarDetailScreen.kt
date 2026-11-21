package com.example.ui.screens.adhkar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.theme.LocalAppTheme
import com.example.ui.util.copyTextToClipboard
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.AdhkarUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val cleanChapter = chapterTitle.trim()
    val items = remember(category, cleanChapter, uiState.itemsByCategory, uiState.customDuas) {
        uiState.itemsForScope(category, cleanChapter)
    }
    val safeInitialPage = remember(items.size, initialIndex) {
        initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    }
    val pagerState = rememberPagerState(initialPage = safeInitialPage, pageCount = { items.size.coerceAtLeast(1) })
    val currentItem = items.getOrNull(pagerState.currentPage)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val activeTheme = LocalAppTheme.current
    val displayTitle = cleanChapter.ifBlank { category.titleAr }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { onStopAudio() }
    }

    fun handleIncrement(item: DhikrItem): Boolean {
        val completed = onIncrementDhikr(item)
        if (completed && autoAdvanceDhikr && pagerState.currentPage < items.lastIndex) {
            scope.launch {
                delay(320L)
                val target = (pagerState.currentPage + 1).coerceAtMost(items.lastIndex)
                pagerState.animateScrollToPage(target)
            }
        }
        return completed
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { QuranSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AdhkarDetailTopBar(
                    title = displayTitle,
                    fontSizeSp = uiState.fontSizeSp,
                    onBack = onBack,
                    onAdjustFontSize = onAdjustFontSize,
                    onResetCategory = {
                        if (cleanChapter.isNotBlank()) onResetItems(items.map { it.id }) else onResetCategory(category)
                        scope.launch { snackbarHostState.showQuranToast("تم تصفير عداد $displayTitle") }
                    },
                    scrollBehavior = scrollBehavior
                )
                AdhkarDetailProgressStrip(currentIndex = pagerState.currentPage, totalCount = items.size)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            }
        },
        bottomBar = {
            if (currentItem != null) {
                AdhkarDetailBottomBar(
                    item = currentItem,
                    currentCount = uiState.countsById[currentItem.id] ?: 0,
                    hasPrevious = pagerState.currentPage > 0,
                    isLastPage = pagerState.currentPage >= items.lastIndex,
                    hapticEnabled = hapticEnabled,
                    onIncrement = { handleIncrement(currentItem) },
                    onReset = { onResetDhikr(currentItem.id) },
                    onCopy = {
                        copyTextToClipboard(context, currentItem.title, "${currentItem.arabicText}\n\n(${currentItem.reference})")
                        scope.launch { snackbarHostState.showQuranToast("تم نسخ الذكر") }
                    },
                    onShare = {
                        sharePlainText(
                            context = context,
                            chooserTitle = "مشاركة الذكر",
                            text = "${currentItem.title}\n\n${currentItem.arabicText}\n\n${currentItem.virtueOrNote} (${currentItem.reference})"
                        )
                    },
                    onShareImage = {
                        com.example.ui.util.ShareCardGenerator.shareStyledCard(
                            context = context,
                            headerLabel = currentItem.title,
                            bodyText = currentItem.arabicText,
                            footerLabel = currentItem.reference,
                            appTheme = activeTheme
                        )
                    },
                    onPrevious = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                    onNextOrFinish = {
                        if (pagerState.currentPage < items.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onBack()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.widthIn(max = 760.dp).fillMaxSize()
            ) { pageIndex ->
                val item = items[pageIndex]
                val cardSharedModifier = if (pageIndex == pagerState.currentPage) {
                    Modifier.quranSharedBounds(QuranSharedKey.AdhkarItemCard(item.id))
                } else Modifier
                AdhkarDetailPageContent(
                    item = item,
                    currentCount = uiState.countsById[item.id] ?: 0,
                    fontSizeSp = uiState.fontSizeSp,
                    isFavorite = item.id in uiState.favoriteIds,
                    hapticEnabled = hapticEnabled,
                    audioState = uiState.audioState,
                    onToggleFavorite = { onToggleFavorite(item.id) },
                    onTogglePlayAudio = { onTogglePlayAudio(item) },
                    onIncrement = { handleIncrement(item) },
                    cardModifier = cardSharedModifier
                )
            }
        }
    }
}
