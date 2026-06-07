package com.shamela.library.domain.model

/**
 * Extra "about the book" metadata extracted from the EPUB's info.xhtml at build time and shipped in
 * assets/book-details/<category>.json. Resolved on demand by (categoryName, title); not persisted in
 * Room and not part of the list/search hot path.
 */
data class BookDetails(
    val authorDeathYear: Int?,
    val about: List<BookInfoItem>,
    /** Reader-facing "what is this book about" نبذة, sourced from Wikipedia for famous books (null otherwise). */
    val description: String? = null,
    val descriptionSource: String? = null,
    val descriptionUrl: String? = null,
)

data class BookInfoItem(
    val label: String,
    val value: String,
)
