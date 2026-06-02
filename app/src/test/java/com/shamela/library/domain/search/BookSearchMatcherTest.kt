package com.shamela.library.domain.search

import com.shamela.library.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for BookSearchMatcher.
 *
 * Regression tests (marked REGRESSION) must pass before and after every change.
 * Feature tests (marked FEATURE - PHASE 1) will fail until Phase 1 is implemented
 * and should turn green after Arabic normalization + author search are added.
 */
class BookSearchMatcherTest {

    private fun book(title: String, author: String = "مؤلف مجهول") =
        Book("id-$title", title, author, 100, "قسم")

    // ─── REGRESSION: basic title matching ────────────────────────────────────

    @Test // REGRESSION
    fun `exact title match returns true`() {
        val book = book("صحيح البخاري")
        assertTrue(BookSearchMatcher.matches(book, "صحيح البخاري"))
    }

    @Test // REGRESSION
    fun `title substring match returns true`() {
        val book = book("صحيح البخاري")
        assertTrue(BookSearchMatcher.matches(book, "البخاري"))
    }

    @Test // REGRESSION
    fun `query not present in title or author returns false`() {
        val book = book("صحيح البخاري", "محمد البخاري")
        assertFalse(BookSearchMatcher.matches(book, "الترمذي"))
    }

    @Test // REGRESSION
    fun `empty query matches every book`() {
        assertTrue(BookSearchMatcher.matches(book("أي كتاب"), ""))
    }

    // ─── FEATURE (Phase 1): author search ────────────────────────────────────

    @Test // FEATURE - PHASE 1
    fun `exact author name match returns true`() {
        val book = book("عنوان ما", author = "ابن تيمية")
        assertTrue(BookSearchMatcher.matches(book, "ابن تيمية"))
    }

    @Test // FEATURE - PHASE 1
    fun `partial author name match returns true`() {
        val book = book("عنوان ما", author = "ابن تيمية")
        assertTrue(BookSearchMatcher.matches(book, "تيمية"))
    }

    @Test // FEATURE - PHASE 1
    fun `query matching only author but not title returns true`() {
        val book = book("الرسالة", author = "الشافعي")
        assertTrue(BookSearchMatcher.matches(book, "الشافعي"))
    }

    // ─── FEATURE (Phase 1): Arabic normalization ─────────────────────────────

    @Test // FEATURE - PHASE 1
    fun `alef_hamza_above in query matches plain alef in title`() {
        // أ (U+0623) in query should match ا (U+0627) in title after normalization
        val book = book("الإمام") // starts with ا
        assertTrue(BookSearchMatcher.matches(book, "أمام")) // query uses أ
    }

    @Test // FEATURE - PHASE 1
    fun `alef_hamza_below in query matches plain alef in title`() {
        // إ (U+0625) in query should match ا (U+0627) in title after normalization
        val book = book("الاجتهاد")
        assertTrue(BookSearchMatcher.matches(book, "إجتهاد"))
    }

    @Test // FEATURE - PHASE 1
    fun `alef_madda in title matches plain alef in query`() {
        // آ (U+0622) in title normalizes to ا, so a plain ا query finds it
        val book = book("آداب النفس")
        assertTrue(BookSearchMatcher.matches(book, "اداب النفس"))
    }

    @Test // FEATURE - PHASE 1
    fun `tashkeel in query is stripped before matching`() {
        val book = book("صحيح البخاري")
        // Query has fatha/kasra diacritics that the stored title lacks
        assertTrue(BookSearchMatcher.matches(book, "صَحِيح"))
    }

    @Test // FEATURE - PHASE 1
    fun `tashkeel in title is stripped before matching`() {
        val book = book("صَحِيحُ البُخَارِيّ") // title itself has tashkeel
        assertTrue(BookSearchMatcher.matches(book, "صحيح البخاري"))
    }

    @Test // FEATURE - PHASE 1
    fun `teh_marbuta in title matches heh in query`() {
        // ة (U+0629) normalizes to ه (U+0647)
        val book = book("قصة النبي") // title uses ة
        assertTrue(BookSearchMatcher.matches(book, "قصه النبي")) // query uses ه
    }

