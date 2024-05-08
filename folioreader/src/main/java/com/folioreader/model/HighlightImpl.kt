package com.folioreader.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.util.Date

/**
 * This data structure holds information about an individual highlight.
 *
 * @author mahavir on 5/12/16.
 */
class HighlightImpl : Parcelable, HighLight {
    /**
     * Database id
     */
    var id = 0

    /**
     *
     *  Book id, which can be provided to intent to folio reader, if not provided id is
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
    override var bookId: String = ""

    /**
     * Highlighted text content text content.
     */
    override var content: String = ""

    /**
     * Date time when highlight is created (format:- MMM dd, yyyy | HH:mm).
     */
    override var date: Date? = null

    /**
     * Field defines the color of the highlight. [HighlightStyle]
     */
    override var type: String = ""

    /**
     * Page index in the book taken from Epub spine reference.
     */
    override var pageNumber = 0

    /**
     * href of the page from the Epub spine list.
     */
    override var pageId: String = ""

    /**
     *
     *  Contains highlight meta data in terms of rangy format.
     * **format **:- start$end$id$class$containerId.
     *
     * for reference, look here: [rangy](https://github.com/timdown/rangy).
     */
    override var rangy: String = ""

    /**
     * Unique identifier for a highlight for sync across devices.
     *
     * for reference, look here:
     * [UUID](https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html#toString()).
     */
    override var uUID: String = ""

    /**
     * Note linked to the highlight (optional)
     */
    override var note: String = ""

    enum class HighlightStyle {
        Yellow, Green, Blue, Pink, Underline, TextColor, DottetUnderline, Normal;

        companion object {
            /**
             * Return CSS class for HighlightStyle.
             */
            fun classForStyle(style: HighlightStyle?): String {
                return when (style) {
                    Yellow -> "highlight_yellow"
                    Green -> "highlight_green"
                    Blue -> "highlight_blue"
                    Pink -> "highlight_pink"
                    Underline -> "highlight_underline"
                    DottetUnderline -> "mediaOverlayStyle1"
                    TextColor -> "mediaOverlayStyle2"
                    else -> "mediaOverlayStyle0"
                }
            }
        }
    }

    constructor(
        id: Int, bookId: String, content: String, date: Date?, type: String,
        pageNumber: Int, pageId: String,
        rangy: String, note: String, uuid: String
    ) {
        this.id = id
        this.bookId = bookId
        this.content = content
        this.date = date
        this.type = type
        this.pageNumber = pageNumber
        this.pageId = pageId
        this.rangy = rangy
        this.note = note
        uUID = uuid
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val highlightImpl = o as HighlightImpl
        return (id == highlightImpl.id
                && bookId == highlightImpl.bookId)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + bookId.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + if (date != null) date.hashCode() else 0
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String {
        return "HighlightImpl{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", pageNumber=" + pageNumber +
                ", pageId='" + pageId + '\'' +
                ", rangy='" + rangy + '\'' +
                ", note='" + note + '\'' +
                ", uuid='" + uUID + '\'' +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(bookId)
        dest.writeString(pageId)
        dest.writeString(rangy)
        dest.writeString(content)
        dest.writeSerializable(date)
        dest.writeString(type)
        dest.writeInt(pageNumber)
        dest.writeString(note)
        dest.writeString(uUID)
    }

    private fun readFromParcel(`in`: Parcel) {
        id = `in`.readInt()
        bookId = `in`.readString().toString()
        pageId = `in`.readString().toString()
        rangy = `in`.readString().toString()
        content = `in`.readString().toString()
        date = `in`.readSerializable() as Date?
        type = `in`.readString().toString()
        pageNumber = `in`.readInt()
        note = `in`.readString().toString()
        uUID = `in`.readString().toString()
    }

    companion object {
        val INTENT = HighlightImpl::class.java.name
        const val BROADCAST_EVENT = "highlight_broadcast_event"
        @JvmField
        val CREATOR: Creator<HighlightImpl> = object : Creator<HighlightImpl> {
            override fun createFromParcel(`in`: Parcel): HighlightImpl {
                return HighlightImpl(`in`)
            }

            override fun newArray(size: Int): Array<HighlightImpl> {
                return arrayOf()
            }
        }
    }
}