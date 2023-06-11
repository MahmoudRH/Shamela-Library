package com.shamela.library.presentaion.theme.colors

import androidx.compose.material3.ColorScheme

sealed class AppColorScheme(
    val name :String = "",
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme,
)