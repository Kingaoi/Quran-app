package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AdvancedStatsSnapshot
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.StatsAchievementBadge
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun WirdStatsMilestonesCard(
    snapshot: AdvancedStatsSnapshot,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val unlockedCount = snapshot.badges.count { it.isUnlocked }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = qc.surface,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.fillMaxWidth().testTag("stats_milestones_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = qc.accent, modifier = Modifier.size(19.dp))
                    Text(
                        text = "أوسمة الإنجاز والهمّة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = qc.accent.copy(alpha = 0.14f)) {
                    Text(
                        text = "${unlockedCount.toAppDigits(useWestern)}/${snapshot.badges.size.toAppDigits(useWestern)} مفتوح",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = qc.accent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            snapshot.badges.chunked(2).forEach { rowBadges ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowBadges.forEach { badge ->
                        MilestoneBadgeTile(badge = badge, useWestern = useWestern, modifier = Modifier.weight(1f))
                    }
                    if (rowBadges.size == 1) Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MilestoneBadgeTile(
    badge: StatsAchievementBadge,
    useWestern: Boolean,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val accent = qc.categoryPastelAt(badge.accentIndex)
    val bgColor = if (badge.isUnlocked) accent.copy(alpha = 0.12f) else qc.surface2.copy(alpha = 0.55f)
    val borderColor = if (badge.isUnlocked) accent.copy(alpha = 0.48f) else qc.border

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.testTag("stats_badge_${badge.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(11.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(if (badge.isUnlocked) accent else qc.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (badge.isUnlocked) Icons.Rounded.Verified else Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) qc.onPrimary else qc.textMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
                val capped = badge.currentValue.coerceAtMost(badge.targetValue)
                val pct = (badge.progressFraction * 100).toInt()
                Text(
                    text = if (badge.isUnlocked) "${100.toAppDigits(useWestern)}٪" else "${capped.toAppDigits(useWestern)}/${badge.targetValue.toAppDigits(useWestern)} • ${pct.toAppDigits(useWestern)}٪",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (badge.isUnlocked) accent else qc.textMuted,
                    maxLines = 1
                )
            }
            Text(
                text = badge.titleAr,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = qc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = badge.requirementAr,
                style = MaterialTheme.typography.labelSmall,
                color = qc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { badge.progressFraction },
                color = if (badge.isUnlocked) accent else qc.primary,
                trackColor = qc.border.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
