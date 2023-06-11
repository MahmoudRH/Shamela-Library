package com.shamela.library.presentaion.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


//______Light_Olive_______//
private val olivePrimary = Color(0xFF6B8E23)
private val oliveSecondary = Color(0xFF556B2F)
private val oliveTertiary = Color(0xFFA2B964)
private val oliveSurface = Color(0xFFF5FAFF)
private val oliveSecondaryContainer = Color(0xFF678D32)

//______Dark_Olive_______//
private val olivePrimaryDark = Color(0xFF526F1D)
private val oliveSecondaryDark = Color(0xFF3E5125)
private val oliveTertiaryDark = Color(0xFF8E9E51)
private val oliveSurfaceDark = Color(0xFF232426)
private val oliveSecondaryContainerDark = Color(0xFF4D6E26)


object Olive : AppColorScheme(
    "أخضر زيتي",
    lightColorScheme(
        primary = olivePrimary,
        secondary = oliveSecondary,
        tertiary = oliveTertiary,
        surface = oliveSurface,
        secondaryContainer = oliveSecondaryContainer
    ),
    darkColorScheme(
        primary = olivePrimaryDark,
        secondary = oliveSecondaryDark,
        tertiary = oliveTertiaryDark,
        surface = oliveSurfaceDark,
        secondaryContainer = oliveSecondaryContainerDark
    )
)