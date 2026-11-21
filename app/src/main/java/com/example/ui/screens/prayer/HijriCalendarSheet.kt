package com.example.ui.screens.prayer

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.toAppDigits
import com.example.domain.prayer.HijriSunnahConfig
import com.example.ui.components.OrnamentalDivider
import java.util.Calendar

private val HIJRI_MONTHS = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
    "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
    "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
)

private val WEEKDAY_HEADERS = listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")

private data class HijriDayCell(
    val dayOfMonth: Int,
    val dayOfWeek: Int,
    val isToday: Boolean,
    val isWhiteDay: Boolean,
    val isSunnahFastDay: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HijriCalendarSheet(
    hijriOffsetDays: Int = 0,
    sunnahConfig: HijriSunnahConfig? = null,
    onToggleSunnahMaster: ((Boolean) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val useWestern = LocalUseWesternDigits.current
    var monthOffset by remember { mutableIntStateOf(0) }
    val calendarData = remember(monthOffset, hijriOffsetDays) { buildHijriMonthGrid(monthOffset, hijriOffsetDays) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { monthOffset -= 1 },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp).testTag("hijri_prev_month")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "الشهر السابق")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${HIJRI_MONTHS.getOrElse(calendarData.monthIndex) { "" }} ${calendarData.year.toAppDigits(useWestern)} هـ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (hijriOffsetDays != 0) {
                        val sign = if (hijriOffsetDays > 0) "+" else ""
                        Text(
                            text = "تعديل الرؤية: $sign${hijriOffsetDays.toAppDigits(useWestern)} يوم",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (monthOffset != 0) {
                        TextButton(onClick = { monthOffset = 0 }) {
                            Icon(Icons.Rounded.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("الشهر الحالي")
                        }
                    }
                }
                FilledTonalIconButton(
                    onClick = { monthOffset += 1 },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp).testTag("hijri_next_month")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "الشهر التالي")
                }
            }

            OrnamentalDivider()

            Row(modifier = Modifier.fillMaxWidth()) {
                WEEKDAY_HEADERS.forEach { hdr ->
                    Text(
                        text = hdr,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val totalSlots = calendarData.leadingEmpty + calendarData.days.size
            val rowCount = (totalSlots + 6) / 7
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until rowCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val idx = row * 7 + col - calendarData.leadingEmpty
                            val cell = calendarData.days.getOrNull(idx)
                            if (cell == null) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                HijriDayTile(
                                    cell = cell,
                                    useWestern = useWestern,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            HijriSunnahInsightBanner(
                hijriOffsetDays = hijriOffsetDays,
                useWestern = useWestern,
                sunnahConfig = sunnahConfig,
                onToggleSunnahMaster = onToggleSunnahMaster
            )

            val occBrush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    MaterialTheme.colorScheme.surface
                )
            )
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(occBrush).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "أيام مستحب صيامها ومناسبات الشهر",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "• الأيام البيض (${13.toAppDigits(useWestern)}، ${14.toAppDigits(useWestern)}، ${15.toAppDigits(useWestern)}) • صيام الاثنين والخميس",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    monthOccasionNote(calendarData.monthIndex, useWestern)?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HijriDayTile(
    cell: HijriDayCell,
    useWestern: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        cell.isToday -> MaterialTheme.colorScheme.primary
        cell.isWhiteDay -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }
    val fg = when {
        cell.isToday -> MaterialTheme.colorScheme.onPrimary
        cell.isWhiteDay -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (cell.isSunnahFastDay && !cell.isToday) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), CircleShape)
                } else Modifier
            )
    ) {
        Text(
            text = cell.dayOfMonth.toAppDigits(useWestern),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (cell.isToday || cell.isWhiteDay) FontWeight.Bold else FontWeight.Medium
            ),
            color = fg
        )
    }
}

private data class HijriMonthGrid(
    val year: Int,
    val monthIndex: Int,
    val leadingEmpty: Int,
    val days: List<HijriDayCell>
)

private fun buildHijriMonthGrid(offsetMonths: Int, hijriOffsetDays: Int): HijriMonthGrid {
    val todayCal = IslamicCalendar(ULocale("ar@calendar=islamic-umalqura")).apply {
        if (hijriOffsetDays != 0) add(android.icu.util.Calendar.DAY_OF_MONTH, hijriOffsetDays)
    }
    val todayYear = todayCal.get(android.icu.util.Calendar.YEAR)
    val todayMonth = todayCal.get(android.icu.util.Calendar.MONTH)
    val todayDay = todayCal.get(android.icu.util.Calendar.DAY_OF_MONTH)

    val viewCal = IslamicCalendar(ULocale("ar@calendar=islamic-umalqura")).apply {
        if (hijriOffsetDays != 0) add(android.icu.util.Calendar.DAY_OF_MONTH, hijriOffsetDays)
        set(android.icu.util.Calendar.DAY_OF_MONTH, 1)
        add(android.icu.util.Calendar.MONTH, offsetMonths)
    }
    val viewYear = viewCal.get(android.icu.util.Calendar.YEAR)
    val viewMonth = viewCal.get(android.icu.util.Calendar.MONTH)
    val maxDay = viewCal.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH)
    val firstDow = viewCal.get(android.icu.util.Calendar.DAY_OF_WEEK)
    val leadingEmpty = (firstDow - Calendar.SUNDAY).coerceIn(0, 6)

    val days = (1..maxDay).map { day ->
        viewCal.set(android.icu.util.Calendar.DAY_OF_MONTH, day)
        val dow = viewCal.get(android.icu.util.Calendar.DAY_OF_WEEK)
        HijriDayCell(
            dayOfMonth = day,
            dayOfWeek = dow,
            isToday = viewYear == todayYear && viewMonth == todayMonth && day == todayDay,
            isWhiteDay = day in 13..15,
            isSunnahFastDay = dow == Calendar.MONDAY || dow == Calendar.THURSDAY
        )
    }
    return HijriMonthGrid(viewYear, viewMonth.coerceIn(0, 11), leadingEmpty, days)
}

private fun monthOccasionNote(monthIndex: Int, useWestern: Boolean): String? = when (monthIndex) {
    0 -> "• ${10.toAppDigits(useWestern)} محرم: يوم عاشوراء (يستحب صيامه وتاسوعاء)"
    6 -> "• شهر رجب الحرام"
    7 -> "• شهر شعبان: يكثر فيه الصيام والاستعداد لرمضان"
    8 -> "• شهر رمضان المبارك: شهر القرآن والعتق من النيران"
    9 -> "• ${1.toAppDigits(useWestern)} شوال: عيد الفطر المبارك • صيام ست من شوال"
    11 -> "• عشر ذي الحجة • ${9.toAppDigits(useWestern)} يوم عرفة • ${10.toAppDigits(useWestern)} عيد الأضحى"
    else -> null
}
