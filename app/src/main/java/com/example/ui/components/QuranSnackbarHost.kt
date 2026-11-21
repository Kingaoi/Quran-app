package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.quranColors

suspend fun SnackbarHostState.showQuranToast(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}

@Composable
fun QuranSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        QuranToastCard(data = data)
    }
}

private data class QuranToastVisualStyle(
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
private fun resolveToastVisualStyle(message: String): QuranToastVisualStyle {
    val qc = MaterialTheme.quranColors
    return when {
        message.contains("تقبل") || message.contains("تقبّل") || message.contains("ما شاء الله") ||
            message.contains("أتممت") || message.contains("ختمة") ->
            QuranToastVisualStyle(Icons.Rounded.Verified, qc.success)

        message.contains("نسخ") ->
            QuranToastVisualStyle(Icons.Rounded.ContentCopy, qc.primary)

        message.contains("حذف") || message.contains("إزالة") || message.contains("تصفير") || message.contains("إيقاف") ->
            QuranToastVisualStyle(Icons.Rounded.DeleteOutline, qc.danger)

        message.contains("التراجع") ->
            QuranToastVisualStyle(Icons.Rounded.History, qc.accent)

        message.contains("تعذّر") || message.contains("رفض") || message.contains("يرجى") ||
            message.contains("غير صالح") || message.contains("حدد") ->
            QuranToastVisualStyle(Icons.Rounded.ErrorOutline, qc.danger)

        message.contains("حفظ") || message.contains("استعادة") || message.contains("تصدير") ||
            message.contains("تحديث") || message.contains("تفعيل") || message.contains("بدء") ->
            QuranToastVisualStyle(Icons.Rounded.CheckCircle, qc.success)

        else ->
            QuranToastVisualStyle(Icons.Rounded.Info, qc.primary)
    }
}

@Composable
private fun QuranToastCard(
    data: SnackbarData,
    modifier: Modifier = Modifier
) {
    val style = resolveToastVisualStyle(data.visuals.message)
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.34f)),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("quran_custom_toast")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(style.accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            data.visuals.actionLabel?.let { label ->
                TextButton(
                    onClick = { data.performAction() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("quran_toast_action_button")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = style.accentColor
                    )
                }
            }
        }
    }
}
