package com.example.ui.screens.reader

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarksRepository
import com.example.domain.model.AyahItem
import com.example.domain.model.TafsirEdition
import com.example.domain.model.toAppDigits
import com.example.ui.components.OrnamentalDivider
import com.example.ui.components.RubElHizbBadge
import com.example.ui.theme.LocalQuranFontFamily
import com.example.ui.util.copyTextToClipboard
import com.example.ui.util.sharePlainText
import com.example.ui.viewmodel.AyahTafsirUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AyahActionsBottomSheet(
    ayah: AyahItem,
    markerAccentColor: Color,
    bookmarks: List<AyahBookmark>,
    tafsirState: AyahTafsirUiState,
    onLoadTafsir: (Int, Int, TafsirEdition) -> Unit,
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onStartLoopFromAyah: () -> Unit,
    onPlay: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImageCard: () -> Unit = {},
    onTafsirCopied: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareMenu by remember { mutableStateOf(false) }
    val existingBookmark = remember(bookmarks, ayah) {
        val targetId = BookmarksRepository.buildId(ayah.pageNumber, ayah.surahNumber, ayah.ayahNumber)
        bookmarks.firstOrNull { it.id == targetId }
    }

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
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    RubElHizbBadge(number = ayah.ayahNumber, size = 38.dp)
                    Column {
                        Text(
                            text = "سورة ${ayah.surahNameAr}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "الآية ${ayah.ayahNumber.toAppDigits()} • صفحة ${ayah.pageNumber.toAppDigits()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "إغلاق نافذة الآية",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, markerAccentColor.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(ayah.text)
                        append(' ')
                        withStyle(SpanStyle(color = markerAccentColor)) { append(ayah.marker) }
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = LocalQuranFontFamily.current,
                        fontSize = 24.sp,
                        lineHeight = 42.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AyahActionTile(
                    icon = Icons.Rounded.PlayCircle,
                    label = "استماع",
                    highlighted = true,
                    onClick = onPlay,
                    modifier = Modifier.weight(1f)
                )
                AyahActionTile(
                    icon = Icons.Rounded.Repeat,
                    label = "تحفيظ",
                    onClick = onStartLoopFromAyah,
                    modifier = Modifier.weight(1f)
                )
                AyahActionTile(
                    icon = Icons.Rounded.ContentCopy,
                    label = "نسخ",
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                )
                Box(modifier = Modifier.weight(1f)) {
                    AyahActionTile(
                        icon = Icons.Rounded.Share,
                        label = "مشاركة",
                        onClick = { showShareMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.Image, contentDescription = null) },
                            text = { Text("مشاركة كبطاقة صورة") },
                            onClick = { showShareMenu = false; onShareImageCard() },
                            modifier = Modifier.testTag("ayah_share_image_card")
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.TextFields, contentDescription = null) },
                            text = { Text("مشاركة كنص") },
                            onClick = { showShareMenu = false; onShare() },
                            modifier = Modifier.testTag("ayah_share_plain_text")
                        )
                    }
                }
            }

            OrnamentalDivider()

            AyahTafsirAndBookmarkSection(
                ayah = ayah,
                existingBookmark = existingBookmark,
                tafsirState = tafsirState,
                onLoadTafsir = onLoadTafsir,
                onSaveBookmark = onSaveBookmark,
                onDeleteBookmark = onDeleteBookmark,
                onTafsirCopied = onTafsirCopied
            )
        }
    }
}

@Composable
private fun AyahActionTile(
    icon: ImageVector,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.heightIn(min = 58.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

internal fun copyAyahToClipboard(context: Context, ayah: AyahItem) {
    copyTextToClipboard(
        context = context,
        label = "آية قرآنية",
        text = "﴿${ayah.text}﴾ [سورة ${ayah.surahNameAr}: ${ayah.ayahNumber}]"
    )
}

internal fun shareAyah(context: Context, ayah: AyahItem) {
    sharePlainText(
        context = context,
        chooserTitle = "مشاركة الآية",
        text = "﴿${ayah.text}﴾\n[سورة ${ayah.surahNameAr} - الآية ${ayah.ayahNumber}]"
    )
}
