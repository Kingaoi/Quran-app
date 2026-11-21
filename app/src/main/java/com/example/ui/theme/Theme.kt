package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.AppTheme
import com.example.data.local.QuranFontOption

val LocalFloatingNavBottomPadding = compositionLocalOf<Dp> { 0.dp }
val LocalAppTheme = compositionLocalOf { AppTheme.SEPIA }

@Immutable
data class QuranSemanticColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val selected: Color,
    val onSelected: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val danger: Color,
    val onDanger: Color,
    val dangerContainer: Color,
    val pastelRose: Color,
    val pastelSage: Color,
    val pastelSky: Color,
    val pastelAmber: Color,
    val pastelLavender: Color,
    val pastelPeach: Color
) {
    fun categoryPastelAt(index: Int): Color = when ((index % 6 + 6) % 6) {
        0 -> pastelRose
        1 -> pastelSage
        2 -> pastelSky
        3 -> pastelAmber
        4 -> pastelLavender
        else -> pastelPeach
    }
}

private val SepiaSemanticColors = QuranSemanticColors(
    bg = SepiaBackground,
    surface = SepiaSurfaceLow,
    surface2 = SepiaSurfaceHigh,
    border = SepiaOutlineVariant,
    text = SepiaInk,
    textMuted = SepiaSecondaryInk,
    textDisabled = Color(0xFF846E57),
    primary = SepiaPrimary,
    onPrimary = OnSepiaPrimary,
    accent = SepiaSecondary,
    selected = SepiaTertiary,
    onSelected = OnSepiaTertiary,
    success = Color(0xFF3E7B50),
    onSuccess = OnSepiaPrimary,
    successContainer = Color(0xFFC8DCB8),
    danger = Color(0xFFA34133),
    onDanger = OnSepiaPrimary,
    dangerContainer = Color(0xFFE0BCA8),
    pastelRose = Color(0xFFC7867D),
    pastelSage = Color(0xFF769D87),
    pastelSky = Color(0xFF7798B0),
    pastelAmber = Color(0xFFB79260),
    pastelLavender = Color(0xFF9E8AB6),
    pastelPeach = Color(0xFFC28E73)
)

private val DarkSemanticColors = QuranSemanticColors(
    bg = WarmDarkBackground,
    surface = WarmDarkCardSurface,
    surface2 = WarmDarkElevatedSurface,
    border = WarmDarkDividerOutline,
    text = CreamOnBackgroundDark,
    textMuted = CreamSecondaryDark,
    textDisabled = WarmDarkDisabledAndOutline,
    primary = OlivePrimaryDark,
    onPrimary = OnOlivePrimaryDark,
    accent = GoldSecondaryDark,
    selected = DarkSelected,
    onSelected = DarkOnSelected,
    success = DarkSuccess,
    onSuccess = WarmDarkBackground,
    successContainer = Color(0xFF213627),
    danger = DarkDanger,
    onDanger = WarmDarkBackground,
    dangerContainer = Color(0xFF3E221E),
    pastelRose = PastelRose,
    pastelSage = PastelSage,
    pastelSky = PastelSky,
    pastelAmber = PastelAmber,
    pastelLavender = PastelLavender,
    pastelPeach = PastelPeach
)

val LocalQuranColors = compositionLocalOf { SepiaSemanticColors }

val MaterialTheme.quranColors: QuranSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalQuranColors.current

@Composable
fun quranFilterChipColors(): SelectableChipColors {
    val qc = MaterialTheme.quranColors
    return FilterChipDefaults.filterChipColors(
        containerColor = qc.surface,
        labelColor = qc.text,
        selectedContainerColor = qc.selected,
        selectedLabelColor = qc.onSelected,
        selectedLeadingIconColor = qc.onSelected
    )
}

