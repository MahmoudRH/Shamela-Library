package com.shamela.apptheme

import com.shamela.apptheme.data.util.ArabicNormalizer
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ArabicNormalizerTest {
    @Test
    fun testNormalizer() {
        val normalizer = ArabicNormalizer()
        val text = "فَرَاشَةٌ مُلَوَّنَةٌ تَطِيْرُ في البُسْتَانِ"
        val result = normalizer.normalize(text)
        assertEquals("فراشه ملونه تطير في البستان", result)
    }
}
