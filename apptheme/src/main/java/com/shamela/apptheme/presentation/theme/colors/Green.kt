package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


//______Light_Green_______//
private val greenPrimary = Color(0xFF52A954)
private val greenSecondary = Color(0xFF368C40)
private val greenTertiary = Color(0xFF86B56B)
private val greenSurface = Color(0xFFF5FAFF)
private val greenSecondaryContainer = Color(0xFF4C9E4D)

//______Dark_Green_______//
private val greenPrimaryDark = Color(0xFF468B4C)
private val greenSecondaryDark = Color(0xFF2E7537)
private val greenTertiaryDark = Color(0xFF7CA861)
private val greenSurfaceDark = Color(0xFF232426)
private val greenSecondaryContainerDark = Color(0xFF307438)

object Green : AppColorScheme(
    "أخضر",
    lightColorScheme(
        primary = greenPrimary,
        secondary = greenSecondary,
        tertiary = greenTertiary,
        surface = greenSurface,
        secondaryContainer = greenSecondaryContainer
    ),
    darkColorScheme(
        primary = greenPrimaryDark,
        secondary = greenSecondaryDark,
        tertiary = greenTertiaryDark,
        surface = greenSurfaceDark,
        secondaryContainer = greenSecondaryContainerDark
    )
)