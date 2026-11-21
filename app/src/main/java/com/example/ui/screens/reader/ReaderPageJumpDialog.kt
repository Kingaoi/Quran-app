package com.example.ui.screens.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.toAppDigits
import com.example.ui.components.QuranDialogPresetChip
import com.example.ui.components.QuranDialogTextField
import com.example.ui.components.QuranStyledDialog

private val QUICK_PAGE_PRESETS = listOf(
    1 to "الفاتحة",
    302 to "المنتصف",
    582 to "جزء عم",
    604 to "النهاية"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PageJumpAlertDialog(
    initialPage: Int,
    onConfirmPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pageInput by remember { mutableStateOf(initialPage.toString()) }
    val parsedPage = pageInput.toIntOrNull()?.takeIf { it in 1..604 }
    val hasError = pageInput.isNotEmpty() && parsedPage == null

    fun submitIfValid() {
        val target = pageInput.toIntOrNull()?.takeIf { it in 1..604 } ?: return
        onConfirmPage(target)
        onDismiss()
    }

    QuranStyledDialog(
        onDismissRequest = onDismiss,
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        title = "الانتقال إلى صفحة",
        subtitle = "أدخل رقم الصفحة من ${1.toAppDigits()} إلى ${604.toAppDigits()}",
        confirmLabel = "انتقال",
        confirmEnabled = parsedPage != null,
        confirmTestTag = "page_jump_confirm",
        onConfirm = ::submitIfValid
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = {
                    val cur = pageInput.toIntOrNull() ?: initialPage
                    pageInput = (cur - 1).coerceIn(1, 604).toString()
                },
                modifier = Modifier.size(54.dp)
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "الصفحة السابقة")
            }

            QuranDialogTextField(
                value = pageInput,
                onValueChange = { txt -> pageInput = txt.filter { it.isDigit() }.take(3) },
                label = "رقم الصفحة",
                placeholder = "١ - ٦٠٤",
                isError = hasError,
                supportingText = if (hasError) "أدخل رقماً بين ${1.toAppDigits()} و ${604.toAppDigits()}" else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onDone = ::submitIfValid,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("page_jump_input")
            )

            FilledTonalIconButton(
                onClick = {
                    val cur = pageInput.toIntOrNull() ?: initialPage
                    pageInput = (cur + 1).coerceIn(1, 604).toString()
                },
                modifier = Modifier.size(54.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "الصفحة التالية")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QUICK_PAGE_PRESETS.forEach { (presetPage, label) ->
                QuranDialogPresetChip(
                    label = "${presetPage.toAppDigits()} • $label",
                    selected = parsedPage == presetPage,
                    onClick = { pageInput = presetPage.toString() }
                )
            }
        }
    }
}
