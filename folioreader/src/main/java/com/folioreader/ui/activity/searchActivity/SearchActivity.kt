package com.folioreader.ui.activity.searchActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.folioreader.ui.activity.folioActivity.FolioActivity
import com.shamela.apptheme.common.EmptyListScreen
import com.shamela.apptheme.common.SearchTopBar
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import org.readium.r2.shared.LocatorText


class SearchActivity : ComponentActivity() {
    private val viewModel: SearchViewModel by viewModels()
    private val focusRequester = FocusRequester()

    companion object {
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val BUNDLE_SPINE_SIZE = "BUNDLE_SPINE_SIZE"
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val spineSize = intent.getIntExtra(BUNDLE_SPINE_SIZE, 0)
        viewModel.spineSize = spineSize
        setContent {
            val state = viewModel.state.collectAsState().value

            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    Column(Modifier.fillMaxSize()) {
                        SearchTopBar(
                            onNavigateBack = /*navigateBack*/ { finish() },
                            hint = "بحث..",
                            focusRequester = focusRequester,
                            value = state.searchQuery,
                            onValueChanged = { viewModel.onEven(SearchEvent.OnSearchQueryChanged(it)) },
                            onClickClear = { viewModel.onEven(SearchEvent.ClearSearchQuery) },
                            onClickSearch = { query -> viewModel.onEven(SearchEvent.Search(query)) },
                        )
                        AnimatedVisibility(visible = state.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                progress = state.searchProgress,
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
                                    this@Column.AnimatedVisibility(visible = state.searchResults.isNotEmpty() || state.isLoading) {
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
                                                text = "عدد النتائج: ${state.searchResults.size}",
                                                style = AppFonts.textNormalBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Row {
                                                AnimatedVisibility(visible = state.isLoading) {
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
                                items(state.searchResults) {
                                    it.text?.let { locatorText ->
                                        SearchResult(modifier = Modifier.animateItemPlacement(),locatorText){
                                            val intent = Intent()
                                            intent.putExtra(FolioActivity.EXTRA_SEARCH_ITEM, it as Parcelable)
                                            setResult(Activity.RESULT_OK, intent)
                                            finish()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    EmptyListScreen(
                        visibility = state.isListEmpty,
                        text = "لم يتم العثور على أي نتائج..",
                    )
                }

                DisposableEffect(Unit) {
                    focusRequester.requestFocus()
                    onDispose { }
                }
            }
        }
    }

    @Composable
    private fun SearchResult(modifier: Modifier,locator: LocatorText, onItemClicked:()->Unit) {
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
}

