package com.example.ui.screens.reader

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.AudioPlaybackState
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarkCategory
import com.example.data.local.BookmarksRepository
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.domain.quran.QuranWaqfCatalog
import com.example.ui.components.showQuranToast
import com.example.ui.theme.LocalQuranFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun ReaderAyahQuickBarHost(
    ayah: AyahItem?,
    visible: Boolean,
    accentColor: Color,
    controlsVisible: Boolean,
    hasBottomWirdBanner: Boolean = false,
    playbackState: AudioPlaybackState = AudioPlaybackState(),
    bookmarks: List<AyahBookmark>,
    getMutashabihatCount: (Int, Int) -> Int,
    context: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onPlayAyah: (Int, Int) -> Unit,
    onTogglePlayPause: () -> Unit = {},
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onOpenMemorizationLoop: (AyahItem) -> Unit,
    onAddToPlaylist: (AyahItem) -> Unit = {},
    onOpenDedicatedCard: (AyahItem, AyahDetailCardType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && ayah != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = slideOutVertically { it / 2 } + fadeOut(),
        modifier = modifier
    ) {
        ayah?.let { target ->
            val targetId = remember(target) {
                BookmarksRepository.buildId(target.pageNumber, target.surahNumber, target.ayahNumber)
            }
            val isBookmarked = remember(bookmarks, targetId) { bookmarks.any { it.id == targetId } }
            val mutashabihatCount = remember(target.surahNumber, target.ayahNumber) {
                getMutashabihatCount(target.surahNumber, target.ayahNumber)
            }
            val waqfCount = remember(target.surahNumber, target.ayahNumber) {
                QuranWaqfCatalog.countMarksInAyah(target)
            }
            val isThisAyahActive = playbackState.isActive &&
                playbackState.currentSurah == target.surahNumber &&
                playbackState.currentAyah == target.ayahNumber

            ReaderAyahQuickBar(
                ayah = target,
                accentColor = accentColor,
                isBookmarked = isBookmarked,
                isThisAyahActive = isThisAyahActive,
                isPlaying = isThisAyahActive && playbackState.isPlaying,
                isBuffering = isThisAyahActive && playbackState.isBuffering,
                mutashabihatCount = mutashabihatCount,
                waqfCount = waqfCount,
                controlsVisible = controlsVisible,
                hasBottomWirdBanner = hasBottomWirdBanner,
                onPlay = {
                    if (isThisAyahActive) onTogglePlayPause()
                    else onPlayAyah(target.surahNumber, target.ayahNumber)
                },
                onToggleQuickBookmark = {
                    if (isBookmarked) {
                        onDeleteBookmark(targetId)
                        scope.launch { snackbarHostState.showQuranToast("تمت إزالة علامة الآية") }
                    } else {
                        onSaveBookmark(
                            AyahBookmark(
                                id = targetId,
                                page = target.pageNumber,
                                surahNumber = target.surahNumber,
                                ayahNumber = target.ayahNumber,
                                surahNameAr = target.surahNameAr,
                                category = BookmarkCategory.RECITATION
                            )
                        )
                        scope.launch { snackbarHostState.showQuranToast("تم حفظ علامة الآية") }
                    }
                },
                onCopy = {
                    copyAyahToClipboard(context, target)
                    scope.launch { snackbarHostState.showQuranToast("تم نسخ الآية") }
                    onDismiss()
                },
                onOpenMemorization = {
                    onOpenMemorizationLoop(target)
                },
                onAddToPlaylist = {
                    onAddToPlaylist(target)
                },
                onOpenCard = { cardType ->
                    onOpenDedicatedCard(target, cardType)
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
internal fun ReaderAyahQuickBar(
    ayah: AyahItem,
    accentColor: Color,
    isBookmarked: Boolean,
    isThisAyahActive: Boolean = false,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    mutashabihatCount: Int,
    waqfCount: Int,
    controlsVisible: Boolean,
    hasBottomWirdBanner: Boolean = false,
    onPlay: () -> Unit,
    onToggleQuickBookmark: () -> Unit,
    onCopy: () -> Unit,
    onOpenMemorization: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onOpenCard: (AyahDetailCardType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 6.dp,
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 540.dp)
            .navigationBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(bottom = if (controlsVisible) (if (hasBottomWirdBanner) 198.dp else 108.dp) else 18.dp)
            .testTag("ayah_quick_action_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(accentColor))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "﴿${ayah.text}﴾",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = LocalQuranFontFamily.current, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AyahQuickPlayButton(
                    isThisAyahActive = isThisAyahActive,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    onClick = onPlay,
                    modifier = Modifier.testTag("ayah_quick_play")
                )
                IconButton(onClick = onAddToPlaylist, modifier = Modifier.size(48.dp).testTag("ayah_quick_add_playlist")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = "إضافة إلى قائمة التلاوة",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onToggleQuickBookmark, modifier = Modifier.size(48.dp).testTag("ayah_quick_bookmark")) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (isBookmarked) "إزالة العلامة" else "حفظ سريع",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp).testTag("ayah_quick_close")) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق الشريط", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickActionPill(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    label = "التفسير",
                    highlighted = true,
                    tag = "ayah_quick_tafsir",
                    onClick = { onOpenCard(AyahDetailCardType.TAFSIR) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.AutoStories,
                    label = "أسباب وغريب",
                    tag = "ayah_quick_asbab_gharib",
                    onClick = { onOpenCard(AyahDetailCardType.ASBAB_GHARIB) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.CompareArrows,
                    label = "المتشابهات",
                    badgeCount = mutashabihatCount,
                    tag = "ayah_quick_mutashabihat",
                    onClick = { onOpenCard(AyahDetailCardType.MUTASHABIHAT) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.PauseCircleOutline,
                    label = "الوقوفات",
                    badgeCount = waqfCount,
                    tag = "ayah_quick_waqf",
                    onClick = { onOpenCard(AyahDetailCardType.WAQF) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickActionPill(
                    icon = Icons.Rounded.Repeat,
                    label = "تحفيظ",
                    tag = "ayah_quick_memorize",
                    onClick = onOpenMemorization,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.BookmarkAdd,
                    label = "تدبّر وحفظ",
                    tag = "ayah_quick_bookmark_note",
                    onClick = { onOpenCard(AyahDetailCardType.BOOKMARK) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.Share,
                    label = "مشاركة",
                    tag = "ayah_quick_card",
                    onClick = { onOpenCard(AyahDetailCardType.SHARE) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Rounded.ContentCopy,
                    label = "نسخ",
                    tag = "ayah_quick_copy",
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
