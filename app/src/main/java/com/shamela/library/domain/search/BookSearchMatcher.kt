package com.shamela.library.domain.search

import com.shamela.apptheme.data.util.ArabicNormalizer
import com.shamela.library.domain.model.Book

object BookSearchMatcher {
    private val normalizer = ArabicNormalizer()

    fun matches(book: Book, query: String): Boolean {
        if (query.isEmpty()) return true
        val normalizedQuery = normalizer.normalize(query)
        return normalizer.normalize(book.title).contains(normalizedQuery) ||
               normalizer.normalize(book.author).contains(normalizedQuery)
    }

    /**
     * Returns the range in [text] (original, un-normalized) that matches [query] after Arabic
     * normalization, so the range can be used directly for a highlight span.
     *
     * Handles tashkeel in the original text: the returned range covers the original characters
     * (including any stripped tashkeel) so the span aligns with what the user sees.
     * Returns null if query is empty or there is no match.
     */
    fun findHighlightRange(text: String, query: String): IntRange? {
        if (query.isEmpty()) return null
        val normalizedQuery = normalizer.normalize(query)

        // Build a parallel list: normalizedText[i] came from original index origPositions[i].
        // Tashkeel chars are dropped by normalize(), so they have no entry here.
        val origPositions = mutableListOf<Int>()
        val normalizedText = StringBuilder()
        for ((i, ch) in text.withIndex()) {
            val normCh = normalizer.normalize(ch.toString())
            if (normCh.isNotEmpty()) {
                normalizedText.append(normCh)
                origPositions.add(i)
            }
        }

        val matchStart = normalizedText.indexOf(normalizedQuery)
        if (matchStart < 0) return null
        val matchEnd = matchStart + normalizedQuery.length

        val origStart = origPositions[matchStart]
        // If matchEnd points past the last normalized char, the match runs to the end of the
        // original string (the remaining chars were all tashkeel after the last kept char).
        val origEnd = if (matchEnd < origPositions.size) origPositions[matchEnd] else text.length
        return origStart until origEnd
    }
}
