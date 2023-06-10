package com.shamela.library.presentaion.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shamela.library.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val NaskhFamily = FontFamily(
    Font(R.font.naskh_regular, FontWeight.Normal),
    Font(R.font.naskh_bold, FontWeight.Bold)
)
val AmiriFamily = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
)
val AlMajeedFamily = FontFamily(
    Font(R.font.al_majeed_regular, FontWeight.Normal),
)

val naskhSmall = TextStyle(fontFamily = NaskhFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
val naskhPlain = TextStyle(fontFamily = NaskhFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp)
val naskhBold = TextStyle(fontFamily = NaskhFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)

val amiriSmall = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
val amiriPlain = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp)

val alMajeedSmall = TextStyle(fontFamily = AlMajeedFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
val alMajeedPlain = TextStyle(fontFamily = AlMajeedFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp)


