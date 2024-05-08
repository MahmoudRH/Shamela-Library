package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

//______Light_Blue_______//
private val bluePrimary = Color(0xff699BC9)
private val blueSecondary = Color(0xff4482BB)
private val blueTertiary = Color(0xff925461)
private val blueSurface = Color(0xFFF5FAFF)
private val blueSecondaryContainer = Color(0xFF539FE6)

//______Dark_Blue_______//
private val bluePrimaryDark = Color(0xFF306796)
private val blueSecondaryDark = Color(0xFF165087)
private val blueTertiaryDark = Color(0xffD9A87F)
private val blueSurfaceDark = Color(0xFF232426)
private val blueSecondaryContainerDark = Color(0xFF144A80)

object Blue : AppColorScheme(
    name = "أزرق",
    lightColorScheme = lightColorScheme(
        primary = bluePrimary,
        secondary = blueSecondary,
        tertiary = blueTertiary,
        surface = blueSurface,
        secondaryContainer = blueSecondaryContainer
    ),
    darkColorScheme = darkColorScheme(
        primary = bluePrimaryDark,
        secondary = blueSecondaryDark,
        tertiary = blueTertiaryDark,
        surface = blueSurfaceDark,
        secondaryContainer = blueSecondaryContainerDark
    ),

    )