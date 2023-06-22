package com.shamela.apptheme.theme.colors

import android.content.Context
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import com.shamela.apptheme.theme.colors.AppColorScheme

class MaterialYou(context: Context) : AppColorScheme(
    name = "ألوان النظام (اندرويد 12 فأعلى)",
    lightColorScheme = dynamicLightColorScheme(context),
    darkColorScheme = dynamicDarkColorScheme(context)
)