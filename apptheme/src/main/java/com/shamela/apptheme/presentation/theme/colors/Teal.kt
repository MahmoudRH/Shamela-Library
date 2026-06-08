package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Teal : AppColorScheme(
    name = "فيروزي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF006972),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF8EF2FF),
        onPrimaryContainer = Color(0xFF001F23),
        secondary = Color(0xFF4A6365),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8EA),
        onSecondaryContainer = Color(0xFF051F22),
        background = Color(0xFFFAFDFA),
        onBackground = Color(0xFF191C1C),
        surface = Color(0xFFFAFDFA),
        onSurface = Color(0xFF191C1C),
        surfaceVariant = Color(0xFFDAE4E5),
        onSurfaceVariant = Color(0xFF3F484A),
        outline = Color(0xFF6F797A),
        outlineVariant = Color(0xFFBEC8C9)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFF4ED8E6),
        onPrimary = Color(0xFF00363B),
        primaryContainer = Color(0xFF004F56),
        onPrimaryContainer = Color(0xFF8EF2FF),
        secondary = Color(0xFFB0CCCE),
        onSecondary = Color(0xFF1C3437),
        secondaryContainer = Color(0xFF324B4D),
        onSecondaryContainer = Color(0xFFCCE8EA),
        background = Color(0xFF191C1C),
        onBackground = Color(0xFFE0E3E2),
        surface = Color(0xFF191C1C),
        onSurface = Color(0xFFE0E3E2),
        surfaceVariant = Color(0xFF3F484A),
        onSurfaceVariant = Color(0xFFBEC8C9),
        outline = Color(0xFF899294),
        outlineVariant = Color(0xFF3F484A)
    )
)