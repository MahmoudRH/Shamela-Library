package com.folioreader.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.model.HighLight.HighLightAction
import com.folioreader.model.HighlightImpl
import com.folioreader.model.sqlite.HighLightTable
import org.json.JSONException
import org.json.JSONObject
import java.util.Arrays
import java.util.Calendar

/**
 * Created by priyank on 5/12/16.
 */
object HighlightUtil {
    private const val TAG = "HighlightUtil"
    fun createHighlightRangy(
        context: Context,
        content: String,
        bookId: String,
        pageId: String,
        pageNo: Int,
        oldRangy: String
    ): String {
        try {
            val jObject = JSONObject(content)
            val rangy = jObject.getString("rangy")
            val textContent = jObject.getString("content")
            val color = jObject.getString("color")
            val rangyHighlightElement = getRangyString(rangy, oldRangy)
            val highlightImpl = HighlightImpl()
            highlightImpl.content = textContent
            highlightImpl.type = color
            highlightImpl.pageNumber = pageNo
            highlightImpl.bookId = bookId
            highlightImpl.pageId = pageId
            highlightImpl.rangy = rangyHighlightElement
            highlightImpl.date = Calendar.getInstance().time
            // save highlight to database
            val id = HighLightTable.insertHighlight(highlightImpl)
            if (id != -1L) {
                highlightImpl.id = id.toInt()
                sendHighlightBroadcastEvent(context, highlightImpl, HighLightAction.NEW)
            }
            return rangy
        } catch (e: JSONException) {
            Log.e(TAG, "createHighlightRangy failed", e)
        }
        return ""
    }

    /**
     * function extracts rangy element corresponding to latest highlight.
     *
     * @param rangy    new rangy string generated after adding new highlight.
     * @param oldRangy rangy string before new highlight.
     * @return rangy element corresponding to latest element.
     */
    private fun getRangyString(rangy: String, oldRangy: String): String {
        val rangyList = getRangyArray(rangy)
        for (firs in getRangyArray(oldRangy)) {
            if (rangyList.contains(firs)) {
                rangyList.remove(firs)
            }
        }
        return if (rangyList.size >= 1) {
            rangyList[0]
        } else {
            ""
        }
    }

    /**
     * function converts Rangy text into each individual element
     * splitting with '|'.
     *
     * @param rangy rangy test with format: type:textContent|start$end$id$class$containerId
     * @return ArrayList of each rangy element corresponding to each highlight
     */
    private fun getRangyArray(rangy: String): MutableList<String> {
        val rangyElementList: MutableList<String> = ArrayList()
        rangyElementList.addAll(
            Arrays.asList(
                *rangy.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()))
        if (rangyElementList.contains("type:textContent")) {
            rangyElementList.remove("type:textContent")
        } else if (rangyElementList.contains("")) {
            return ArrayList()
        }
        return rangyElementList
    }

    fun generateRangyString(pageId: String): String {
        val rangyList = HighLightTable.getHighlightsForPageId(pageId)
        val builder = StringBuilder()
        if (rangyList.isNotEmpty()) {
            builder.append("type:textContent")
            for (rangy in rangyList) {
                builder.append('|')
                builder.append(rangy)
            }
        }
        return builder.toString()
    }

    fun sendHighlightBroadcastEvent(
        context: Context?,
        highlightImpl: HighlightImpl?,
        action: HighLightAction?
    ) {
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(
            getHighlightBroadcastIntent(highlightImpl, action)
        )
    }

    private fun getHighlightBroadcastIntent(
        highlightImpl: HighlightImpl?,
        modify: HighLightAction?
    ): Intent {
        val bundle = Bundle()
        bundle.putParcelable(HighlightImpl.Companion.INTENT, highlightImpl)
        bundle.putSerializable(HighLightAction::class.java.name, modify)
        return Intent(HighlightImpl.Companion.BROADCAST_EVENT).putExtras(bundle)
    }
}