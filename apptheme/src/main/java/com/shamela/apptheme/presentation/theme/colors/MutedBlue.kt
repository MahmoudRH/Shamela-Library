package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object MutedBlue : AppColorScheme(
    name = "أزرق باهت",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF3A6481),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCBE6FF),
        onPrimaryContainer = Color(0xFF001E30),
        secondary = Color(0xFF51606A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD4E4F1),
        onSecondaryContainer = Color(0xFF0C1D26),
        tertiary = Color(0xFF63597C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE8DEFF),
        onTertiaryContainer = Color(0xFF1F1635),
        background = Color(0xFFF6F9FF),
        onBackground = Color(0xFF181C1F),
        surface = Color(0xFFF6F9FF),
        onSurface = Color(0xFF181C1F),
        surfaceVariant = Color(0xFFDDE3EA),
        onSurfaceVariant = Color(0xFF41474D),
        outline = Color(0xFF72787E),
        outlineVariant = Color(0xFFC1C7CE)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFF99CBFA),
        onPrimary = Color(0xFF01354F),
        primaryContainer = Color(0xFF1F4C68),
        onPrimaryContainer = Color(0xFFCBE6FF),
        secondary = Color(0xFFB8C8D5),
        onSecondary = Color(0xFF23323B),
        secondaryContainer = Color(0xFF394852),
        onSecondaryContainer = Color(0xFFD4E4F1),
        tertiary = Color(0xFFCBC1E9),
        onTertiary = Color(0xFF342B4B),
        tertiaryContainer = Color(0xFF4B4263),
        onTertiaryContainer = Color(0xFFE8DEFF),
        background = Color(0xFF101417),
        onBackground = Color(0xFFE0E3E7),
        surface = Color(0xFF101417),
        onSurface = Color(0xFFE0E3E7),
        surfaceVariant = Color(0xFF41474D),
        onSurfaceVariant = Color(0xFFC1C7CE),
        outline = Color(0xFF8B9198),
        outlineVariant = Color(0xFF41474D)
    )
)