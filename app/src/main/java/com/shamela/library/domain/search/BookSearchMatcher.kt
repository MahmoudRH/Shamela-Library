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
     * Returns a relevance score for sorting search results.
     * Higher is more relevant. Callers should sort descending.
     *
     *   4 – exact title match
     *   3 – title starts with query
     *   2 – query appears anywhere in title
     *   1 – query appears only in author
     *   0 – no match (caller should have already filtered with matches())
     */
    fun relevanceScore(book: Book, query: String): Int {
        if (query.isEmpty()) return 0
        val normQuery = normalizer.normalize(query)
        val normTitle = normalizer.normalize(book.title)
        val normAuthor = normalizer.normalize(book.author)
        return when {
            normTitle == normQuery -> 4
            normTitle.startsWith(normQuery) -> 3
            normTitle.contains(normQuery) -> 2
            normAuthor.contains(normQuery) -> 1
            else -> 0
        }
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
