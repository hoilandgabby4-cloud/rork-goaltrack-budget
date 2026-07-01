package com.rork.budgetflow.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BudgetColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = OnMint,
    primaryContainer = MintDeep,
    onPrimaryContainer = MintBright,
    secondary = Sky,
    onSecondary = Ink,
    tertiary = Gold,
    onTertiary = Ink,
    background = Ink,
    onBackground = TextPrimary,
    surface = InkElevated,
    onSurface = TextPrimary,
    surfaceVariant = InkSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = InkSurface,
    surfaceContainerHigh = InkSurfaceHigh,
    error = Coral,
    onError = Ink,
    outline = Hairline,
    outlineVariant = Hairline,
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Ink.toArgb()
            window.navigationBarColor = Ink.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = BudgetColorScheme,
        typography = AppTypography,
        content = content
    )
}
