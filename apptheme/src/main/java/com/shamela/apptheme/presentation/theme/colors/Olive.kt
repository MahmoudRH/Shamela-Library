package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Olive : AppColorScheme(
    name = "أخضر زيتي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF4E662A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCEECA8),
        onPrimaryContainer = Color(0xFF122000),
        secondary = Color(0xFF586249),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDCE7C8),
        onSecondaryContainer = Color(0xFF161E0A),
        tertiary = Color(0xFF396660),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBCECE5),
        onTertiaryContainer = Color(0xFF00201D),
        background = Color(0xFFFDFDF5),
        onBackground = Color(0xFF1A1C16),
        surface = Color(0xFFFDFDF5),
        onSurface = Color(0xFF1A1C16),
        surfaceVariant = Color(0xFFE2E4D4),
        onSurfaceVariant = Color(0xFF45483B),
        outline = Color(0xFF75786A),
        outlineVariant = Color(0xFFC5C8B9)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFB2D08E),
        onPrimary = Color(0xFF223600),
        primaryContainer = Color(0xFF374E14),
        onPrimaryContainer = Color(0xFFCEECA8),
        secondary = Color(0xFFC0CBAD),
        onSecondary = Color(0xFF2A331E),
        secondaryContainer = Color(0xFF414A33),
        onSecondaryContainer = Color(0xFFDCE7C8),
        tertiary = Color(0xFFA0D0C9),
        onTertiary = Color(0xFF013732),
        tertiaryContainer = Color(0xFF204E48),
        onTertiaryContainer = Color(0xFFBCECE5),
        background = Color(0xFF12140E),
        onBackground = Color(0xFFE3E3DC),
        surface = Color(0xFF12140E),
        onSurface = Color(0xFFE3E3DC),
        surfaceVariant = Color(0xFF45483B),
        onSurfaceVariant = Color(0xFFC5C8B9),
        outline = Color(0xFF8F9283),
        outlineVariant = Color(0xFF45483B)
    )
)