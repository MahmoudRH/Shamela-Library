package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Brown : AppColorScheme(
    name = "بني",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF8D4A2B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCA),
        onPrimaryContainer = Color(0xFF331200),
        secondary = Color(0xFF765749),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDBCA),
        onSecondaryContainer = Color(0xFF2B160B),
        tertiary = Color(0xFF675E30),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEFE3A9),
        onTertiaryContainer = Color(0xFF201C00),
        background = Color(0xFFFFF8F6),
        onBackground = Color(0xFF221A16),
        surface = Color(0xFFFFF8F6),
        onSurface = Color(0xFF221A16),
        surfaceVariant = Color(0xFFF4DED5),
        onSurfaceVariant = Color(0xFF53433C),
        outline = Color(0xFF85736B),
        outlineVariant = Color(0xFFD8C2B9)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFFFB693),
        onPrimary = Color(0xFF552002),
        primaryContainer = Color(0xFF713316),
        onPrimaryContainer = Color(0xFFFFDBCA),
        secondary = Color(0xFFE6BEAD),
        onSecondary = Color(0xFF432A1E),
        secondaryContainer = Color(0xFF5C4033),
        onSecondaryContainer = Color(0xFFFFDBCA),
        tertiary = Color(0xFFD2C78F),
        onTertiary = Color(0xFF373006),
        tertiaryContainer = Color(0xFF4E471B),
        onTertiaryContainer = Color(0xFFEFE3A9),
        background = Color(0xFF1A120E),
        onBackground = Color(0xFFF1DFD8),
        surface = Color(0xFF1A120E),
        onSurface = Color(0xFFF1DFD8),
        surfaceVariant = Color(0xFF53433C),
        onSurfaceVariant = Color(0xFFD8C2B9),
        outline = Color(0xFFA08D84),
        outlineVariant = Color(0xFF53433C)
    )
)