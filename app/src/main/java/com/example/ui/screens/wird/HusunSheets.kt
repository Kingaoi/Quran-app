package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.HusunConfig
import com.example.domain.wird.HusunDailyPlanUiState
import com.example.domain.wird.HusunFortressType
import com.example.domain.wird.HusunNewRate
import com.example.domain.wird.WirdDirection
import com.example.ui.theme.quranColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HusunDailySheet(
    husunPlan: HusunDailyPlanUiState,
    onToggleFortress: (HusunFortressType) -> Unit,
    onIncrementRepeat: () -> Unit,
    onOpenReaderPage: (Int) -> Unit,
    onCompleteDayAndAdvance: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = qc.surface,
        contentColor = qc.text
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("husun_daily_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حصون اليوم الخمسة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = "أنجزت ${husunPlan.completedTodayCount.toAppDigits(useWestern)} من ${5.toAppDigits(useWestern)} حصون • إجمالي الحفظ: ${husunPlan.totalMemorizedPages.toAppDigits(useWestern)} وجهاً",
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.textMuted
                    )
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Settings, contentDescription = "إعدادات الحصون", tint = qc.primary)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.textMuted)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).testTag("husun_daily_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(husunPlan.fortresses, key = { it.type.index }) { fortress ->
                    HusunFortressCard(
                        item = fortress,
                        repeatCount = husunPlan.todayRepeatCount,
                        onToggleComplete = { onToggleFortress(fortress.type) },
                        onIncrementRepeat = onIncrementRepeat,
                        onOpenReader = {
                            onDismiss()
                            onOpenReaderPage(fortress.targetPage)
                        }
                    )
                }
            }

            Button(
                onClick = onCompleteDayAndAdvance,
                enabled = !husunPlan.isAllHifzCompleted,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("husun_advance_day_button")
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = if (husunPlan.isAllHifzCompleted) "أتممت حفظ المصحف الكريم بحمد الله" else "اعتماد إنجاز اليوم والانتقال للورد التالي",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HusunSetupSheet(
    initialConfig: HusunConfig?,
    onConfirmSetup: (WirdDirection, Int, HusunNewRate, Int, Int) -> Unit,
    onResetHusun: () -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var direction by remember { mutableStateOf(initialConfig?.direction ?: WirdDirection.FATIHA_TO_NAS) }
    var startingPages by remember { mutableIntStateOf(initialConfig?.startingMemorizedPages ?: 0) }
    var newRate by remember { mutableStateOf(initialConfig?.dailyNewRate ?: HusunNewRate.ONE_PAGE) }
    var continuousPages by remember { mutableIntStateOf(initialConfig?.continuousPagesPerDay ?: 40) }
    var farDivisions by remember { mutableIntStateOf(initialConfig?.farReviewDivisions ?: 7) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = qc.surface,
        contentColor = qc.text
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .testTag("husun_setup_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("إعداد نظام الحصون الخمسة", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = qc.text)
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.textMuted)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HusunOptionSectionTitle("اتجاه الحفظ والمراجعة")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WirdDirection.entries.forEach { dir ->
                        HusunSelectChip(
                            label = dir.labelAr,
                            selected = direction == dir,
                            onClick = { direction = dir },
                            modifier = Modifier.weight(1f).testTag("husun_dir_${dir.key}")
                        )
                    }
                }

                HusunOptionSectionTitle("كم وجهاً تحفظ حالياً من هذا الاتجاه؟")
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalIconButton(
                        onClick = { startingPages = (startingPages - 1).coerceAtLeast(0) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.text),
                        modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.Rounded.Remove, contentDescription = "إنقاص وجه") }
                    Surface(shape = RoundedCornerShape(14.dp), color = qc.bg, border = BorderStroke(1.dp, qc.border), modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${startingPages.toAppDigits(useWestern)} وجهاً",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = qc.primary,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { startingPages = (startingPages + 1).coerceAtMost(603) },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.text),
                        modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.Rounded.Add, contentDescription = "زيادة وجه") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "من الصفر", 20 to "جزء (٢٠)", 40 to "جزءان (٤٠)", 100 to "١٠٠ وجه").forEach { (preset, label) ->
                        HusunSelectChip(label = label, selected = startingPages == preset, onClick = { startingPages = preset }, modifier = Modifier.weight(1f))
                    }
                }

                HusunOptionSectionTitle("مقدار الحفظ الجديد اليومي (الحصن ٣)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HusunNewRate.entries.forEach { rate ->
                        HusunSelectChip(
                            label = rate.shortAr,
                            selected = newRate == rate,
                            onClick = { newRate = rate },
                            modifier = Modifier.weight(1f).testTag("husun_rate_${rate.key}")
                        )
                    }
                }

                HusunOptionSectionTitle("ورد القراءة المستمرة (الحصن ١)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20 to "جزء (٢٠ وجهاً)", 40 to "جزءان (٤٠ وجهاً)").forEach { (pages, label) ->
                        HusunSelectChip(label = label, selected = continuousPages == pages, onClick = { continuousPages = pages }, modifier = Modifier.weight(1f))
                    }
                }

                HusunOptionSectionTitle("دورة مراجعة البعيد (الحصن ٥)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5 to "٥ أيام", 7 to "أسبوع", 10 to "١٠ أيام", 14 to "أسبوعين").forEach { (days, label) ->
                        HusunSelectChip(label = label, selected = farDivisions == days, onClick = { farDivisions = days }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Button(
                onClick = { onConfirmSetup(direction, startingPages, newRate, continuousPages, farDivisions); onDismiss() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("husun_save_setup_button")
            ) {
                Text("حفظ وتفعيل نظام الحصون", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            if (initialConfig?.isActive == true) {
                OutlinedButton(
                    onClick = { onResetHusun(); onDismiss() },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, qc.danger.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("husun_reset_button")
                ) {
                    Text("إيقاف خطة الحصون الحالية", color = qc.danger, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun HusunOptionSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.quranColors.text
    )
}

@Composable
private fun HusunSelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) qc.selected else qc.bg,
        border = BorderStroke(1.dp, if (selected) qc.selected else qc.border),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) qc.onSelected else qc.text,
                maxLines = 1
            )
        }
    }
}
