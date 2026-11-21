package com.example.ui.screens.adhkar

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ReadingStats
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

private val DEFAULT_TASBIH_PHRASES = listOf(
    "سبحان الله",
    "الحمد لله",
    "الله أكبر",
    "لا إله إلا الله",
    "أستغفر الله",
    "اللهم صل على محمد"
)

private val TASBIH_TARGETS = listOf(33, 99, 100, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihBottomSheet(
    stats: ReadingStats,
    onIncrement: () -> Unit,
    onAddCustomPhrase: (String) -> Unit,
    onRemoveCustomPhrase: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val useWestern = LocalUseWesternDigits.current

    val allPhrases = remember(stats.customTasbihPhrases) {
        DEFAULT_TASBIH_PHRASES + stats.customTasbihPhrases.filterNot { it in DEFAULT_TASBIH_PHRASES }
    }
    var selectedPhrase by remember { mutableStateOf(DEFAULT_TASBIH_PHRASES.first()) }
    var targetCount by remember { mutableIntStateOf(33) }
    var sessionCount by remember { mutableIntStateOf(0) }
    var showAddPhraseDialog by remember { mutableStateOf(false) }

    val progressFraction = if (targetCount > 0) {
        (sessionCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "tasbih_ring")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المسبحة الإلكترونية",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { showAddPhraseDialog = true },
                        modifier = Modifier.testTag("tasbih_add_phrase_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "إضافة ذكر مخصص")
                    }
                    IconButton(
                        onClick = { sessionCount = 0 },
                        modifier = Modifier.testTag("tasbih_reset_session_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "تصفير الجلسة")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allPhrases.forEach { phrase ->
                    val isCustom = phrase !in DEFAULT_TASBIH_PHRASES
                    val isSelected = selectedPhrase == phrase
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                selectedPhrase = phrase
                                sessionCount = 0
                            }
                        },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = { Text(phrase) },
                        trailingIcon = if (isCustom) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "حذف العبارة",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            if (selectedPhrase == phrase) {
                                                selectedPhrase = DEFAULT_TASBIH_PHRASES.first()
                                            }
                                            onRemoveCustomPhrase(phrase)
                                        }
                                )
                            }
                        } else null
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                TASBIH_TARGETS.forEach { tgt ->
                    val isSelected = targetCount == tgt
                    FilterChip(
                        selected = isSelected,
                        onClick = { targetCount = tgt },
                        colors = quranFilterChipColors(),
                        border = quranFilterChipBorder(isSelected),
                        label = { Text(if (tgt == 0) "مفتوح" else tgt.toAppDigits(useWestern)) },
                        modifier = Modifier.testTag("tasbih_target_$tgt")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(204.dp)
                    .clip(CircleShape)
                    .clickable {
                        val next = sessionCount + 1
                        val hitTarget = targetCount > 0 && next % targetCount == 0
                        sessionCount = if (targetCount > 0 && sessionCount >= targetCount) 1 else next
                        onIncrement()
                        haptic.performHapticFeedback(
                            if (hitTarget) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                        )
                    }
                    .testTag("tasbih_tap_button")
            ) {
                val qc = MaterialTheme.quranColors
                CircularProgressIndicator(
                    progress = { if (targetCount > 0) animatedProgress else 1f },
                    strokeWidth = 10.dp,
                    trackColor = qc.surface2,
                    color = if (targetCount > 0 && sessionCount >= targetCount) qc.success else qc.primary,
                    modifier = Modifier.size(200.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = sessionCount.toAppDigits(useWestern),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (targetCount > 0) {
                        Text(
                            text = "من ${targetCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedPhrase,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("اليوم", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = stats.tasbihTodayCount.toAppDigits(useWestern),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الإجمالي", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = stats.tasbihTotalCount.toAppDigits(useWestern),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showAddPhraseDialog) {
        AddTasbihPhraseDialog(
            onConfirm = { phrase ->
                onAddCustomPhrase(phrase)
                selectedPhrase = phrase.trim()
                sessionCount = 0
                showAddPhraseDialog = false
            },
            onDismiss = { showAddPhraseDialog = false }
        )
    }
}
