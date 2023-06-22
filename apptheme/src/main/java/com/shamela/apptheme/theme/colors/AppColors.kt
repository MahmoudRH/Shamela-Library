package com.shamela.apptheme.theme.colors

import android.content.Context


object AppColors {

    private const val MATERIAL = "ألوان النظام (اندرويد 12 فأعلى)"

    private val availableColorSchemes =
        AppColorScheme::class.sealedSubclasses.map { it.objectInstance }

    fun colorSchemeOf(color: String, context: Context): AppColorScheme {
        return availableColorSchemes.find { it?.name == color } ?: if (color == MATERIAL)
            MaterialYou(context)
        else
            availableColorSchemes.first()!!
    }

    fun getAvailableColorSchemes(): Set<String> {
        return availableColorSchemes.map { it?.name ?: MATERIAL }.toSet()
    }


}



