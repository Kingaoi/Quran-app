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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
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
import com.example.ui.theme.quranColors

@Composable
internal fun RangeBoundaryCard(
    heading: String,
    surahName: String,
    ayahLabel: String,
    surahTag: String,
    ayahTag: String,
    onClickSurah: () -> Unit,
    onClickAyah: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = qc.surface2,
        border = BorderStroke(1.dp, qc.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = heading,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = qc.primary
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeSelectorPill(
                    label = surahName,
                    tag = surahTag,
                    onClick = onClickSurah,
                    modifier = Modifier.weight(1.35f)
                )
                RangeSelectorPill(
                    label = ayahLabel,
                    tag = ayahTag,
                    onClick = onClickAyah,
                    modifier = Modifier.weight(0.95f)
                )
            }
        }
    }
}

@Composable
private fun RangeSelectorPill(
    label: String,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val shape = RoundedCornerShape(12.dp)
    Surface(
        shape = shape,
        color = qc.surface,
        modifier = modifier
            .heightIn(min = 48.dp)
            .border(1.dp, qc.border, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = qc.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, tint = qc.primary)
        }
    }
}
