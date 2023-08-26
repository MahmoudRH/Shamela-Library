package com.folioreader.ui.activity.searchActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.activity.folioActivity.FolioActivity
import com.shamela.apptheme.presentation.common.EmptyListScreen
import com.shamela.apptheme.presentation.common.SearchTopBar
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import org.readium.r2.shared.LocatorText
import java.io.File
import java.util.UUID


class SearchActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels()
    private val focusRequester = FocusRequester()

    companion object {
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val Book_ID = "Book_ID"
        const val Search_Type = "Search_Type"
        const val Search_Query = "Search_Query"
        const val Search_Categories = "Search_Categories"

        const val Search_Type_SingleBookSearch = "SingleBookSearch"
        const val Search_Type_SectionsSearch = "SectionsSearch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val downloadsFolder = externalMediaDirs.firstOrNull() ?: run {
            File(applicationContext.filesDir, "fallback_directory")
        }
        val searchType = intent.getStringExtra(Search_Type) ?: ""
        val epubFilePath = intent.getStringExtra(Constants.EPUB_FILE_PATH)
        val bookId = intent.getStringExtra(Book_ID) ?: ""
        val searchQuery = intent.getStringExtra(Search_Query) ?: ""
        val searchCategories =
            intent.getStringArrayExtra(Search_Categories)?.toList() ?: emptyList()

        viewModel.onEven(SearchEvent.OnSearchQueryChanged(searchQuery))
        if (searchType == Search_Type_SingleBookSearch) {
            viewModel.onEven(SearchEvent.InitEpub(epubFilePath))
        } else {
            Log.e(LOG_TAG, "onCreate: searchQuery = $searchQuery")
            Log.e(LOG_TAG, "onCreate: searchCategories = $searchCategories")
            viewModel.onEven(SearchEvent.SearchCategories(searchQuery, searchCategories, this))
        }

        setContent {
            val state = viewModel.state.collectAsState().value
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    if (searchType == Search_Type_SingleBookSearch) {
                        SingleBookSearchResults(
                            query = state.searchQuery,
                            onClickSearch = { query ->
                                viewModel.onEven(SearchEvent.SearchBook(bookId, query, this))
                            },
                            isLoading = state.isLoading,
                            searchProgress = state.searchProgress,
                            searchResults = state.searchResults,
                            isListEmpty = state.isListEmpty
                        )

                    } else {
                        SectionSearchResults(
                            query = state.searchQuery,
                            onClickSearch = {
                                viewModel.onEven(
                                    SearchEvent.SearchCategories(state.searchQuery, searchCategories, this)
                                )
                            },
                            isLoading = state.isLoading,
                            searchProgress = state.searchProgress,
                            searchResults = state.sectionSearchResults,
                            isListEmpty = state.sectionSearchResults.isEmpty(),
                            onSearchItemClicked =  {bookTitle, searchLocator->
                                val subFilePath = "ShamelaDownloads/${searchLocator.title}/${bookTitle}.epub"
                                val bookFile = File(downloadsFolder, subFilePath)
                                val uuidName = bookTitle + searchLocator.title
                                val bookID = UUID.nameUUIDFromBytes(uuidName.toByteArray()).toString()
                                FolioReader.get().openBook(
                                    assetOrSdcardPath = bookFile.path,
                                    startPageHref= searchLocator.href,
                                    bookId = bookID,
                                    onAddQuoteToFavorite = { pageIndex: Int, pageHref: String, text: String ->
                                      /*  onAddQuoteToFavorite(
                                            Quote(
                                                text = text,
                                                pageIndex = pageIndex,
                                                pageHref = pageHref,
                                                bookName = book.title,
                                                bookId = book.id,
                                            )
                                        )*/
                                    }
                                )

/*                                val intent = Intent()
                                intent.putExtra(
                                    FolioActivity.EXTRA_SEARCH_ITEM,
                                    searchLocator as Parcelable
                                )
                                setResult(Activity.RESULT_OK, intent)
                                finish()*/
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SingleBookSearchResults(
        query: String = "",
        onClickSearch: (String) -> Unit = {},
        isLoading: Boolean = false,
        searchProgress: Float = 0f,
        searchResults: List<SearchLocator> = emptyList(),
        isListEmpty: Boolean = false,
    ) {
        Column(Modifier.fillMaxSize()) {
            SearchTopBar(
                onNavigateBack = /*navigateBack*/ { finish() },
                hint = "بحث..",
                focusRequester = focusRequester,
                value = query,
                onValueChanged = { viewModel.onEven(SearchEvent.OnSearchQueryChanged(it)) },
                onClickClear = { viewModel.onEven(SearchEvent.ClearSearchQuery) },
                onClickSearch = onClickSearch,
            )
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = searchProgress,
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        this@Column.AnimatedVisibility(visible = searchResults.isNotEmpty() || isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                                            0.2.dp
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "عدد النتائج: ${searchResults.size}",
                                    style = AppFonts.textNormalBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row {
                                    AnimatedVisibility(visible = isLoading) {
                                        Text(
                                            text = "جار البحث",
                                            style = AppFonts.textNormalBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }

                    }
                    items(searchResults) {
                        it.text?.let { locatorText ->
                            SearchResult(
                                modifier = Modifier.animateItemPlacement(),
                                locatorText
                            ) {
                                val intent = Intent()
                                intent.putExtra(
                                    FolioActivity.EXTRA_SEARCH_ITEM,
                                    it as Parcelable
                                )
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        }
                    }
                }

                EmptyListScreen(
                    visibility = isListEmpty,
                    text = "لم يتم العثور على أي نتائج..",
                )
            }
        }
        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun SectionSearchResults(
        query: String = "",
        onClickSearch: (String) -> Unit = {},
        isLoading: Boolean = false,
        searchProgress: Float = 0f,
        searchResults: List<Pair<String, SearchLocator>> = emptyList(),
        isListEmpty: Boolean = false,
        onSearchItemClicked: (String, SearchLocator)->Unit
    ) {
        Column(Modifier.fillMaxSize()) {
            SearchTopBar(
                onNavigateBack = { finish() },
                hint = "بحث..",
                focusRequester = focusRequester,
                value = query,
                onValueChanged = { viewModel.onEven(SearchEvent.OnSearchQueryChanged(it)) },
                onClickClear = { viewModel.onEven(SearchEvent.ClearSearchQuery) },
                onClickSearch = onClickSearch,
            )
            AnimatedVisibility(visible = isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = searchProgress,
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        this@Column.AnimatedVisibility(visible = searchResults.isNotEmpty() || isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "عدد النتائج: ${searchResults.size}",
                                    style = AppFonts.textNormalBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row {
                                    AnimatedVisibility(visible = isLoading) {
                                        Text(
                                            text = "جار البحث",
                                            style = AppFonts.textNormalBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }

                    }
                    searchResults.groupBy { it.second.title }.forEach { (category, results) ->
                        item {
                            Header(category, 0.2f,true)
                        }
                        results.groupBy { it.first }.forEach { (bookTitle, searchLocators) ->
                            stickyHeader {
                                Header(bookTitle)
                            }
                            items(searchLocators) { (bookTitle, searchLocator) ->
                                searchLocator.text?.let { locatorText ->
                                    SearchResult(
                                        Modifier.animateItemPlacement(),
                                        locatorText
                                    ) { onSearchItemClicked(bookTitle,searchLocator) }
                                }
                            }
                        }


                    }
                }

                EmptyListScreen(
                    visibility = isListEmpty,
                    text = "لم يتم العثور على أي نتائج..",
                )
            }
        }
        DisposableEffect(Unit) {
            focusRequester.requestFocus()
            onDispose { }
        }
    }

    @Composable
    private fun SearchResult(modifier: Modifier, locator: LocatorText, onItemClicked: () -> Unit) {
        val text = buildAnnotatedString {
            val before = locator.before
            val highlightedText = locator.hightlight
            val after = locator.after

            append("$before")
            withStyle(
                style = SpanStyle(
                    fontSize = (AppFonts.textNormal.fontSize.value + 4f).sp,
                    background = Color(0xfff8ff00),
                    color = Color.Black
                )
            ) {
                append("$highlightedText")
            }
            append("$after")
        }

        Column(
            modifier = modifier
                .clickable { onItemClicked() }
        ) {
            Text(
                text = text,
                style = AppFonts.textNormal,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }

    @Composable
    private fun Header(text: String, backgroundColorAlpha : Float = 0.1f, largeText:Boolean = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary
                        .copy(backgroundColorAlpha)
                        .compositeOver(MaterialTheme.colorScheme.background)
                )
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = text,
                style = if(largeText) AppFonts.textLarge else  AppFonts.textNormalBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

