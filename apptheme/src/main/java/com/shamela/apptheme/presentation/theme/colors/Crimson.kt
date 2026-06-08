package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Crimson : AppColorScheme(
    name = "قرمزي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF904147),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD8),
        onPrimaryContainer = Color(0xFF3B000C),
        secondary = Color(0xFF765655),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDAD8),
        onSecondaryContainer = Color(0xFF2C1514),
        background = Color(0xFFFFF8F7),
        onBackground = Color(0xFF221919),
        surface = Color(0xFFFFF8F7),
        onSurface = Color(0xFF221919),
        surfaceVariant = Color(0xFFF4DDDD),
        onSurfaceVariant = Color(0xFF524343),
        outline = Color(0xFF857372),
        outlineVariant = Color(0xFFD7C1C1)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFFFB3B8),
        onPrimary = Color(0xFF57141D),
        primaryContainer = Color(0xFF732A31),
        onPrimaryContainer = Color(0xFFFFDAD8),
        secondary = Color(0xFFE6BDBC),
        onSecondary = Color(0xFF442928),
        secondaryContainer = Color(0xFF5D3F3E),
        onSecondaryContainer = Color(0xFFFFDAD8),
        background = Color(0xFF1A1111),
        onBackground = Color(0xFFF0DFDF),
        surface = Color(0xFF1A1111),
        onSurface = Color(0xFFF0DFDF),
        surfaceVariant = Color(0xFF524343),
        onSurfaceVariant = Color(0xFFD7C1C1),
        outline = Color(0xFFA08C8B),
        outlineVariant = Color(0xFF524343)
    )
)