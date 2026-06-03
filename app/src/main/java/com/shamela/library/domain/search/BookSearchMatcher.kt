package com.shamela.library.domain.search

import com.shamela.apptheme.data.util.ArabicNormalizer
import com.shamela.library.domain.model.Book

object BookSearchMatcher {
    private val normalizer = ArabicNormalizer()

    fun matches(book: Book, query: String): Boolean {
        if (query.isEmpty()) return true
        return relevanceScore(book, query) > 0
    }

    /**
     * Returns a relevance score for sorting search results.
     * Higher is more relevant. Callers should sort descending.
     *
     *   6 – exact title match
     *   5 – title starts with query (prefix)
     *   4 – query is a contiguous substring of title
     *   3 – all query words appear in title (non-contiguous / different order)
     *   2 – all query words approximately match title words (typo tolerance)
     *   1 – any of the above, but matched author instead of title
     *   0 – no match
     */
    fun relevanceScore(book: Book, query: String): Int {
        if (query.isEmpty()) return 0
        val normQuery = normalizer.normalize(query)
        val normTitle = normalizer.normalize(book.title)
        val normAuthor = normalizer.normalize(book.author)
        return when {
            normTitle == normQuery -> 6
            normTitle.startsWith(normQuery) -> 5
            normTitle.contains(normQuery) -> 4
            allWordsIn(normQuery, normTitle) -> 3
            allWordsFuzzyMatch(normQuery, normTitle) -> 2
            normAuthor.contains(normQuery)
                || allWordsIn(normQuery, normAuthor)
                || allWordsFuzzyMatch(normQuery, normAuthor) -> 1
            else -> 0
        }
    }

    // Returns true when every space-separated token in normQuery appears
    // as a substring somewhere in normText.
    private fun allWordsIn(normQuery: String, normText: String): Boolean =
        normQuery.split(' ').filter { it.isNotEmpty() }.all { normText.contains(it) }

    // Returns true when every query token approximately matches at least one
    // text token within the edit-distance threshold for that token's length.
    private fun allWordsFuzzyMatch(normQuery: String, normText: String): Boolean {
        val queryWords = normQuery.split(' ').filter { it.isNotEmpty() }
        val textWords  = normText.split(' ').filter { it.isNotEmpty() }
        return queryWords.all { qWord ->
            val maxDist = fuzzyThreshold(qWord.length)
            textWords.any { tWord -> levenshtein(qWord, tWord) <= maxDist }
        }
    }

    // 0 edits for very short words (avoids noisy matches), 1 for 3-4 char words, 2 for longer.
    private fun fuzzyThreshold(wordLength: Int): Int = when {
        wordLength <= 2 -> 0
        wordLength <= 4 -> 1
        else -> 2
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length; val n = b.length
        if (m == 0) return n
        if (n == 0) return m
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) for (j in 1..n) {
            dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                       else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
        }
        return dp[m][n]
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
