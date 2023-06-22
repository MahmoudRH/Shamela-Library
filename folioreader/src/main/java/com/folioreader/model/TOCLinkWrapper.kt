package com.folioreader.model

import com.folioreader.util.MultiLevelExpIndListAdapter.ExpIndData
import org.readium.r2.shared.Link

/**
 * Created by Mahavir on 3/10/17.
 */
class TOCLinkWrapper(var tocLink: Link, var indentation: Int) : ExpIndData {
    var tocLinkWrappers: ArrayList<TOCLinkWrapper> = ArrayList()
    override var isGroup: Boolean = false
    private var mGroupSize = 0
    init {
        isGroup = tocLink.children.size > 0
    }
    fun addChild(tocLinkWrapper: TOCLinkWrapper) {
        tocLinkWrappers.add(tocLinkWrapper)
    }

    override val children: List<ExpIndData>
        get() = tocLinkWrappers

    override fun setGroupSize(groupSize: Int) {
        mGroupSize = groupSize
    }
}