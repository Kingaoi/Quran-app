package com.example.ui.screens.reader.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaybackState
import com.example.data.local.AyahBookmark
import com.example.domain.model.AyahItem
import com.example.domain.model.toAppDigits
import com.example.ui.screens.reader.AyahBookmarkReflectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahBookmarkSheet(
    ayah: AyahItem,
    accentColor: Color,
    existingBookmark: AyahBookmark?,
    playbackState: AudioPlaybackState? = null,
    onPlayAyah: ((Int, Int) -> Unit)? = null,
    onTogglePlayPause: (() -> Unit)? = null,
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("dedicated_bookmark_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AyahCardSheetHeader(
                title = "حفظ وتدوين تدبّر",
                subtitle = "سورة ${ayah.surahNameAr} • الآية ${ayah.ayahNumber.toAppDigits()} • صفحة ${ayah.pageNumber.toAppDigits()}",
                icon = Icons.Rounded.BookmarkAdd,
                accentColor = accentColor,
                ayah = ayah,
                playbackState = playbackState,
                onPlayAyah = onPlayAyah,
                onTogglePlayPause = onTogglePlayPause,
                onDismiss = onDismiss
            )

            AyahBookmarkReflectionCard(
                ayah = ayah,
                existingBookmark = existingBookmark,
                onSaveBookmark = { bookmark ->
                    onSaveBookmark(bookmark)
                    onDismiss()
                },
                onDeleteBookmark = { id ->
                    onDeleteBookmark(id)
                    onDismiss()
                }
            )
        }
    }
}
