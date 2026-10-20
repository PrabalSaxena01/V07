package com.v07.v3.ui.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * V07 Sanctuary Design Tokens
 * Faithful 1:1 match to the dark sovereign aesthetic of the V3 main branch.
 */
object SanctuaryPalette {
    // Brand Teals & Accents
    val PrimaryTeal = Color(0xFF2E7D6A)
    val PrimaryTealDark = Color(0xFF1D5A4C)
    val PrimaryTealLight = Color(0xFFE6F4F1)
    val PrimaryGlow = Color(0x262E7D6A)

    // Functional Accents from V3 Cockpit
    val AccentChrono = Color(0xFF0284C7) // Sky blue for focus today
    val AccentChronoBg = Color(0x1A0284C7)
    val AccentHabits = Color(0xFF10B981) // Emerald green for cadence ratio
    val AccentHabitsBg = Color(0x1A10B981)
    val AccentTasks = Color(0xFF3F9A83)  // Mint for obligations
    val AccentTasksBg = Color(0x1A3F9A83)
    val AccentGamify = Color(0xFFF59E0B) // Amber gold for mastery rank
    val AccentGamifyBg = Color(0x1AF59E0B)

    // Dark Mode Canvas & Surfaces (Faithful to main index.html & shell.css)
    val DarkCanvas = Color(0xFF0E1217)
    val DarkCard = Color(0xFF151B22)
    val DarkCardAlt = Color(0xFF1A212B)
    val DarkBorder = Color(0xFF21262D)
    val DarkBorderSubtle = Color(0xFF1B222A)
    val DarkTextPrimary = Color(0xFFF0F6FC)
    val DarkTextSecondary = Color(0xFF8B949E)
    val DarkTextMuted = Color(0xFF56606D)

    // Light Mode Canvas & Surfaces
    val LightCanvas = Color(0xFFFAF6F9)
    val LightCard = Color(0xFFFFFFFF)
    val LightCardAlt = Color(0xFFF0F4F2)
    val LightBorder = Color(0xFFDDE4E0)
    val LightBorderSubtle = Color(0xFFE8EFEA)
    val LightTextPrimary = Color(0xFF182230)
    val LightTextSecondary = Color(0xFF5B6A78)
    val LightTextMuted = Color(0xFF8C9BA8)
}

val PillShape = RoundedCornerShape(9999.dp)
val CardShape = RoundedCornerShape(16.dp)

val SanctuaryShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val SanctuaryTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 15.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 17.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 13.sp
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = SanctuaryPalette.PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = SanctuaryPalette.PrimaryTealDark,
    onPrimaryContainer = Color.White,
    secondary = SanctuaryPalette.AccentChrono,
    onSecondary = Color.White,
    tertiary = SanctuaryPalette.AccentHabits,
    onTertiary = Color.White,
    background = SanctuaryPalette.DarkCanvas,
    onBackground = SanctuaryPalette.DarkTextPrimary,
    surface = SanctuaryPalette.DarkCard,
    onSurface = SanctuaryPalette.DarkTextPrimary,
    surfaceVariant = SanctuaryPalette.DarkCardAlt,
    onSurfaceVariant = SanctuaryPalette.DarkTextSecondary,
    outline = SanctuaryPalette.DarkBorder,
    outlineVariant = SanctuaryPalette.DarkBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = SanctuaryPalette.PrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = SanctuaryPalette.PrimaryTealLight,
    onPrimaryContainer = SanctuaryPalette.PrimaryTealDark,
    secondary = SanctuaryPalette.AccentChrono,
    onSecondary = Color.White,
    tertiary = SanctuaryPalette.AccentHabits,
    onTertiary = Color.White,
    background = SanctuaryPalette.LightCanvas,
    onBackground = SanctuaryPalette.LightTextPrimary,
    surface = SanctuaryPalette.LightCard,
    onSurface = SanctuaryPalette.LightTextPrimary,
    surfaceVariant = SanctuaryPalette.LightCardAlt,
    onSurfaceVariant = SanctuaryPalette.LightTextSecondary,
    outline = SanctuaryPalette.LightBorder,
    outlineVariant = SanctuaryPalette.LightBorderSubtle
)

@Composable
fun SanctuaryTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = SanctuaryShapes,
        typography = SanctuaryTypography,
        content = content
    )
}
