package com.shamela.library.domain.model

/**
 * Extra "about the book" metadata extracted from the EPUB's info.xhtml at build time and shipped in
 * assets/book-details/<category>.json. Resolved on demand by (categoryName, title); not persisted in
 * Room and not part of the list/search hot path.
 */
data class BookDetails(
    val authorDeathYear: Int?,
    val about: List<BookInfoItem>,
    /**
     * Reader-facing "what is this book about" نبذة. `descriptionSource` is set for web-sourced ones
     * (e.g. "ويكيبيديا"); a description with a null source is AI-generated (shown as such in the UI).
     */
    val description: String? = null,
    val descriptionSource: String? = null,
    val descriptionUrl: String? = null,
    /** Which AI model produced this description (null for web-sourced or older untagged entries).
     *  Surfaced only in debug builds for QA — never shown in release. */
    val descriptionModel: String? = null,
    /** Chapter titles from the book's table of contents (الموضوعات), when meaningful. */
    val topics: List<String> = emptyList(),
)

data class BookInfoItem(
    val label: String,
    val value: String,
)
