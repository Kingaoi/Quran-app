package com.example.ui.screens.adhkar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.ui.components.OrnamentalDivider
import com.example.ui.theme.quranColors

@Composable
internal fun AdhkarThankYouPageContent(
    sectionTitle: String,
    completedCount: Int,
    totalItemsCount: Int,
    hasIncompleteItems: Boolean,
    onJumpToIncomplete: () -> Unit,
    onRestartSection: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val allDone = totalItemsCount > 0 && completedCount >= totalItemsCount
    val countLabel = "${completedCount.toAppDigits(useWestern)} / ${totalItemsCount.toAppDigits(useWestern)}"
    val headline = stringResource(
        if (allDone) R.string.adhkar_thank_you_headline_all_done else R.string.adhkar_thank_you_headline_reached_end
    )
    val message = if (allDone) {
        stringResource(R.string.adhkar_thank_you_message_all_done, sectionTitle)
    } else {
        stringResource(R.string.adhkar_thank_you_message_partial, sectionTitle, countLabel)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = qc.surface,
            border = BorderStroke(
                1.5.dp,
                if (allDone) qc.success.copy(alpha = 0.45f) else qc.primary.copy(alpha = 0.35f)
            ),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("adhkar_detail_thank_you_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                (if (allDone) qc.successContainer else MaterialTheme.colorScheme.primaryContainer).copy(alpha = 0.34f),
                                qc.surface,
                                qc.surface2.copy(alpha = 0.65f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                val accent = if (allDone) qc.success else qc.primary
                val accentContainer = if (allDone) qc.successContainer else MaterialTheme.colorScheme.primaryContainer

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(84.dp)
                        .background(accentContainer.copy(alpha = 0.55f), CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .background(accent.copy(alpha = 0.16f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = stringResource(R.string.adhkar_thank_you_icon_cd),
                            tint = accent,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = accent,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = qc.text,
                    textAlign = TextAlign.Center
                )

                OrnamentalDivider()

                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.32f))
                ) {
                    Text(
                        text = "«$sectionTitle» • $countLabel",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (hasIncompleteItems) {
                        ThankYouUtilityChip(
                            label = stringResource(R.string.adhkar_thank_you_complete_remaining),
                            icon = Icons.Rounded.ArrowForward,
                            onClick = onJumpToIncomplete,
                            modifier = Modifier.testTag("adhkar_thank_you_incomplete_button")
                        )
                    }
                    ThankYouUtilityChip(
                        label = stringResource(R.string.adhkar_thank_you_restart_section),
                        icon = Icons.Rounded.Refresh,
                        onClick = onRestartSection,
                        modifier = Modifier.testTag("adhkar_thank_you_reset_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun ThankYouUtilityChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = qc.surface2,
        border = BorderStroke(1.dp, qc.border),
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = qc.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = qc.text
            )
        }
    }
}

@Composable
internal fun AdhkarThankYouBottomBar(
    onPrevious: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val cardShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val edgeBorderColor = qc.border.copy(alpha = 0.9f)

    Surface(
        shape = cardShape,
        color = qc.surface,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(qc.surface, qc.surface2)))
                .drawWithContent {
                    drawContent()
                    val cornerPx = 28.dp.toPx()
                    val strokePx = 1.dp.toPx()
                    val half = strokePx / 2f
                    val path = Path().apply {
                        moveTo(half, cornerPx)
                        arcTo(Rect(half, half, cornerPx * 2f - half, cornerPx * 2f - half), 180f, 90f, false)
                        lineTo(size.width - cornerPx, half)
                        arcTo(Rect(size.width - cornerPx * 2f + half, half, size.width - half, cornerPx * 2f - half), 270f, 90f, false)
                    }
                    drawPath(path = path, color = edgeBorderColor, style = Stroke(width = strokePx))
                }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                modifier = Modifier.weight(1f).height(48.dp).testTag("adhkar_thank_you_finish_button")
            ) {
                Text(text = stringResource(R.string.adhkar_finish_section), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            }
            OutlinedButton(
                onClick = onPrevious,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, qc.border),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = stringResource(R.string.adhkar_prev_label), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
            }
        }
    }
}
