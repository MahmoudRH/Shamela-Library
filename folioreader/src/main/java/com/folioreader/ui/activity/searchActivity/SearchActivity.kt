package com.folioreader.ui.activity.searchActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shamela.apptheme.common.EmptyListScreen
import com.shamela.apptheme.common.LoadingScreen
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
                                    it.text?.let { locator ->
                                        SearchResult(locator)
                                    }
                                }
                            }
                        }
                    }
                    EmptyListScreen(
                        visibility = state.isListEmpty,
                        text = "لم يتم العثور على أي نتائج..",
                    )
//                    LoadingScreen(visibility = state.isLoading)

                }

                DisposableEffect(Unit) {
                    focusRequester.requestFocus()
                    onDispose { }
                }
            }
        }
    }

    @Composable
    private fun SearchResult(locator: LocatorText) {
        val text = buildAnnotatedString {
            val before = locator.before
            val highlightedText = locator.hightlight
            val after = locator.after
            val isDarkTheme = AppTheme.isDarkTheme(this@SearchActivity)

            append("$before")
            withStyle(
                style = SpanStyle(
                    fontSize = (AppFonts.textNormal.fontSize.value + 4f).sp,
                    background = Color(0xfff8ff00),
                    color = Color.Blue
                )
            ) {
                append("$highlightedText")
            }
            append("$after")
        }

        Column(
            modifier = Modifier
                .clickable { /* Handle click event */ }
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SearchTopBar(
        onNavigateBack: () -> Unit = {},
        value: String,
        onValueChanged: (String) -> Unit,
        onClickSearch: (String) -> Unit = {},
        onClickClear: () -> Unit = {},
        hint: String,
        focusRequester: FocusRequester,
    ) {
        TopAppBar(
            title = {
                SearchTextField(
                    value,
                    onValueChanged,
                    hint = hint,
                    focusRequester = focusRequester,
                    onSearch = { onClickSearch(value) }
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            ),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
                }
            },
            actions = {
                AnimatedVisibility(
                    value.trim().isNotEmpty(),
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onClickClear
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "مسح"
                        )
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun SearchTextField(
        value: String,
        onValueChanged: (String) -> Unit,
        hint: String,
        focusRequester: FocusRequester,
        onSearch: () -> Unit = {},
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused)
                        keyboardController?.show()
                },
            value = value,
            onValueChange = onValueChanged,
            singleLine = true,
            decorationBox = { innerTextField ->
                AnimatedVisibility(
                    value.isEmpty(),
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    Text(text = hint, color = Color.Gray, fontSize = 18.sp)
                }
                innerTextField()
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                onSearch()
            }),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        )
    }
}

