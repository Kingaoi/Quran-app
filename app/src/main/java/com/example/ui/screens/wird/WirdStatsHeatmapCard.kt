package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.domain.model.AdvancedStatsSnapshot
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun WirdStatsHeatmapCard(
    snapshot: AdvancedStatsSnapshot,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    var selectedCell by remember(snapshot.todayEpochDay) {
        mutableStateOf(snapshot.heatmap84.lastOrNull())
    }
    val weeks = remember(snapshot.heatmap84) { snapshot.heatmap84.chunked(7) }
    val commitmentPct = ((snapshot.activeDaysLast84 * 100f) / 84f).toInt().coerceIn(0, 100)

    fun colorForLevel(level: Int): Color = when (level) {
        0 -> qc.surface2.copy(alpha = 0.7f)
        1 -> qc.primary.copy(alpha = 0.28f)
        2 -> qc.primary.copy(alpha = 0.50f)
        3 -> qc.primary.copy(alpha = 0.74f)
        else -> qc.primary
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.fillMaxWidth().testTag("stats_heatmap_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = qc.primary, modifier = Modifier.size(19.dp))
                    Column {
                        Text(
                            text = "خريطة الالتزام (١٢ أسبوعاً)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text("${snapshot.activeDaysLast84.toAppDigits(useWestern)} من ٨٤ يوماً نشطاً", style = MaterialTheme.typography.bodySmall, color = qc.textMuted)
                    }
                }
                Surface(shape = RoundedCornerShape(50), color = qc.primary.copy(alpha = 0.14f)) {
                    Text(
                        text = "${commitmentPct.toAppDigits(useWestern)}٪ التزام",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        weeks.forEach { week ->
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { cell ->
                                    val isSelected = selectedCell?.epochDay == cell.epochDay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colorForLevel(cell.intensityLevel))
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, qc.accent, RoundedCornerShape(4.dp))
                                                else Modifier
                                            )
                                            .clickable { selectedCell = cell }
                                    )
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = snapshot.heatmap84.firstOrNull()?.dateLabelAr.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = qc.textMuted
                        )
                        Text(
                            text = "اليوم",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = qc.primary
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                selectedCell?.let { cell ->
                    Text(
                        text = "${cell.dateLabelAr}: ${cell.pages.toAppDigits(useWestern)} ص • ${cell.adhkar.toAppDigits(useWestern)} ذكر • ${cell.tasbih.toAppDigits(useWestern)} تسبيحة",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("أقل", style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                    (0..4).forEach { lvl ->
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(colorForLevel(lvl)))
                    }
                    Text("أكثر", style = MaterialTheme.typography.labelSmall, color = qc.textMuted)
                }
            }
        }
    }
}
