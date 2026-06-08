package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Indigo : AppColorScheme(
    name = "نيلي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF434EAE),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDFE0FF),
        onPrimaryContainer = Color(0xFF000B62),
        secondary = Color(0xFF5B5D72),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE0E1F9),
        onSecondaryContainer = Color(0xFF181A2C),
        tertiary = Color(0xFF76546D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD7F3),
        onTertiaryContainer = Color(0xFF2D1228),
        background = Color(0xFFFBFBFF),
        onBackground = Color(0xFF1A1B21),
        surface = Color(0xFFFBFBFF),
        onSurface = Color(0xFF1A1B21),
        surfaceVariant = Color(0xFFE3E1EC),
        onSurfaceVariant = Color(0xFF46464F),
        outline = Color(0xFF767680),
        outlineVariant = Color(0xFFC7C5D0)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFBCC2FF),
        onPrimary = Color(0xFF0C1B80),
        primaryContainer = Color(0xFF293496),
        onPrimaryContainer = Color(0xFFDFE0FF),
        secondary = Color(0xFFC3C5DD),
        onSecondary = Color(0xFF2D2F42),
        secondaryContainer = Color(0xFF434659),
        onSecondaryContainer = Color(0xFFE0E1F9),
        tertiary = Color(0xFFE6BAD9),
        onTertiary = Color(0xFF44263E),
        tertiaryContainer = Color(0xFF5D3C55),
        onTertiaryContainer = Color(0xFFFFD7F3),
        background = Color(0xFF1A1B21),
        onBackground = Color(0xFFE4E2E6),
        surface = Color(0xFF1A1B21),
        onSurface = Color(0xFFE4E2E6),
        surfaceVariant = Color(0xFF46464F),
        onSurfaceVariant = Color(0xFFC7C5D0),
        outline = Color(0xFF90909A),
        outlineVariant = Color(0xFF46464F)
    )
)