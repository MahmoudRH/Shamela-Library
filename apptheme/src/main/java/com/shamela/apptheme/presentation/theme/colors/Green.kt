package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Green : AppColorScheme(
    name = "أخضر",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF2E6B38),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB0F3B2),
        onPrimaryContainer = Color(0xFF002107),
        secondary = Color(0xFF516350),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD4E8D0),
        onSecondaryContainer = Color(0xFF0F1F10),
        tertiary = Color(0xFF39656B),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBCEBF2),
        onTertiaryContainer = Color(0xFF001F23),
        background = Color(0xFFFCFDF7),
        onBackground = Color(0xFF1A1C19),
        surface = Color(0xFFFCFDF7),
        onSurface = Color(0xFF1A1C19),
        surfaceVariant = Color(0xFFDFE4D8),
        onSurfaceVariant = Color(0xFF424940),
        outline = Color(0xFF73796F),
        outlineVariant = Color(0xFFC3C8BB)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFF95D698),
        onPrimary = Color(0xFF003913),
        primaryContainer = Color(0xFF125223),
        onPrimaryContainer = Color(0xFFB0F3B2),
        secondary = Color(0xFFB8CCB5),
        onSecondary = Color(0xFF243424),
        secondaryContainer = Color(0xFF3A4B3A),
        onSecondaryContainer = Color(0xFFD4E8D0),
        tertiary = Color(0xFFA0CFD5),
        onTertiary = Color(0xFF00363C),
        tertiaryContainer = Color(0xFF1F4D53),
        onTertiaryContainer = Color(0xFFBCEBF2),
        background = Color(0xFF1A1C19),
        onBackground = Color(0xFFE2E3DD),
        surface = Color(0xFF1A1C19),
        onSurface = Color(0xFFE2E3DD),
        surfaceVariant = Color(0xFF424940),
        onSurfaceVariant = Color(0xFFC3C8BB),
        outline = Color(0xFF8C9388),
        outlineVariant = Color(0xFF424940)
    )
)