@Composable
fun quranFilterChipBorder(selected: Boolean): BorderStroke {
    val qc = MaterialTheme.quranColors
    return FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = qc.border,
        selectedBorderColor = qc.selected
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = OlivePrimaryDark,
    onPrimary = OnOlivePrimaryDark,
    primaryContainer = OliveContainerDark,
    onPrimaryContainer = OnOliveContainerDark,
    secondary = GoldSecondaryDark,
    onSecondary = OnGoldSecondaryDark,
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = OnGoldContainerDark,
    tertiary = WarmBronzeTertiaryDark,
    onTertiary = OnWarmBronzeTertiaryDark,
    tertiaryContainer = CreamRoseContainerDark,
    onTertiaryContainer = OnCreamRoseContainerDark,
    background = WarmDarkBackground,
    onBackground = CreamOnBackgroundDark,
    surface = WarmDarkCardSurface,
    onSurface = CreamOnBackgroundDark,
    surfaceVariant = WarmDarkElevatedSurface,
    onSurfaceVariant = CreamSecondaryDark,
    surfaceContainerLowest = WarmDarkBackground,
    surfaceContainerLow = WarmDarkCardSurface,
    surfaceContainer = WarmDarkCardSurface,
    surfaceContainerHigh = WarmDarkElevatedSurface,
    surfaceContainerHighest = WarmDarkElevatedSurface,
    outline = WarmDarkDisabledAndOutline,
    outlineVariant = WarmDarkDividerOutline,
    error = DarkDanger,
    onError = WarmDarkBackground,
    errorContainer = Color(0xFF3E221E),
    onErrorContainer = Color(0xFFF6CBC4)
)

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = OnSepiaPrimary,
    primaryContainer = SepiaPrimaryContainer,
    onPrimaryContainer = OnSepiaPrimaryContainer,
    secondary = SepiaSecondary,
    onSecondary = OnSepiaSecondary,
    secondaryContainer = SepiaSecondaryContainer,
    onSecondaryContainer = OnSepiaSecondaryContainer,
    tertiary = SepiaTertiary,
    onTertiary = OnSepiaTertiary,
    tertiaryContainer = SepiaTertiaryContainer,
    onTertiaryContainer = OnSepiaTertiaryContainer,
    background = SepiaBackground,
    onBackground = SepiaInk,
    surface = SepiaSurfaceLow,
    onSurface = SepiaInk,
    surfaceVariant = SepiaSurfaceHigh,
    onSurfaceVariant = SepiaSecondaryInk,
    surfaceContainerLowest = SepiaSurfaceLow,
    surfaceContainerLow = SepiaSurfaceLow,
    surfaceContainer = SepiaBackground,
    surfaceContainerHigh = SepiaSurfaceHigh,
    surfaceContainerHighest = SepiaSurfaceHighest,
    outline = SepiaOutline,
    outlineVariant = SepiaOutlineVariant,
    error = Color(0xFFA34133),
    onError = OnSepiaPrimary,
    errorContainer = Color(0xFFE0BCA8),
    onErrorContainer = Color(0xFF4F1D14)
)

val QuranShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun resolveEffectiveAppTheme(appTheme: AppTheme): AppTheme {
    val systemDark = isSystemInDarkTheme()
    return when (appTheme) {
        AppTheme.SYSTEM -> if (systemDark) AppTheme.DARK else AppTheme.SEPIA
        else -> appTheme
    }
}

@Composable
fun QuranWsTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    quranFontOption: QuranFontOption = QuranFontOption.ALYAMAMA,
    uthmanicFontFamily: FontFamily? = null,
    useWesternDigits: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveTheme = resolveEffectiveAppTheme(appTheme)
    val colorScheme = when (effectiveTheme) {
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SEPIA, AppTheme.SYSTEM -> SepiaColorScheme
    }
    val semanticColors = when (effectiveTheme) {
        AppTheme.DARK -> DarkSemanticColors
        AppTheme.SEPIA, AppTheme.SYSTEM -> SepiaSemanticColors
    }

    val activeQuranFont = remember(quranFontOption, uthmanicFontFamily) {
        resolveQuranFontFamily(quranFontOption, uthmanicFontFamily)
    }

    com.example.domain.model.AppDigitConfig.useWesternDigits = useWesternDigits

    CompositionLocalProvider(
        LocalAppTheme provides effectiveTheme,
        LocalQuranColors provides semanticColors,
        LocalQuranFontFamily provides activeQuranFont,
        com.example.domain.model.LocalUseWesternDigits provides useWesternDigits
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = QuranShapes,
            content = content
        )
    }
}
