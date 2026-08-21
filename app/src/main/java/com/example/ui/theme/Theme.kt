package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Theme Presets Data Class
data class ThemePresetConfig(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme
) {
    val darkScheme: ColorScheme get() = darkColorScheme
    val lightScheme: ColorScheme get() = lightColorScheme
}

val SapphireDarkScheme = darkColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.White,
    primaryContainer = SapphirePrimaryDark,
    onPrimaryContainer = SapphirePrimaryContainer,
    secondary = SapphireSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = SapphireSecondary,
    tertiary = SapphireTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF312E81),
    onTertiaryContainer = Color(0xFFC7D2FE),
    background = Color(0xFF0A0F1D),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F293D),
    error = SleekAlertRed,
    onError = Color.White
)

val SapphireLightScheme = lightColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.White,
    primaryContainer = SapphirePrimaryContainer,
    onPrimaryContainer = SapphirePrimaryDark,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = SapphireSecondaryContainer,
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = SapphireTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SapphireTertiaryContainer,
    onTertiaryContainer = Color(0xFF312E81),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = SleekAlertRed,
    onError = Color.White
)

val EmeraldDarkScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryDark,
    onPrimaryContainer = EmeraldPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = EmeraldSecondary,
    tertiary = EmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF06140F),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF0C241B),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF14382B),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF1F513F),
    outlineVariant = Color(0xFF103D2E),
    error = SleekAlertRed,
    onError = Color.White
)

val EmeraldLightScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldPrimaryDark,
    secondary = EmeraldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = EmeraldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldTertiaryContainer,
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF2FAF6),
    onBackground = Color(0xFF062B1D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF062B1D),
    surfaceVariant = Color(0xFFE0F3EA),
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFFA7D8C4),
    outlineVariant = Color(0xFFC6E7D8),
    error = SleekAlertRed,
    onError = Color.White
)

val GoldDarkScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldPrimaryDark,
    onPrimaryContainer = GoldPrimaryContainer,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = GoldSecondary,
    tertiary = GoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF12100C),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF1C1813),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF29231B),
    onSurfaceVariant = Color(0xFFFCD34D),
    outline = Color(0xFF4B3F2E),
    outlineVariant = Color(0xFF33291B),
    error = SleekAlertRed,
    onError = Color.White
)

val GoldLightScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldPrimaryContainer,
    onPrimaryContainer = GoldPrimaryDark,
    secondary = GoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = GoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFFDFBF7),
    onBackground = Color(0xFF2E1B07),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2E1B07),
    surfaceVariant = Color(0xFFF5EDE0),
    onSurfaceVariant = Color(0xFF92400E),
    outline = Color(0xFFE2CEB1),
    outlineVariant = Color(0xFFEFE2CD),
    error = SleekAlertRed,
    onError = Color.White
)

val PurpleDarkScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryDark,
    onPrimaryContainer = PurplePrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = PurpleSecondary,
    tertiary = PurpleTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF581C87),
    onTertiaryContainer = Color(0xFFF3E8FF),
    background = Color(0xFF0E0B19),
    onBackground = Color(0xFFFAF5FF),
    surface = Color(0xFF171228),
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = Color(0xFF231B3C),
    onSurfaceVariant = Color(0xFFD8B4FE),
    outline = Color(0xFF402E66),
    outlineVariant = Color(0xFF2E1F4A),
    error = SleekAlertRed,
    onError = Color.White
)

val PurpleLightScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = PurplePrimaryDark,
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = PurpleSecondaryContainer,
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = PurpleTertiary,
    onTertiary = Color.Black,
    tertiaryContainer = PurpleTertiaryContainer,
    onTertiaryContainer = Color(0xFF581C87),
    background = Color(0xFFFAF7FD),
    onBackground = Color(0xFF2E1065),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF2E1065),
    surfaceVariant = Color(0xFFEFE8FC),
    onSurfaceVariant = Color(0xFF6B21A8),
    outline = Color(0xFFD8B4FE),
    outlineVariant = Color(0xFFE9D5FF),
    error = SleekAlertRed,
    onError = Color.White
)

