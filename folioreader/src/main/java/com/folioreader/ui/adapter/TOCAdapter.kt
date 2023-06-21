package com.folioreader.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.TOCLinkWrapper
import com.folioreader.util.MultiLevelExpIndListAdapter

/**
 * Created by mahavir on 3/10/17.
 */
class TOCAdapter(
    private val mContext: Context?,
    tocLinkWrappers: ArrayList<TOCLinkWrapper>,
    private val selectedHref: String?,
    private val mConfig: Config?
) : MultiLevelExpIndListAdapter(tocLinkWrappers) {
    private var callback: TOCCallback? = null
    fun setCallback(callback: TOCCallback?) {
        this.callback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TOCRowViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_table_of_contents, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as TOCRowViewHolder
        val tocLinkWrapper = getItemAt(position) as TOCLinkWrapper
        if (tocLinkWrapper.children.isEmpty()) {
            viewHolder.children.visibility = View.INVISIBLE
        } else {
            viewHolder.children.visibility = View.VISIBLE
        }
        viewHolder.sectionTitle.text = tocLinkWrapper.tocLink.title
        if (mConfig!!.isNightMode) {
            if (tocLinkWrapper.isGroup) {
                viewHolder.children.setImageResource(R.drawable.ic_plus_white_24dp)
            } else {
                viewHolder.children.setImageResource(R.drawable.ic_minus_white_24dp)
            }
        } else {
            if (tocLinkWrapper.isGroup) {
                viewHolder.children.setImageResource(R.drawable.ic_plus_black_24dp)
            } else {
                viewHolder.children.setImageResource(R.drawable.ic_minus_black_24dp)
            }
        }
        val leftPadding =
            getPaddingPixels(mContext, LEVEL_ONE_PADDING_PIXEL) * tocLinkWrapper.indentation
        viewHolder.view.setPadding(leftPadding, 0, 0, 0)

        // set color to each indentation level
        if (tocLinkWrapper.indentation == 0) {
            viewHolder.view.setBackgroundColor(Color.WHITE)
            viewHolder.sectionTitle.setTextColor(Color.BLACK)
        } else if (tocLinkWrapper.indentation == 1) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#f7f7f7"))
            viewHolder.sectionTitle.setTextColor(Color.BLACK)
        } else if (tocLinkWrapper.indentation == 2) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#b3b3b3"))
            viewHolder.sectionTitle.setTextColor(Color.WHITE)
        } else if (tocLinkWrapper.indentation == 3) {
            viewHolder.view.setBackgroundColor(Color.parseColor("#f7f7f7"))
            viewHolder.sectionTitle.setTextColor(Color.BLACK)
        }
        if (tocLinkWrapper.children == null || tocLinkWrapper.children.isEmpty()) {
            viewHolder.children.visibility = View.INVISIBLE
        } else {
            viewHolder.children.visibility = View.VISIBLE
        }
        if (mConfig.isNightMode) {
            viewHolder.container.setBackgroundColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.black
                )
            )
            viewHolder.children.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.black
                )
            )
            viewHolder.sectionTitle.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.white
                )
            )
        } else {
            viewHolder.container.setBackgroundColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.white
                )
            )
            viewHolder.children.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.white
                )
            )
            viewHolder.sectionTitle.setTextColor(
                ContextCompat.getColor(
                    mContext,
                    R.color.black
                )
            )
        }
        if (tocLinkWrapper.tocLink.href == selectedHref) {
            viewHolder.sectionTitle.setTextColor(mConfig.themeColor)
        }
    }

    interface TOCCallback {
        fun onTocClicked(position: Int)
        fun onExpanded(position: Int)
    }

    inner class TOCRowViewHolder internal constructor(val view: View) : RecyclerView.ViewHolder(
        view
    ) {
        var children: ImageView
        var sectionTitle: TextView
        val container: LinearLayout

        init {
            children = itemView.findViewById<View>(R.id.children) as ImageView
            container = itemView.findViewById<View>(R.id.container) as LinearLayout
            children.setOnClickListener {
                if (callback != null) callback!!.onExpanded(
                    adapterPosition
                )
            }
            sectionTitle = itemView.findViewById<View>(R.id.section_title) as TextView
            view.setOnClickListener { if (callback != null) callback!!.onTocClicked(adapterPosition) }
        }
    }

    companion object {
        private const val LEVEL_ONE_PADDING_PIXEL = 15
        private fun getPaddingPixels(context: Context?, dpValue: Int): Int {
            // Get the screen's density scale
            val scale = context!!.resources.displayMetrics.density
            // Convert the dps to pixels, based on density scale
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}