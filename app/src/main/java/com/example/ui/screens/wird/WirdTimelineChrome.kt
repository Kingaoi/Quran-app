package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun TimelineTopBar(
    title: String,
    onBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalIconButton(
            onClick = onBack,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = qc.surface2, contentColor = qc.text),
            modifier = Modifier.size(48.dp).testTag("timeline_back_button")
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = qc.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(50),
            color = qc.surface2,
            border = BorderStroke(1.dp, qc.border)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenEdit, modifier = Modifier.size(48.dp).testTag("timeline_edit_button")) {
                    Icon(Icons.Outlined.Edit, contentDescription = "تعديل الخطة", tint = qc.text)
                }
                VerticalDivider(modifier = Modifier.height(22.dp).width(1.dp), color = qc.border)
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp).testTag("timeline_settings_button")) {
                    Icon(Icons.Rounded.Tune, contentDescription = "إعدادات الخطة", tint = qc.text)
                }
            }
        }
    }
}

@Composable
internal fun TimelineExpandButton(
    expanded: Boolean,
    hiddenCount: Int,
    useWestern: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(onClick = onClick, modifier = Modifier.testTag(testTag)) {
            Text(
                text = if (expanded) "عرض أقل" else "عرض كل الجلسات (${hiddenCount.toAppDigits(useWestern)})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = qc.accent
            )
            Spacer(modifier = Modifier.size(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = qc.success,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
