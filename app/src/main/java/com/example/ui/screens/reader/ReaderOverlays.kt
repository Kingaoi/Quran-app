package com.example.ui.screens.reader

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.MemorizationLoopConfig
import com.example.data.local.AyahBookmark
import com.example.domain.model.AyahItem
import com.example.domain.model.SurahInfo
import com.example.domain.model.TafsirEdition
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.ui.components.AudioMemorizationDialog
import com.example.ui.components.PlaylistQuickPickerSheet
import com.example.ui.components.SurahHeaderOrnament
import com.example.ui.components.showQuranToast
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.theme.QURAN_LINE_HEIGHT_RATIO
import com.example.ui.viewmodel.AudioPlaylistActions
import com.example.ui.viewmodel.AyahTafsirUiState
import com.example.ui.viewmodel.QuranUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun OfflineMushafPageFallback(
    pageAyahs: List<AyahItem>,
    paperInk: Color,
    markerAccentColor: Color,
    fontSizeSp: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        pageAyahs.forEachIndexed { idx, ayah ->
            if (ayah.ayahNumber == 1) {
                SurahHeaderOrnament(
                    surahNameAr = ayah.surahNameAr,
                    surahNumber = ayah.surahNumber,
                    ayahCount = 0,
                    revelationLabel = "سورة",
                    showBasmalah = ayah.surahNumber != 1 && ayah.surahNumber != 9
                )
            }
            if (idx == 0 || pageAyahs[idx - 1].surahNumber != ayah.surahNumber) {
                val slice = pageAyahs.drop(idx).takeWhile { it.surahNumber == ayah.surahNumber }
                val combined = remember(slice, markerAccentColor) {
                    buildAnnotatedString {
                        slice.forEachIndexed { itemIdx, item ->
                            if (itemIdx > 0) append(' ')
                            append(item.text)
                            append(' ')
                            withStyle(SpanStyle(color = markerAccentColor)) { append(item.marker) }
                        }
                    }
                }
                Text(
                    text = combined,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * QURAN_LINE_HEIGHT_RATIO).sp
                    ),
                    color = paperInk,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
internal fun MushafReaderDialogsHost(
    uiState: QuranUiState,
    currentPage: Int,
    currentSurahLabel: String,
    playbackState: AudioPlaybackState,
    activePageAyahs: List<AyahItem>,
    bookmarks: List<AyahBookmark>,
    tafsirState: AyahTafsirUiState,
    currentLoop: MemorizationLoopConfig,
    totalAyahsFallback: Int,
    markerAccentColor: Color,
    showQuickJumpSheet: Boolean,
    showPageAyahsSheet: Boolean,
    showPageJumpDialog: Boolean,
    showDimmerDialog: Boolean,
    memLoopSeedAyah: Pair<Int, Int>?,
    activeAyahCard: Pair<AyahItem, AyahDetailCardType>?,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    getMutashabihatCount: (Int, Int) -> Int,
    findMutashabihat: (AyahItem) -> List<MutashabihMatch>,
    compareAyahWords: (AyahItem, AyahItem) -> List<AyahWordDiffSegment>,
    onSelectSurah: (Int) -> Unit,
    onJumpToPage: (Int) -> Unit,
    onJumpToAyah: (Int, Int, Int) -> Unit,
    onPlayAyah: (Int, Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onLoadTafsir: (Int, Int, TafsirEdition) -> Unit,
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onSetDimmerAlpha: (Float) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onStartMemorizationLoop: (MemorizationLoopConfig) -> Unit,
    onClearMemorizationLoop: () -> Unit,
    onSelectAyahInQuickBar: (AyahItem) -> Unit,
    onOpenPageWaqfGuide: () -> Unit,
    onDismissAyahCard: () -> Unit,
    onDismissQuickJump: () -> Unit,
    onDismissPageAyahs: () -> Unit,
    onDismissPageJumpDialog: () -> Unit,
    onDismissDimmerDialog: () -> Unit,
    onDismissMemLoopDialog: () -> Unit,
    playlists: List<AudioPlaylist> = emptyList(),
    surahsByNumber: Map<Int, SurahInfo> = emptyMap(),
    playlistActions: AudioPlaylistActions = AudioPlaylistActions(),
    quickAddAyahToPlaylist: AyahItem? = null,
    onDismissQuickAddPlaylist: () -> Unit = {}
) {
    MushafReaderModals(
        uiState = uiState,
        currentPage = currentPage,
        playbackState = playbackState,
        activePageAyahs = activePageAyahs,
        bookmarks = bookmarks,
        tafsirState = tafsirState,
        markerAccentColor = markerAccentColor,
        showQuickJumpSheet = showQuickJumpSheet,
        showPageAyahsSheet = showPageAyahsSheet,
        showPageJumpDialog = showPageJumpDialog,
        activeAyahCard = activeAyahCard,
        context = context,
        scope = scope,
        snackbarHostState = snackbarHostState,
        getMutashabihatCount = getMutashabihatCount,
        findMutashabihat = findMutashabihat,
        compareAyahWords = compareAyahWords,
        onSelectSurah = onSelectSurah,
        onJumpToPage = onJumpToPage,
        onJumpToAyah = onJumpToAyah,
        onPlayAyah = onPlayAyah,
        onTogglePlayPause = onTogglePlayPause,
        onLoadTafsir = onLoadTafsir,
        onSaveBookmark = onSaveBookmark,
        onDeleteBookmark = onDeleteBookmark,
        onSelectAyahInQuickBar = onSelectAyahInQuickBar,
        onOpenPageWaqfGuide = onOpenPageWaqfGuide,
        onDismissAyahCard = onDismissAyahCard,
        onDismissQuickJump = onDismissQuickJump,
        onDismissPageAyahs = onDismissPageAyahs,
        onDismissPageJumpDialog = onDismissPageJumpDialog
    )

    if (showDimmerDialog) {
        ReaderDimmerPopover(
            dimmerAlpha = uiState.settings.readerDimmerAlpha,
            keepScreenOn = uiState.settings.keepScreenOn,
            onChangeDimmer = onSetDimmerAlpha,
            onChangeKeepScreenOn = onSetKeepScreenOn,
            onDismiss = onDismissDimmerDialog
        )
    }

    memLoopSeedAyah?.let { (seedSurahNum, seedAyah) ->
        val surahObj = uiState.surahs.firstOrNull { it.number == seedSurahNum }
        AudioMemorizationDialog(
            surahNumber = seedSurahNum,
            surahNameAr = surahObj?.nameAr ?: currentSurahLabel,
            totalAyahs = surahObj?.ayahCount ?: totalAyahsFallback,
            initialAyah = seedAyah,
            currentLoop = currentLoop,
            isSurahLevelReciter = playbackState.reciter.isSurahLevelAudio,
            onStartLoop = onStartMemorizationLoop,
            onClearLoop = onClearMemorizationLoop,
            onDismiss = onDismissMemLoopDialog
        )
    }

    if (quickAddAyahToPlaylist != null) {
        PlaylistQuickPickerSheet(
            playlists = playlists,
            surahsByNumber = surahsByNumber,
            playbackState = playbackState,
            actions = playlistActions,
            quickAddAyah = quickAddAyahToPlaylist,
            onAddedSurahToast = { msg -> scope.launch { snackbarHostState.showQuranToast(msg) } },
            onDismiss = onDismissQuickAddPlaylist
        )
    }
}
