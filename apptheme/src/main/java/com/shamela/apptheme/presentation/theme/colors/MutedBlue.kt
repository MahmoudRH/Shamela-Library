package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val mutedBluePrimary = Color(0xFF4B6E85)
private val mutedBlueSecondary = Color(0xFF345268)
private val mutedBlueTertiary = Color(0xFF7D9EB3)
private val mutedBlueSurface = Color(0xFFE8F0F5)
private val mutedBlueSecondaryContainer = Color(0xFF587D96)

private val mutedBluePrimaryDark = Color(0xFF345268)
private val mutedBlueSecondaryDark = Color(0xFF213B4C)
private val mutedBlueTertiaryDark = Color(0xFF5B768D)
private val mutedBlueSurfaceDark = Color(0xFF232426)
private val mutedBlueSecondaryContainerDark = Color(0xFF3B576C)

object MutedBlue : AppColorScheme(
    name = "أزرق باهت",
    lightColorScheme = lightColorScheme(
        primary = mutedBluePrimary,
        secondary = mutedBlueSecondary,
        tertiary = mutedBlueTertiary,
        surface = mutedBlueSurface,
        secondaryContainer = mutedBlueSecondaryContainer
    ),
    darkColorScheme = darkColorScheme(
        primary = mutedBluePrimaryDark,
        secondary = mutedBlueSecondaryDark,
        tertiary = mutedBlueTertiaryDark,
        surface = mutedBlueSurfaceDark,
        secondaryContainer = mutedBlueSecondaryContainerDark
    )
)