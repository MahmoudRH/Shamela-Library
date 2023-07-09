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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.folioreader.Constants
import com.folioreader.Constants.CHAPTER_SELECTED
import com.folioreader.Constants.SETTINGS_CHANGED
import com.folioreader.ui.composables.LinkItem
import com.shamela.apptheme.presentation.common.DefaultTopBar
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.settings.SettingsScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Link
import org.readium.r2.streamer.parser.EpubParser

class ContentHighlightActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookPath = intent.getStringExtra(Constants.EPUB_FILE_PATH)
        val bookTitle = intent.getStringExtra(Constants.BOOK_TITLE)
        val selectedViewType = when (intent.getStringExtra(SELECTED_VIEW_TYPE)) {
            Settings -> ViewType.Settings
            TableOfContent -> ViewType.TableOfContents
            else -> ViewType.TableOfContents
        }
        val linkItems = mutableStateListOf<Link>()
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
        }

        setContent {
            AppTheme.ShamelaLibraryTheme {
                val currentViewType = rememberSaveable { mutableStateOf(selectedViewType) }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            Column {
                                DefaultTopBar(title = bookTitle ?: "الشاملة") {
                                    finish()
                                }

                                ViewTypeSection(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    selectedViewType = currentViewType.value
                                ) { viewType -> currentViewType.value = viewType }
                            }
                        }
                    ) {
                        when (currentViewType.value) {
                            ViewType.TableOfContents -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(it),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(vertical = 16.dp)
                                ) {
                                    items(linkItems) { link ->
                                        LinkItem(link, 0, link == linkItems.first(), ::onTocClicked)
                                    }
                                }
                            }

                            ViewType.Settings -> {
                                SettingsScreen(
                                    modifier = Modifier.padding(it),
                                    onSettingsChanged = { hash-> onSettingsChanged(hash) })
                            }
                        }
                    }
                }
                LoadingScreen(isLoading.value)
            }
        }
    }


    private fun onTocClicked(title: String?, href: String?) {
        val intent = Intent()
        intent.putExtra(CHAPTER_SELECTED, href)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onSettingsChanged(hash:Int) {
        val intent = Intent()
        intent.putExtra(SETTINGS_CHANGED, hash)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val SELECTED_VIEW_TYPE = "SELECTED_VIEW_TYPE"
        const val Settings = "Settings_View_type"
        const val TableOfContent = "TOC_View_type"
    }
}


private enum class ViewType(val label: String) {
    TableOfContents(label = "أقسام الكتاب"),
    Settings(label = "الإعدادات")
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
