package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ReadingStats
import com.example.domain.model.DhikrCategory
import com.example.ui.components.QuranSnackbarHost
import com.example.ui.components.showQuranToast
import com.example.ui.screens.adhkar.AdhkarCategoriesHeader
import com.example.ui.screens.adhkar.AdhkarCategoryCard
import com.example.ui.screens.adhkar.AdhkarDailyHeroCard
import com.example.ui.screens.adhkar.AsmaAllahBottomSheet
import com.example.ui.screens.adhkar.AsmaAllahEntryCard
import com.example.ui.screens.adhkar.TasbihBottomSheet
import com.example.ui.theme.LocalFloatingNavBottomPadding
import com.example.ui.viewmodel.AdhkarUiState
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun AdhkarScreen(
    uiState: AdhkarUiState,
    stats: ReadingStats = ReadingStats(),
    isTabActive: Boolean = true,
    onOpenCategory: (DhikrCategory) -> Unit,
    onOpenDhikrItem: (com.example.domain.model.DhikrItem) -> Unit = { onOpenCategory(it.category) },
    onRefreshSource: () -> Unit = {},
    onClearSyncStatus: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onToggleFavoritesOnly: () -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onAddCustomDua: (title: String, arabicText: String, targetCount: Int, note: String) -> Unit = { _, _, _, _ -> },
    onDeleteCustomDua: (String) -> Unit = {},
    onTogglePlayAudio: (com.example.domain.model.DhikrItem) -> Unit = {},
    onIncrementTasbih: () -> Unit = {},
    onAddCustomTasbihPhrase: (String) -> Unit = {},
    onRemoveCustomTasbihPhrase: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bottomClearance = LocalFloatingNavBottomPadding.current
    val scope = rememberCoroutineScope()
    var showTasbihSheet by remember { mutableStateOf(false) }
    var showCustomDuaDialog by remember { mutableStateOf(false) }
    var showAsmaSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = isTabActive && (uiState.searchQuery.isNotBlank() || uiState.favoritesOnly)) {
        if (uiState.searchQuery.isNotBlank()) onSearchQueryChange("") else onToggleFavoritesOnly()
    }

    LaunchedEffect(isTabActive, uiState.syncStatusMessage) {
        if (!isTabActive) return@LaunchedEffect
        uiState.syncStatusMessage?.takeIf { it.isNotBlank() }?.let { msg ->
            snackbarHostState.showQuranToast(msg)
            onClearSyncStatus()
        }
    }

    val allItems = remember(uiState.itemsByCategory, uiState.customDuas) { uiState.allItems }
    val totalDhikrCount = allItems.size
    val completedDhikrCount = remember(uiState.countsById, allItems) {
        allItems.count { (uiState.countsById[it.id] ?: 0) >= it.targetCount }
    }
    val suggestedCategory = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour in 4..11 -> DhikrCategory.MORNING
            hour in 12..14 -> DhikrCategory.AFTER_PRAYER
            hour in 15..20 -> DhikrCategory.EVENING
            else -> DhikrCategory.SLEEP
        }
    }

    val isSearching = uiState.searchQuery.trim().length >= 2
    val filteredList = remember(isSearching, uiState.favoritesOnly, uiState.searchQuery, uiState.favoriteIds, allItems) {
        when {
            isSearching -> uiState.searchResults
            uiState.favoritesOnly -> uiState.favoriteItems
            else -> emptyList()
        }
    }

    val featuredCategories = remember { listOf(DhikrCategory.MORNING, DhikrCategory.EVENING) }
    val otherCategories = remember { DhikrCategory.entries.filterNot { it in featuredCategories } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            QuranSnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = bottomClearance))
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 680.dp
            val columnCount = if (isWide) 3 else 2

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp + bottomClearance),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(max = if (isWide) 920.dp else 760.dp).fillMaxSize().statusBarsPadding()
            ) {
                item(key = "top_bar", span = { GridItemSpan(maxLineSpan) }) {
                    AdhkarCategoriesHeader(
                        completedTotal = completedDhikrCount,
                        totalCount = totalDhikrCount,
                        isHisnSynced = uiState.isHisnSynced,
                        isSyncingSource = uiState.isSyncingSource,
                        onRefreshSource = onRefreshSource,
                        onOpenTasbih = { showTasbihSheet = true }
                    )
                }

                item(key = "search_filter_bar", span = { GridItemSpan(maxLineSpan) }) {
                    com.example.ui.screens.adhkar.AdhkarSearchAndFilterBar(
                        searchQuery = uiState.searchQuery,
                        favoritesOnly = uiState.favoritesOnly,
                        favoritesCount = uiState.favoriteIds.size,
                        onSearchQueryChange = onSearchQueryChange,
                        onToggleFavoritesOnly = onToggleFavoritesOnly,
                        onOpenAddCustomDua = { showCustomDuaDialog = true }
                    )
                }

                if (isSearching || uiState.favoritesOnly) {
                    item(key = "filtered_label", span = { GridItemSpan(maxLineSpan) }) {
                        val title = when {
                            isSearching -> "نتائج البحث (${filteredList.size})"
                            else -> "الأذكار والأدعية المفضلة (${filteredList.size})"
                        }
                        AdhkarSectionLabel(title = title)
                    }
                    if (filteredList.isEmpty()) {
                        item(key = "empty_filtered", span = { GridItemSpan(maxLineSpan) }) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isSearching) "لا توجد نتائج مطابقة لبحثك" else "لم تُضف أذكاراً إلى المفضلة بعد، اضغط القلب داخل أي ذكر لإضافته.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    } else {
                        items(items = filteredList, key = { "filtered_${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { item ->
                            com.example.ui.screens.adhkar.AdhkarSearchResultCard(
                                item = item,
                                isFavorite = item.id in uiState.favoriteIds,
                                audioState = uiState.audioState,
                                onOpenCategory = { onOpenDhikrItem(item) },
                                onToggleFavorite = { onToggleFavorite(item.id) },
                                onTogglePlayAudio = { onTogglePlayAudio(item) },
                                onDeleteCustom = if (item.isCustom) ({ onDeleteCustomDua(item.id) }) else null
                            )
                        }
                    }
                } else {
                    item(key = "daily_hero", span = { GridItemSpan(maxLineSpan) }) {
                        AdhkarDailyHeroCard(
                            completedTotal = completedDhikrCount, totalCount = totalDhikrCount, stats = stats,
                            suggestedCategory = suggestedCategory, onOpenSuggestedCategory = { onOpenCategory(suggestedCategory) },
                            onOpenTasbih = { showTasbihSheet = true }
                        )
                    }
                    item(key = "asma_allah_entry", span = { GridItemSpan(maxLineSpan) }) {
                        AsmaAllahEntryCard(onClick = { showAsmaSheet = true })
                    }

                    if (uiState.customDuas.isNotEmpty()) {
                        item(key = "section_custom", span = { GridItemSpan(maxLineSpan) }) {
                            AdhkarSectionLabel(title = "أدعيتي الخاصة")
                        }
                        items(items = uiState.customDuas, key = { "custom_${it.id}" }, span = { GridItemSpan(maxLineSpan) }) { item ->
                            com.example.ui.screens.adhkar.AdhkarSearchResultCard(
                                item = item,
                                isFavorite = item.id in uiState.favoriteIds,
                                audioState = uiState.audioState,
                                onOpenCategory = { onOpenDhikrItem(item) },
                                onToggleFavorite = { onToggleFavorite(item.id) },
                                onTogglePlayAudio = { onTogglePlayAudio(item) },
                                onDeleteCustom = { onDeleteCustomDua(item.id) }
                            )
                        }
                    }

                    item(key = "section_daily", span = { GridItemSpan(maxLineSpan) }) {
                        AdhkarSectionLabel(title = "أذكار اليوم والليلة")
                    }

                    items(items = featuredCategories, key = { it.id }, span = { GridItemSpan(maxLineSpan) }) { category ->
                        val itemsForCategory = remember(category, uiState.itemsByCategory, uiState.customDuas) { uiState.itemsForCategory(category) }
                        val completedInCat = remember(uiState.countsById, itemsForCategory) {
                            itemsForCategory.count { (uiState.countsById[it.id] ?: 0) >= it.targetCount }
                        }
                        AdhkarCategoryCard(
                            category = category,
                            completedItems = completedInCat,
                            totalItems = itemsForCategory.size,
                            onClick = { onOpenCategory(category) }
                        )
                    }

                    item(key = "section_all", span = { GridItemSpan(maxLineSpan) }) {
                        AdhkarSectionLabel(title = "جميع أبواب حصن المسلم")
                    }

                    items(
                        items = otherCategories,
                        key = { it.id },
                        span = { category -> GridItemSpan(if (category.isWideCard) maxLineSpan else 1) }
                    ) { category ->
                        val itemsForCategory = remember(category, uiState.itemsByCategory, uiState.customDuas) { uiState.itemsForCategory(category) }
                        val subChaptersCount = remember(category, uiState.itemsByCategory, uiState.customDuas) { uiState.subChaptersForCategory(category).size }
                        val completedInCat = remember(uiState.countsById, itemsForCategory) {
                            itemsForCategory.count { (uiState.countsById[it.id] ?: 0) >= it.targetCount }
                        }
                        AdhkarCategoryCard(
                            category = category,
                            completedItems = completedInCat,
                            totalItems = itemsForCategory.size,
                            subChapterCount = subChaptersCount,
                            onClick = { onOpenCategory(category) }
                        )
                    }
                }
            }
        }
    }

    if (showTasbihSheet) {
        TasbihBottomSheet(
            stats = stats,
            onIncrement = onIncrementTasbih,
            onAddCustomPhrase = onAddCustomTasbihPhrase,
            onRemoveCustomPhrase = onRemoveCustomTasbihPhrase,
            onDismiss = { showTasbihSheet = false }
        )
    }
    if (showCustomDuaDialog) {
        com.example.ui.screens.adhkar.AddCustomDuaDialog(onDismiss = { showCustomDuaDialog = false }, onSave = onAddCustomDua)
    }
    if (showAsmaSheet) {
        AsmaAllahBottomSheet(
            onToastMessage = { msg -> scope.launch { snackbarHostState.showQuranToast(msg) } },
            onDismiss = { showAsmaSheet = false }
        )
    }
}

@Composable
private fun AdhkarSectionLabel(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Bookmark,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
