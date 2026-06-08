package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Blue : AppColorScheme(
    name = "أزرق",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF1E609A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD2E4FF),
        onPrimaryContainer = Color(0xFF001C37),
        secondary = Color(0xFF535F70),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD7E3F8),
        onSecondaryContainer = Color(0xFF101C2B),
        tertiary = Color(0xFF6B5778),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF2DAFF),
        onTertiaryContainer = Color(0xFF251431),
        background = Color(0xFFFCFCFF),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFCFCFF),
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFDFE2EB),
        onSurfaceVariant = Color(0xFF43474E),
        outline = Color(0xFF73777F),
        outlineVariant = Color(0xFFC3C6CF)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFA0C9FF),
        onPrimary = Color(0xFF00325A),
        primaryContainer = Color(0xFF004880),
        onPrimaryContainer = Color(0xFFD2E4FF),
        secondary = Color(0xFFBBC7DB),
        onSecondary = Color(0xFF253140),
        secondaryContainer = Color(0xFF3B4858),
        onSecondaryContainer = Color(0xFFD7E3F8),
        tertiary = Color(0xFFD6BEE4),
        onTertiary = Color(0xFF3B2948),
        tertiaryContainer = Color(0xFF523F5F),
        onTertiaryContainer = Color(0xFFF2DAFF),
        background = Color(0xFF1A1C1E),
        onBackground = Color(0xFFE2E2E6),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE2E2E6),
        surfaceVariant = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C6CF),
        outline = Color(0xFF8D9199),
        outlineVariant = Color(0xFF43474E)
    )
)