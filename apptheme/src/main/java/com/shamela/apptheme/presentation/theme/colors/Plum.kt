package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Plum : AppColorScheme(
    name = "أرجواني",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF794B73),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD7F5),
        onPrimaryContainer = Color(0xFF2F072D),
        secondary = Color(0xFF6A5767),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3DAEE),
        onSecondaryContainer = Color(0xFF241523),
        background = Color(0xFFFEF7FF),
        onBackground = Color(0xFF1D1A1D),
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1A1D),
        surfaceVariant = Color(0xFFEEDEE8),
        onSurfaceVariant = Color(0xFF4E444B),
        outline = Color(0xFF80747B),
        outlineVariant = Color(0xFFD1C2CC)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFEAB7E3),
        onPrimary = Color(0xFF461D43),
        primaryContainer = Color(0xFF5F345A),
        onPrimaryContainer = Color(0xFFFFD7F5),
        secondary = Color(0xFFD6BED1),
        onSecondary = Color(0xFF3B2A38),
        secondaryContainer = Color(0xFF52404F),
        onSecondaryContainer = Color(0xFFF3DAEE),
        background = Color(0xFF1D1A1D),
        onBackground = Color(0xFFE8E0E5),
        surface = Color(0xFF1D1A1D),
        onSurface = Color(0xFFE8E0E5),
        surfaceVariant = Color(0xFF4E444B),
        onSurfaceVariant = Color(0xFFD1C2CC),
        outline = Color(0xFF9A8D95),
        outlineVariant = Color(0xFF4E444B)
    )
)