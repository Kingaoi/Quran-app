package com.example.ui.screens.prayer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.quranColors

@Composable
internal fun PrayerTopBar(
    showQibla: Boolean,
    onToggleQibla: () -> Unit,
    onOpenHijriCalendar: () -> Unit = {}
) {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.prayer_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = qc.text
                )
                Surface(
                    shape = CircleShape,
                    color = qc.accent.copy(alpha = 0.22f),
                    modifier = Modifier.size(8.dp)
                ) {}
            }
            Text(
                text = stringResource(R.string.prayer_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = qc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = qc.surface,
                border = BorderStroke(1.dp, qc.border),
                modifier = Modifier.size(48.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onOpenHijriCalendar,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = qc.surface,
                        contentColor = qc.primary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("prayer_hijri_calendar_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = stringResource(R.string.prayer_hijri_calendar_cd)
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = if (showQibla) qc.primary else qc.surface,
                border = BorderStroke(1.dp, if (showQibla) qc.primary else qc.border),
                modifier = Modifier.size(48.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onToggleQibla,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (showQibla) qc.primary else qc.surface,
                        contentColor = if (showQibla) qc.onPrimary else qc.primary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("prayer_qibla_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = stringResource(R.string.prayer_qibla_toggle_cd)
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrayerHeroDateNavRow(
    hijriDateAr: String,
    gregorianDateAr: String,
    dayOffset: Int,
    onPrevDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPrevDay,
            modifier = Modifier
                .size(48.dp)
                .testTag("prayer_prev_day_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.prayer_prev_day_cd),
                    tint = qc.primary
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = hijriDateAr,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = qc.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = gregorianDateAr,
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = qc.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (dayOffset != 0) {
                Spacer(modifier = Modifier.height(5.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = qc.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, qc.primary.copy(alpha = 0.30f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToday)
                        .testTag("prayer_today_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = null,
                            tint = qc.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.prayer_today_label),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = qc.primary
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNextDay,
            modifier = Modifier
                .size(48.dp)
                .testTag("prayer_next_day_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.prayer_next_day_cd),
                    tint = qc.primary
                )
            }
        }
    }
}
