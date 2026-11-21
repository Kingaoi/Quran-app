package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AyahItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.SurahInfo
import com.example.domain.model.toAppDigits
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdDirection
import com.example.domain.wird.WirdPlanType
import com.example.ui.theme.quranColors

private enum class RangePickerTarget {
    FROM_SURAH, FROM_AYAH, TO_SURAH, TO_AYAH
}

@Composable
internal fun CreatePlanStep2DirectionRange(
    planType: WirdPlanType,
    direction: WirdDirection,
    fromSurah: Int,
    fromAyah: Int,
    toSurah: Int,
    toAyah: Int,
    isPeriodicWird: Boolean,
    surahs: List<SurahInfo>,
    allAyahs: List<AyahItem>,
    onSelectDirection: (WirdDirection) -> Unit,
    onUpdateRange: (fromSurah: Int, fromAyah: Int, toSurah: Int, toAyah: Int) -> Unit,
    onChangePeriodicWird: (Boolean) -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val resolvedSurahs = remember(surahs) { surahs.ifEmpty { fallbackSurahList() } }
    var activePicker by remember { mutableStateOf<RangePickerTarget?>(null) }

    val fromSurahInfo = resolvedSurahs.firstOrNull { it.number == fromSurah } ?: resolvedSurahs.first()
    val toSurahInfo = resolvedSurahs.firstOrNull { it.number == toSurah } ?: resolvedSurahs.last()

    val rangeMetrics = remember(direction, fromSurah, fromAyah, toSurah, toAyah, allAyahs, resolvedSurahs) {
        CustomWirdPlanCalculator.calculateMetrics(
            config = NamedWirdPlanConfig(
                id = "preview",
                title = "preview",
                direction = direction,
                fromSurah = fromSurah,
                fromAyah = fromAyah,
                toSurah = toSurah,
                toAyah = toAyah
            ),
            allAyahs = allAyahs,
            surahs = resolvedSurahs
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "الاتجاه",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WirdDirection.entries.forEach { dir ->
                val isSelected = direction == dir
                Surface(
                    onClick = { onSelectDirection(dir) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) qc.selected.copy(alpha = 0.16f) else qc.surface2,
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) qc.primary else qc.border),
                    modifier = Modifier.weight(1f).heightIn(min = 66.dp).testTag("wizard_dir_${dir.key}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (dir == WirdDirection.FATIHA_TO_NAS) {
                                Icons.Rounded.ArrowDownward
                            } else {
                                Icons.Rounded.ArrowUpward
                            },
                            contentDescription = null,
                            tint = if (isSelected) qc.primary else qc.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = dir.labelAr,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) qc.primary else qc.text,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = qc.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Text(
            text = if (planType == WirdPlanType.MEMORIZATION) "نطاق الحفظ" else "نطاق الخطة",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = qc.text,
            modifier = Modifier.testTag("wizard_range_section_title")
        )

        RangeBoundaryCard(
            heading = "من",
            surahName = "${fromSurahInfo.number.toAppDigits(useWestern)}. ${fromSurahInfo.nameAr}",
            ayahLabel = "الآية ${fromAyah.toAppDigits(useWestern)}",
            surahTag = "wizard_from_surah",
            ayahTag = "wizard_from_ayah",
            onClickSurah = { activePicker = RangePickerTarget.FROM_SURAH },
            onClickAyah = { activePicker = RangePickerTarget.FROM_AYAH }
        )

        RangeBoundaryCard(
            heading = "إلى",
            surahName = "${toSurahInfo.number.toAppDigits(useWestern)}. ${toSurahInfo.nameAr}",
            ayahLabel = "الآية ${toAyah.toAppDigits(useWestern)}",
            surahTag = "wizard_to_surah",
            ayahTag = "wizard_to_ayah",
            onClickSurah = { activePicker = RangePickerTarget.TO_SURAH },
            onClickAyah = { activePicker = RangePickerTarget.TO_AYAH }
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().testTag("wizard_range_preview")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "النطاق المختار: من ${fromSurahInfo.nameAr} (${fromAyah.toAppDigits(useWestern)}) إلى ${toSurahInfo.nameAr} (${toAyah.toAppDigits(useWestern)})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                val pagesLabel = CustomWirdPlanCalculator.formatAmountAr(rangeMetrics.totalPagesExact, WirdAmountUnit.PAGE)
                Text(
                    text = "إجمالي النطاق: ${rangeMetrics.totalAyahs.toAppDigits(useWestern)} آية • $pagesLabel تقريباً",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        if (planType == WirdPlanType.RECITATION) {
            Surface(
                onClick = { onChangePeriodicWird(!isPeriodicWird) },
                shape = RoundedCornerShape(16.dp),
                color = qc.surface2,
                border = BorderStroke(1.dp, if (isPeriodicWird) qc.primary else qc.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "وِرد دوري",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = qc.text
                        )
                        Text(
                            text = "تلاوة في أيام معيّنة، مثل الكهف يوم الجمعة",
                            style = MaterialTheme.typography.bodySmall,
                            color = qc.textMuted
                        )
                    }
                    Switch(
                        checked = isPeriodicWird,
                        onCheckedChange = onChangePeriodicWird,
                        modifier = Modifier.testTag("wizard_periodic_wird_switch")
                    )
                }
            }
        }
    }

    when (activePicker) {
        RangePickerTarget.FROM_SURAH -> SurahPickerDialog(
            title = "اختر السورة في خانة «من»",
            surahs = resolvedSurahs,
            selectedSurahNumber = fromSurah,
            onSelectSurah = { selected ->
                val nextToS = if (direction == WirdDirection.FATIHA_TO_NAS && selected.number > toSurah) {
                    selected.number
                } else if (direction == WirdDirection.NAS_TO_FATIHA && selected.number < toSurah) {
                    selected.number
                } else {
                    toSurah
                }
                val nextToInfo = resolvedSurahs.first { it.number == nextToS }
                val nextToA = if (nextToS != toSurah) nextToInfo.ayahCount else toAyah.coerceIn(1, nextToInfo.ayahCount)
                onUpdateRange(selected.number, 1, nextToS, nextToA)
            },
            onDismiss = { activePicker = null }
        )

        RangePickerTarget.FROM_AYAH -> AyahNumberPickerDialog(
            surahNameAr = fromSurahInfo.nameAr,
            maxAyah = fromSurahInfo.ayahCount,
            selectedAyah = fromAyah,
            onSelectAyah = { ayah ->
                val adjustedToA = if (fromSurah == toSurah && ayah > toAyah) ayah else toAyah
                onUpdateRange(fromSurah, ayah, toSurah, adjustedToA)
            },
            onDismiss = { activePicker = null }
        )

        RangePickerTarget.TO_SURAH -> SurahPickerDialog(
            title = "اختر السورة في خانة «إلى»",
            surahs = resolvedSurahs,
            selectedSurahNumber = toSurah,
            onSelectSurah = { selected ->
                val nextFromS = if (direction == WirdDirection.FATIHA_TO_NAS && selected.number < fromSurah) {
                    selected.number
                } else if (direction == WirdDirection.NAS_TO_FATIHA && selected.number > fromSurah) {
                    selected.number
                } else {
                    fromSurah
                }
                val nextFromInfo = resolvedSurahs.first { it.number == nextFromS }
                val nextFromA = if (nextFromS != fromSurah) 1 else fromAyah.coerceIn(1, nextFromInfo.ayahCount)
                onUpdateRange(nextFromS, nextFromA, selected.number, selected.ayahCount)
            },
            onDismiss = { activePicker = null }
        )

        RangePickerTarget.TO_AYAH -> AyahNumberPickerDialog(
            surahNameAr = toSurahInfo.nameAr,
            maxAyah = toSurahInfo.ayahCount,
            selectedAyah = toAyah,
            onSelectAyah = { ayah ->
                val adjustedFromA = if (fromSurah == toSurah && ayah < fromAyah) ayah else fromAyah
                onUpdateRange(fromSurah, adjustedFromA, toSurah, ayah)
            },
            onDismiss = { activePicker = null }
        )

        null -> Unit
    }
}