val SlateDarkScheme = darkColorScheme(
    primary = SlatePrimary,
    onPrimary = Color.White,
    primaryContainer = SlatePrimaryDark,
    onPrimaryContainer = SlatePrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF082F49),
    onSecondaryContainer = SlateSecondary,
    tertiary = SlateTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFFBAE6FD),
    background = Color(0xFF0B111E),
    onBackground = Color(0xFFF0F9FF),
    surface = Color(0xFF121B2B),
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF1C293E),
    onSurfaceVariant = Color(0xFF7DD3FC),
    outline = Color(0xFF283852),
    outlineVariant = Color(0xFF192436),
    error = SleekAlertRed,
    onError = Color.White
)

val SlateLightScheme = lightColorScheme(
    primary = SlatePrimary,
    onPrimary = Color.White,
    primaryContainer = SlatePrimaryContainer,
    onPrimaryContainer = SlatePrimaryDark,
    secondary = SlateSecondary,
    onSecondary = Color.Black,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = Color(0xFF082F49),
    tertiary = SlateTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SlateTertiaryContainer,
    onTertiaryContainer = Color(0xFF075985),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF082F49),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF082F49),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF0369A1),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = SleekAlertRed,
    onError = Color.White
)

object ThemeCatalog {
    val presets = listOf(
        ThemePresetConfig(
            id = "proptech_sapphire",
            name = "PropTech AI Tri-Color Accent (Default)",
            subtitle = "Signature Portal · Sapphire Blue · Neon Cyan · Neural Indigo",
            description = "Official client portal tri-color signature template with electric sapphire, cyber cyan, and neural indigo multi-accent glow.",
            primaryColor = SapphirePrimary,
            secondaryColor = SapphireSecondary,
            tertiaryColor = SapphireTertiary,
            darkColorScheme = SapphireDarkScheme,
            lightColorScheme = SapphireLightScheme
        ),
        ThemePresetConfig(
            id = "emerald_dealmaker",
            name = "Emerald Dealmaker",
            subtitle = "Growth Pipeline · Forest & Mint",
            description = "High-velocity deals, positive ROI green, and crisp mint highlights.",
            primaryColor = EmeraldPrimary,
            secondaryColor = EmeraldSecondary,
            tertiaryColor = EmeraldTertiary,
            darkColorScheme = EmeraldDarkScheme,
            lightColorScheme = EmeraldLightScheme
        ),
        ThemePresetConfig(
            id = "executive_gold",
            name = "Executive Gold",
            subtitle = "Luxury Brokerage · Amber & Champagne",
            description = "Warm amber accents designed for premium commercial real estate.",
            primaryColor = GoldPrimary,
            secondaryColor = GoldSecondary,
            tertiaryColor = GoldTertiary,
            darkColorScheme = GoldDarkScheme,
            lightColorScheme = GoldLightScheme
        ),
        ThemePresetConfig(
            id = "cyber_purple",
            name = "Cyberpunk Midnight",
            subtitle = "AI Deep Intelligence · Neon Purple",
            description = "Deep cyber night canvas with vivid neural purple glow.",
            primaryColor = PurplePrimary,
            secondaryColor = PurpleSecondary,
            tertiaryColor = PurpleTertiary,
            darkColorScheme = PurpleDarkScheme,
            lightColorScheme = PurpleLightScheme
        ),
        ThemePresetConfig(
            id = "slate_corporate",
            name = "Corporate Titanium",
            subtitle = "Clean Enterprise · Sky & Slate",
            description = "Clean slate enterprise neutral with sky blue accents.",
            primaryColor = SlatePrimary,
            secondaryColor = SlateSecondary,
            tertiaryColor = SlateTertiary,
            darkColorScheme = SlateDarkScheme,
            lightColorScheme = SlateLightScheme
        )
    )

    val ALL_THEME_PRESETS: List<ThemePresetConfig> get() = presets

    fun getPreset(id: String): ThemePresetConfig {
        return presets.find { it.id == id } ?: presets.first()
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: String = "proptech_sapphire",
    fontPreset: String = "modern_sans",
    content: @Composable () -> Unit
) {
    val selectedPreset = ThemeCatalog.getPreset(themePreset)
    val colorScheme = if (darkTheme) selectedPreset.darkColorScheme else selectedPreset.lightColorScheme
    val typography = getTypographyForPreset(fontPreset)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
