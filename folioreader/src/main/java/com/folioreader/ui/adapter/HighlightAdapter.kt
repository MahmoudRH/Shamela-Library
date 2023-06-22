package com.folioreader.ui.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.HighlightImpl
import com.folioreader.ui.adapter.HighlightAdapter.HighlightHolder
import com.folioreader.ui.view.UnderlinedTextView
import com.folioreader.util.AppUtil.Companion.formatDate
import com.folioreader.util.UiUtil

/**
 * @author gautam chibde on 16/6/17.
 */
class HighlightAdapter(
    private val context: Context?,
    private val highlights: MutableList<HighlightImpl>,
    private val callback: HighLightAdapterCallback,
    private val config: Config?
) : RecyclerView.Adapter<HighlightHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightHolder {
        return HighlightHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_highlight, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HighlightHolder, position: Int) {
        holder.container.postDelayed({
            (context as AppCompatActivity?)!!.runOnUiThread {
                holder.container.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }, 10)
        holder.content.text = Html.fromHtml(getItem(position).content)
//        UiUtil.setBackColorToTextView(
//            holder.content,
//            getItem(position).type
//        )
        holder.date.text = getItem(position).date?.let { formatDate(it) }
        holder.container.setOnClickListener { callback.onItemClick(getItem(position)) }
        holder.delete.setOnClickListener {
            callback.deleteHighlight(getItem(position).id)
            highlights.removeAt(position)
            notifyDataSetChanged()
        }
        holder.editNote.setOnClickListener { callback.editNote(getItem(position), position) }
        if (getItem(position).note.isEmpty()) {
            holder.note.visibility = View.GONE
        } else {
            holder.note.visibility = View.VISIBLE
            holder.note.text = getItem(position).note
        }
        holder.container.postDelayed({
            val height = holder.container.height
            (context as AppCompatActivity?)!!.runOnUiThread {
                val params = holder.swipeLinearLayout.layoutParams
                params.height = height
                holder.swipeLinearLayout.layoutParams = params
            }
        }, 30)
        if (config!!.isNightMode) {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.black
                )
            )
            holder.note.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            holder.date.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            holder.content.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        } else {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(
                    context!!,
                    R.color.white
                )
            )
            holder.note.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            holder.date.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            holder.content.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
        }
    }

    private fun getItem(position: Int): HighlightImpl {
        return highlights[position]
    }

    override fun getItemCount(): Int {
        return highlights.size
    }

    fun editNote(note: String, position: Int) {
        highlights[position].note = note
        notifyDataSetChanged()
    }

    class HighlightHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: UnderlinedTextView
        val delete: ImageView
        val editNote: ImageView
        val date: TextView
        val container: RelativeLayout
        val note: TextView
        val swipeLinearLayout: LinearLayout

        init {
            container = itemView.findViewById<View>(R.id.container) as RelativeLayout
            swipeLinearLayout =
                itemView.findViewById<View>(R.id.swipe_linear_layout) as LinearLayout
            content = itemView.findViewById<View>(R.id.utv_highlight_content) as UnderlinedTextView
            delete = itemView.findViewById<View>(R.id.iv_delete) as ImageView
            editNote = itemView.findViewById<View>(R.id.iv_edit_note) as ImageView
            date = itemView.findViewById<View>(R.id.tv_highlight_date) as TextView
            note = itemView.findViewById<View>(R.id.tv_note) as TextView
        }
    }

    interface HighLightAdapterCallback {
        fun onItemClick(highlightImpl: HighlightImpl)
        fun deleteHighlight(id: Int)
        fun editNote(highlightImpl: HighlightImpl, position: Int)
    }
}