    @Test // FEATURE - PHASE 1
    fun `teh_marbuta in query matches teh_marbuta in title`() {
        // both sides normalize ة→ه, so they still match each other
        val book = book("الرسالة")
        assertTrue(BookSearchMatcher.matches(book, "الرسالة"))
    }

    @Test // FEATURE - PHASE 1
    fun `normalization applied to author field too`() {
        // Author has إ, query uses ا — should still match after normalization
        val book = book("عنوان", author = "إبن تيمية")
        assertTrue(BookSearchMatcher.matches(book, "ابن تيمية"))
    }

    // ─── findHighlightRange ───────────────────────────────────────────────────

    // ─── relevanceScore ───────────────────────────────────────────────────────

    @Test
    fun `exact title match scores 4`() {
        assertEquals(4, BookSearchMatcher.relevanceScore(book("صحيح البخاري"), "صحيح البخاري"))
    }

    @Test
    fun `title starts-with scores 3`() {
        assertEquals(3, BookSearchMatcher.relevanceScore(book("صحيح البخاري"), "صحيح"))
    }

    @Test
    fun `title substring (not prefix) scores 2`() {
        assertEquals(2, BookSearchMatcher.relevanceScore(book("صحيح البخاري"), "البخاري"))
    }

    @Test
    fun `author-only match scores 1`() {
        val book = book("الرسالة", author = "الشافعي")
        assertEquals(1, BookSearchMatcher.relevanceScore(book, "الشافعي"))
    }

    @Test
    fun `no match scores 0`() {
        assertEquals(0, BookSearchMatcher.relevanceScore(book("صحيح البخاري"), "الترمذي"))
    }

    @Test
    fun `exact title ranks above prefix which ranks above substring`() {
        val exact    = book("الفقه")
        val prefix   = book("الفقه الإسلامي")
        val contains = book("أسس الفقه")
        val query = "الفقه"
        assertTrue(
            BookSearchMatcher.relevanceScore(exact, query) >
            BookSearchMatcher.relevanceScore(prefix, query)
        )
        assertTrue(
            BookSearchMatcher.relevanceScore(prefix, query) >
            BookSearchMatcher.relevanceScore(contains, query)
        )
    }

    @Test
    fun `normalization applied in scoring`() {
        // query أساس (hamza above) should score the same against title اساس (plain alef)
        val book = book("اساس البلاغة")
        assertEquals(3, BookSearchMatcher.relevanceScore(book, "أساس"))
    }

    // ─── findHighlightRange ───────────────────────────────────────────────────

    @Test
    fun `findHighlightRange returns null for empty query`() {
        assertNull(BookSearchMatcher.findHighlightRange("صحيح البخاري", ""))
    }

    @Test
    fun `findHighlightRange returns null when query not found`() {
        assertNull(BookSearchMatcher.findHighlightRange("صحيح البخاري", "الترمذي"))
    }

    @Test
    fun `findHighlightRange returns range covering exact substring`() {
        val text = "صحيح البخاري"
        val range = BookSearchMatcher.findHighlightRange(text, "البخاري")
        assertNotNull(range)
        assertEquals("البخاري", text.substring(range!!))
    }

    @Test
    fun `findHighlightRange returns range in original text for normalized query`() {
        // query إجتهاد (hamza below) matches اجتهاد in الاجتهاد after normalization
        val text = "الاجتهاد"
        val range = BookSearchMatcher.findHighlightRange(text, "إجتهاد")
        assertNotNull(range)
        assertEquals("اجتهاد", text.substring(range!!))
    }

    @Test
    fun `findHighlightRange range covers tashkeel in original title`() {
        // Stored title has tashkeel; normalized query has none; highlight should span the
        // original characters including the stripped tashkeel so the span aligns visually.
        val text = "صَحِيح"
        val range = BookSearchMatcher.findHighlightRange(text, "صحيح")
        assertNotNull(range)
        assertEquals("صَحِيح", text.substring(range!!))
    }

    @Test
    fun `findHighlightRange works on author field for normalized query`() {
        // Author has إ, query uses ا
        val author = "إبن تيمية"
        val range = BookSearchMatcher.findHighlightRange(author, "ابن تيمية")
        assertNotNull(range)
        assertEquals("إبن تيمية", author.substring(range!!))
    }
}
