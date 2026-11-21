package com.example.domain.wird

import androidx.compose.runtime.Immutable

@Immutable
enum class HusunNewRate(
    val key: String,
    val halfPageUnits: Int,
    val labelAr: String,
    val shortAr: String
) {
    HALF_PAGE("half", 1, "نصف وجه يومياً", "نصف وجه"),
    ONE_PAGE("one", 2, "وجه كامل يومياً", "١ وجه"),
    TWO_PAGES("two", 4, "وجهان يومياً", "٢ وجه");

    companion object {
        fun fromKey(key: String?): HusunNewRate =
            entries.firstOrNull { it.key == key } ?: ONE_PAGE
    }
}

@Immutable
enum class HusunFortressType(
    val index: Int,
    val titleAr: String,
    val badgeAr: String,
    val methodDescAr: String
) {
    CONTINUOUS_READING(
        index = 0,
        titleAr = "القراءة المستمرة والاستماع",
        badgeAr = "الحصن ١",
        methodDescAr = "قراءة يومية سريعة (جزءان حدر) مع الاستماع لإبقاء القرآن حياً في الذهن"
    ),
    PREPARATION(
        index = 1,
        titleAr = "التحضير (الأسبوعي والليلي والقبلي)",
        badgeAr = "الحصن ٢",
        methodDescAr = "استمع واقرأ مقرر الغد قبل نومه، وأعد التحضير ١٥ دقيقة قبل الحفظ"
    ),
    NEW_MEMORIZATION(
        index = 2,
        titleAr = "الحفظ الجديد",
        badgeAr = "الحصن ٣",
        methodDescAr = "ركّز على مقطع اليوم وكرّره بصوت مسموع ثم اربطه بالآية التالية"
    ),
    NEAR_REVIEW(
        index = 3,
        titleAr = "مراجعة القريب",
        badgeAr = "الحصن ٤",
        methodDescAr = "تسميع ومراجعة آخر ٢٠ وجهاً سابقة مباشرة لتثبيت الحفظ الحديث"
    ),
    FAR_REVIEW(
        index = 4,
        titleAr = "مراجعة البعيد",
        badgeAr = "الحصن ٥",
        methodDescAr = "ورد دوري من المحفوظ القديم قبل القريب لضمان عدم تفلت الآيات"
    );

    val bitMask: Int get() = 1 shl index
}

@Immutable
data class HusunConfig(
    val isActive: Boolean = false,
    val direction: WirdDirection = WirdDirection.FATIHA_TO_NAS,
    val startingMemorizedPages: Int = 0,
    val dailyNewRate: HusunNewRate = HusunNewRate.ONE_PAGE,
    val continuousPagesPerDay: Int = 40,
    val farReviewDivisions: Int = 7,
    val completedNewUnits: Int = 0,
    val continuousCursorDay: Int = 0,
    val lastActiveEpochDay: Long = 0L,
    val todayFortressMask: Int = 0,
    val todayNewRepeatCount: Int = 0,
    val startEpochDay: Long = 0L
) {
    fun effectiveMask(todayEpochDay: Long): Int =
        if (lastActiveEpochDay == todayEpochDay) todayFortressMask else 0

    fun effectiveRepeatCount(todayEpochDay: Long): Int =
        if (lastActiveEpochDay == todayEpochDay) todayNewRepeatCount else 0
}

@Immutable
data class HusunFortressAssignment(
    val type: HusunFortressType,
    val isCompletedToday: Boolean,
    val rangeTitleAr: String,
    val detailSubtitleAr: String,
    val targetPage: Int,
    val canOpenReader: Boolean
)

@Immutable
data class HusunDailyPlanUiState(
    val config: HusunConfig,
    val totalMemorizedPages: Int,
    val totalMemorizedHalfPages: Int,
    val overallProgressFraction: Float,
    val completedTodayCount: Int,
    val todayRepeatCount: Int,
    val fortresses: List<HusunFortressAssignment>,
    val isAllHifzCompleted: Boolean
)

@Immutable
data class HusunActions(
    val onConfigure: (WirdDirection, Int, HusunNewRate, Int, Int) -> Unit = { _, _, _, _, _ -> },
    val onToggleFortress: (HusunFortressType) -> Unit = {},
    val onIncrementRepeat: () -> Unit = {},
    val onCompleteDay: () -> Unit = {},
    val onReset: () -> Unit = {},
    val onOpenReaderPage: (Int) -> Unit = {}
)
