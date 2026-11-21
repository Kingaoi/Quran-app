package com.example.ui.screens.wird

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.wird.WirdPlanAudience
import com.example.domain.wird.WirdPlanType
import com.example.ui.components.QuranDialogTextField
import com.example.ui.theme.quranColors
import com.example.ui.theme.quranFilterChipBorder
import com.example.ui.theme.quranFilterChipColors

private val SUGGESTED_TITLES = listOf(
    "حفظ سورة البقرة",
    "مراجعة جزء عمّ",
    "تلاوة يومية",
    "حفظ ومراجعة آل عمران",
    "ختمة تدبّر"
)

@Composable
internal fun CreatePlanStep1BasicInfo(
    title: String,
    planType: WirdPlanType,
    audience: WirdPlanAudience,
    allowFollowUp: Boolean,
    onTitleChange: (String) -> Unit,
    onSelectPlanType: (WirdPlanType) -> Unit,
    onSelectAudience: (WirdPlanAudience) -> Unit,
    onChangeAllowFollowUp: (Boolean) -> Unit
) {
    val qc = MaterialTheme.quranColors
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "المعلومات الأساسية",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = qc.text
        )

        QuranDialogTextField(
            value = title,
            onValueChange = { if (it.length <= 44) onTitleChange(it) },
            label = "العنوان",
            placeholder = "مثلاً: حفظ سورة البقرة",
            leadingIcon = Icons.Rounded.EditNote,
            modifier = Modifier.fillMaxWidth().testTag("wizard_title_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SUGGESTED_TITLES.forEach { suggestion ->
                val matched = title.trim() == suggestion
                FilterChip(
                    selected = matched,
                    onClick = { onTitleChange(suggestion) },
                    colors = quranFilterChipColors(),
                    border = quranFilterChipBorder(matched),
                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "نوع الخطة",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                val accent = qc.categoryPastelAt(planType.pastelIndex)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = qc.surface2,
                    border = BorderStroke(1.dp, qc.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { typeMenuExpanded = true }
                        .testTag("wizard_plan_type_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(accent.copy(alpha = 0.16f))
                        ) {
                            Icon(planTypeIcon(planType), contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = planType.labelAr,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = qc.text,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "اختر نوع الخطة", tint = qc.primary)
                    }
                }

                DropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    WirdPlanType.entries.forEach { option ->
                        val selected = option == planType
                        val optAccent = qc.categoryPastelAt(option.pastelIndex)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.labelAr,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) qc.primary else qc.text
                                )
                            },
                            leadingIcon = {
                                Icon(planTypeIcon(option), contentDescription = null, tint = optAccent)
                            },
                            trailingIcon = if (selected) {
                                { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = qc.primary, modifier = Modifier.size(18.dp)) }
                            } else null,
                            onClick = {
                                onSelectPlanType(option)
                                typeMenuExpanded = false
                            },
                            modifier = Modifier.testTag("wizard_type_${option.key}")
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "لمن هذه الخطة؟",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WirdPlanAudience.entries.forEach { option ->
                    BinaryOptionPill(
                        label = option.labelAr,
                        selected = audience == option,
                        tag = "wizard_audience_${option.key}",
                        onClick = { onSelectAudience(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "هل تريد أن يتابعك أحد؟",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = qc.text
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BinaryOptionPill(
                    label = "لا",
                    selected = !allowFollowUp,
                    tag = "wizard_followup_no",
                    onClick = { onChangeAllowFollowUp(false) },
                    modifier = Modifier.weight(1f)
                )
                BinaryOptionPill(
                    label = "نعم",
                    selected = allowFollowUp,
                    tag = "wizard_followup_yes",
                    onClick = { onChangeAllowFollowUp(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BinaryOptionPill(
    label: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) qc.selected.copy(alpha = 0.16f) else qc.surface2,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) qc.primary else qc.border),
        modifier = modifier.heightIn(min = 48.dp).testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) qc.primary else qc.text,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun planTypeIcon(type: WirdPlanType): ImageVector = when (type) {
    WirdPlanType.MEMORIZATION -> Icons.Rounded.School
    WirdPlanType.REVIEW -> Icons.Rounded.Repeat
    WirdPlanType.RECITATION -> Icons.AutoMirrored.Rounded.MenuBook
    WirdPlanType.MEMORIZATION_AND_REVIEW -> Icons.Rounded.Psychology
}
