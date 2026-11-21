package com.example.ui.screens.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.QuranRepository
import com.example.domain.model.toAppDigits
import com.example.domain.model.toArabicIndicDigits
import com.example.ui.components.QuranDialogPresetChip
import com.example.ui.components.QuranDialogTextField
import com.example.ui.components.RubElHizbBadge
import com.example.ui.viewmodel.QuranUiState

private val SHEET_PAGE_PRESETS = listOf(
    1 to "الفاتحة",
    293 to "الكهف",
    440 to "يس",
    531 to "الرحمن",
    582 to "جزء عم",
    604 to "الناس"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun QuickJumpBottomSheet(
    uiState: QuranUiState,
    onSelectSurah: (Int) -> Unit,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tabIndex by remember { mutableIntStateOf(0) }
    var surahFilter by remember { mutableStateOf("") }
    var pageInput by remember { mutableStateOf(uiState.currentPage.toString()) }

    val filteredSurahs = remember(uiState.surahs, surahFilter) {
        if (surahFilter.isBlank()) {
            uiState.surahs
        } else {
            val q = QuranRepository.normalizeArabic(surahFilter)
            uiState.surahs.filter {
                QuranRepository.normalizeArabic(it.nameAr).contains(q) ||
                    it.number.toString() == surahFilter.trim()
            }
        }
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
                .height(520.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "الانتقال السريع في المصحف",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            val tabs = listOf("السور", "الأجزاء", "صفحة")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { idx, label ->
                    SegmentedButton(
                        selected = tabIndex == idx,
                        onClick = { tabIndex = idx },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = tabs.size),
                        modifier = Modifier.testTag("quick_jump_tab_$idx")
                    ) { Text(label) }
                }
            }

            when (tabIndex) {
                0 -> {
                    QuranDialogTextField(
                        value = surahFilter,
                        onValueChange = { surahFilter = it },
                        placeholder = "ابحث عن السورة بالاسم أو الرقم...",
                        leadingIcon = Icons.Rounded.Search,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredSurahs, key = { it.number }) { surah ->
                            Surface(
                                onClick = { onSelectSurah(surah.number) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("jump_surah_${surah.number}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RubElHizbBadge(number = surah.number, size = 36.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "سورة ${surah.nameAr}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
                                        )
                                        Text(
                                            text = "${surah.revelationType} • ${surah.ayahCount.toArabicIndicDigits()} آية",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "ص ${surah.firstPage.toArabicIndicDigits()}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.ajza, key = { it.number }) { juz ->
                            Surface(
                                onClick = { onSelectPage(juz.startPage) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("jump_juz_${juz.number}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RubElHizbBadge(number = juz.number, size = 36.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = juz.nameAr, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "سورة ${juz.startSurahNameAr} • الآية ${juz.startAyahNumber.toArabicIndicDigits()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "ص ${juz.startPage.toArabicIndicDigits()}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    val parsedPage = pageInput.toIntOrNull()?.takeIf { it in 1..604 }
                    val hasError = pageInput.isNotEmpty() && parsedPage == null
                    fun submitPage() {
                        val target = pageInput.toIntOrNull()?.takeIf { it in 1..604 } ?: return
                        onSelectPage(target)
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        val cur = pageInput.toIntOrNull() ?: uiState.currentPage
                                        pageInput = (cur - 1).coerceIn(1, 604).toString()
                                    },
                                    modifier = Modifier.size(54.dp)
                                ) { Icon(Icons.Rounded.Remove, contentDescription = "الصفحة السابقة") }

                                QuranDialogTextField(
                                    value = pageInput,
                                    onValueChange = { txt -> pageInput = txt.filter { it.isDigit() }.take(3) },
                                    label = "رقم الصفحة (${1.toAppDigits()} - ${604.toAppDigits()})",
                                    leadingIcon = Icons.AutoMirrored.Rounded.MenuBook,
                                    isError = hasError,
                                    supportingText = if (hasError) "أدخل رقماً صحيحاً بين ١ و ٦٠٤" else null,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    onDone = ::submitPage,
                                    textStyle = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                FilledTonalIconButton(
                                    onClick = {
                                        val cur = pageInput.toIntOrNull() ?: uiState.currentPage
                                        pageInput = (cur + 1).coerceIn(1, 604).toString()
                                    },
                                    modifier = Modifier.size(54.dp)
                                ) { Icon(Icons.Rounded.Add, contentDescription = "الصفحة التالية") }
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SHEET_PAGE_PRESETS.forEach { (presetPage, label) ->
                                    QuranDialogPresetChip(
                                        label = "${presetPage.toAppDigits()} • $label",
                                        selected = parsedPage == presetPage,
                                        onClick = { pageInput = presetPage.toString() }
                                    )
                                }
                            }

                            Button(
                                onClick = ::submitPage,
                                enabled = parsedPage != null,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "الانتقال للصفحة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
