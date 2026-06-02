package com.shamela.library.domain.util

import com.shamela.library.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for BooksGroupingUtil.groupByFirstChar.
 *
 * All tests here are REGRESSION tests — they pin the grouping contract that the
 * incremental-loading refactor must preserve. Ordering within each group is
 * intentionally NOT asserted because the refactor may change sort timing
 * (global sort vs per-batch sort).
 */
class BooksGroupingTest {

    private fun book(id: String, title: String) =
        Book(id, title, "مؤلف", 100, "قسم")

    @Test
    fun `empty list produces empty map`() {
        val result = BooksGroupingUtil.groupByFirstChar(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single book appears in bucket matching its first character`() {
        val books = listOf(book("1", "فقه الإسلام"))
        val result = BooksGroupingUtil.groupByFirstChar(books)

        assertEquals(setOf('ف'), result.keys)
        assertEquals(1, result['ف']?.size)
        assertEquals("فقه الإسلام", result['ف']?.first()?.title)
    }

    @Test
    fun `books with same first character land in the same bucket`() {
        val books = listOf(
            book("1", "البخاري"),
            book("2", "الترمذي"),
            book("3", "صحيح مسلم"),
        )
        val result = BooksGroupingUtil.groupByFirstChar(books)

        // البخاري and الترمذي both start with ا (alef)
        assertEquals(2, result.keys.size)
        val alefBucket = result['ا']
        assertNotNull(alefBucket)
        assertEquals(2, alefBucket!!.size)
        assertTrue(alefBucket.any { it.title == "البخاري" })
        assertTrue(alefBucket.any { it.title == "الترمذي" })
    }

    @Test
    fun `each distinct first character produces its own bucket`() {
        val books = listOf(
            book("1", "صحيح البخاري"),   // ص
            book("2", "فتح الباري"),      // ف
            book("3", "كتاب الزهد"),      // ك
        )
        val result = BooksGroupingUtil.groupByFirstChar(books)

        assertEquals(setOf('ص', 'ف', 'ك'), result.keys)
        result.values.forEach { assertEquals(1, it.size) }
    }

    @Test
    fun `book with empty title goes into the dash bucket`() {
        val books = listOf(book("1", ""))
        val result = BooksGroupingUtil.groupByFirstChar(books)

        assertEquals(setOf('-'), result.keys)
        assertEquals(1, result['-']?.size)
    }

    @Test
    fun `all books are present across all buckets`() {
        val books = (1..10).map { i -> book("$i", "كتاب $i") }
        val result = BooksGroupingUtil.groupByFirstChar(books)

        val allBooksInMap = result.values.flatten()
        assertEquals(10, allBooksInMap.size)
        books.forEach { original ->
            assertTrue(allBooksInMap.any { it.id == original.id })
        }
    }

    @Test
    fun `bucket key matches first character of title exactly`() {
        val title = "مجموع الفتاوى"
        val books = listOf(book("1", title))
        val result = BooksGroupingUtil.groupByFirstChar(books)

        val expectedKey = title.first() // 'م'
        assertTrue(result.containsKey(expectedKey))
    }
}
