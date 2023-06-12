package com.shamela.library.presentation.theme.colors

import android.content.Context
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

class MaterialYou(context: Context) : AppColorScheme(
    name = "ألوان النظام (اندرويد 12 فأعلى)",
    lightColorScheme = dynamicLightColorScheme(context),
    darkColorScheme = dynamicDarkColorScheme(context)
)