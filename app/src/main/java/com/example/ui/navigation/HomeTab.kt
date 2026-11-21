package com.example.ui.navigation

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

internal const val ROUTE_HOME = "home"
internal const val ROUTE_READER_ARG_FROM_WIRD = "fromWird"
internal const val ROUTE_READER_ARG_WIRD_INDEX = "wirdIndex"
internal const val ROUTE_READER_ARG_PLAN_ID = "planId"
internal const val ROUTE_READER =
    "reader?$ROUTE_READER_ARG_FROM_WIRD={$ROUTE_READER_ARG_FROM_WIRD}&$ROUTE_READER_ARG_WIRD_INDEX={$ROUTE_READER_ARG_WIRD_INDEX}&$ROUTE_READER_ARG_PLAN_ID={$ROUTE_READER_ARG_PLAN_ID}"
internal const val ROUTE_ADHKAR_ARG = "categoryId"
internal const val ROUTE_ADHKAR_CHAPTER_ARG = "chapterTitle"
internal const val ROUTE_ADHKAR_INDEX_ARG = "initialIndex"
internal const val ROUTE_ADHKAR_BROWSE = "adhkar_browse/{$ROUTE_ADHKAR_ARG}"
internal const val ROUTE_ADHKAR_DETAIL =
    "adhkar_detail/{$ROUTE_ADHKAR_ARG}?$ROUTE_ADHKAR_CHAPTER_ARG={$ROUTE_ADHKAR_CHAPTER_ARG}&$ROUTE_ADHKAR_INDEX_ARG={$ROUTE_ADHKAR_INDEX_ARG}"

internal fun readerRoute(fromWird: Boolean = false, wirdIndex: Int = -1, planId: String = ""): String =
    "reader?$ROUTE_READER_ARG_FROM_WIRD=$fromWird&$ROUTE_READER_ARG_WIRD_INDEX=$wirdIndex&$ROUTE_READER_ARG_PLAN_ID=$planId"

internal fun adhkarBrowseRoute(categoryId: String): String = "adhkar_browse/$categoryId"

internal fun adhkarDetailRoute(categoryId: String, chapterTitle: String = "", initialIndex: Int = 0): String =
    "adhkar_detail/$categoryId?$ROUTE_ADHKAR_CHAPTER_ARG=${Uri.encode(chapterTitle)}&$ROUTE_ADHKAR_INDEX_ARG=$initialIndex"

internal enum class HomeTab(
    val route: String,
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    WIRD(
        route = "wird",
        labelAr = "ورد اليوم",
        selectedIcon = QuranNavIcons.WirdSelected,
        unselectedIcon = QuranNavIcons.WirdOutlined
    ),
    ADHKAR(
        route = "adhkar",
        labelAr = "الأذكار",
        selectedIcon = QuranNavIcons.AdhkarSelected,
        unselectedIcon = QuranNavIcons.AdhkarOutlined
    ),
    PRAYER_TIMES(
        route = "prayer_times",
        labelAr = "الصلاة",
        selectedIcon = QuranNavIcons.PrayerSelected,
        unselectedIcon = QuranNavIcons.PrayerOutlined
    ),
    INDEX(
        route = "index",
        labelAr = "الفهرس",
        selectedIcon = QuranNavIcons.MushafSelected,
        unselectedIcon = QuranNavIcons.MushafOutlined
    ),
    MORE(
        route = "more",
        labelAr = "المزيد",
        selectedIcon = QuranNavIcons.MoreSelected,
        unselectedIcon = QuranNavIcons.MoreOutlined
    )
}
