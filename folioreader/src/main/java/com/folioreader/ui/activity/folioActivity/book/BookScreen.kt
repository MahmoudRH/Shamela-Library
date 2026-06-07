package com.folioreader.ui.activity.folioActivity.book


import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioreader.ui.activity.folioActivity.FolioActivity
import com.folioreader.ui.view.CustomWebView
import com.folioreader.util.AppUtil
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import com.shamela.apptheme.presentation.theme.ShamelaIcons
import kotlinx.coroutines.launch
import org.readium.r2.shared.Publication

@Composable
fun BookScreen(
    viewModel: BookViewModel = viewModel(),
    streamUrl: String,
    searchResult: Pair<String, String>,
    selectedChapter: String,
    settingsChanged: Int,
    publication: Publication,
    startPageHref: String,
    navigateToTableOfContent: (Int) -> Unit,
    navigateToSettings: (Int) -> Unit,
    navigateToSearchScreen: () -> Unit,
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val backgroundColor = if (AppTheme.isDarkTheme(context)) 0xff131313 else 0xffffffff
    val pagerState = rememberPagerState(pageCount = { publication.readingOrder.size })
    val scope = rememberCoroutineScope()
    val cachedWebViews = remember { mutableStateMapOf<Int, WebView>() }

    // Save last read on dispose
    DisposableEffect(Unit) {
        onDispose {
            val bookId = publication.metadata.title.hashCode()
            runCatching {
                publication.readingOrder[pagerState.currentPage].href?.let { lastReadHref ->
                    AppUtil.saveLastReadToSharedPreferences(
                        context = context,
                        bookId = bookId.toString(),
                        lastHref = lastReadHref
                    )
                }
            }.onFailure {
                Log.e("BookScreen", "Failed to save last read", it)
            }
        }
    }

    // Initial page logic
    LaunchedEffect(Unit) {
        val bookId = publication.metadata.title.hashCode()
        val lastReadHref = AppUtil.getLastReadFromSharedPreferences(context, bookId.toString())
        val target = when {
            startPageHref.isNotBlank() -> startPageHref
            lastReadHref.isNotBlank() -> lastReadHref
            else -> null
        }
        target?.let {
            val index = publication.readingOrder.indexOfFirst { item -> item.href == it }
            if (index != -1) pagerState.scrollToPage(index)
        }
    }

    // Scroll to search result, chapter, or update on settings change
    LaunchedEffect(searchResult, selectedChapter, settingsChanged) {
        when {
            searchResult.first.isNotBlank() -> {
                val index = publication.readingOrder.indexOfFirst { it.href == searchResult.first }
                if (index != -1) pagerState.scrollToPage(index)
            }
            selectedChapter.isNotBlank() -> {
                val index = publication.readingOrder.indexOfFirst {
                    it.href == selectedChapter.split("#").firstOrNull()
                }
                if (index != -1) pagerState.scrollToPage(index)
            }
            settingsChanged != 0 -> {
                val temp = pagerState.currentPage
                Log.e("BooksScreen", "pagerState.currentPage! : $temp")
                cachedWebViews.clear()
                viewModel.onEvent(BookEvent.ClearCachedPages)
                viewModel.onEvent(
                    BookEvent.OnChangeSelectedPage(
                        pageIndex = temp,
                        context = context,
                        fontFamilyCssClass = AppFonts.selectedFontFamilyCssClass(),
                        isNightMode = AppTheme.isDarkTheme(context),
                        fontSizeCssClass = AppFonts.selectedFontSizeCssClass(),
                        publication = publication,
                        streamUrl = streamUrl
                    )
                )
                pagerState.scrollToPage(temp)

            }
        }
    }

    // Observe current page
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onEvent(
                BookEvent.OnChangeSelectedPage(
                    page,
                    AppFonts.selectedFontSizeCssClass(),
                    AppFonts.selectedFontFamilyCssClass(),
                    AppTheme.isDarkTheme(context),
                    context,
                    publication,
                    streamUrl
                )
            )
        }
    }

    // Evict WebViews that are far from the current page to prevent unbounded memory growth.
    // Keep ±3 so beyondViewportPageCount=2 pages are always within the live window.
    LaunchedEffect(pagerState.currentPage) {
        cachedWebViews.keys.toList().forEach { key ->
            if (kotlin.math.abs(key - pagerState.currentPage) > 3) {
                cachedWebViews[key]?.destroy()
                cachedWebViews.remove(key)
            }
        }
    }

    Column(modifier = Modifier.background(Color(backgroundColor))) {
        BookTopBar(
            title = publication.metadata.title,
            isVisible = state.isAppBarsVisible,
            isMenuVisible = state.isMenuVisible,
            onBack = navigateBack,
            onSearch = navigateToSearchScreen,
            onSettings = { navigateToSettings(pagerState.currentPage) },
            onToc = { navigateToTableOfContent(pagerState.currentPage) },
            onToggleMenu = { viewModel.onEvent(BookEvent.ToggleMenuVisibility) },
            onDismissMenu = { viewModel.onEvent(BookEvent.DismissMenu) }
        )

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 2,
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        viewModel.onEvent(BookEvent.ToggleAppBarsVisibility)
                    })
                },
            key = { index -> publication.readingOrder[index].href ?: index.toString() }
        ) { pageIndex ->
            BookPage(
                index = pageIndex,
                webViews = cachedWebViews,
                state = state,
                publication = publication,
                backgroundColor = backgroundColor,
                javascriptCall = searchResult.second,
                onTapped = { viewModel.onEvent(BookEvent.ToggleAppBarsVisibility) },
                onRequestMeaning = { text ->
                    viewModel.onEvent(BookEvent.ShowMeaningSheet(text, context))
                },
                saveWebView = { index, webview -> cachedWebViews.put(index, webview) }
            )
        }

        BottomBar(
            visibility = state.isAppBarsVisible,
            currentPage = state.currentPageText,
            onCurrentPageChange = { viewModel.onEvent(BookEvent.OnCurrentPageTextChanged(it)) },
            onDone = {
                state.currentPageText.toIntOrNull()?.let {
                    val page = it.coerceIn(0, publication.readingOrder.lastIndex)
                    scope.launch { pagerState.scrollToPage(page) }
                }
            },
            isPrevButtonEnabled = pagerState.currentPage > 0,
            isNextButtonEnabled = pagerState.currentPage < publication.readingOrder.lastIndex,
            onPrevButtonClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            onNextButtonClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
    }

    LoadingScreen(state.isLoading)

    if (state.showMeaningSheet) {
        MeaningBottomSheet(
            word = state.meaningQuery,
            loading = state.meaningLoading,
            dictionaryAvailable = state.dictionaryAvailable,
            results = state.meaningResults,
            onDismiss = { viewModel.onEvent(BookEvent.DismissMeaningSheet) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    visibility: Boolean,
    currentPage: String,
    onCurrentPageChange: (String) -> Unit,
    onDone: () -> Unit,
    isPrevButtonEnabled: Boolean,
    isNextButtonEnabled: Boolean,
    onPrevButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit,
) {

    AnimatedVisibility(
        visible = visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp))
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val focusManager = LocalFocusManager.current
            IconButton(
                modifier = Modifier.padding(start = 20.dp),
                enabled = isPrevButtonEnabled,
                onClick = onPrevButtonClick
            ) {
                Icon(
                    imageVector = ShamelaIcons.KeyboardArrowRight,
                    contentDescription = "Previous page",
                    tint = if (isPrevButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            BasicTextField(
                value = currentPage,
                onValueChange = onCurrentPageChange,
                interactionSource = interactionSource,
                enabled = true,
                singleLine = true,
                modifier = Modifier
                    .width(60.dp)
                    .height(30.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onDone(); focusManager.clearFocus() }),
                textStyle = AppFonts.textNormal.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                ),

                ) {
                OutlinedTextFieldDefaults.DecorationBox(
                    value = currentPage,
                    innerTextField = it,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        cursorColor = MaterialTheme.colorScheme.onBackground,
//                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
//                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
//                    ),
                    interactionSource = interactionSource,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                        start = 4.dp,
                        end = 4.dp,
                        top = 0.dp,
                        bottom = 0.dp
                    ),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier.padding(end = 20.dp),
                enabled = isNextButtonEnabled,
                onClick = onNextButtonClick
            ) {
                Icon(
                    imageVector = ShamelaIcons.KeyboardArrowLeft,
                    contentDescription = "next page",
                    tint = if (isNextButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookTopBar(
    title: String,
    isVisible: Boolean,
    isMenuVisible: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onToc: () -> Unit,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        TopAppBar(
            title = {
                val baseStyle = AppFonts.textLargeBold
                var style by remember(baseStyle) { mutableStateOf(baseStyle) }
                var ready by remember(baseStyle) { mutableStateOf(false) }
                Text(
                    text = title,
                    style = style,
                    maxLines = 2,
                    modifier = Modifier.drawWithContent { if (ready) drawContent() },
                    onTextLayout = {
                        if (it.didOverflowHeight) style = style.copy(fontSize = style.fontSize * 0.9f)
                        else ready = true
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(ShamelaIcons.ArrowForwardIos, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onSearch) {
                    Icon(ShamelaIcons.Search, null)
                }
                Spacer(modifier = Modifier.size(4.dp))
                IconButton(onClick = onToggleMenu) {
                    Icon(ShamelaIcons.MoreVert, null)
                }
                DropdownMenu(expanded = isMenuVisible, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(
                        onClick = {
                            onDismissMenu()
                            onSettings()
                        },
                        leadingIcon = { Icon(ShamelaIcons.Settings, null) },
                        text = { Text("الإعدادات", style = AppFonts.textNormal) }
                    )
                    DropdownMenuItem(
                        onClick = {
                            onDismissMenu()
                            onToc()
                        },
                        leadingIcon = { Icon(ShamelaIcons.FormatListBulleted, null) },
                        text = { Text("الفهرس", style = AppFonts.textNormal) }
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp)
            )
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BookPage(
    index: Int,
    webViews: Map<Int, WebView>,
    state: BookState,
    publication: Publication,
    backgroundColor: Long,
    javascriptCall: String,
    onTapped: () -> Unit,
    onRequestMeaning: (String) -> Unit,
    saveWebView: (Int, WebView) -> Unit,
) {
    val lastAppliedJs = remember { mutableStateOf("") }
    // Start as loaded when we already have a pre-rendered cached WebView for this index.
    val pageLoaded = remember { mutableStateOf(webViews.containsKey(index)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            AndroidView(factory = { context ->
                webViews[index] ?: run {
                    val loadedState = pageLoaded
                    CustomWebView(
                        context,
                        isNightMode = AppTheme.isDarkTheme(context),
                        currentPageIndex = index,
                        currentPageHref = publication.readingOrder[index].href
                    ).apply {
                        setBackgroundColor(backgroundColor.toInt())
                        settings.javaScriptEnabled = true
                        settings.defaultTextEncodingName = "UTF-8"
                        settings.allowFileAccess = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                loadedState.value = true
                            }
                            override fun shouldInterceptRequest(
                                view: WebView,
                                request: WebResourceRequest,
                            ): WebResourceResponse? {
                                if (!request.isForMainFrame
                                    && request.url.path != null
                                    && request.url.path!!.endsWith("/favicon.ico")
                                ) {
                                    try {
                                        return WebResourceResponse("image/png", null, null)
                                    } catch (e: Exception) {
                                        Log.e(FolioActivity.LOG_TAG, "shouldInterceptRequest failed", e)
                                    }
                                }
                                return null
                            }
                        }
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun isTapped() = onTapped()
                            @JavascriptInterface
                            fun textSelected(text: String) {
                                Log.e("CustomWebView", "textSelected: $text")
                            }
                        }, "CustomWebView")
                        addJavascriptInterface(this, "FolioWebView")
                        // Qualify with `this.` — the BookPage `onRequestMeaning` param
                        // (a val) would otherwise shadow this property and fail to assign.
                        this.onRequestMeaning = onRequestMeaning
                    }
                }
            }, update = { webview ->
                (webview as CustomWebView).fullScreenMode.value = !state.isAppBarsVisible
                webViews[index] ?: run {
                    val (url, htmlData) = state.pagesMap[index] ?: ("" to "")
                    if (url.isNotBlank()) {
                        webview.loadDataWithBaseURL(url, htmlData, state.mimeType, "UTF-8", null)
                        saveWebView(index, webview)
                    }
                }
                if (javascriptCall.isNotBlank() && javascriptCall != lastAppliedJs.value && pageLoaded.value) {
                    lastAppliedJs.value = javascriptCall
                    webview.loadUrl(javascriptCall)
                }
            })
        }

        if (!pageLoaded.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}