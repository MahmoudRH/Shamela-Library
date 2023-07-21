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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.folioreader.ui.activity.folioActivity.FolioActivity
import com.folioreader.ui.view.CustomWebView
import com.folioreader.util.AppUtil
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.readium.r2.shared.Publication
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    viewModel: BookViewModel = viewModel(),
    streamUrl: String,
    searchResult: Pair<String, String>,
    selectedChapter: String,
    settingsChanged: Int,
    publication: Publication,
    startPageHref:String,
    navigateToTableOfContent: (Int) -> Unit,
    navigateToSettings: (Int) -> Unit,
    navigateToSearchScreen: () -> Unit,
    navigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val state = viewModel.state.collectAsState().value
    val backgroundColor = if (AppTheme.isDarkTheme(context)) 0xff131313 else 0xffffffff
    val scope = rememberCoroutineScope()
    val webViews = remember(context) { mutableStateMapOf<Int, WebView>() }


    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { publication.readingOrder.size }
    )
    LaunchedEffect(Unit) {
        val bookId = publication.metadata.title.hashCode()
        val lastReadHref = AppUtil.getLastReadFromSharedPreferences(
            context = context,
            bookId = bookId.toString()
        )
        Log.e("BookScreen", "Unit, lastReadHref: $lastReadHref ")
        if (startPageHref.isNotBlank()){
            val pageToScrollTo = publication.readingOrder.indexOfFirst { it.href == startPageHref }
            pagerState.scrollToPage(pageToScrollTo)
        }else if (lastReadHref.isNotBlank()) {
            val pageToScrollTo = publication.readingOrder.indexOfFirst { it.href == lastReadHref }
            pagerState.scrollToPage(pageToScrollTo)
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = state.isAppBarsVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(250)
                )
            ) {
                TopAppBar(
                    modifier = Modifier,
                    title = {
                        var titleTextStyle by remember{ mutableStateOf(AppFonts.textLargeBold)}
                        var readyToDraw by remember { mutableStateOf(false) }
                        Text(
                            text = publication.metadata.title,
                            style = titleTextStyle,
                            maxLines = 2,
                            modifier = Modifier.drawWithContent {
                                if (readyToDraw) drawContent()
                            },
                            onTextLayout = {textLayoutResult->
                                if (textLayoutResult.didOverflowHeight) {
                                    titleTextStyle = titleTextStyle.copy(fontSize = titleTextStyle.fontSize * 0.9)
                                }else{
                                    readyToDraw = true
                                }
                            }
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                            15.dp
                        ),
                    ),
                    actions = {

                        IconButton(onClick = {
                            navigateToSearchScreen()
                        }) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.size(4.dp))

                        IconButton(onClick = {
                            viewModel.onEvent(BookEvent.ToggleMenuVisibility)
                        }) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = state.isMenuVisible,
                            onDismissRequest = {
                                viewModel.onEvent(BookEvent.DismissMenu)
                            }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.onEvent(BookEvent.DismissMenu)
                                    navigateToSettings(pagerState.currentPage)
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Settings, null)
                                },
                                text = {
                                    Text(
                                        text = "الإعدادات",
                                        style = AppFonts.textNormal
                                    )
                                })
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.onEvent(BookEvent.DismissMenu)
                                    navigateToTableOfContent(pagerState.currentPage)
                                },
                                text = {
                                    Text(
                                        text = "الفهرس",
                                        style = AppFonts.textNormal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.FormatListBulleted,
                                        null
                                    )
                                })
                        }

                    },
                    navigationIcon = {
                        IconButton(onClick = {
//                            viewModel.onEvent(BookEvent.StopStreamerServer)
                            navigateBack()
                        }) {
                            Icon(
                                Icons.Default.ArrowForwardIos,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        containerColor = Color(backgroundColor),
        bottomBar = {
            BottomBar(
                visibility = state.isAppBarsVisible,
                currentPage = state.currentPageText,
                onCurrentPageChange = {
                    viewModel.onEvent(
                        BookEvent.OnCurrentPageTextChanged(
                            it
                        )
                    )
                },
                onDone = {
                    scope.launch {
                        state.currentPageText.toIntOrNull()?.let {
                            val page = it.coerceIn(
                                0,
                                publication.readingOrder.size - 1
                            )
                            pagerState.scrollToPage(page)
                        }
                    }
                },
                isPrevButtonEnabled = pagerState.currentPage != 0,
                isNextButtonEnabled = pagerState.currentPage != publication.readingOrder.size - 1,
                onPrevButtonClick = {
                    pagerState.apply {
                        val previousPage = max(0, currentPage - 1)
                        scope.launch { animateScrollToPage(previousPage) }
                    }
                },
                onNextButtonClick = {
                    pagerState.apply {
                        val nextPage =
                            min(
                                currentPage + 1,
                                publication.readingOrder.size - 1
                            )
                        scope.launch { animateScrollToPage(nextPage) }
                    }
                }
            )
        }
    ) { paddingValues ->

        LaunchedEffect(pagerState.currentPage) {
            webViews.keys.forEach { key ->
                val maxCacheDistance = 5
                if (abs(key - pagerState.currentPage) >= maxCacheDistance) {
                    webViews.remove(key)
                    println("webView $key deinited")
                }
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                viewModel.onEvent(
                    BookEvent.OnChangeSelectedPage(
                        pageIndex = page,
                        context = context,
                        fontFamilyCssClass = AppFonts.selectedFontFamilyCssClass(),
                        isNightMode = AppTheme.isDarkTheme(context),
                        fontSizeCssClass = AppFonts.selectedFontSizeCssClass(),
                        publication = publication,
                        streamUrl = streamUrl
                    )
                )
            }
        }
        LaunchedEffect(searchResult) {
            val href = searchResult.first
            if (href.isNotBlank()) {
                val pageToScrollTo = publication.readingOrder.indexOfFirst { it.href == href }
                pagerState.scrollToPage(pageToScrollTo)
            }
        }
        LaunchedEffect(selectedChapter) {
            if (selectedChapter.isNotBlank()) {
                val pageToScrollTo = publication.readingOrder.indexOfFirst {
                    it.href == selectedChapter.split('#').first()
                }
                pagerState.scrollToPage(pageToScrollTo)
            }
        }
        LaunchedEffect(settingsChanged) {
            Log.e("BooksScreen", "SettingsChanged! : $settingsChanged")
            if (settingsChanged != 0) {
                val temp = pagerState.currentPage
                Log.e("BooksScreen", "pagerState.currentPage! : $temp")
                webViews.clear()
                viewModel.onEvent(BookEvent.ClearCachedPages)
//                delay(200)
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
        HorizontalPager(
            state = pagerState,
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures(
                        onTap = {
                            //when the webview doesn't fill the whole screen and the user clicks outside of the webview
                            viewModel.onEvent(BookEvent.ToggleAppBarsVisibility)
                        }
                    )
                },
            key = { index ->
                publication.readingOrder[index].href ?: index.toString()
            },
        ) { currentPageIndex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                AndroidView(factory = { context ->
                    webViews[currentPageIndex] ?: run {
                        CustomWebView(
                            context,
                            isNightMode = AppTheme.isDarkTheme(context),
                            currentPageIndex = currentPageIndex,
                            currentPageHref = publication.readingOrder[currentPageIndex].href
                        ).apply {
                            setBackgroundColor(backgroundColor.toInt())
                            settings.javaScriptEnabled = true
                            settings.defaultTextEncodingName = "UTF-8"
                            settings.allowFileAccess = true
                            webViewClient = mMebViewClient
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun isTapped() {
                                    Log.e(
                                        "CustomWebView",
                                        "onClickHtml: isTapped"
                                    )
                                    viewModel.onEvent(BookEvent.ToggleAppBarsVisibility)
                                }
                                @JavascriptInterface
                                fun textSelected(text:String){
                                    Log.e("CustomWebView", "textSelected: $text", )
                                }
                            }, "CustomWebView")
                            addJavascriptInterface(this, "FolioWebView")
                        }
                    }
                }, update = { webview ->
                    (webview as CustomWebView).fullScreenMode.value = !state.isAppBarsVisible
                    webViews[currentPageIndex] ?: run {
                        val (url, htmlData) = state.pagesMap[currentPageIndex]
                            ?: ("" to "")
                        if (url.isNotBlank()) {
                            webview.loadDataWithBaseURL(
                                url,
                                htmlData,
                                state.mimeType,
                                "UTF-8",
                                null
                            )
                            webViews[currentPageIndex] = webview
                        }
                    }
                    scope.launch {
                        delay(200)
                        val (_, javascriptCall) = searchResult
                        if (javascriptCall.isNotBlank()) {
                            webview.loadUrl(javascriptCall)
                        }
                    }
                })
            }

        }
    }
    LoadingScreen(state.isLoading)

    DisposableEffect(Unit) {
        onDispose {
            /*
            to prevent modifying the last read href when the user is viewing a quote,
            check if startPageHref is blank or not
             */
            try {
                val bookId = publication.metadata.title.hashCode()
                publication.readingOrder[pagerState.currentPage].href?.let { lastReadHref ->
                    AppUtil.saveLastReadToSharedPreferences(
                        context = context,
                        bookId = bookId.toString(),
                        lastHref = lastReadHref
                    )
                }
            } catch (e: IndexOutOfBoundsException) {
                Log.e(
                    "BooksScreen",
                    "IndexOutOfBoundsException while saving last read: \n${e.stackTrace}",
                )
            }
        }
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
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                        15.dp
                    )
                ),
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
                    imageVector = Icons.Outlined.KeyboardArrowRight,
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
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "next page",
                    tint = if (isNextButtonEnabled) MaterialTheme.colorScheme.onBackground else Color.Gray
                )
            }
        }
    }
}

private val mMebViewClient = object : WebViewClient() {
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