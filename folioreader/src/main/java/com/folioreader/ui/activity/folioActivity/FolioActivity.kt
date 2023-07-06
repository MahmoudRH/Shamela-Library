package com.folioreader.ui.activity.folioActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.activity.searchActivity.SearchActivity
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppFonts
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class FolioActivity : ComponentActivity() {
    private val viewModel: FolioActivityViewModel by viewModels()
    private var searchResultsFlow = emptyFlow<Pair<String, String>>()

    @OptIn(ExperimentalFoundationApi::class)
    private lateinit var pagerState: PagerState

    companion object {
        const val LOG_TAG = "FolioActivityCompose"
        const val INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        const val EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM"
    }

    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(LOG_TAG, "-> onCreate")
        val epubFilePath = intent.getStringExtra(INTENT_EPUB_SOURCE_PATH) ?: ""
        val isDarkTheme = AppTheme.isDarkTheme(this)
        val fontFamilyCss = AppFonts.selectedFontFamilyCssClass()
        val fontSizeCss = AppFonts.selectedFontSizeCssClass()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            closeBroadcastReceiver,
            IntentFilter(FolioReader.ACTION_CLOSE_FOLIOREADER)
        )
        viewModel.onEvent(
            FolioActivityEvent.InitializeEpubBook(
                filePath = epubFilePath,
                portNumber = Constants.DEFAULT_PORT_NUMBER,
                context = applicationContext,
                fontFamilyCssClass = fontFamilyCss,
                isNightMode = isDarkTheme,
                fontSizeCssClass = fontSizeCss
            )
        )

        setContent {
            val state = viewModel.state.collectAsState().value
            val backgroundColor = if (AppTheme.isDarkTheme(this)) 0xff131313 else 0xffffffff
            val scope = rememberCoroutineScope()
            val webViews = remember(LocalContext.current) { mutableStateMapOf<Int, WebView>() }

            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    state.publication?.let { publication ->
                        pagerState = rememberPagerState(
                            initialPage = 0,
                            initialPageOffsetFraction = 0f,
                            pageCount = { publication.readingOrder.size }
                        )
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
                                    CenterAlignedTopAppBar(
                                        modifier = Modifier,
                                        title = {
                                            Text(
                                                text = state.bookTitle,
                                                style = AppFonts.textLargeBold
                                            )
                                        },
                                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                15.dp
                                            ),
                                        ),
                                        actions = {

                                            IconButton(onClick = {
                                                onSearchButtonClick(publication.readingOrder.size)
                                            }) {
                                                Icon(
                                                    Icons.Outlined.Search,
                                                    contentDescription = null
                                                )
                                            }

                                            Spacer(modifier = Modifier.size(4.dp))

                                            IconButton(onClick = {
                                                viewModel.onEvent(FolioActivityEvent.ToggleMenuVisibility)
                                            }) {
                                                Icon(
                                                    Icons.Outlined.MoreVert,
                                                    contentDescription = null
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = state.isMenuVisible,
                                                onDismissRequest = {
                                                    viewModel.onEvent(
                                                        FolioActivityEvent.DismissMenu
                                                    )
                                                }
                                            ) {
                                                DropdownMenuItem(
                                                    onClick = { /*TODO*/ },
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
                                                    onClick = { /*TODO*/ },
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
                                            IconButton(onClick = { finish() }) {
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
                                            FolioActivityEvent.OnCurrentPageTextChanged(
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
                                        FolioActivityEvent.OnChangeSelectedPage(
                                            pageIndex = page,
                                            context = applicationContext,
                                            fontFamilyCssClass = fontFamilyCss,
                                            isNightMode = isDarkTheme,
                                            fontSizeCssClass = fontSizeCss
                                        )
                                    )
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
                                                viewModel.onEvent(FolioActivityEvent.ToggleAppBarsVisibility)
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
                                    val searchResult =
                                        searchResultsFlow.collectAsState(initial = "" to "").value
                                    AndroidView(factory = { context ->
                                        webViews[currentPageIndex] ?: run {
                                            com.folioreader.ui.view.CustomWebView(
                                                context,
                                                isNightMode = AppTheme.isDarkTheme(context)
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
                                                        viewModel.onEvent(FolioActivityEvent.ToggleAppBarsVisibility)
                                                    }
                                                }, "CustomWebView")
                                                addJavascriptInterface(this, "FolioWebView")
                                            }
                                        }
                                    }, update = { webview ->
                                        webViews[currentPageIndex] ?: run{
                                            val (url, htmlData) = state.pagesMap[currentPageIndex] ?: ("" to "")
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
                                            val (href, javascriptCall) = searchResult
                                            if (state.pagesMap[currentPageIndex]?.first?.contains(href) == true && javascriptCall.isNotBlank()
                                            ) {
                                                webview.loadUrl(javascriptCall)
                                            }
                                        }
                                    })
                                }

                            }
                        }

                        LoadingScreen(state.isLoading)

                    }
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
                    textStyle = AppFonts.textNormal.copy(textAlign = TextAlign.Center)
                ) {
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = currentPage,
                        innerTextField = it,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
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


    private fun onSearchButtonClick(spineSize: Int?) {
        val intent = Intent(this@FolioActivity, SearchActivity::class.java)
        intent.putExtra(SearchActivity.BUNDLE_SPINE_SIZE, spineSize ?: 0)
        searchLauncher.launch(intent)
    }


    @OptIn(ExperimentalFoundationApi::class)
    private val searchLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    val searchLocator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data.getParcelableExtra(EXTRA_SEARCH_ITEM, SearchLocator::class.java)
                    } else {
                        data.getParcelableExtra(EXTRA_SEARCH_ITEM)
                    }
                    Log.e(LOG_TAG, "data of searchLauncher: ${searchLocator.toString()}")
                    lifecycleScope.launch {
                        searchLocator?.let {
                            val page = viewModel.state.value.pagesMap.values.indexOfFirst {
                                it.first.contains(searchLocator.href)
                            }
                            pagerState.scrollToPage(page)
                            searchResultsFlow = flow<Pair<String,String>> {
                                emit(searchLocator.href to highlightSearchLocator(searchLocator) )
                            }
                        }
                    }
                }

            }
        }

    private fun highlightSearchLocator(searchLocator: SearchLocator): String {
        return "javascript:highlightSearchLocator('${searchLocator.locations.cfi}')"
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
                    Log.e(LOG_TAG, "shouldInterceptRequest failed", e)
                }

            }
            return null
        }
    }

    @JavascriptInterface
    fun onReceiveHighlights(html: String?) {
//        if (html != null) {
//           val rangy = HighlightUtil.createHighlightRangy(
//                applicationContext,
//                html,
//                viewModel.state,
//                pageName,
//                spineIndex,
//                rangy
//            )
//        }
    }

    @JavascriptInterface
    fun getUpdatedHighlightId(id: String?, style: String) {
//        if (id != null) {
//            val highlightImpl = HighLightTable.updateHighlightStyle(id, style)
//            if (highlightImpl != null) {
//                HighlightUtil.sendHighlightBroadcastEvent(
//                    requireActivity().applicationContext,
//                    highlightImpl,
//                    HighLight.HighLightAction.MODIFY
//                )
//            }
//            val rangyString = HighlightUtil.generateRangyString(pageName)
//            requireActivity().runOnUiThread { loadRangy(rangyString) }
//
//        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.v(LOG_TAG, "-> onNewIntent")

        val action = getIntent().action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

            if (!viewModel.state.value.isTopActivity) {
                finish()
                // To determine if app in background or foreground
                val taskImportance = viewModel.state.value.taskImportance
                var appInBackground = false
                if (Build.VERSION.SDK_INT < 26) {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND == taskImportance)
                        appInBackground = true
                } else {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED == taskImportance)
                        appInBackground = true
                }
                if (appInBackground)
                    moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.v(LOG_TAG, "-> onResume")
        viewModel.onEvent(FolioActivityEvent.OnChangeTopActivity(true))
        val action = intent.action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.v(LOG_TAG, "-> onStop")
        viewModel.onEvent(FolioActivityEvent.OnChangeTopActivity(false))
    }

    private val closeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "-> closeBroadcastReceiver -> onReceive -> ${intent.action}")

            val action = intent.action
            if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

                try {
                    val activityManager =
                        context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.runningAppProcesses
                    viewModel.onEvent(FolioActivityEvent.OnChangeTaskImportance(tasks[0].importance))
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "-> ", e)
                }

                val closeIntent = Intent(applicationContext, FolioActivity::class.java)
                closeIntent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                closeIntent.action = FolioReader.ACTION_CLOSE_FOLIOREADER
                this@FolioActivity.startActivity(closeIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.unregisterReceiver(closeBroadcastReceiver)
        viewModel.onEvent(FolioActivityEvent.StopStreamerServer)
        if (isFinishing) {
            localBroadcastManager.sendBroadcast(Intent(FolioReader.ACTION_FOLIOREADER_CLOSED))
            FolioReader.get().retrofit = null
            FolioReader.get().r2StreamerApi = null
        }
    }

}