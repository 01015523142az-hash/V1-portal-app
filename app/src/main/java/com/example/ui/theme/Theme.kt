package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryDark,
    onPrimaryContainer = SleekPrimaryContainer,
    secondary = SleekSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekSurfaceNeutralDark,
    onSecondaryContainer = SleekTextPrimaryDark,
    tertiary = SleekInfoCyan,
    onTertiary = Color.White,
    tertiaryContainer = SleekInfoCyanDim,
    onTertiaryContainer = SleekInfoCyan,
    background = SleekCanvasDark,
    onBackground = SleekTextPrimaryDark,
    surface = SleekCardDark,
    onSurface = SleekTextPrimaryDark,
    surfaceVariant = SleekSurfaceVariantDark,
    onSurfaceVariant = SleekTextSecondaryDark,
    outline = SleekBorderDark,
    outlineVariant = SleekBorderDarkSubtle,
    error = SleekAlertRed,
    onError = Color.White,
    errorContainer = SleekAlertRedDim,
    onErrorContainer = SleekAlertRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekPrimaryDark,
    secondary = SleekSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekPrimaryDark,
    tertiary = SleekInfoCyan,
    onTertiary = Color.White,
    tertiaryContainer = SleekInfoCyanDim,
    onTertiaryContainer = SleekInfoCyan,
    background = SleekCanvasLight,
    onBackground = SleekTextPrimary,
    surface = SleekCardLight,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurfaceVariantLight,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorderLight,
    outlineVariant = SleekBorderLightSubtle,
    error = SleekAlertRed,
    onError = Color.White,
    errorContainer = SleekAlertRedDim,
    onErrorContainer = SleekAlertRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = if (darkTheme) SleekNavBackgroundDark.toArgb() else SleekNavBackgroundLight.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !darkTheme
        insetsController.isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
