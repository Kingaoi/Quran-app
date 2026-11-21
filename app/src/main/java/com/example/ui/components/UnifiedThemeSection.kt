package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppTheme
import com.example.ui.theme.CreamOnBackgroundDark
import com.example.ui.theme.MushafPaperNight
import com.example.ui.theme.MushafPaperSepia
import com.example.ui.theme.SepiaInk
import com.example.ui.theme.quranColors

@Composable
internal fun UnifiedThemeSection(
    selectedTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "المظهر الموحد (التطبيق والمصحف)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val isSelected = selectedTheme == theme
                    val (bgColor, labelColor) = when (theme) {
                        AppTheme.SYSTEM -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface
                        AppTheme.SEPIA -> MushafPaperSepia to SepiaInk
                        AppTheme.DARK -> MushafPaperNight to CreamOnBackgroundDark
                    }
                    val qc = MaterialTheme.quranColors
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) qc.selected else qc.border,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("unified_theme_${theme.name}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeSwatchBadge(theme = theme, isSelected = isSelected)
                        Text(
                            text = theme.labelAr,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = labelColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatchBadge(theme: AppTheme, isSelected: Boolean) {
    val qc = MaterialTheme.quranColors
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = if (isSelected) qc.selected else Color.Gray.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (theme == AppTheme.SYSTEM) {
            Row(modifier = Modifier.matchParentSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MushafPaperSepia))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MushafPaperNight))
            }
        } else {
            val dotColor = when (theme) {
                AppTheme.SEPIA -> MushafPaperSepia
                AppTheme.DARK -> MushafPaperNight
                AppTheme.SYSTEM -> Color.Transparent
            }
            Box(modifier = Modifier.matchParentSize().background(dotColor))
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(qc.selected),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "محدد",
                    tint = qc.onSelected,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
