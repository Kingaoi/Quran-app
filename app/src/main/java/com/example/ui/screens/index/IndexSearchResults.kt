package com.example.ui.screens.index

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.domain.model.SearchHit
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.quran.ConcordanceScopeFilter
import com.example.domain.quran.QuranConcordanceReport
import com.example.domain.quran.QuranRootLexicon
import com.example.domain.quran.QuranSearchMode
import com.example.ui.theme.LocalFloatingNavBottomPadding

@Composable
internal fun IndexSearchResultsList(
    filteredSurahs: List<SurahInfo>,
    ayahSearchHits: List<SearchHit>,
    searchMode: QuranSearchMode = QuranSearchMode.WORD,
    concordanceReport: QuranConcordanceReport = QuranConcordanceReport.empty(),
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    onOpenSurah: (Int) -> Unit,
    onOpenHit: (SearchHit) -> Unit,
    onOpenConcordanceAyah: (page: Int, surah: Int, ayah: Int) -> Unit = { p, _, _ -> onOpenSurah(p) },
    onTogglePlaySurah: (Int) -> Unit = {},
    onShowSurahInfo: (SurahInfo) -> Unit = {},
    horizontalPadding: Dp = 16.dp,
    topContent: (@Composable () -> Unit)? = null
) {
    val bottomClearance = LocalFloatingNavBottomPadding.current
    var scopeFilter by remember(concordanceReport.query, concordanceReport.mode) {
        mutableStateOf(ConcordanceScopeFilter.ALL)
    }
    var selectedSurahFilter by remember(concordanceReport.query, concordanceReport.mode) {
        mutableStateOf<Int?>(null)
    }
    var selectedJuzFilter by remember(concordanceReport.query, concordanceReport.mode) {
        mutableStateOf<Int?>(null)
    }
    var selectedFormFilter by remember(concordanceReport.query, concordanceReport.mode) {
        mutableStateOf<String?>(null)
    }
    var visibleLimit by remember(
        concordanceReport.query, concordanceReport.mode,
        scopeFilter, selectedSurahFilter, selectedJuzFilter, selectedFormFilter
    ) {
        mutableIntStateOf(60)
    }

    val useConcordance = concordanceReport.totalMatchingAyahs > 0
    val filteredConcordanceHits = remember(
        concordanceReport.hits, scopeFilter, selectedSurahFilter, selectedJuzFilter, selectedFormFilter
    ) {
        val normForm = selectedFormFilter?.let { QuranRootLexicon.normalizeToken(it) }
        concordanceReport.hits.filter { hit ->
            val matchesScope = when (scopeFilter) {
                ConcordanceScopeFilter.ALL -> true
                ConcordanceScopeFilter.MAKKI -> hit.isMakki
                ConcordanceScopeFilter.MADANI -> !hit.isMakki
            }
            val matchesSurah = selectedSurahFilter == null || hit.surahNumber == selectedSurahFilter
            val matchesJuz = selectedJuzFilter == null || hit.juzNumber == selectedJuzFilter
            val matchesForm = normForm == null || hit.matchedTokens.any {
                QuranRootLexicon.normalizeToken(it) == normForm
            }
            matchesScope && matchesSurah && matchesJuz && matchesForm
        }
    }
    val displayedConcordanceHits = remember(filteredConcordanceHits, visibleLimit) {
        filteredConcordanceHits.take(visibleLimit)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(concordanceReport.query, concordanceReport.mode) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            listState.scrollToItem(0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (topContent != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 4.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topContent()
            }
        }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 4.dp,
                bottom = 12.dp + bottomClearance
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("index_search_results_list")
        ) {
        if (filteredSurahs.isNotEmpty() && searchMode == QuranSearchMode.WORD) {
            item(key = "surah_matches_title") {
                Text(
                    text = "السور المطابقة (${filteredSurahs.size.toAppDigits()})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            items(filteredSurahs, key = { "surah_${it.number}" }) { surah ->
                val isActive = playbackState.isActive && playbackState.currentSurah == surah.number
                SearchSurahCard(
                    surah = surah,
                    isActiveSurah = isActive,
                    isPlayingSurah = isActive && playbackState.isPlaying,
                    onClick = { onOpenSurah(surah.number) },
                    onTogglePlaySurah = { onTogglePlaySurah(surah.number) },
                    onShowInfo = { onShowSurahInfo(surah) }
                )
            }
        }

        if (useConcordance) {
            item(key = "concordance_stats_card") {
                IndexConcordanceStatsCard(
                    report = concordanceReport,
                    scopeFilter = scopeFilter,
                    selectedSurahFilter = selectedSurahFilter,
                    selectedJuzFilter = selectedJuzFilter,
                    selectedFormFilter = selectedFormFilter,
                    onSelectScope = { scopeFilter = it },
                    onToggleSurahFilter = { num ->
                        selectedSurahFilter = if (selectedSurahFilter == num) null else num
                    },
                    onToggleJuzFilter = { juz ->
                        selectedJuzFilter = if (selectedJuzFilter == juz) null else juz
                    },
                    onToggleFormFilter = { form ->
                        selectedFormFilter = if (selectedFormFilter == form) null else form
                    },
                    onClearSubFilters = {
                        scopeFilter = ConcordanceScopeFilter.ALL
                        selectedSurahFilter = null
                        selectedJuzFilter = null
                        selectedFormFilter = null
                    },
                    onOpenHit = { hit -> onOpenConcordanceAyah(hit.pageNumber, hit.surahNumber, hit.ayahNumber) }
                )
            }
            item(key = "concordance_hits_header") {
                Text(
                    text = "الآيات المطابقة (${filteredConcordanceHits.size.toAppDigits()})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }
            items(
                displayedConcordanceHits,
                key = { "chit_${it.surahNumber}_${it.ayahNumber}" }
            ) { hit ->
                ConcordanceHitCard(
                    hit = hit,
                    showConceptTag = searchMode == QuranSearchMode.MEANING,
                    onClick = { onOpenConcordanceAyah(hit.pageNumber, hit.surahNumber, hit.ayahNumber) }
                )
            }
            if (filteredConcordanceHits.size > displayedConcordanceHits.size) {
                val remaining = filteredConcordanceHits.size - displayedConcordanceHits.size
                item(key = "concordance_load_more") {
                    OutlinedButton(
                        onClick = { visibleLimit += 60 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("concordance_load_more_button")
                    ) {
                        Text("عرض المزيد (${remaining.toAppDigits()} آية متبقية)")
                    }
                }
            }
        } else if (ayahSearchHits.isNotEmpty()) {
            item(key = "legacy_hits_header") {
                Text(
                    text = "نتائج الآيات",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            items(ayahSearchHits, key = { "hit_${it.surahNumber}_${it.ayahNumber}" }) { hit ->
                SearchHitCard(hit = hit, onClick = { onOpenHit(hit) })
            }
        }

        if (filteredSurahs.isEmpty() && !useConcordance && ayahSearchHits.isEmpty()) {
            item(key = "search_empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (searchMode) {
                            QuranSearchMode.ROOT -> "لم يُعثر على جذر مطابق — جرّب ٣ أحرف مثل: رحم أو علم أو صبر"
                            QuranSearchMode.MEANING -> "لم تتطابق آيات كافية — جرّب مفهوماً مثل: السكينة، الرزق، بر الوالدين"
                            QuranSearchMode.WORD -> "لا توجد نتائج مطابقة للبحث"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }
    }
}
