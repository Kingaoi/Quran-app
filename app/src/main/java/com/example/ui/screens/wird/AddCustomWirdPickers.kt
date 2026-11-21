package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.components.QuranDialogTextField
import com.example.ui.theme.quranColors
import java.util.concurrent.TimeUnit

internal enum class CustomPlanIconType(
    val key: String,
    val labelAr: String,
    val icon: ImageVector,
    val pastelIndex: Int
) {
    HIFZ("hifz", "حفظ", Icons.Rounded.School, 3),
    REVIEW("review", "مراجعة", Icons.Rounded.Repeat, 2),
    RECITATION("recitation", "تلاوة", Icons.AutoMirrored.Rounded.MenuBook, 0),
    HIFZ_REVIEW("hifz_review", "حفظ ومراجعة", Icons.Rounded.Psychology, 1),
    BOOK("book", "تلاوة", Icons.AutoMirrored.Rounded.MenuBook, 0),
    TADABBUR("tadabbur", "تدبّر", Icons.Rounded.AutoStories, 1);

    companion object {
        fun fromKey(key: String): CustomPlanIconType =
            entries.firstOrNull { it.key == key } ?: RECITATION
    }
}

internal fun fallbackSurahList(): List<SurahInfo> = (1..114).map { n ->
    SurahInfo(
        number = n,
        nameAr = if (n == 1) "الفاتحة" else if (n == 114) "الناس" else "سورة ${n.toAppDigits()}",
        nameEn = "Surah $n",
        ayahCount = if (n == 1) 7 else if (n == 2) 286 else if (n == 114) 6 else 20,
        revelationType = "مكية",
        isMakki = true,
        hasBasmalah = n != 9,
        firstPage = ((n - 1) * 5 + 1).coerceAtMost(604),
        lastPage = (n * 5).coerceAtMost(604)
    )
}

@Composable
internal fun SurahPickerDialog(
    title: String,
    surahs: List<SurahInfo>,
    selectedSurahNumber: Int,
    onSelectSurah: (SurahInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(surahs, query) {
        val q = query.trim()
        if (q.isEmpty()) surahs else surahs.filter {
            it.nameAr.contains(q) || it.number.toString() == q || it.number.toAppDigits(false).contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = qc.surface,
            border = BorderStroke(1.dp, qc.border),
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.textMuted)
                    }
                }
                QuranDialogTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "ابحث عن سورة",
                    placeholder = "مثال: البقرة أو ٢",
                    leadingIcon = Icons.Rounded.Search,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    items(filtered, key = { it.number }) { surah ->
                        val isSelected = surah.number == selectedSurahNumber
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSurah(surah)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${surah.number.toAppDigits(useWestern)}. سورة ${surah.nameAr}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) qc.primary else qc.text
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${surah.ayahCount.toAppDigits(useWestern)} آية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = qc.textMuted
                                )
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = qc.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = qc.border.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
internal fun AyahNumberPickerDialog(
    surahNameAr: String,
    maxAyah: Int,
    selectedAyah: Int,
    onSelectAyah: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var query by remember { mutableStateOf("") }
    val allNums = remember(maxAyah) { (1..maxAyah.coerceAtLeast(1)).toList() }
    val filtered = remember(allNums, query) {
        val q = query.trim()
        if (q.isEmpty()) allNums else allNums.filter {
            it.toString().contains(q) || it.toAppDigits(false).contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = qc.surface,
            border = BorderStroke(1.dp, qc.border),
            modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اختر رقم الآية • $surahNameAr",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.textMuted)
                    }
                }
                QuranDialogTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "رقم الآية",
                    placeholder = "١ - ${maxAyah.toAppDigits(useWestern)}",
                    leadingIcon = Icons.Rounded.Search,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                    items(filtered, key = { it }) { ayahNum ->
                        val isSelected = ayahNum == selectedAyah
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAyah(ayahNum)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الآية ${ayahNum.toAppDigits(useWestern)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) qc.primary else qc.text
                            )
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = qc.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = qc.border.copy(alpha = 0.35f))
                    }
                }
            }
        }
    }
}

