package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val goldenPrimary = Color(0xFFB08D57)
private val goldenSecondary = Color(0xFF8B6F40)
private val goldenTertiary = Color(0xFFC5A675)
private val goldenSurface = Color(0xFFFDF6E5)
private val goldenSecondaryContainer = Color(0xFF997D51)

private val goldenPrimaryDark = Color(0xFF8B6F40)
private val goldenSecondaryDark = Color(0xFF6B5430)
private val goldenTertiaryDark = Color(0xFF9F864E)
private val goldenSurfaceDark = Color(0xFF232426)
private val goldenSecondaryContainerDark = Color(0xFF7E6541)

object Golden : AppColorScheme(
    name = "ذهبي",
    lightColorScheme = lightColorScheme(
        primary = goldenPrimary,
        secondary = goldenSecondary,
        tertiary = goldenTertiary,
        surface = goldenSurface,
        secondaryContainer = goldenSecondaryContainer
    ),
    darkColorScheme = darkColorScheme(
        primary = goldenPrimaryDark,
        secondary = goldenSecondaryDark,
        tertiary = goldenTertiaryDark,
        surface = goldenSurfaceDark,
        secondaryContainer = goldenSecondaryContainerDark
    )
)