package com.shamela.apptheme.presentation.theme.colors

import android.content.Context


object AppColors {

    private const val MATERIAL = "ألوان النظام"

    private val availableColorSchemes =
        AppColorScheme::class.sealedSubclasses.map { it.objectInstance }

    fun colorSchemeOf(color: String, context: Context): AppColorScheme {
        return availableColorSchemes.find { it?.name == color } ?: if (color == MATERIAL)
            MaterialYou(context)
        else
            availableColorSchemes.first()!!
    }

    fun getAvailableColorSchemes(): Set<String> {
        val colorsSet = availableColorSchemes.map { it?.name ?: MATERIAL }.toSet()
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) colorsSet else colorsSet.filterNot { it == MATERIAL }.toSet()
    }


}



