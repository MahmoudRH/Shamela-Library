package com.shamela.apptheme.presentation.theme

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp


object AppFonts {

    private const val DEFAULT = "خط النظام"
    private const val AMIRI = "خط أميري"
    private const val NASKH = "خط النسخ"
    private const val NASKH_2 = "خط النسخ 2"
    private const val NASKH_3 = "خط النسخ 3"
    private const val KITAB = "خط كِتاب"
    private const val JOZOOR = "خط جُذور"
    private const val FLAT = "خط مسطح"
    private const val TAJAWAL = "خط تَجَوَّل"
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

    private lateinit var AmiriFamily: FontFamily
    private lateinit var FlatFamily: FontFamily
    private lateinit var JozoorFamily: FontFamily
    private lateinit var KitabFamily: FontFamily
    private lateinit var NaskhFamily: FontFamily
    private lateinit var Naskh2Family: FontFamily
    private lateinit var Naskh3Family: FontFamily
    private lateinit var TajawalFamily: FontFamily
    private lateinit var MessiriFamily: FontFamily
    private lateinit var KufiFamily: FontFamily
    private lateinit var ReqaFamily: FontFamily

    private val availableFonts = mutableMapOf<String, Pair<FontFamily, Typeface?>>()

    fun init(context: Context) {
        val assets = context.assets
        fun typeface(file: String) = Typeface.createFromAsset(assets, "fonts/$file")

        val amiriTypeface = typeface("amiri_regular.ttf")
        val flatTypeface = typeface("flat_regular.ttf")
        val jozoorTypeface = typeface("jozoor_regular.ttf")
        val kitabTypeface = typeface("kitab_regular.ttf")
        val naskhTypeface = typeface("naskh_regular.ttf")
        val naskh2Typeface = typeface("naskh_2_regular.ttf")
        val naskh3Typeface = typeface("naskh_3_regular.ttf")
        val tajawalTypeface = typeface("tajawal_regular.ttf")
        val messiriTypeface = typeface("messiri_regular.ttf")
        val kufiTypeface = typeface("kufi_regular.ttf")
        val reqaTypeface = typeface("reqa_regular.ttf")

        AmiriFamily = FontFamily(amiriTypeface)
        FlatFamily = FontFamily(flatTypeface)
        JozoorFamily = FontFamily(jozoorTypeface)
        KitabFamily = FontFamily(kitabTypeface)
        NaskhFamily = FontFamily(naskhTypeface)
        Naskh2Family = FontFamily(naskh2Typeface)
        Naskh3Family = FontFamily(naskh3Typeface)
        TajawalFamily = FontFamily(tajawalTypeface)
        MessiriFamily = FontFamily(messiriTypeface)
        KufiFamily = FontFamily(kufiTypeface)
        ReqaFamily = FontFamily(reqaTypeface)

        availableFonts.clear()
        availableFonts[DEFAULT] = Pair(FontFamily.Default, null)
        availableFonts[AMIRI] = Pair(AmiriFamily, amiriTypeface)
        availableFonts[FLAT] = Pair(FlatFamily, flatTypeface)
        availableFonts[JOZOOR] = Pair(JozoorFamily, jozoorTypeface)
        availableFonts[KITAB] = Pair(KitabFamily, kitabTypeface)
        availableFonts[NASKH] = Pair(NaskhFamily, naskhTypeface)
        availableFonts[NASKH_2] = Pair(Naskh2Family, naskh2Typeface)
        availableFonts[NASKH_3] = Pair(Naskh3Family, naskh3Typeface)
        availableFonts[TAJAWAL] = Pair(TajawalFamily, tajawalTypeface)
        availableFonts[Messiri] = Pair(MessiriFamily, messiriTypeface)
        availableFonts[KUFI] = Pair(KufiFamily, kufiTypeface)
        availableFonts[REQA] = Pair(ReqaFamily, reqaTypeface)

        selectedFontFamily.value = TajawalFamily
    }

    fun getAvailableFontFamilies(): Set<String> {
        return availableFonts.keys.sorted().toSet()
    }

    fun getAvailableFontSizes() = setOf("4", "2", "0", "-2", "-4")

    fun fontFamilyOf(font: String): FontFamily {
        return availableFonts[font]?.first ?: availableFonts[NASKH]?.first ?: FontFamily.Default
    }

    private val selectedFontFamily = mutableStateOf<FontFamily>(FontFamily.Default)
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
            fontSize = (selectedFontSize.value + 12).sp
        )
    }
    val textSmallBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value + 12).sp
        )
    }

    val textNormal by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = (selectedFontSize.value + 16).sp,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Bottom, trim = LineHeightStyle.Trim.Both)
        )
    }

    val textNormalBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value + 16).sp
        )
    }

    val textLarge by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Normal,
            fontSize = (selectedFontSize.value + 20).sp
        )
    }

    val textLargeBold by derivedStateOf {
        TextStyle(
            fontFamily = selectedFontFamily.value,
            fontWeight = FontWeight.Bold,
            fontSize = (selectedFontSize.value + 20).sp
        )
    }

    fun selectedFontTypeFace(context: Context): Typeface? {
        return availableFonts.entries.find { it.value.first == selectedFontFamily.value }?.value?.second
    }

    fun selectedFontFamilyCssClass(): String {
        return when (selectedFontFamily.value) {
            AmiriFamily -> "amiri"
            FlatFamily -> "flat"
            JozoorFamily -> "jozoor"
            KitabFamily -> "kitab"
            KufiFamily -> "kufi"
            MessiriFamily -> "messiri"
            NaskhFamily -> "naskh"
            Naskh2Family -> "naskh_2"
            Naskh3Family -> "naskh_3"
            ReqaFamily -> "reqa"
            TajawalFamily -> "tajawal"
            else -> ""
        }
    }

    fun selectedFontSizeCssClass(): String {
        val fontSizeClasses =
            listOf("textSizeOne", "textSizeTwo", "textSizeThree", "textSizeFour", "textSizeFive")
        val availableFontSizes = getAvailableFontSizes().map { it.toInt() }.sorted()
        val fontSizeClassMap = availableFontSizes.zip(fontSizeClasses).toMap()
        return fontSizeClassMap[selectedFontSize.value] ?: "textSizeTwo"
    }
}
