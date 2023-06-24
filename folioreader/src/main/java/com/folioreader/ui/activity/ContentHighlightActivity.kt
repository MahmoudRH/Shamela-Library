package com.folioreader.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.model.HighlightImpl
import com.folioreader.model.event.UpdateHighlightEvent
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.composables.HighlightsItem
import com.folioreader.ui.composables.LinkItem
import com.shamela.apptheme.common.DefaultTopBar
import com.shamela.apptheme.common.EmptyListScreen
import com.shamela.apptheme.common.LoadingScreen
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.readium.r2.shared.Link
import org.readium.r2.streamer.parser.EpubParser

class ContentHighlightActivity : ComponentActivity() {
    private lateinit var highlightsItems: SnapshotStateList<HighlightImpl>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookPath = intent.getStringExtra(Constants.EPUB_FILE_PATH)
        val bookId = intent.getStringExtra(FolioReader.EXTRA_BOOK_ID)
        val bookTitle = intent.getStringExtra(Constants.BOOK_TITLE)

        val linkItems = mutableStateListOf<Link>()
        highlightsItems = mutableStateListOf<HighlightImpl>()
        val isLoading = mutableStateOf(true)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                bookPath?.let { filepath ->
                    EpubParser().parse(filepath, "")?.let { pubBox ->
                        val list =
                            pubBox.publication.tableOfContents.ifEmpty { pubBox.publication.readingOrder }
                        linkItems.addAll(list)
                        isLoading.value = false
                    }
                }
            }
            withContext(Dispatchers.IO) {
                val list = HighLightTable.getAllHighlights(bookId = bookId).toList()
                highlightsItems.addAll(list)
            }
        }

        setContent {
            AppTheme.ShamelaLibraryTheme {
                val selectedViewType = rememberSaveable { mutableStateOf(ViewType.TableOfContents) }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            DefaultTopBar(title = bookTitle ?: "الشاملة") { finish() }
                        }
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                ViewTypeSection(
                                    modifier = Modifier,
                                    selectedViewType = selectedViewType.value
                                ) { viewType -> selectedViewType.value = viewType }
                            }

                            when (selectedViewType.value) {
                                ViewType.TableOfContents -> {
                                    items(linkItems) { link ->
                                        LinkItem(link, 0, link == linkItems.first(), ::onTocClicked)
                                    }
                                }

                                ViewType.Highlights -> {
                                    bookId?.let {
                                        items(highlightsItems) { highlightItem ->
                                            HighlightsItem(
                                                item = highlightItem,
                                                onItemClick = ::onHighlightItemClicked,
                                                onDeleteClicked = {
                                                    deleteHighlightedItem(highlightItem.id)
                                                    highlightsItems.remove(highlightItem)
                                                }
                                            )
                                        }
                                        item {
                                            EmptyListScreen(highlightsItems.isEmpty(), "لا توجد أي تظليلات", modifier = Modifier.fillParentMaxSize())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LoadingScreen(isLoading.value)
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

    private fun deleteHighlightedItem(itemId: Int) {
        if (HighLightTable.deleteHighlight(itemId)) {
            EventBus.getDefault().post(UpdateHighlightEvent())
        }
    }

    private fun onTocClicked(title: String?, href: String?) {
        val intent = Intent()
        intent.putExtra(Constants.SELECTED_CHAPTER_POSITION, href)
        intent.putExtra(Constants.BOOK_TITLE, title)
        intent.putExtra(Constants.TYPE, Constants.CHAPTER_SELECTED)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val HIGHLIGHT_ITEM = "highlight_item"
    }
}


private enum class ViewType(val label: String) {
    TableOfContents(label = "أقسام الكتاب"),
    Highlights(label = "التظليلات")
}

@Composable
private fun ViewTypeSection(
    modifier: Modifier,
    selectedViewType: ViewType,
    onClick: (ViewType) -> Unit,
) {
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
