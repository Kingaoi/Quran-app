package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.localizeDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.HusunDailyPlanUiState
import com.example.domain.wird.HusunFortressAssignment
import com.example.domain.wird.HusunFortressType
import com.example.ui.theme.quranColors

@Composable
internal fun HusunFeatureCard(
    husunPlan: HusunDailyPlanUiState?,
    onSetupOrConfigure: () -> Unit,
    onOpenDailySheet: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = qc.surface,
        border = BorderStroke(1.2.dp, qc.primary.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth().testTag("husun_feature_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(qc.primary.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = qc.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "نظام الحصون الخمسة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = if (husunPlan == null) {
                            "منهجية متكاملة للحفظ المتقن ومنع التفلت"
                        } else {
                            "محفوظك: ${husunPlan.totalMemorizedPages.toAppDigits(useWestern)} / ${604.toAppDigits(useWestern)} وجهاً • اليوم: ${husunPlan.completedTodayCount.toAppDigits(useWestern)}/${5.toAppDigits(useWestern)} حصون"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (husunPlan != null) {
                    IconButton(
                        onClick = onSetupOrConfigure,
                        modifier = Modifier.size(48.dp).testTag("husun_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "إعدادات نظام الحصون الخمسة",
                            tint = qc.primary
                        )
                    }
                }
            }

            if (husunPlan == null) {
                Text(
                    text = "يجمع بين القراءة المستمرة، التحضير، الحفظ الجديد، مراجعة القريب (آخر ٢٠ وجهاً)، ومراجعة البعيد بورد يومي منظم.",
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.text
                )
                Button(
                    onClick = onSetupOrConfigure,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("husun_start_button")
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("تفعيل نظام الحصون الخمسة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                val pct = (husunPlan.overallProgressFraction * 100f).toInt().coerceIn(0, 100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { husunPlan.overallProgressFraction },
                        color = qc.primary,
                        trackColor = qc.surface2,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50))
                    )
                    Text(
                        text = "${pct.toAppDigits(useWestern)}٪",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    husunPlan.fortresses.forEach { f ->
                        val chipBg = if (f.isCompletedToday) qc.successContainer else qc.bg
                        val chipFg = if (f.isCompletedToday) qc.success else qc.textMuted
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = chipBg,
                            border = BorderStroke(1.dp, if (f.isCompletedToday) qc.success.copy(alpha = 0.45f) else qc.border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "${(f.type.index + 1).toAppDigits(useWestern)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = chipFg
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onOpenDailySheet,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("husun_open_daily_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("فتح حصون اليوم الخمسة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
internal fun HusunFortressCard(
    item: HusunFortressAssignment,
    repeatCount: Int,
    onToggleComplete: () -> Unit,
    onIncrementRepeat: () -> Unit,
    onOpenReader: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val accent = if (item.isCompletedToday) qc.success else qc.primary
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = qc.bg,
        border = BorderStroke(1.dp, if (item.isCompletedToday) qc.success.copy(alpha = 0.55f) else qc.border),
        modifier = Modifier.fillMaxWidth().testTag("husun_fortress_card_${item.type.index}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(shape = CircleShape, color = accent.copy(alpha = 0.14f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (item.type.index + 1).toAppDigits(useWestern),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.type.titleAr,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text
                    )
                    Text(
                        text = item.type.methodDescAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = qc.textMuted
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(role = Role.Checkbox, onClick = onToggleComplete)
                        .testTag("husun_fortress_check_${item.type.index}")
                ) {
                    Icon(
                        imageVector = if (item.isCompletedToday) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = "إنجاز ${item.type.titleAr}",
                        tint = if (item.isCompletedToday) qc.success else qc.textMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Surface(shape = RoundedCornerShape(12.dp), color = qc.surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.rangeTitleAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = qc.primary
                    )
                    Text(
                        text = item.detailSubtitleAr.localizeDigits(useWestern),
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.textMuted
                    )
                }
            }

            if (item.type == HusunFortressType.NEW_MEMORIZATION || item.canOpenReader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.canOpenReader) {
                        FilledTonalButton(
                            onClick = onOpenReader,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = qc.selected, contentColor = qc.onSelected),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("husun_fortress_open_${item.type.index}")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.size(5.dp))
                            Text("اقرأ في المصحف", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    if (item.type == HusunFortressType.NEW_MEMORIZATION) {
                        FilledTonalButton(
                            onClick = onIncrementRepeat,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = qc.surface2, contentColor = qc.text),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp).testTag("husun_repeat_increment_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "تكرار: ${repeatCount.toAppDigits(useWestern)} / ${25.toAppDigits(useWestern)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
