package com.folioreader.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.model.HighLight.HighLightAction
import com.folioreader.model.HighlightImpl
import com.folioreader.model.event.UpdateHighlightEvent
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.adapter.HighlightAdapter
import com.folioreader.ui.adapter.HighlightAdapter.HighLightAdapterCallback
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import com.folioreader.util.HighlightUtil
import org.greenrobot.eventbus.EventBus

class HighlightFragment : Fragment(), HighLightAdapterCallback {
    private var mRootView: View? = null
    private var adapter: HighlightAdapter? = null
    private var mBookId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mRootView = inflater.inflate(R.layout.fragment_highlight_list, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val highlightsView = mRootView!!.findViewById<View>(R.id.rv_highlights) as RecyclerView
        val config = getSavedConfig(activity)
        mBookId = requireArguments().getString(FolioReader.EXTRA_BOOK_ID)
        if (config!!.isNightMode) {
            mRootView!!.findViewById<View>(R.id.rv_highlights).setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.black
                )
            )
        }
        highlightsView.layoutManager = LinearLayoutManager(activity)
        highlightsView.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
//        adapter = HighlightAdapter(activity, HighLightTable.getAllHighlights(mBookId), this, config)
//        highlightsView.adapter = adapter
    }

    override fun onItemClick(highlightImpl: HighlightImpl) {
        val intent = Intent()
        intent.putExtra(HIGHLIGHT_ITEM, highlightImpl)
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun deleteHighlight(id: Int) {
        if (HighLightTable.deleteHighlight(id)) {
            EventBus.getDefault().post(UpdateHighlightEvent())
        }
    }

    override fun editNote(highlightImpl: HighlightImpl, position: Int) {
        val dialog = Dialog(requireActivity(), R.style.DialogCustomTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_notes)
        dialog.show()
        val noteText = highlightImpl.note
        (dialog.findViewById<View>(R.id.edit_note) as EditText).setText(noteText)
        dialog.findViewById<View>(R.id.btn_save_note).setOnClickListener {
            val note = (dialog.findViewById<View>(R.id.edit_note) as EditText).text.toString()
            if (!TextUtils.isEmpty(note)) {
                highlightImpl.note = note
                if (HighLightTable.updateHighlight(highlightImpl)) {
                    HighlightUtil.sendHighlightBroadcastEvent(
                        this@HighlightFragment.requireActivity().applicationContext,
                        highlightImpl,
                        HighLightAction.MODIFY
                    )
                    adapter!!.editNote(note, position)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.please_enter_note),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val HIGHLIGHT_ITEM = "highlight_item"
        fun newInstance(bookId: String?, epubTitle: String?): HighlightFragment {
            val highlightFragment = HighlightFragment()
            val args = Bundle()
            args.putString(FolioReader.Companion.EXTRA_BOOK_ID, bookId)
            args.putString(Constants.BOOK_TITLE, epubTitle)
            highlightFragment.arguments = args
            return highlightFragment
        }
    }
}