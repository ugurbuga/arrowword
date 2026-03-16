package com.ugurbuga.arrowword.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AwPrimaryDark,
    onPrimary = AwOnPrimaryDark,
    primaryContainer = AwPrimaryContainerDark,
    onPrimaryContainer = AwOnPrimaryContainerDark,

    secondary = AwSecondaryDark,
    onSecondary = AwOnSecondaryDark,
    secondaryContainer = AwSecondaryContainerDark,
    onSecondaryContainer = AwOnSecondaryContainerDark,

    tertiary = AwTertiaryDark,
    onTertiary = AwOnTertiaryDark,
    tertiaryContainer = AwTertiaryContainerDark,
    onTertiaryContainer = AwOnTertiaryContainerDark,

    background = AwBackgroundDark,
    onBackground = AwOnBackgroundDark,
    surface = AwSurfaceDark,
    onSurface = AwOnSurfaceDark,
    surfaceVariant = AwSurfaceVariantDark,
    onSurfaceVariant = AwOnSurfaceVariantDark,
    outline = AwOutlineDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AwPrimaryLight,
    onPrimary = AwOnPrimaryLight,
    primaryContainer = AwPrimaryContainerLight,
    onPrimaryContainer = AwOnPrimaryContainerLight,

    secondary = AwSecondaryLight,
    onSecondary = AwOnSecondaryLight,
    secondaryContainer = AwSecondaryContainerLight,
    onSecondaryContainer = AwOnSecondaryContainerLight,

    tertiary = AwTertiaryLight,
    onTertiary = AwOnTertiaryLight,
    tertiaryContainer = AwTertiaryContainerLight,
    onTertiaryContainer = AwOnTertiaryContainerLight,

    background = AwBackgroundLight,
    onBackground = AwOnBackgroundLight,
    surface = AwSurfaceLight,
    onSurface = AwOnSurfaceLight,
    surfaceVariant = AwSurfaceVariantLight,
    onSurfaceVariant = AwOnSurfaceVariantLight,
    outline = AwOutlineLight,
)

@Composable
fun ArrowwordTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}