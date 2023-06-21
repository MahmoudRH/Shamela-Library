package com.folioreader.model

import com.folioreader.util.MultiLevelExpIndListAdapter.ExpIndData
import org.readium.r2.shared.Link

/**
 * Created by Mahavir on 3/10/17.
 */
class TOCLinkWrapper(var tocLink: Link, var indentation: Int) : ExpIndData {
    var tocLinkWrappers: ArrayList<TOCLinkWrapper>
    override var isGroup: Boolean = false
    private var mGroupSize = 0

    init {
        tocLinkWrappers = ArrayList()
        isGroup = tocLink.children.size > 0
    }

    override fun toString(): String {
        return "TOCLinkWrapper{" +
                "tocLink=" + tocLink +
                ", indentation=" + indentation +
                ", tocLinkWrappers=" + tocLinkWrappers +
                ", mIsGroup=" + isGroup +
                ", mGroupSize=" + mGroupSize +
                '}'
    }

    fun addChild(tocLinkWrapper: TOCLinkWrapper) {
        tocLinkWrappers.add(tocLinkWrapper)
        //tocLinkWrapper.setIndentation(getIndentation() + 1);
    }

    override val children: List<ExpIndData>
        get() = tocLinkWrappers

    override fun setGroupSize(groupSize: Int) {
        mGroupSize = groupSize
    }

    fun getGroupSize(): Int {
        return mGroupSize
    }
}