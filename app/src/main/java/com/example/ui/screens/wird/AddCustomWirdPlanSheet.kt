package com.example.ui.screens.wird

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.domain.model.AyahItem
import com.example.domain.model.LocalUseWesternDigits
import com.example.domain.model.SurahInfo
import com.example.domain.wird.ALL_WEEK_DAYS_MASK
import com.example.domain.wird.CustomWirdPlanCalculator
import com.example.domain.wird.NamedWirdPlanConfig
import com.example.domain.wird.WirdAmountUnit
import com.example.domain.wird.WirdDirection
import com.example.domain.wird.WirdDurationUnit
import com.example.domain.wird.WirdGoalType
import com.example.domain.wird.WirdPlanAudience
import com.example.domain.wird.WirdPlanType
import com.example.domain.wird.WirdSessionTimingMode
import com.example.domain.wird.defaultWirdSessions
import com.example.ui.navigation.QuranMotionSpecs
import com.example.ui.navigation.predictiveBackTransform
import com.example.ui.navigation.rememberPredictiveBackState
import com.example.ui.theme.quranColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCustomWirdPlanSheet(
    surahs: List<SurahInfo>,
    allAyahs: List<AyahItem>,
    initialConfig: NamedWirdPlanConfig? = null,
    onConfirmAdd: (NamedWirdPlanConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val qc = MaterialTheme.quranColors
    val useWestern = LocalUseWesternDigits.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resolvedSurahs = remember(surahs) { surahs.ifEmpty { fallbackSurahList() } }

    var step by remember { mutableIntStateOf(1) }
    var title by remember { mutableStateOf(initialConfig?.title.orEmpty()) }
    var planType by remember { mutableStateOf(initialConfig?.planType ?: WirdPlanType.MEMORIZATION) }
    var audience by remember { mutableStateOf(initialConfig?.audience ?: WirdPlanAudience.FOR_ME) }
    var allowFollowUp by remember { mutableStateOf(initialConfig?.allowFollowUp ?: false) }

    var direction by remember { mutableStateOf(initialConfig?.direction ?: WirdDirection.FATIHA_TO_NAS) }
    var fromSurah by remember { mutableIntStateOf(initialConfig?.fromSurah ?: 1) }
    var fromAyah by remember { mutableIntStateOf(initialConfig?.fromAyah ?: 1) }
    var toSurah by remember { mutableIntStateOf(initialConfig?.toSurah ?: 1) }
    var toAyah by remember { mutableIntStateOf(initialConfig?.toAyah ?: 7) }
    var isPeriodicWird by remember { mutableStateOf(initialConfig?.isPeriodicWird ?: false) }

    var activeDaysMask by remember { mutableIntStateOf(initialConfig?.activeDaysMask ?: ALL_WEEK_DAYS_MASK) }
    var goalType by remember { mutableStateOf(initialConfig?.goalType ?: WirdGoalType.AMOUNT) }
    var amountValue by remember { mutableFloatStateOf(initialConfig?.amountValue ?: 1f) }
    var amountUnit by remember { mutableStateOf(initialConfig?.amountUnit ?: WirdAmountUnit.PAGE) }
    var rabtAmount by remember { mutableFloatStateOf(initialConfig?.rabtAmount ?: 0f) }
    var reviewAmountValue by remember { mutableFloatStateOf(initialConfig?.reviewAmountValue ?: 1f) }
    var reviewAmountUnit by remember { mutableStateOf(initialConfig?.reviewAmountUnit ?: WirdAmountUnit.PAGE) }
    var targetDurationCount by remember { mutableIntStateOf(initialConfig?.targetDurationDays ?: 30) }
    var durationUnit by remember { mutableStateOf(initialConfig?.durationUnit ?: WirdDurationUnit.DAY) }

    val todayEpoch = remember { CustomWirdPlanCalculator.currentEpochDay() }
    var startEpochDay by remember { mutableLongStateOf(initialConfig?.startEpochDay?.takeIf { it > 0L } ?: todayEpoch) }
    var optionalAmountEndEpochDay by remember {
        mutableStateOf(if (initialConfig?.goalType == WirdGoalType.AMOUNT && initialConfig.hasExplicitEndDate) initialConfig.endEpochDay else null)
    }
    var durationStartEpochDay by remember {
        mutableStateOf(if (initialConfig?.goalType == WirdGoalType.DURATION) initialConfig.startEpochDay.takeIf { it > 0L } else null)
    }
    var rangeEndEpochDay by remember {
        mutableStateOf(if (initialConfig?.goalType == WirdGoalType.DATE_RANGE) initialConfig.endEpochDay.takeIf { it > 0L } else null)
    }

    var sessionsPerDay by remember { mutableIntStateOf(initialConfig?.sessionsPerDay ?: 1) }
    var timingMode by remember { mutableStateOf(initialConfig?.sessionTimingMode ?: WirdSessionTimingMode.UNSPECIFIED) }
    var sessions by remember { mutableStateOf(initialConfig?.sessions ?: defaultWirdSessions()) }
    var reminderLeadMinutes by remember { mutableIntStateOf(initialConfig?.reminderLeadMinutes ?: 0) }
    var reminderEnabled by remember { mutableStateOf(initialConfig?.reminderEnabled ?: false) }

    val effectiveStart = if (goalType == WirdGoalType.DURATION) durationStartEpochDay ?: todayEpoch else startEpochDay
    val effectiveEnd = when (goalType) {
        WirdGoalType.AMOUNT -> optionalAmountEndEpochDay ?: 0L
        WirdGoalType.DURATION -> 0L
        WirdGoalType.DATE_RANGE -> rangeEndEpochDay ?: startEpochDay
    }
    val hasExplicitEnd = when (goalType) {
        WirdGoalType.AMOUNT -> optionalAmountEndEpochDay != null
        WirdGoalType.DURATION -> durationStartEpochDay != null
        WirdGoalType.DATE_RANGE -> rangeEndEpochDay != null
    }

    val draftConfig = remember(
        title, planType, audience, allowFollowUp, direction, fromSurah, fromAyah, toSurah, toAyah,
        isPeriodicWird, activeDaysMask, goalType, amountValue, amountUnit, rabtAmount,
        reviewAmountValue, reviewAmountUnit, targetDurationCount, durationUnit,
        effectiveStart, effectiveEnd, hasExplicitEnd, sessionsPerDay, timingMode, sessions,
        reminderLeadMinutes, reminderEnabled
    ) {
        NamedWirdPlanConfig(
            id = "",
            title = title.trim(),
            isV2Plan = true,
            iconKey = planType.key,
            planType = planType,
            audience = audience,
            allowFollowUp = allowFollowUp,
            direction = direction,
            fromSurah = fromSurah,
            fromAyah = fromAyah,
            toSurah = toSurah,
            toAyah = toAyah,
            isPeriodicWird = planType == WirdPlanType.RECITATION && isPeriodicWird,
            activeDaysMask = activeDaysMask,
            goalType = goalType,
            amountValue = amountValue,
            amountUnit = amountUnit,
            rabtAmount = if (planType == WirdPlanType.REVIEW || planType == WirdPlanType.RECITATION) 0f else rabtAmount,
            reviewAmountValue = reviewAmountValue,
            reviewAmountUnit = reviewAmountUnit,
            targetDurationDays = targetDurationCount,
            durationUnit = durationUnit,
            startEpochDay = effectiveStart,
            endEpochDay = effectiveEnd,
            hasExplicitEndDate = hasExplicitEnd,
            sessionsPerDay = sessionsPerDay,
            sessionTimingMode = timingMode,
            sessions = sessions,
            reminderEnabled = reminderEnabled,
            reminderLeadMinutes = reminderLeadMinutes
        )
    }

    val metrics = remember(draftConfig, allAyahs, resolvedSurahs) {
        CustomWirdPlanCalculator.calculateMetrics(draftConfig, allAyahs, resolvedSurahs)
    }
    val step3Error: String? = when (goalType) {
        WirdGoalType.AMOUNT -> {
            val optEnd = optionalAmountEndEpochDay
            if (optEnd != null && optEnd < startEpochDay) "تاريخ النهاية يجب أن يكون بعد أو يساوي تاريخ البدء" else null
        }
        WirdGoalType.DURATION -> if (durationStartEpochDay == null) "أكمل الحقول المطلوبة للمتابعة: اختر تاريخ البدء" else null
        WirdGoalType.DATE_RANGE -> when {
            rangeEndEpochDay == null -> "أكمل الحقول المطلوبة للمتابعة: اختر تاريخ النهاية"
            rangeEndEpochDay!! < startEpochDay -> "تاريخ النهاية يجب أن يكون بعد أو يساوي تاريخ البدء"
            else -> null
        }
    }
    val disabledReason: String? = when (step) {
        1 -> if (title.trim().isEmpty()) "أدخل عنوان الخطة للانتقال إلى الخطوة التالية" else null
        3 -> step3Error
        else -> null
    }
    val canProceed = disabledReason == null

    val stepPredictiveState = rememberPredictiveBackState(enabled = step > 1) {
        step = (step - 1).coerceAtLeast(1)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = qc.surface) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WizardTopHeader(step = step, useWestern = useWestern, isEditing = initialConfig != null, onBack = { if (step > 1) step-- }, onClose = onDismiss)
                WizardProgressSegments(step = step)

                Box(
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState())
                        .predictiveBackTransform(stepPredictiveState, maxCornerRadius = 18.dp, maxScaleDrop = 0.05f, maxShiftDp = 16.dp)
                ) {
                    AnimatedContent(targetState = step, transitionSpec = QuranMotionSpecs.wizardStepTransition(isRtl = true), label = "create_plan_step") { activeStep ->
                        when (activeStep) {
                            1 -> CreatePlanStep1BasicInfo(
                                title = title,
                                planType = planType,
                                audience = audience,
                                allowFollowUp = allowFollowUp,
                                onTitleChange = { title = it },
                                onSelectPlanType = { planType = it },
                                onSelectAudience = { audience = it },
                                onChangeAllowFollowUp = { allowFollowUp = it }
                            )
                            2 -> CreatePlanStep2DirectionRange(
                                planType = planType,
                                direction = direction,
                                fromSurah = fromSurah,
                                fromAyah = fromAyah,
                                toSurah = toSurah,
                                toAyah = toAyah,
                                isPeriodicWird = isPeriodicWird,
                                surahs = resolvedSurahs,
                                allAyahs = allAyahs,
                                onSelectDirection = { nextDir ->
                                    if (nextDir != direction) {
                                        direction = nextDir
                                        val swap = (nextDir == WirdDirection.NAS_TO_FATIHA && fromSurah < toSurah) ||
                                            (nextDir == WirdDirection.FATIHA_TO_NAS && fromSurah > toSurah)
                                        if (swap) {
                                            val prevFromS = fromSurah
                                            fromSurah = toSurah
                                            fromAyah = 1
                                            toSurah = prevFromS
                                            toAyah = resolvedSurahs.firstOrNull { it.number == prevFromS }?.ayahCount ?: 7
                                        }
                                    }
                                },
                                onUpdateRange = { fs, fa, ts, ta ->
                                    fromSurah = fs
                                    fromAyah = fa
                                    toSurah = ts
                                    toAyah = ta
                                },
                                onChangePeriodicWird = { isPeriodicWird = it }
                            )
                            3 -> CreatePlanStep3GoalDates(
                                planType = planType,
                                activeDaysMask = activeDaysMask,
                                goalType = goalType,
                                amountValue = amountValue,
                                amountUnit = amountUnit,
                                rabtAmount = rabtAmount,
                                reviewAmountValue = reviewAmountValue,
                                reviewAmountUnit = reviewAmountUnit,
                                targetDurationCount = targetDurationCount,
                                durationUnit = durationUnit,
                                startEpochDay = startEpochDay,
                                optionalAmountEndEpochDay = optionalAmountEndEpochDay,
                                durationStartEpochDay = durationStartEpochDay,
                                rangeEndEpochDay = rangeEndEpochDay,
                                metrics = metrics,
                                onChangeActiveDaysMask = { activeDaysMask = it },
                                onChangeGoalType = { goalType = it },
                                onChangeAmountValue = { amountValue = it },
                                onChangeAmountUnit = { next ->
                                    amountUnit = next
                                    amountValue = when (next) {
                                        WirdAmountUnit.AYAH -> 10f
                                        WirdAmountUnit.QUARTER -> 2f
                                        else -> 1f
                                    }
                                },
                                onChangeRabtAmount = { rabtAmount = it },
                                onChangeReviewAmountValue = { reviewAmountValue = it },
                                onChangeReviewAmountUnit = { next ->
                                    reviewAmountUnit = next
                                    reviewAmountValue = if (next == WirdAmountUnit.AYAH) 10f else 1f
                                },
                                onChangeTargetDurationCount = { targetDurationCount = it },
                                onChangeDurationUnit = { durationUnit = it },
                                onChangeStartEpochDay = { startEpochDay = it },
                                onChangeOptionalAmountEndEpochDay = { optionalAmountEndEpochDay = it },
                                onChangeDurationStartEpochDay = { durationStartEpochDay = it },
                                onChangeRangeEndEpochDay = { rangeEndEpochDay = it }
                            )
                            else -> CreatePlanStep4SessionsReminder(
                                planTitle = title,
                                planType = planType,
                                amountUnit = amountUnit,
                                rabtAmount = rabtAmount,
                                reviewAmountValue = reviewAmountValue,
                                reviewAmountUnit = reviewAmountUnit,
                                sessionsPerDay = sessionsPerDay,
                                timingMode = timingMode,
                                sessions = sessions,
                                reminderLeadMinutes = reminderLeadMinutes,
                                reminderEnabled = reminderEnabled,
                                metrics = metrics,
                                onChangeSessionsPerDay = { sessionsPerDay = it.coerceIn(1, 5) },
                                onChangeTimingMode = { timingMode = it },
                                onUpdateSessionSlot = { idx, sh, sm, eh, em ->
                                    sessions = sessions.map { slot ->
                                        if (slot.index == idx) slot.copy(startHour = sh, startMinute = sm, endHour = eh, endMinute = em) else slot
                                    }
                                },
                                onChangeReminderLeadMinutes = { reminderLeadMinutes = it },
                                onChangeReminderEnabled = { reminderEnabled = it }
                            )
                        }
                    }
                }

                WizardBottomActionsBar(
                    step = step, canProceed = canProceed, disabledReason = disabledReason,
                    isEditing = initialConfig != null,
                    onBack = { if (step > 1) step-- },
                    onPrimaryAction = {
                        if (step < 4) step++ else {
                            onConfirmAdd(draftConfig.copy(id = initialConfig?.id?.ifBlank { null } ?: UUID.randomUUID().toString(), endEpochDay = metrics.endEpochDay))
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}
