package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarkCategory
import com.example.data.local.BookmarksRepository
import com.example.domain.model.AyahItem
import com.example.ui.components.QuranDialogTextField
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@Composable
internal fun AyahBookmarkReflectionCard(
    ayah: AyahItem,
    existingBookmark: AyahBookmark?,
    onSaveBookmark: (AyahBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    val qc = MaterialTheme.quranColors
    var selectedCategory by remember(ayah, existingBookmark) {
        mutableStateOf(existingBookmark?.category ?: BookmarkCategory.RECITATION)
    }
    var noteText by remember(ayah, existingBookmark) {
        mutableStateOf(existingBookmark?.note.orEmpty())
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (existingBookmark != null) qc.successContainer else qc.surface2)
                    ) {
                        Icon(
                            imageVector = if (existingBookmark != null) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                            contentDescription = null,
                            tint = if (existingBookmark != null) qc.success else qc.pastelRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "علامة وملاحظة تدبّر",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (existingBookmark != null) "العلامة محفوظة في الفهرس" else "احفظ الآية مع خاطرة شخصية",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (existingBookmark != null) qc.success else qc.textMuted
                        )
                    }
                }
                if (existingBookmark != null) {
                    IconButton(
                        onClick = { onDeleteBookmark(existingBookmark.id) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف العلامة",
                            tint = qc.danger
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BookmarkCategory.entries.forEach { cat ->
                    val isCatSel = selectedCategory == cat
                    FilterChip(
                        selected = isCatSel,
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(50),
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isCatSel),
                        label = { Text(cat.labelAr, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            QuranDialogTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = "أضف خاطرة أو ملاحظة تدبر حول الآية...",
                leadingIcon = Icons.Rounded.EditNote,
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayah_bookmark_note_input")
            )

            Button(
                onClick = {
                    onSaveBookmark(
                        AyahBookmark(
                            id = BookmarksRepository.buildId(ayah.pageNumber, ayah.surahNumber, ayah.ayahNumber),
                            page = ayah.pageNumber,
                            surahNumber = ayah.surahNumber,
                            ayahNumber = ayah.ayahNumber,
                            surahNameAr = ayah.surahNameAr,
                            category = selectedCategory,
                            note = noteText.trim(),
                            createdAtMillis = existingBookmark?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("ayah_save_bookmark_button")
            ) {
                Icon(
                    imageVector = if (existingBookmark != null) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (existingBookmark != null) "تحديث العلامة والملاحظة" else "حفظ العلامة")
            }
        }
    }
}
