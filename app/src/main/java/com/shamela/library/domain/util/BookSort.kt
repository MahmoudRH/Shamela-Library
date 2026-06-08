package com.shamela.library.domain.util

import com.shamela.library.domain.model.Book
import java.text.Collator
import java.util.Locale

/**
 * The criteria a user can sort a list of books by.
 *
 * [defaultAscending] is the natural direction applied when the option is first picked:
 * names/authors read أ→ي and fewest pages first, while [DOWNLOAD_TIME] defaults to
 * newest-first which is what users usually expect from a "recently added" sort.
 */
enum class BookSortOption(val label: String, val defaultAscending: Boolean) {
    NAME(label = "اسم الكتاب", defaultAscending = true),
    AUTHOR(label = "اسم المؤلف", defaultAscending = true),
    PAGE_COUNT(label = "عدد الصفحات", defaultAscending = true),
    DOWNLOAD_TIME(label = "تاريخ التحميل", defaultAscending = false),
}

object BookSorter {

    /**
     * Pure, side-effect-free sort. [downloadTimes] maps a book id to its file's last-modified
     * timestamp (the download time); ids absent from the map are treated as 0 (oldest). The
     * caller is responsible for computing those timestamps off the main thread.
     */
    fun sortBooks(
        books: List<Book>,
        option: BookSortOption,
        ascending: Boolean,
        downloadTimes: Map<String, Long> = emptyMap(),
    ): List<Book> {
        // Built once per call (Collator construction is relatively expensive). Arabic locale +
        // PRIMARY strength gives correct alphabetical order while ignoring tashkeel/hamza variants.
        val collator = Collator.getInstance(Locale("ar")).apply { strength = Collator.PRIMARY }
        val byTitle = Comparator<Book> { a, b -> collator.compare(a.title, b.title) }

        val comparator: Comparator<Book> = when (option) {
            BookSortOption.NAME -> byTitle
            BookSortOption.AUTHOR ->
                Comparator<Book> { a, b -> collator.compare(a.author, b.author) }.then(byTitle)
            BookSortOption.PAGE_COUNT ->
                compareBy<Book> { it.pageCount }.then(byTitle)
            BookSortOption.DOWNLOAD_TIME ->
                compareBy<Book> { downloadTimes[it.id] ?: 0L }.then(byTitle)
        }

        val sorted = books.sortedWith(comparator)
        return if (ascending) sorted else sorted.reversed()
    }
}