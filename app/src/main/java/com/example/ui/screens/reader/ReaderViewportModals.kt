package com.example.ui.screens.reader

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.audio.AudioPlaybackState
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarksRepository
import com.example.domain.model.AyahItem
import com.example.domain.model.TafsirEdition
import com.example.domain.model.toAppDigits
import com.example.domain.quran.AyahWordDiffSegment
import com.example.domain.quran.MutashabihMatch
import com.example.ui.components.showQuranToast
import com.example.ui.screens.reader.cards.AyahAsbabGharibSheet
import com.example.ui.screens.reader.cards.AyahBookmarkSheet
import com.example.ui.screens.reader.cards.AyahMutashabihatSheet
import com.example.ui.screens.reader.cards.AyahShareSheet
import com.example.ui.screens.reader.cards.AyahTafsirCardSheet
import com.example.ui.screens.reader.cards.AyahWaqfSheet
import com.example.ui.theme.LocalAppTheme
import com.example.ui.util.ShareCardGenerator
import com.example.ui.viewmodel.AyahTafsirUiState
import com.example.ui.viewmodel.QuranUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun MushafReaderModals(
    uiState: QuranUiState,
    currentPage: Int,
    playbackState: AudioPlaybackState,
    activePageAyahs: List<AyahItem>,
    bookmarks: List<AyahBookmark>,
    tafsirState: AyahTafsirUiState,
    markerAccentColor: Color,
    showQuickJumpSheet: Boolean,
    showPageAyahsSheet: Boolean,
    showPageJumpDialog: Boolean,
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
    onSelectAyahInQuickBar: (AyahItem) -> Unit,
    onOpenPageWaqfGuide: () -> Unit,
    onDismissAyahCard: () -> Unit,
    onDismissQuickJump: () -> Unit,
    onDismissPageAyahs: () -> Unit,
    onDismissPageJumpDialog: () -> Unit
) {
    val activeTheme = LocalAppTheme.current
    if (showQuickJumpSheet) {
        QuickJumpBottomSheet(
            uiState = uiState,
            onSelectSurah = { onSelectSurah(it); onDismissQuickJump() },
            onSelectPage = { page -> onJumpToPage(page); onDismissQuickJump() },
            onDismiss = onDismissQuickJump
        )
    }
    if (showPageAyahsSheet) {
        PageAyahsBottomSheet(
            currentPage = currentPage,
            pageAyahs = activePageAyahs,
            markerAccentColor = markerAccentColor,
            playbackState = playbackState,
            getMutashabihatCount = getMutashabihatCount,
            onSelectAyah = { onDismissPageAyahs(); onSelectAyahInQuickBar(it) },
            onOpenPageWaqfGuide = { onDismissPageAyahs(); onOpenPageWaqfGuide() },
            onPlayAyah = onPlayAyah,
            onTogglePlayPause = onTogglePlayPause,
            onDismiss = onDismissPageAyahs
        )
    }

    activeAyahCard?.let { (ayah, cardType) ->
        val existingBookmark = bookmarks.firstOrNull {
            it.id == BookmarksRepository.buildId(ayah.pageNumber, ayah.surahNumber, ayah.ayahNumber)
        }
        when (cardType) {
            AyahDetailCardType.TAFSIR -> AyahTafsirCardSheet(
                ayah = ayah,
                accentColor = markerAccentColor,
                tafsirState = tafsirState,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onLoadTafsir = onLoadTafsir,
                onTafsirCopied = { scope.launch { snackbarHostState.showQuranToast("تم نسخ التفسير") } },
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.ASBAB_GHARIB -> AyahAsbabGharibSheet(
                ayah = ayah,
                accentColor = markerAccentColor,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onCopied = { scope.launch { snackbarHostState.showQuranToast("تم نسخ معاني الكلمات") } },
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.MUTASHABIHAT -> AyahMutashabihatSheet(
                ayah = ayah,
                accentColor = markerAccentColor,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                findMutashabihat = findMutashabihat,
                compareWords = compareAyahWords,
                onJumpToAyah = { page, surah, ayahNum -> onJumpToAyah(page, surah, ayahNum) },
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.WAQF -> AyahWaqfSheet(
                ayah = ayah,
                pageAyahs = activePageAyahs,
                accentColor = markerAccentColor,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                initialTabPage = false,
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.WAQF_PAGE_GUIDE -> AyahWaqfSheet(
                ayah = ayah,
                pageAyahs = activePageAyahs,
                accentColor = markerAccentColor,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                initialTabPage = true,
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.BOOKMARK -> AyahBookmarkSheet(
                ayah = ayah,
                accentColor = markerAccentColor,
                existingBookmark = existingBookmark,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onSaveBookmark = { bookmark ->
                    onSaveBookmark(bookmark)
                    scope.launch { snackbarHostState.showQuranToast("تم حفظ العلامة والملاحظة") }
                },
                onDeleteBookmark = { id ->
                    onDeleteBookmark(id)
                    scope.launch { snackbarHostState.showQuranToast("تم حذف العلامة") }
                },
                onDismiss = onDismissAyahCard
            )
            AyahDetailCardType.SHARE -> AyahShareSheet(
                ayah = ayah,
                accentColor = markerAccentColor,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onShareImageCard = {
                    ShareCardGenerator.shareStyledCard(
                        context = context,
                        headerLabel = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                        bodyText = "﴿${ayah.text}﴾",
                        footerLabel = "المصحف الشريف",
                        appTheme = activeTheme
                    )
                },
                onSharePlainText = { shareAyah(context, ayah) },
                onCopyText = {
                    copyAyahToClipboard(context, ayah)
                    scope.launch { snackbarHostState.showQuranToast("تم نسخ الآية") }
                },
                onDismiss = onDismissAyahCard
            )
        }
    }

    if (showPageJumpDialog) {
        PageJumpAlertDialog(initialPage = currentPage, onConfirmPage = onJumpToPage, onDismiss = onDismissPageJumpDialog)
    }
}
