package com.example.ui.screens.wird

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.domain.model.JuzInfo
import com.example.domain.model.toArabicIndicDigits
import com.example.domain.wird.WirdPlanCalculator
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewKhatmahSetupSheet(
    ajza: List<JuzInfo>,
    initialStartJuz: Int,
    initialQuartersPerDay: Int,
    onConfirmPlan: (startJuz: Int, quartersPerDay: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var startJuz by remember { mutableIntStateOf(initialStartJuz.coerceIn(1, 30)) }
    val remainingQuarters = WirdPlanCalculator.remainingQuartersFromJuz(startJuz)
    var quartersPerDay by remember {
        mutableIntStateOf(initialQuartersPerDay.coerceIn(1, remainingQuarters))
    }

    val stepPredictiveState = rememberPredictiveBackState(enabled = step == 2) {
        step = 1
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (step == 2) {
                        IconButton(
                            onClick = { step = 1 },
                            modifier = Modifier.size(48.dp).testTag("khatmah_wizard_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "السابق")
                        }
                    }
                    Text(
                        text = "ختمة جديدة",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "الخطوة ${step.toArabicIndicDigits()} من ٢",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = QuranMotionSpecs.wizardStepTransition(isRtl = true),
                modifier = Modifier.fillMaxWidth().predictiveBackTransform(
                    state = stepPredictiveState,
                    maxCornerRadius = 18.dp,
                    maxScaleDrop = 0.05f,
                    maxShiftDp = 16.dp
                ),
                label = "khatmah_wizard_step"
            ) { activeStep ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeStep == 1) {
                        StartPointStepContent(
                            ajza = ajza,
                            startJuz = startJuz,
                            remainingQuarters = remainingQuarters,
                            onSelectStartJuz = { nextJuz ->
                                startJuz = nextJuz
                                val nextRem = WirdPlanCalculator.remainingQuartersFromJuz(nextJuz)
                                if (quartersPerDay > nextRem) quartersPerDay = nextRem
                            },
                            onContinue = { step = 2 }
                        )
                    } else {
                        DurationAndPortionStepContent(
                            totalRemainingQuarters = remainingQuarters,
                            quartersPerDay = quartersPerDay,
                            onChangeQuartersPerDay = { q ->
                                quartersPerDay = q.coerceIn(1, remainingQuarters)
                            },
                            onConfirm = {
                                onConfirmPlan(startJuz, quartersPerDay)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartPointStepContent(
    ajza: List<JuzInfo>,
    startJuz: Int,
    remainingQuarters: Int,
    onSelectStartJuz: (Int) -> Unit,
    onContinue: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = WirdPlanCalculator.formatStartPointAr(startJuz, ajza)
    val selectedInfo = ajza.firstOrNull { it.number == startJuz }

    Text(
        text = "الرجاء تحديد المكان الذي تريد أن تبدأ منه",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "البدء من:",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.weight(1f)) {
            val shape = RoundedCornerShape(14.dp)
            Surface(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                    .clip(shape)
                    .clickable { expanded = true }
                    .testTag("khatmah_start_juz_selector")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "اختر نقطة البداية",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 320.dp)
            ) {
                (1..30).forEach { juzNum ->
                    val label = WirdPlanCalculator.formatStartPointAr(juzNum, ajza)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (juzNum == startJuz) FontWeight.Bold else FontWeight.Normal,
                                color = if (juzNum == startJuz) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        trailingIcon = if (juzNum == startJuz) {
                            { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        } else null,
                        onClick = {
                            onSelectStartJuz(juzNum)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        modifier = Modifier.fillMaxWidth()
    ) {
        val startSurahDetail = selectedInfo?.let {
            "سورة ${it.startSurahNameAr} (آية ${it.startAyahNumber.toArabicIndicDigits()} • صفحة ${it.startPage.toArabicIndicDigits()})"
        } ?: "الفاتحة (صفحة ١)"
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "بداية التلاوة: $startSurahDetail",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "المتبقي حتى نهاية المصحف: ${(31 - startJuz).toArabicIndicDigits()} أجزاء (${remainingQuarters.toArabicIndicDigits()} ربعاً)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }

    Button(
        onClick = onContinue,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("khatmah_step1_continue_button")
    ) {
        Text("الاستمرار", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}
