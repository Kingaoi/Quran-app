package com.example.ui.screens.adhkar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.LocalQuranFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCustomDuaDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, arabicText: String, targetCount: Int, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var count by remember { mutableIntStateOf(1) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddCustomDuaHeader(onDismiss = onDismiss)
            OrnamentalDivider()

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الدعاء (اختياري)") },
                placeholder = { Text("مثال: دعاء التوفيق والبركة") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_dua_title_input")
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("نص الدعاء أو الذكر") },
                placeholder = { Text("اكتب الدعاء أو الذكر المأثور هنا...") },
                trailingIcon = if (text.isNotEmpty()) {
                    {
                        IconButton(onClick = { text = "" }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Rounded.Clear, contentDescription = "مسح النص")
                        }
                    }
                } else null,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = LocalQuranFontFamily.current,
                    fontSize = 18.sp,
                    lineHeight = 28.sp
                ),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_dua_text_input")
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("فضل الذكر أو ملاحظة (اختياري)") },
                placeholder = { Text("مثال: يُقال بعد الصلوات أو صباحاً ومساءً") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_dua_note_input")
            )

            DuaRepeatSelectorCard(count = count, onCountChange = { count = it })

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(title, text, count, note)
                            onDismiss()
                        }
                    },
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("custom_dua_save_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "حفظ الدعاء",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
