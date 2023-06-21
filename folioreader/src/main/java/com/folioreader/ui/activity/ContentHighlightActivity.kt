package com.folioreader.ui.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.ui.fragment.HighlightFragment
import com.folioreader.ui.fragment.TableOfContentFragment
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import com.folioreader.util.UiUtil
import org.readium.r2.shared.Publication

class ContentHighlightActivity : AppCompatActivity() {
    private var mIsNightMode = false
    private var mConfig: Config? = null
    private var publication: Publication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_highlight)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        publication = intent.getSerializableExtra(Constants.PUBLICATION) as Publication?
        mConfig = getSavedConfig(this)
        mIsNightMode = mConfig != null && mConfig!!.isNightMode
        initViews()
    }

    private fun initViews() {
        mConfig?.let {
            UiUtil.setColorIntToDrawable(
                it.themeColor,
                (findViewById<View>(R.id.btn_close) as ImageView).drawable
            )
        }
        findViewById<View>(R.id.layout_content_highlights).setBackgroundDrawable(
            mConfig?.let {
                UiUtil.getShapeDrawable(
                    it.themeColor
                )
            }
        )
        if (mIsNightMode) {
            findViewById<View>(R.id.toolbar).setBackgroundColor(Color.BLACK)
            findViewById<View>(R.id.btn_contents).setBackgroundDrawable(
                mConfig?.let {
                    UiUtil.createStateDrawable(
                        it.themeColor,
                        ContextCompat.getColor(this, R.color.black)
                    )
                }
            )
            findViewById<View>(R.id.btn_highlights).setBackgroundDrawable(
                mConfig?.let {
                    UiUtil.createStateDrawable(
                        it.themeColor,
                        ContextCompat.getColor(this, R.color.black)
                    )
                }
            )
            (findViewById<View>(R.id.btn_contents) as TextView).setTextColor(
                mConfig?.let {
                    UiUtil.getColorList(
                        ContextCompat.getColor(this, R.color.black),
                        it.themeColor
                    )
                }
            )
            (findViewById<View>(R.id.btn_highlights) as TextView).setTextColor(
                mConfig?.let {
                    UiUtil.getColorList(
                        ContextCompat.getColor(this, R.color.black),
                        it.themeColor
                    )
                }
            )
        } else {
            (findViewById<View>(R.id.btn_contents) as TextView).setTextColor(
                mConfig?.let {
                    UiUtil.getColorList(
                        ContextCompat.getColor(this, R.color.white),
                        it.themeColor
                    )
                }
            )
            (findViewById<View>(R.id.btn_highlights) as TextView).setTextColor(
                mConfig?.let {
                    UiUtil.getColorList(
                        ContextCompat.getColor(this, R.color.white),
                        it.themeColor
                    )
                }
            )
            findViewById<View>(R.id.btn_contents).setBackgroundDrawable(
                mConfig?.let {
                    UiUtil.createStateDrawable(
                        it.themeColor,
                        ContextCompat.getColor(this, R.color.white)
                    )
                }
            )
            findViewById<View>(R.id.btn_highlights).setBackgroundDrawable(
                mConfig?.let {
                    UiUtil.createStateDrawable(
                        it.themeColor,
                        ContextCompat.getColor(this, R.color.white)
                    )
                }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color: Int
            color = if (mIsNightMode) {
                ContextCompat.getColor(this, R.color.black)
            } else {
                val attrs = intArrayOf(android.R.attr.navigationBarColor)
                val typedArray = theme.obtainStyledAttributes(attrs)
                typedArray.getColor(0, ContextCompat.getColor(this, R.color.white))
            }
            window.navigationBarColor = color
        }
        loadContentFragment()
        findViewById<View>(R.id.btn_close).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_contents).setOnClickListener { loadContentFragment() }
        findViewById<View>(R.id.btn_highlights).setOnClickListener { loadHighlightsFragment() }
    }

    private fun loadContentFragment() {
        findViewById<View>(R.id.btn_contents).isSelected = true
        findViewById<View>(R.id.btn_highlights).isSelected = false
        val contentFrameLayout: TableOfContentFragment =
            TableOfContentFragment.Companion.newInstance(
                publication,
                intent.getStringExtra(Constants.CHAPTER_SELECTED),
                intent.getStringExtra(Constants.BOOK_TITLE)
            )
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.parent, contentFrameLayout)
        ft.commit()
    }

    private fun loadHighlightsFragment() {
        findViewById<View>(R.id.btn_contents).isSelected = false
        findViewById<View>(R.id.btn_highlights).isSelected = true
        val bookId = intent.getStringExtra(FolioReader.Companion.EXTRA_BOOK_ID)
        val bookTitle = intent.getStringExtra(Constants.BOOK_TITLE)
        val highlightFragment: HighlightFragment =
            HighlightFragment.Companion.newInstance(bookId, bookTitle)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.parent, highlightFragment)
        ft.commit()
    }
}