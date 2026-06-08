package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Monochrome : AppColorScheme(
    name = "رمادي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF454749),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFC8CACC),
        onPrimaryContainer = Color(0xFF131416),
        secondary = Color(0xFF5C5E60),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE2E2E4),
        onSecondaryContainer = Color(0xFF191C1D),
        background = Color(0xFFF9F9F9),
        onBackground = Color(0xFF1A1C1D),
        surface = Color(0xFFF9F9F9),
        onSurface = Color(0xFF1A1C1D),
        surfaceVariant = Color(0xFFDFE3E5),
        onSurfaceVariant = Color(0xFF434749),
        outline = Color(0xFF73777A),
        outlineVariant = Color(0xFFC3C7CA)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFC6C6C6),
        onPrimary = Color(0xFF171B1D),
        primaryContainer = Color(0xFF2D3133),
        onPrimaryContainer = Color(0xFFE2E2E4),
        secondary = Color(0xFFC6C6C6),
        onSecondary = Color(0xFF2E3132),
        secondaryContainer = Color(0xFF444749),
        onSecondaryContainer = Color(0xFFE2E2E4),
        background = Color(0xFF121415),
        onBackground = Color(0xFFE2E2E4),
        surface = Color(0xFF121415),
        onSurface = Color(0xFFE2E2E4),
        surfaceVariant = Color(0xFF434749),
        onSurfaceVariant = Color(0xFFC3C7CA),
        outline = Color(0xFF8D9194),
        outlineVariant = Color(0xFF434749)
    )
)