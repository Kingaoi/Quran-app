package com.example.ui.screens.wird

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.model.toAppDigits
import com.example.ui.theme.quranColors

@Composable
internal fun WizardTopHeader(
    step: Int,
    useWestern: Boolean,
    isEditing: Boolean = false,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val stepTitle = when (step) {
        1 -> "المعلومات الأساسية"
        2 -> "الاتجاه والنطاق"
        3 -> "الهدف والتواريخ"
        else -> "الجلسات والتذكير"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (step > 1) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp).testTag("wizard_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع", tint = qc.text)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditing) "تعديل الخطة" else "خطة جديدة",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = qc.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "الخطوة ${step.toAppDigits(useWestern)}: $stepTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = qc.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    text = "${step.toAppDigits(useWestern)} / ${4.toAppDigits(useWestern)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp).testTag("wizard_close_button")
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = qc.textMuted)
            }
        }
    }
}

@Composable
internal fun WizardProgressSegments(step: Int) {
    val qc = MaterialTheme.quranColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..4) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (i <= step) qc.primary else qc.surface2)
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
internal fun WizardBottomActionsBar(
    step: Int,
    canProceed: Boolean,
    disabledReason: String? = null,
    isEditing: Boolean = false,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    HorizontalDivider(color = qc.border.copy(alpha = 0.6f))
    if (!canProceed && !disabledReason.isNullOrBlank()) {
        Text(
            text = disabledReason,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = qc.danger,
            modifier = Modifier.fillMaxWidth().testTag("wizard_disabled_reason")
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step > 1) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(0.9f).height(52.dp).testTag("wizard_bottom_back_button")
            ) {
                Text("رجوع", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
        Button(
            onClick = onPrimaryAction,
            enabled = canProceed,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
            modifier = Modifier
                .weight(if (step > 1) 1.4f else 1f)
                .height(52.dp)
                .testTag(if (step < 4) "wizard_next_button" else "wizard_create_plan_button")
        ) {
            Text(
                text = when {
                    step < 4 -> "التالي"
                    isEditing -> "حفظ التعديلات"
                    else -> "إنشاء الخطة"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
