package com.shamela.apptheme.data.util


class ArabicNormalizer {

    companion object {
        // Define mapping for normalization
        private val normalizationMap = mapOf(
            '\u0622' to '\u0627', // ALEF_MADDA to ALEF
            '\u0623' to '\u0627', // ALEF_HAMZA_ABOVE to ALEF
            '\u0625' to '\u0627', // ALEF_HAMZA_BELOW to ALEF
            '\u0629' to '\u0647', // TEH_MARBUTA to HEH
        )
        private val tashkeel = listOf(
            '\u064B',            // FATHATAN
            '\u064C',            // DAMMATAN
            '\u064D',            // KASRATAN
            '\u064E',            // FATHA
            '\u064F',            // DAMMA
            '\u0650',            // KASRA
            '\u0651',            // SHADDA
            '\u0652'             // SUKUN
        )
    }

    /**
     * Normalize an Arabic text input.
     *
     * @param input The input string containing Arabic text.
     * @return The normalized string.
     */
    fun normalize(input: String): String {
        val normalizedText = StringBuilder()
        for (char in input) {
            if (char in tashkeel) continue
            val normalizedChar = normalizationMap[char] ?: char
            normalizedText.append(normalizedChar)
        }
        return normalizedText.toString()
    }
}
