package com.shamela.library.presentaion.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shamela.library.R


object AppFonts {

    private const val DEFAULT = "تلقائي"
    private const val NASKH = "خط النسخ"
    private const val TAJAWAL = "خط تجول"
    private const val AL_MAJEED = "خط المجيد"
    private const val KUFI = "خط كوفي"
    private const val REQA = "خط رقعة"
    private const val Messiri = "خط المسيري"

    val Typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
    )
    private val NaskhFamily = FontFamily(
        Font(R.font.naskh_regular, FontWeight.Normal),
        Font(R.font.naskh_bold, FontWeight.Bold)
    )
    private val TajawalFamily = FontFamily(
        Font(R.font.tajawal_regular, FontWeight.Normal),
        Font(R.font.tajawal_medium, FontWeight.Bold),
    )
    private val MessiriFamily = FontFamily(
        Font(R.font.messiri_regular, FontWeight.Normal),
        Font(R.font.messiri_medium, FontWeight.Bold),
    )
    private val AlMajeedFamily = FontFamily(Font(R.font.al_majeed_regular, FontWeight.Normal))
    private val KufiFamily = FontFamily(Font(R.font.kufi_regular, FontWeight.Normal))
    private val ReqaFamily = FontFamily(Font(R.font.reqa_regular, FontWeight.Normal))

    private val availableFonts = mapOf(
        DEFAULT to FontFamily.Default,
        NASKH to NaskhFamily,
        TAJAWAL to TajawalFamily,
        AL_MAJEED to AlMajeedFamily,
        KUFI to KufiFamily,
        REQA to ReqaFamily,
        Messiri to MessiriFamily,
    )

    fun getAvailableFontFamilies(): Set<String> {
        return availableFonts.keys
    }

    fun getAvailableFontSizes() = setOf("4","2","0","-2","-4")

    fun fontFamilyOf(font: String): FontFamily {
        return if (availableFonts.contains(font)) {
            availableFonts[font]!!
        } else
            availableFonts[NASKH]!!
    }

    private val selectedFontFamily = mutableStateOf(TajawalFamily)
    private val selectedFontSize = mutableStateOf(0)
    fun changeFontFamily(newFontFamily: FontFamily) {
        selectedFontFamily.value = newFontFamily
    }
    fun changeFontSize(newFontSize: Int) {
        selectedFontSize.value = newFontSize
    }

    val textSmall by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = (selectedFontSize.value+12).sp
        )
    }
    val textSmallBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value+12).sp
        )
    }

    val textNormal by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = (selectedFontSize.value+16).sp
        )
    }

    val textNormalBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value+16).sp
        )
    }

    val textLarge by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = (selectedFontSize.value+20).sp
        )
    }

    val textLargeBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value+20).sp
        )
    }
}



