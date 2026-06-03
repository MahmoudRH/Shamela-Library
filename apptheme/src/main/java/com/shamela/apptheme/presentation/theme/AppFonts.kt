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
    private const val KITAB = "خط كِتاب"
    private const val TAJAWAL = "خط تَجَوَّل"
    private const val KUFI = "خط كوفي"
    private const val MESSIRI = "خط المسيري"
    private const val CAIRO = "خط كايرو"
    private const val IBM_PLEX = "خط آي بي إم"
    private const val NOTO_NASKH = "خط نوتو نسخ"
    private const val SCHEHERAZADE = "خط شهرزاد"

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
    private lateinit var KitabFamily: FontFamily
    private lateinit var TajawalFamily: FontFamily
    private lateinit var MessiriFamily: FontFamily
    private lateinit var KufiFamily: FontFamily
    private lateinit var CairoFamily: FontFamily
    private lateinit var IbmPlexFamily: FontFamily
    private lateinit var NotoNaskhFamily: FontFamily
    private lateinit var ScheherazadeFamily: FontFamily

    private val availableFonts = mutableMapOf<String, Pair<FontFamily, Typeface?>>()

    fun init(context: Context) {
        val assets = context.assets
        fun typeface(file: String) = Typeface.createFromAsset(assets, "fonts/$file")

        val amiriTypeface = typeface("amiri_regular.ttf")
        val kitabTypeface = typeface("kitab_regular.ttf")
        val tajawalTypeface = typeface("tajawal_regular.ttf")
        val messiriTypeface = typeface("messiri_regular.ttf")
        val kufiTypeface = typeface("kufi_regular.ttf")
        val cairoTypeface = typeface("cairo_regular.ttf")
        val ibmPlexTypeface = typeface("ibm_plex_arabic_regular.ttf")
        val notoNaskhTypeface = typeface("noto_naskh_arabic_regular.ttf")
        val scheherazadeTypeface = typeface("scheherazade_regular.ttf")

        AmiriFamily = FontFamily(amiriTypeface)
        KitabFamily = FontFamily(kitabTypeface)
        TajawalFamily = FontFamily(tajawalTypeface)
        MessiriFamily = FontFamily(messiriTypeface)
        KufiFamily = FontFamily(kufiTypeface)
        CairoFamily = FontFamily(cairoTypeface)
        IbmPlexFamily = FontFamily(ibmPlexTypeface)
        NotoNaskhFamily = FontFamily(notoNaskhTypeface)
        ScheherazadeFamily = FontFamily(scheherazadeTypeface)

        availableFonts.clear()
        availableFonts[DEFAULT] = Pair(FontFamily.Default, null)
        availableFonts[AMIRI] = Pair(AmiriFamily, amiriTypeface)
        availableFonts[KITAB] = Pair(KitabFamily, kitabTypeface)
        availableFonts[TAJAWAL] = Pair(TajawalFamily, tajawalTypeface)
        availableFonts[MESSIRI] = Pair(MessiriFamily, messiriTypeface)
        availableFonts[KUFI] = Pair(KufiFamily, kufiTypeface)
        availableFonts[CAIRO] = Pair(CairoFamily, cairoTypeface)
        availableFonts[IBM_PLEX] = Pair(IbmPlexFamily, ibmPlexTypeface)
        availableFonts[NOTO_NASKH] = Pair(NotoNaskhFamily, notoNaskhTypeface)
        availableFonts[SCHEHERAZADE] = Pair(ScheherazadeFamily, scheherazadeTypeface)

        selectedFontFamily.value = TajawalFamily
    }

    fun getAvailableFontFamilies(): Set<String> {
        return availableFonts.keys.sorted().toSet()
    }

    fun getAvailableFontSizes() = setOf("4", "2", "0", "-2", "-4")

    fun fontFamilyOf(font: String): FontFamily {
        return availableFonts[font]?.first ?: availableFonts[NOTO_NASKH]?.first ?: FontFamily.Default
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
            KitabFamily -> "kitab"
            KufiFamily -> "kufi"
            MessiriFamily -> "messiri"
            TajawalFamily -> "tajawal"
            CairoFamily -> "cairo"
            IbmPlexFamily -> "ibm_plex"
            NotoNaskhFamily -> "noto_naskh"
            ScheherazadeFamily -> "scheherazade"
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
