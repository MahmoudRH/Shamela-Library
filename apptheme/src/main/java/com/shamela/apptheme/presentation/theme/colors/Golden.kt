package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Golden : AppColorScheme(
    name = "ذهبي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFFB08D57),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEEDBB3),
        onPrimaryContainer = Color(0xFF3D2700),
        secondary = Color(0xFF8B6F40),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8D4B0),
        onSecondaryContainer = Color(0xFF2E220F),
        tertiary = Color(0xFF755B32),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF9DDA8),
        onTertiaryContainer = Color(0xFF281800),
        background = Color(0xFFFDF6E5),
        onBackground = Color(0xFF1E1B16),
        surface = Color(0xFFFDF6E5),
        onSurface = Color(0xFF1E1B16),
        surfaceVariant = Color(0xFFEBE0C8),
        onSurfaceVariant = Color(0xFF4D4533),
        outline = Color(0xFF807560),
        outlineVariant = Color(0xFFD3C5AB)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFD8B57E),
        onPrimary = Color(0xFF3D2700),
        primaryContainer = Color(0xFF8B6F40),
        onPrimaryContainer = Color(0xFFEEDBB3),
        secondary = Color(0xFFC7B38F),
        onSecondary = Color(0xFF332711),
        secondaryContainer = Color(0xFF4B3D25),
        onSecondaryContainer = Color(0xFFE8D4B0),
        tertiary = Color(0xFFDCC18E),
        onTertiary = Color(0xFF402D0A),
        tertiaryContainer = Color(0xFF5A431D),
        onTertiaryContainer = Color(0xFFF9DDA8),
        background = Color(0xFF232426),
        onBackground = Color(0xFFE9E1D8),
        surface = Color(0xFF232426),
        onSurface = Color(0xFFE9E1D8),
        surfaceVariant = Color(0xFF4D4533),
        onSurfaceVariant = Color(0xFFD3C5AB),
        outline = Color(0xFF9B8F79),
        outlineVariant = Color(0xFF4D4533)
    )
)