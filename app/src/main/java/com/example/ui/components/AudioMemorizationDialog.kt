package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.audio.MemorizationLoopConfig
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioMemorizationDialog(
    surahNumber: Int,
    surahNameAr: String,
    totalAyahs: Int,
    initialAyah: Int,
    currentLoop: MemorizationLoopConfig,
    isSurahLevelReciter: Boolean = false,
    onStartLoop: (MemorizationLoopConfig) -> Unit,
    onClearLoop: () -> Unit,
    onDismiss: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxAyah = totalAyahs.coerceAtLeast(1)
    var startAyah by remember(surahNumber) {
        mutableIntStateOf(
            if (currentLoop.enabled && currentLoop.surahNumber == surahNumber) {
                currentLoop.startAyah.coerceIn(1, maxAyah)
            } else {
                initialAyah.coerceIn(1, maxAyah)
            }
        )
    }
    var endAyah by remember(surahNumber) {
        mutableIntStateOf(
            if (currentLoop.enabled && currentLoop.surahNumber == surahNumber) {
                currentLoop.endAyah.coerceIn(startAyah, maxAyah)
            } else {
                (initialAyah + 4).coerceIn(startAyah, maxAyah)
            }
        )
    }
    var ayahRepeat by remember { mutableIntStateOf(currentLoop.ayahRepeatCount.coerceIn(1, 10)) }
    var rangeRepeat by remember { mutableIntStateOf(currentLoop.rangeRepeatCount) }
    var pauseSeconds by remember { mutableIntStateOf(currentLoop.pauseSeconds) }
    val selectedCount = (endAyah - startAyah + 1).coerceAtLeast(1)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MemorizationHeroHeader(
                surahNameAr = surahNameAr,
                startAyah = startAyah,
                endAyah = endAyah,
                selectedCount = selectedCount,
                useWestern = useWestern
            )

            if (isSurahLevelReciter) {
                Text(
                    text = "تنبيه: القارئ الحالي يعتمد تلاوة السورة كاملة؛ للتكرار الآية بآية اختر قارئاً بتلاوة مقطّعة للآيات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            OrnamentalDivider()

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "نطاق الآيات",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    AyahStepperRow(
                        label = "بداية النطاق",
                        value = startAyah,
                        minValue = 1,
                        maxValue = maxAyah,
                        useWestern = useWestern,
                        onValueChange = { next ->
                            startAyah = next.coerceIn(1, maxAyah)
                            if (endAyah < startAyah) endAyah = startAyah
                        }
                    )
                    if (maxAyah > 1) {
                        Slider(
                            value = startAyah.toFloat(),
                            onValueChange = { v ->
                                startAyah = v.roundToInt().coerceIn(1, maxAyah)
                                if (endAyah < startAyah) endAyah = startAyah
                            },
                            valueRange = 1f..maxAyah.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AyahStepperRow(
                        label = "نهاية النطاق",
                        value = endAyah,
                        minValue = startAyah,
                        maxValue = maxAyah,
                        useWestern = useWestern,
                        onValueChange = { next ->
                            endAyah = next.coerceIn(startAyah, maxAyah)
                        }
                    )
                    if (startAyah < maxAyah) {
                        Slider(
                            value = endAyah.toFloat(),
                            onValueChange = { v -> endAyah = v.roundToInt().coerceIn(startAyah, maxAyah) },
                            valueRange = startAyah.toFloat()..maxAyah.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoopOptionSection(icon = Icons.Rounded.RepeatOne, title = "تكرار كل آية") {
                        listOf(1, 2, 3, 5).forEach { cnt ->
                            MemorizationChoiceChip(
                                selected = ayahRepeat == cnt,
                                label = "${cnt.toAppDigits(useWestern)}×",
                                onClick = { ayahRepeat = cnt }
                            )
                        }
                    }

                    LoopOptionSection(icon = Icons.Rounded.Repeat, title = "تكرار المقطع كاملاً") {
                        listOf(1, 3, 5, 0).forEach { cnt ->
                            MemorizationChoiceChip(
                                selected = rangeRepeat == cnt,
                                label = if (cnt == 0) "مستمر" else "${cnt.toAppDigits(useWestern)}×",
                                onClick = { rangeRepeat = cnt }
                            )
                        }
                    }

                    LoopOptionSection(icon = Icons.Rounded.HourglassEmpty, title = "سكتة بين التلاوات للترديد") {
                        listOf(0, 2, 4, 6).forEach { sec ->
                            MemorizationChoiceChip(
                                selected = pauseSeconds == sec,
                                label = if (sec == 0) "بدون" else "${sec.toAppDigits(useWestern)}ث",
                                onClick = { pauseSeconds = sec }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onStartLoop(
                            MemorizationLoopConfig(
                                enabled = true,
                                surahNumber = surahNumber,
                                startAyah = startAyah,
                                endAyah = endAyah,
                                ayahRepeatCount = ayahRepeat,
                                rangeRepeatCount = rangeRepeat,
                                pauseSeconds = pauseSeconds
                            )
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("start_memorization_loop_button")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بدء التكرار", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }

                if (currentLoop.enabled) {
                    OutlinedButton(
                        onClick = { onClearLoop(); onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إيقاف")
                    }
                }

                TextButton(onClick = onDismiss, modifier = Modifier.height(50.dp)) {
                    Text("إغلاق")
                }
            }
        }
    }
}
