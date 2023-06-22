package com.folioreader.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.model.HighlightImpl
import com.folioreader.model.event.UpdateHighlightEvent
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.fragment.HighlightFragment
import com.folioreader.ui.fragment.TableOfContentFragment
import com.folioreader.ui.screens.highlights.HighlightsScreen
import com.folioreader.ui.screens.tableOfContent.TableOfContentScreen
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import com.folioreader.util.UiUtil
import com.shamela.apptheme.common.DefaultTopBar
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import org.greenrobot.eventbus.EventBus
import org.readium.r2.shared.Publication

/*
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
}*/

class ContentHighlightActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val publication = intent.getSerializableExtra(Constants.PUBLICATION) as? Publication
        val bookId = intent.getStringExtra(FolioReader.EXTRA_BOOK_ID)
        val bookTitle = intent.getStringExtra(Constants.BOOK_TITLE)

        setContent {

            AppTheme.ShamelaLibraryTheme {
                val selectedViewType = rememberSaveable { mutableStateOf(ViewType.TableOfContents) }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            DefaultTopBar(title = bookTitle ?: "الشاملة") {
                                finish()
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ViewTypeSection(
                                modifier = Modifier,
                                selectedViewType = selectedViewType.value
                            ) { viewType -> selectedViewType.value = viewType }

                            when (selectedViewType.value) {
                                ViewType.TableOfContents -> {
                                    TableOfContentScreen()
                                }

                                ViewType.Highlights -> {
                                    bookId?.let {
                                        HighlightsScreen(
                                            bookId = bookId,
                                            onItemClick = ::onHighlightItemClicked,
                                            onDeleteClicked = ::deleteHighlightedItem
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun onHighlightItemClicked(item: HighlightImpl) {
        val intent = Intent()
        intent.putExtra(HIGHLIGHT_ITEM, item)
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
    private fun deleteHighlightedItem(itemId: Int){
        if (HighLightTable.deleteHighlight(itemId)) {
            EventBus.getDefault().post(UpdateHighlightEvent())
        }
    }

    companion object{
        private const val HIGHLIGHT_ITEM = "highlight_item"
    }
}



enum class ViewType(val label: String) {
    TableOfContents(label = "أقسام الكتاب"),
    Highlights(label = "التظليلات")
}

@Composable
fun ViewTypeSection(modifier: Modifier, selectedViewType: ViewType, onClick: (ViewType) -> Unit) {
    Row(
        modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 16.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            .height(IntrinsicSize.Min)
    ) {
        ViewType.values().forEach {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selectedViewType == it) MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.4f
                        ) else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .clickable { onClick(it) }
                    .padding(vertical = 12.dp),
                text = it.label,
                style = AppFonts.textNormalBold,
                textAlign = TextAlign.Center
            )
            if (it != ViewType.values().last()) {
                Box(
                    Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
            }
        }
    }
}
