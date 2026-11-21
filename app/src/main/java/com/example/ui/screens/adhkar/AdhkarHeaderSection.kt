package com.example.ui.screens.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.components.QuranDialogPresetChip
import com.example.ui.components.QuranDialogTextField
import com.example.ui.components.QuranStyledDialog

private val SUGGESTED_TASBIH_PHRASES = listOf(
    "لا حول ولا قوة إلا بالله",
    "أستغفر الله العظيم",
    "سبحان الله وبحمده",
    "حسبي الله ونعم الوكيل"
)

@Composable
internal fun AdhkarCategoriesHeader(
    completedTotal: Int,
    totalCount: Int,
    isHisnSynced: Boolean = false,
    isSyncingSource: Boolean = false,
    onRefreshSource: () -> Unit = {},
    onOpenTasbih: () -> Unit
) {
    val useWestern = LocalUseWesternDigits.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "الأذكار",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = when {
                    isSyncingSource -> "جاري التحديث من حصن المسلم..."
                    isHisnSynced -> "محدّث من كتاب حصن المسلم"
                    else -> "حصن المسلم والأدعية المأثورة"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isHisnSynced || isSyncingSource) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onRefreshSource,
                enabled = !isSyncingSource,
                modifier = Modifier.size(48.dp).testTag("adhkar_refresh_source_button")
            ) {
                if (isSyncingSource) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "تحديث الأذكار من حصن المسلم",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (totalCount > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${completedTotal.toAppDigits(useWestern)} / ${totalCount.toAppDigits(useWestern)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            FilledTonalIconButton(
                onClick = onOpenTasbih,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = "فتح المسبحة الإلكترونية"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddTasbihPhraseDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val inputState = remember { mutableStateOf("") }
    val trimmed = inputState.value.trim()

    fun submitIfReady() {
        if (trimmed.isNotEmpty()) onConfirm(trimmed)
    }

    QuranStyledDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Fingerprint,
        title = "إضافة ذكر للمسبحة",
        subtitle = "أضف ذكراً مخصصاً أو اختر من المقترحات",
        confirmLabel = "إضافة",
        confirmEnabled = trimmed.isNotEmpty(),
        onConfirm = ::submitIfReady
    ) {
        QuranDialogTextField(
            value = inputState.value,
            onValueChange = { inputState.value = it },
            label = "عبارة التسبيح أو الذكر",
            placeholder = "مثال: لا حول ولا قوة إلا بالله",
            leadingIcon = Icons.Rounded.Edit,
            onDone = ::submitIfReady,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tasbih_add_phrase_input")
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SUGGESTED_TASBIH_PHRASES.forEach { suggestion ->
                QuranDialogPresetChip(
                    label = suggestion,
                    selected = trimmed == suggestion,
                    onClick = { inputState.value = suggestion }
                )
            }
        }
    }
}
