package com.example.ui.screens.index

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AyahBookmark
import com.example.data.local.BookmarkCategory
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookmarksManagementSheet(
    ayahBookmarks: List<AyahBookmark>,
    bookmarkedPages: Set<Int>,
    onOpenAyah: (page: Int, surah: Int, ayah: Int) -> Unit,
    onOpenPage: (Int) -> Unit,
    onDeleteAyahBookmark: (String) -> Unit,
    onRemovePageBookmark: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<BookmarkCategory?>(null) }
    val filtered = remember(ayahBookmarks, selectedCategory) {
        if (selectedCategory == null) ayahBookmarks else ayahBookmarks.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "العلامات المرجعية وملاحظات التدبر",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            val qc = MaterialTheme.quranColors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allSelected = selectedCategory == null
                FilterChip(
                    selected = allSelected,
                    onClick = { selectedCategory = null },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(allSelected),
                    label = { Text("الكل (${ayahBookmarks.size.toAppDigits()})") }
                )
                BookmarkCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = { Text(cat.labelAr) }
                    )
                }
            }

            if (filtered.isEmpty() && bookmarkedPages.isEmpty()) {
                Text(
                    text = "لا توجد علامات مرجعية محفوظة بعد. اضغط مطولاً على أي آية داخل المصحف لإضافة علامة وملاحظة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filtered, key = { it.id }) { bm ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenAyah(bm.page, bm.surahNumber, bm.ayahNumber)
                                    onDismiss()
                                }
                                .testTag("bookmark_item_${bm.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = "سورة ${bm.surahNameAr} • الآية ${bm.ayahNumber.toAppDigits()} • ص ${bm.page.toAppDigits()}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = bm.category.labelAr + if (bm.note.isNotBlank()) " — ${bm.note}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteAyahBookmark(bm.id) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "حذف العلامة",
                                        tint = qc.danger
                                    )
                                }
                            }
                        }
                    }

                    if (selectedCategory == null && bookmarkedPages.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(bookmarkedPages.sorted(), key = { "page_$it" }) { pageNum ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenPage(pageNum)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bookmark,
                                        contentDescription = null,
                                        tint = qc.pastelRose,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "علامة صفحة ${pageNum.toAppDigits()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp)
                                    )
                                    IconButton(onClick = { onRemovePageBookmark(pageNum) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "إزالة علامة الصفحة",
                                            tint = qc.danger
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
