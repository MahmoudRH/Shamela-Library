package com.folioreader.model

import java.util.Date

/**
 * Interface to access Highlight data.
 *
 * @author gautam chibde on 9/10/17.
 */
interface HighLight {
    /**
     * Highlight action
     */
    enum class HighLightAction {
        NEW, DELETE, MODIFY
    }

    /**
     *
     *  Returns Book id, which can be provided to intent to folio reader, if not provided id is
     * used from epub's dc:identifier field in metadata.
     *
     * for reference, look here:
     * [IDPF](http://www.idpf.org/epub/30/spec/epub30-publications.html#sec-package-metadata-identifiers).
     * in case identifier is not found in the epub,
     * [hash code](https://docs.oracle.com/javase/7/docs/api/java/lang/String.html#hashCode())
     * of book title is used also if book title is not found then
     * hash code of the book file name is used.
     *
     */
    val bookId: String?

    /**
     * Returns Highlighted text content text content.
     */
    val content: String?

    /**
     * Returns Date time when highlight is created (format:- MMM dd, yyyy | HH:mm).
     */
    val date: Date?

    /**
     * Returns Field defines the color of the highlight.
     */
    val type: String?

    /**
     * Returns Page index in the book taken from Epub spine reference.
     */
    val pageNumber: Int

    /**
     * Returns href of the page from the Epub spine list.
     */
    val pageId: String?

    /**
     *
     *  Contains highlight meta data in terms of rangy format.
     * **format **:- start$end$id$class$containerId.
     *
     * for reference, look here: [rangy](https://github.com/timdown/rangy).
     */
    val rangy: String?

    /**
     * returns Unique identifier.
     */
    val uUID: String?

    /**
     * Returns Note linked to the highlight (optional)
     */
    val note: String?
}