package com.v07.v3.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================================
// V07 Sanctuary Calm & Organic Color Palette (Matches home_fixed.png)
// =============================================================================

object SanctuaryPalette {
    // Brand Teals & Mints
    val TealPrimary = Color(0xFF2E7D6A)
    val TealDark = Color(0xFF225E50)
    val TealLight = Color(0xFFE6F4F1)
    val MintAccent = Color(0xFF3F9A83)
    val MintLight = Color(0xFFEEF8F5)

    // Warm Cedar / Terracotta (Reading Companion Accent)
    val CedarAccent = Color(0xFF8C5E58)
    val CedarLight = Color(0xFFFAF0EE)
    val CedarBorder = Color(0xFFEADCDA)

    // Daylight Warm Paper / Canvas
    val LightCanvas = Color(0xFFF4F7F5)
    val LightCard = Color(0xFFFFFFFF)
    val LightCardAlt = Color(0xFFF0F5F2)
    val LightBorder = Color(0xFFDDE7E2)
    val LightBorderSubtle = Color(0xFFE8EFEA)
    val LightTextPrimary = Color(0xFF182230)
    val LightTextSecondary = Color(0xFF5B6A78)
    val LightTextMuted = Color(0xFF8C9BA8)

    // Nighttime Calm Slate (Restful Dark Mode)
    val DarkCanvas = Color(0xFF0F141C)
    val DarkCard = Color(0xFF182230)
    val DarkCardAlt = Color(0xFF1F2B3A)
    val DarkBorder = Color(0xFF283648)
    val DarkBorderSubtle = Color(0xFF1E2835)
    val DarkTealPrimary = Color(0xFF4DB69E)
    val DarkTealLight = Color(0xFF16322A)
    val DarkMintAccent = Color(0xFF5BC4A9)
    val DarkCedarAccent = Color(0xFFD49A92)
    val DarkCedarLight = Color(0xFF33201E)
    val DarkTextPrimary = Color(0xFFF1F5F9)
    val DarkTextSecondary = Color(0xFF94A3B8)
    val DarkTextMuted = Color(0xFF64748B)

    // Status Semantics
    val Success = Color(0xFF2E7D6A)
    val Warning = Color(0xFFD97706)
    val Danger = Color(0xFFE11D48)
    val Info = Color(0xFF0284C7)
}

val SanctuaryShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val PillShape = RoundedCornerShape(9999.dp)

val SanctuaryTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 14.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = SanctuaryPalette.TealPrimary,
    onPrimary = Color.White,
    primaryContainer = SanctuaryPalette.TealLight,
    onPrimaryContainer = SanctuaryPalette.TealDark,
    secondary = SanctuaryPalette.CedarAccent,
    onSecondary = Color.White,
    secondaryContainer = SanctuaryPalette.CedarLight,
    onSecondaryContainer = SanctuaryPalette.CedarAccent,
    tertiary = SanctuaryPalette.MintAccent,
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

private val DarkColorScheme = darkColorScheme(
    primary = SanctuaryPalette.DarkTealPrimary,
    onPrimary = Color(0xFF0F141C),
    primaryContainer = SanctuaryPalette.DarkTealLight,
    onPrimaryContainer = SanctuaryPalette.DarkMintAccent,
    secondary = SanctuaryPalette.DarkCedarAccent,
    onSecondary = Color(0xFF0F141C),
    secondaryContainer = SanctuaryPalette.DarkCedarLight,
    onSecondaryContainer = SanctuaryPalette.DarkCedarAccent,
    tertiary = SanctuaryPalette.DarkMintAccent,
    onTertiary = Color(0xFF0F141C),
    background = SanctuaryPalette.DarkCanvas,
    onBackground = SanctuaryPalette.DarkTextPrimary,
    surface = SanctuaryPalette.DarkCard,
    onSurface = SanctuaryPalette.DarkTextPrimary,
    surfaceVariant = SanctuaryPalette.DarkCardAlt,
    onSurfaceVariant = SanctuaryPalette.DarkTextSecondary,
    outline = SanctuaryPalette.DarkBorder,
    outlineVariant = SanctuaryPalette.DarkBorderSubtle
)

@Composable
fun SanctuaryTheme(
    darkTheme: Boolean = false,
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
