package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.audio.AudioPlaylist
import com.example.data.audio.AudioPlaylistTrack
import com.example.data.audio.PlaylistIconKey
import com.example.data.audio.PresetAudioPlaylists
import com.example.data.repository.QuranRepository
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPlaylistSheet(
    existingPlaylist: AudioPlaylist? = null,
    onSave: (title: String, subtitle: String, iconKey: PlaylistIconKey, accentIndex: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(existingPlaylist?.id) { mutableStateOf(existingPlaylist?.titleAr.orEmpty()) }
    var subtitle by remember(existingPlaylist?.id) { mutableStateOf(existingPlaylist?.subtitleAr.orEmpty()) }
    var selectedIcon by remember(existingPlaylist?.id) { mutableStateOf(existingPlaylist?.iconKey ?: PlaylistIconKey.QUEUE) }
    var selectedAccent by remember(existingPlaylist?.id) { mutableIntStateOf(existingPlaylist?.accentIndex ?: 1) }
    val qc = MaterialTheme.quranColors

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (existingPlaylist == null) "إنشاء قائمة تلاوة جديدة" else "تعديل القائمة",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("اسم القائمة (مثل: تلاوات الفجر)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("playlist_title_input")
            )

            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("وصف مختصر (اختياري)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("playlist_subtitle_input")
            )

            Text("أيقونة القائمة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaylistIconKey.entries.forEach { key ->
                    val selected = key == selectedIcon
                    FilterChip(
                        selected = selected,
                        onClick = { selectedIcon = key },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(selected),
                        leadingIcon = { Icon(key.toImageVector(), contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(key.labelAr) }
                    )
                }
            }

            Text("لون البطاقة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                (0..5).forEach { idx ->
                    val color = qc.categoryPastelAt(idx)
                    val selected = idx == selectedAccent
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = if (selected) 0.95f else 0.45f))
                            .clickable { selectedAccent = idx }
                    ) {
                        if (selected) Icon(Icons.Rounded.Check, contentDescription = "محدد", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Button(
                onClick = { onSave(title, subtitle, selectedIcon, selectedAccent); onDismiss() },
                enabled = title.trim().isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("save_playlist_button")
            ) {
                Text(if (existingPlaylist == null) "حفظ وإنشاء القائمة" else "حفظ التعديلات", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistTrackSheet(
    surahs: List<SurahInfo>,
    initialSurahNumber: Int = 1,
    onAddTrack: (AudioPlaylistTrack) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    var selectedSurah by remember(initialSurahNumber, surahs) {
        mutableStateOf(surahs.firstOrNull { it.number == initialSurahNumber } ?: surahs.firstOrNull())
    }
    var fullSurahMode by remember { mutableStateOf(true) }
    var startAyah by remember { mutableIntStateOf(1) }
    var endAyah by remember { mutableIntStateOf(selectedSurah?.ayahCount ?: 7) }
    var customLabel by remember { mutableStateOf("") }

    val filteredSurahs = remember(search, surahs) {
        val q = QuranRepository.normalizeArabic(search)
        if (q.isBlank()) surahs else surahs.filter {
            QuranRepository.normalizeArabic(it.nameAr).contains(q) || it.number.toString() == search.trim()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp).padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("إضافة مقطع للقائمة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.Close, contentDescription = "إغلاق") }
            }

            Text("مقاطع مشهورة سريعة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PresetAudioPlaylists.famousPassages.forEach { preset ->
                    Surface(
                        onClick = { onAddTrack(preset.toTrack()); onDismiss() },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(preset.titleAr, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { s ->
                    val isSel = selectedSurah?.number == s.number
                    Surface(
                        onClick = {
                            selectedSurah = s
                            startAyah = 1
                            endAyah = s.ayahCount
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${s.number.toAppDigits()}. سورة ${s.nameAr}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${s.ayahCount.toAppDigits()} آية", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            selectedSurah?.let { surah ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = fullSurahMode,
                        onClick = { fullSurahMode = true; startAyah = 1; endAyah = surah.ayahCount },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(fullSurahMode),
                        label = { Text("السورة كاملة") }
                    )
                    FilterChip(
                        selected = !fullSurahMode,
                        onClick = { fullSurahMode = false; startAyah = 1; endAyah = minOf(10, surah.ayahCount) },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(!fullSurahMode),
                        label = { Text("نطاق آيات محدد") }
                    )
                }

                if (!fullSurahMode) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AyahStepperBox("من الآية", startAyah, 1, surah.ayahCount, { v -> startAyah = v; if (endAyah < v) endAyah = v }, Modifier.weight(1f))
                        AyahStepperBox("إلى الآية", endAyah, startAyah, surah.ayahCount, { v -> endAyah = v }, Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("تسمية المقطع (اختياري)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        onAddTrack(
                            AudioPlaylistTrack(
                                surahNumber = surah.number,
                                startAyah = if (fullSurahMode) 1 else startAyah,
                                endAyah = if (fullSurahMode) surah.ayahCount else endAyah,
                                labelAr = if (fullSurahMode) null else customLabel.trim().takeIf { it.isNotEmpty() }
                            )
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("confirm_add_track_button")
                ) {
                    Text("إضافة سورة ${surah.nameAr} للقائمة", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AyahStepperBox(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = { if (value > min) onChange(value - 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Remove, contentDescription = "إنقاص", modifier = Modifier.size(16.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value.toAppDigits(), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            IconButton(onClick = { if (value < max) onChange(value + 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
            }
        }
    }
}
