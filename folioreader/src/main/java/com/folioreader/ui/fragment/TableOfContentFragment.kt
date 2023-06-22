package com.folioreader.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.R
import com.folioreader.model.TOCLinkWrapper
import com.folioreader.ui.adapter.TOCAdapter
import com.folioreader.ui.adapter.TOCAdapter.TOCCallback
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import org.readium.r2.shared.Link
import org.readium.r2.shared.Publication

class TableOfContentFragment : Fragment(), TOCCallback {
    private var mTOCAdapter: TOCAdapter? = null
    private var mTableOfContentsRecyclerView: RecyclerView? = null
    private var errorView: TextView? = null
    private var mConfig: Config? = null
    private var mBookTitle: String? = null
    private var publication: Publication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publication = requireArguments().getSerializable(Constants.PUBLICATION) as Publication?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val mRootView = inflater.inflate(R.layout.fragment_contents, container, false)
        mConfig = getSavedConfig(activity)
        mBookTitle = requireArguments().getString(Constants.BOOK_TITLE)
        if (mConfig!!.isNightMode) {
            mRootView.findViewById<View>(R.id.recycler_view_menu).setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.black
                )
            )
        }
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTableOfContentsRecyclerView =
            view.findViewById<View>(R.id.recycler_view_menu) as RecyclerView
        errorView = view.findViewById<View>(R.id.tv_error) as TextView
        configRecyclerViews()
        initAdapter()
    }

    fun configRecyclerViews() {
        mTableOfContentsRecyclerView!!.setHasFixedSize(true)
        mTableOfContentsRecyclerView!!.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mTableOfContentsRecyclerView!!.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun initAdapter() {
        if (publication != null) {
            if (publication!!.tableOfContents.isNotEmpty()) {
                val tocLinkWrappers = ArrayList<TOCLinkWrapper>()
                for (tocLink in publication!!.tableOfContents) {
                    val tocLinkWrapper = createTocLinkWrapper(tocLink, 0)
                    tocLinkWrappers.add(tocLinkWrapper)
                }
                onLoadTOC(tocLinkWrappers)
            } else {
                onLoadTOC(createTOCFromSpine(publication!!.readingOrder))
            }
        } else {
            onError()
        }
    }

    fun onLoadTOC(tocLinkWrapperList: ArrayList<TOCLinkWrapper>) {
        mTOCAdapter = TOCAdapter(
            activity, tocLinkWrapperList,
            requireArguments().getString(Constants.SELECTED_CHAPTER_POSITION), mConfig
        )
        mTOCAdapter!!.setCallback(this)
        mTableOfContentsRecyclerView!!.adapter = mTOCAdapter
    }

    fun onError() {
        errorView!!.visibility = View.VISIBLE
        mTableOfContentsRecyclerView!!.visibility = View.GONE
        errorView!!.text = "Table of content \n not found"
    }

    override fun onTocClicked(position: Int) {
        val tocLinkWrapper = mTOCAdapter!!.getItemAt(position) as TOCLinkWrapper
        val intent = Intent()
        intent.putExtra(Constants.SELECTED_CHAPTER_POSITION, tocLinkWrapper.tocLink.href)
        intent.putExtra(Constants.BOOK_TITLE, tocLinkWrapper.tocLink.title)
        intent.putExtra(Constants.TYPE, Constants.CHAPTER_SELECTED)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun onExpanded(position: Int) {
        val tocLinkWrapper = mTOCAdapter!!.getItemAt(position) as TOCLinkWrapper
        if (tocLinkWrapper.children.isNotEmpty()) {
            mTOCAdapter!!.toggleGroup(position)
        }
    }

    companion object {
        fun newInstance(
            publication: Publication?,
            selectedChapterHref: String?, bookTitle: String?
        ): TableOfContentFragment {
            val tableOfContentFragment = TableOfContentFragment()
            val args = Bundle()
            args.putSerializable(Constants.PUBLICATION, publication)
            args.putString(Constants.SELECTED_CHAPTER_POSITION, selectedChapterHref)
            args.putString(Constants.BOOK_TITLE, bookTitle)
            tableOfContentFragment.arguments = args
            return tableOfContentFragment
        }

        /**
         * [RECURSIVE]
         *
         *
         * function generates list of [TOCLinkWrapper] of TOC list from publication manifest
         *
         * @param tocLink     table of content elements
         * @param indentation level of hierarchy of the child elements
         * @return generated [TOCLinkWrapper] list
         */
        private fun createTocLinkWrapper(tocLink: Link, indentation: Int): TOCLinkWrapper {
            val tocLinkWrapper = TOCLinkWrapper(tocLink, indentation)
            for (tocLink1 in tocLink.children) {
                val tocLinkWrapper1 = createTocLinkWrapper(tocLink1, indentation + 1)
                if (tocLinkWrapper1.indentation != 3) {
                    tocLinkWrapper.addChild(tocLinkWrapper1)
                }
            }
            return tocLinkWrapper
        }

        private fun createTOCFromSpine(spine: List<Link>): ArrayList<TOCLinkWrapper> {
            val tocLinkWrappers = ArrayList<TOCLinkWrapper>()
            for (link in spine) {
                val tocLink = Link()
                tocLink.title = link.title
                tocLink.href = link.href
                tocLinkWrappers.add(TOCLinkWrapper(tocLink, 0))
            }
            return tocLinkWrappers
        }
    }
}