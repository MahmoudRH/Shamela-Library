package com.shamela.apptheme.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


private val indigoPrimary = Color(0xFF2C394B)
private val indigoSecondary = Color(0xFF1D2731)
private val indigoTertiary = Color(0xFF495E70)
private val indigoSurface = Color(0xFFF2F4F7)
private val indigoSecondaryContainer = Color(0xFF506880)

private val indigoPrimaryDark = Color(0xFF2C3847)
private val indigoSecondaryDark = Color(0xFF1C3A45)
private val indigoTertiaryDark = Color(0xFF3A4B5B)
private val indigoSurfaceDark = Color(0xFF0D1117)
private val indigoSecondaryContainerDark = Color(0xFF293441)


object Indigo : AppColorScheme(
    name = "نيلي",
    lightColorScheme = lightColorScheme(
        primary = indigoPrimary,
        secondary = indigoSecondary,
        tertiary = indigoTertiary,
        surface = indigoSurface,
        secondaryContainer = indigoSecondaryContainer
    ),
    darkColorScheme = darkColorScheme(
        primary = indigoPrimaryDark,
        secondary = indigoSecondaryDark,
        tertiary = indigoTertiaryDark,
        surface = indigoSurfaceDark,
        secondaryContainer = indigoSecondaryContainerDark
    )
)