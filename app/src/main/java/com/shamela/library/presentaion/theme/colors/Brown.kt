package com.shamela.library.presentaion.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val brownPrimary = Color(0xFF8B4513)
private val brownSecondary = Color(0xFF703B1D)
private val brownTertiary = Color(0xFFA05A2C)
private val brownSurface = Color(0xFFF5EDE1)
private val brownSecondaryContainer = Color(0xFF78472B)

private val brownPrimaryDark = Color(0xFF703B1D)
private val brownSecondaryDark = Color(0xFF522E18)
private val brownTertiaryDark = Color(0xFF864B29)
private val brownSurfaceDark = Color(0xFF232426)
private val brownSecondaryContainerDark = Color(0xFF633D24)


object Brown : AppColorScheme(
    name = "بني",
    lightColorScheme = lightColorScheme(
        primary = brownPrimary,
        secondary = brownSecondary,
        tertiary = brownTertiary,
        surface = brownSurface,
        secondaryContainer = brownSecondaryContainer
    ),
    darkColorScheme = darkColorScheme(
        primary = brownPrimaryDark,
        secondary = brownSecondaryDark,
        tertiary = brownTertiaryDark,
        surface = brownSurfaceDark,
        secondaryContainer = brownSecondaryContainerDark
    )
)