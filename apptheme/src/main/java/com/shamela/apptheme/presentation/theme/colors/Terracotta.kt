package com.shamela.apptheme.presentation.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Terracotta : AppColorScheme(
    name = "قرميدي",
    lightColorScheme = lightColorScheme(
        primary = Color(0xFF984726),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCD),
        onPrimaryContainer = Color(0xFF360F00),
        secondary = Color(0xFF76574B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDBCD),
        onSecondaryContainer = Color(0xFF2C160C),
        background = Color(0xFFFFFBFF),
        onBackground = Color(0xFF201A18),
        surface = Color(0xFFFFFBFF),
        onSurface = Color(0xFF201A18),
        surfaceVariant = Color(0xFFF4DED5),
        onSurfaceVariant = Color(0xFF53433C),
        outline = Color(0xFF85736B),
        outlineVariant = Color(0xFFD8C2B9)
    ),
    darkColorScheme = darkColorScheme(
        primary = Color(0xFFFFB598),
        onPrimary = Color(0xFF5A1C00),
        primaryContainer = Color(0xFF7A3010),
        onPrimaryContainer = Color(0xFFFFDBCD),
        secondary = Color(0xFFE7BDB0),
        onSecondary = Color(0xFF442A1F),
        secondaryContainer = Color(0xFF5D4034),
        onSecondaryContainer = Color(0xFFFFDBCD),
        background = Color(0xFF201A18),
        onBackground = Color(0xFFECE0DB),
        surface = Color(0xFF201A18),
        onSurface = Color(0xFFECE0DB),
        surfaceVariant = Color(0xFF53433C),
        onSurfaceVariant = Color(0xFFD8C2B9),
        outline = Color(0xFFA08C84),
        outlineVariant = Color(0xFF53433C)
    )
)