package com.example.ui.screens.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.QuranRepository
import com.example.domain.model.DhikrCategory
import com.example.domain.model.DhikrItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.quranSharedBounds
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.theme.quranColors
import com.example.ui.viewmodel.AdhkarUiState
import kotlinx.coroutines.launch

private enum class AdhkarBrowseMode { CHAPTERS, ALL_ITEMS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarCategoryBrowseScreen(
    category: DhikrCategory,
    uiState: AdhkarUiState,
    hapticEnabled: Boolean = true,
    onIncrementDhikr: (DhikrItem) -> Boolean,
    onResetCategory: (DhikrCategory) -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onTogglePlayAudio: (DhikrItem) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onOpenReader: (chapterTitle: String, initialIndex: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val items = remember(category, uiState.itemsByCategory, uiState.customDuas) {
        uiState.itemsForCategory(category)
    }
    val subChapters = remember(items) { uiState.subChaptersForCategory(category) }
    val isMultiChapter = subChapters.size > 1 && !category.isSequentialLitany

    var localQuery by remember(category) { mutableStateOf("") }
    var browseMode by remember(category) {
        mutableStateOf(if (isMultiChapter) AdhkarBrowseMode.CHAPTERS else AdhkarBrowseMode.ALL_ITEMS)
    }
    var expandedChapters by remember(category) { mutableStateOf<Set<String>>(emptySet()) }

    DisposableEffect(Unit) { onDispose { onStopAudio() } }
    val searchPredictiveBack = rememberPredictiveBackState(enabled = localQuery.isNotBlank()) { localQuery = "" }

    val completedCount = remember(items, uiState.countsById) {
        items.count { (uiState.countsById[it.id] ?: 0) >= it.targetCount }
    }
    val firstIncompleteIdx = remember(items, uiState.countsById) {
        items.indexOfFirst { (uiState.countsById[it.id] ?: 0) < it.targetCount }.takeIf { it >= 0 } ?: 0
    }

    val normalizedQuery = remember(localQuery) { QuranRepository.normalizeArabic(localQuery.trim()) }
    val filteredItems = remember(items, normalizedQuery) {
        if (normalizedQuery.isBlank()) items
        else items.filter { item ->
            QuranRepository.normalizeArabic(item.title).contains(normalizedQuery) ||
                QuranRepository.normalizeArabic(item.arabicText).contains(normalizedQuery) ||
                QuranRepository.normalizeArabic(item.resolvedChapterTitle()).contains(normalizedQuery)
        }
    }
    val filteredChapters = remember(subChapters, normalizedQuery) {
        if (normalizedQuery.isBlank()) subChapters
        else subChapters.filter { group ->
            QuranRepository.normalizeArabic(group.chapterTitle).contains(normalizedQuery) ||
                group.items.any { item ->
                    QuranRepository.normalizeArabic(item.title).contains(normalizedQuery) ||
                        QuranRepository.normalizeArabic(item.arabicText).contains(normalizedQuery)
                }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("adhkar_browse_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = qc.bg,
        snackbarHost = { QuranSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = qc.bg, titleContentColor = qc.text),
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface, contentColor = qc.text),
                            modifier = Modifier.padding(start = 6.dp).size(48.dp).testTag("adhkar_browse_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع إلى الأذكار")
                        }
                    },
                    title = {
                        Text(
                            text = category.titleAr,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        FilledTonalIconButton(
                            onClick = {
                                onResetCategory(category)
                                scope.launch { snackbarHostState.showQuranToast("تم تصفير عداد ${category.titleAr}") }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.primary),
                            modifier = Modifier.padding(end = 6.dp).size(48.dp).testTag("adhkar_browse_reset_button")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = "تصفير عداد القسم")
                        }
                    }
                )
                HorizontalDivider(color = qc.border.copy(alpha = 0.6f))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).navigationBarsPadding(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .predictiveBackTransform(searchPredictiveBack, maxCornerRadius = 20.dp, maxScaleDrop = 0.03f, maxShiftDp = 12.dp)
                    .testTag("adhkar_browse_list")
            ) {
                item(key = "hero") {
                    AdhkarBrowseHeroBanner(
                        category = category,
                        completedCount = completedCount,
                        totalCount = items.size,
                        subChapterCount = subChapters.size,
                        firstIncompleteIndex = firstIncompleteIdx,
                        onStartSequence = { idx -> onOpenReader("", idx) }
                    )
                }

                item(key = "search_box") {
                    OutlinedTextField(
                        value = localQuery,
                        onValueChange = { localQuery = it },
                        singleLine = true,
                        placeholder = { Text("ابحث داخل ${category.titleAr}...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = qc.primary) },
                        trailingIcon = {
                            if (localQuery.isNotEmpty()) {
                                IconButton(onClick = { localQuery = "" }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = qc.surface,
                            unfocusedContainerColor = qc.surface,
                            focusedBorderColor = qc.primary,
                            unfocusedBorderColor = qc.border
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("adhkar_browse_search")
                    )
                }

                if (isMultiChapter) {
                    item(key = "mode_tabs") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                AdhkarBrowseMode.CHAPTERS to "الأنواع (${filteredChapters.size.toAppDigits(useWestern)})",
                                AdhkarBrowseMode.ALL_ITEMS to "كل الأذكار (${filteredItems.size.toAppDigits(useWestern)})"
                            ).forEach { (mode, label) ->
                                val selected = browseMode == mode
                                Surface(
                                    onClick = { browseMode = mode },
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) qc.selected else qc.surface,
                                    border = BorderStroke(1.dp, if (selected) qc.selected else qc.border),
                                    modifier = Modifier.testTag("adhkar_browse_mode_${mode.name.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) qc.onSelected else qc.textMuted,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (browseMode == AdhkarBrowseMode.CHAPTERS && isMultiChapter) {
                    if (filteredChapters.isEmpty()) {
                        item(key = "empty_chapters") { EmptyBrowseHint() }
                    } else {
                        itemsIndexed(items = filteredChapters, key = { idx, grp -> "ch_${idx}_${grp.chapterTitle}" }) { idx, grp ->
                            val expanded = grp.chapterTitle in expandedChapters
                            AdhkarSubChapterCard(
                                index = idx,
                                categoryId = category.id,
                                group = grp,
                                countsById = uiState.countsById,
                                favoriteIds = uiState.favoriteIds,
                                audioState = uiState.audioState,
                                hapticEnabled = hapticEnabled,
                                isExpanded = expanded,
                                onToggleExpand = {
                                    expandedChapters = if (expanded) expandedChapters - grp.chapterTitle else expandedChapters + grp.chapterTitle
                                },
                                onOpenChapterReader = { onOpenReader(grp.chapterTitle, 0) },
                                onOpenItemInChapterReader = { itemIdx -> onOpenReader(grp.chapterTitle, itemIdx) },
                                onIncrementItem = onIncrementDhikr,
                                onToggleFavorite = onToggleFavorite,
                                onTogglePlayAudio = onTogglePlayAudio
                            )
                        }
                    }
                } else {
                    if (filteredItems.isEmpty()) {
                        item(key = "empty_items") { EmptyBrowseHint() }
                    } else {
                        items(items = filteredItems, key = { it.id }) { item ->
                            val itemIdx = items.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
                            val targetChapter = if (isMultiChapter) item.resolvedChapterTitle() else ""
                            val scopedIdx = if (targetChapter.isNotBlank()) {
                                uiState.itemsForScope(category, targetChapter).indexOfFirst { it.id == item.id }.coerceAtLeast(0)
                            } else itemIdx
                            AdhkarBrowseItemCard(
                                item = item,
                                currentCount = uiState.countsById[item.id] ?: 0,
                                isFavorite = item.id in uiState.favoriteIds,
                                audioState = uiState.audioState,
                                hapticEnabled = hapticEnabled,
                                showChapterTag = isMultiChapter,
                                onIncrement = { onIncrementDhikr(item) },
                                onToggleFavorite = { onToggleFavorite(item.id) },
                                onTogglePlayAudio = { onTogglePlayAudio(item) },
                                onOpenReader = { onOpenReader(targetChapter, scopedIdx) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBrowseHint() {
    val qc = MaterialTheme.quranColors
    Surface(shape = RoundedCornerShape(18.dp), color = qc.surface, border = BorderStroke(1.dp, qc.border), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "لا توجد أذكار مطابقة لبحثك في هذا القسم.",
            style = MaterialTheme.typography.bodyMedium,
            color = qc.textMuted,
            modifier = Modifier.padding(18.dp)
        )
    }
}
