package com.folioreader.model.media_overlay

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * @author gautam chibde on 13/6/17.
 */
class OverlayItems : Parcelable {
    var id: String? = null
        private set
    var tag: String? = null
        private set
    private var classType: String? = null
    var spineHref: String? = null
    var text: String? = null

    constructor() {}
    constructor(id: String?, tag: String?) {
        this.id = id
        this.tag = tag
    }

    constructor(id: String?, tag: String?, spineHref: String?) {
        this.id = id
        this.tag = tag
        this.spineHref = spineHref
    }

    constructor(id: String?, tag: String?, spineHref: String?, text: String?) {
        this.id = id
        this.tag = tag
        this.spineHref = spineHref
        this.text = text
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        tag = `in`.readString()
        classType = `in`.readString()
        spineHref = `in`.readString()
        text = `in`.readString()
    }

    override fun toString(): String {
        return "OverlayItems{" +
                "id='" + id + '\'' +
                ", tag='" + tag + '\'' +
                ", classType='" + classType + '\'' +
                ", spineHref='" + spineHref + '\'' +
                ", text='" + text + '\'' +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(tag)
        dest.writeString(classType)
        dest.writeString(spineHref)
        dest.writeString(text)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<OverlayItems> = object : Creator<OverlayItems> {
            override fun createFromParcel(`in`: Parcel): OverlayItems {
                return OverlayItems(`in`)
            }

            override fun newArray(size: Int): Array<OverlayItems> {
                return arrayOf()
            }
        }
    }
}