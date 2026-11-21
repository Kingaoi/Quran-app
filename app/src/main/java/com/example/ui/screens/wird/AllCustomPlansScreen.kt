package com.example.ui.screens.wird

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.wird.HusunDailyPlanUiState
import com.example.domain.wird.NamedWirdPlanItem
import com.example.ui.navigation.QuranSharedKey
import com.example.ui.navigation.quranSharedElement
import com.example.ui.theme.quranColors

@Composable
internal fun AllCustomPlansScreen(
    customPlans: List<NamedWirdPlanItem>,
    husunPlan: HusunDailyPlanUiState? = null,
    bottomClearance: Dp,
    onBack: () -> Unit,
    onAddPlan: () -> Unit,
    onOpenPlanReader: (page: Int, wirdIndex: Int, planId: String) -> Unit,
    onCompletePlanWird: (String) -> Unit,
    onTogglePlanReminder: (String) -> Unit = {},
    onOpenPlanSchedule: (String) -> Unit,
    onRequestDeletePlan: (NamedWirdPlanItem) -> Unit,
    onOpenHusunSetup: () -> Unit = {},
    onOpenHusunDaily: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 20.dp + bottomClearance
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .widthIn(max = 760.dp)
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("all_plans_screen")
    ) {
        item(key = "all_plans_top_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = qc.surface2,
                        contentColor = qc.text
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("all_plans_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "رجوع إلى الورد اليومي"
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.LibraryBooks,
                    contentDescription = null,
                    tint = qc.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .quranSharedElement(QuranSharedKey.WirdPlansAnchor, zIndexInOverlay = 1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "كل الخطط",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = qc.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val subtitle = when (customPlans.size) {
                        0 -> "خطط وأوراد مخصصة مستقلة عن وردك الأساسي"
                        1 -> "خطة واحدة مستقلة عن الورد اليومي"
                        2 -> "خطتان مستقلتان عن الورد اليومي"
                        in 3..10 -> "${customPlans.size.toAppDigits(useWestern)} خطط مستقلة عن الورد اليومي"
                        else -> "${customPlans.size.toAppDigits(useWestern)} خطة مستقلة عن الورد اليومي"
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = qc.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onAddPlan,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = qc.primary, contentColor = qc.onPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("wird_add_plan_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(5.dp))
                    Text("إضافة خطة", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        item(key = "husun_khamsah_card") {
            HusunFeatureCard(
                husunPlan = husunPlan,
                onSetupOrConfigure = onOpenHusunSetup,
                onOpenDailySheet = onOpenHusunDaily
            )
        }

        if (customPlans.isEmpty()) {
            item(key = "custom_plans_empty") {
                CustomWirdPlansEmptyCard(onAddPlan = onAddPlan)
            }
        } else {
            items(customPlans, key = { "custom_${it.config.id}" }) { item ->
                val cur = item.summary.currentWird
                CustomWirdPlanCard(
                    item = item,
                    onOpenReader = {
                        if (cur != null) {
                            onOpenPlanReader(item.summary.resumePage, cur.index, item.config.id)
                        }
                    },
                    onCompleteWird = { onCompletePlanWird(item.config.id) },
                    onToggleReminder = { onTogglePlanReminder(item.config.id) },
                    onOpenSchedule = { onOpenPlanSchedule(item.config.id) },
                    onRequestDelete = { onRequestDeletePlan(item) }
                )
            }
        }
    }
}
