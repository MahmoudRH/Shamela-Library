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
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.folioreader.ui.base.HtmlUtil
import com.folioreader.ui.fragment.FolioPageFragment
import com.shamela.apptheme.common.DefaultTopBar
import com.shamela.apptheme.common.LoadingScreen
import com.shamela.apptheme.theme.AppFonts
import com.shamela.apptheme.theme.AppTheme
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


class FolioActivity : ComponentActivity() {
    private val viewModel: FolioActivityViewModel by viewModels()
    private var searchResultToBeHighlighted = ""

    @OptIn(ExperimentalFoundationApi::class)
    private var pagerState: PagerState? = null

    companion object {
        const val LOG_TAG = "FolioActivityCompose"
        const val INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        const val EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM"
    }

    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalFoundationApi::class)
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
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            AnimatedVisibility(
                                visible = state.isAppBarsVisible,
                                enter = slideInVertically(initialOffsetY = { -it }),
                                exit = slideOutVertically(
                                    targetOffsetY = { -it },
                                    animationSpec = tween(250)
                                ),

                                ) {
                                DefaultTopBar(
                                    title = state.bookTitle,
                                    actionIcon = Icons.Outlined.Search,
                                    onActionClick = {
                                        val intent =
                                            Intent(this@FolioActivity, SearchActivity::class.java)
                                        intent.putExtra(
                                            SearchActivity.BUNDLE_SPINE_SIZE,
                                            state.publication?.readingOrder?.size ?: 0
                                        )
                                        searchLauncher.launch(intent)
                                    },
                                    onNavigateBack = {
                                        finish()
                                    }
                                )
                            }
                        },
                        containerColor = Color(backgroundColor),
                        floatingActionButton = {
                            AnimatedVisibility(
                                visible = state.isAppBarsVisible,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(250)
                                ),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AnimatedVisibility(
                                        visible = state.isMenuVisible,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "الإعدادات",
                                                    style = AppFonts.textSmall,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(25))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .clickable { /* TODO: Open Settings */ }
                                                        .padding(
                                                            horizontal = 10.dp,
                                                            vertical = 5.dp
                                                        ),
                                                    color = MaterialTheme.colorScheme.contentColorFor(
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                SmallFloatingActionButton(
                                                    onClick = { /*TODO: Open Settings*/ },
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Settings,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "الفهرس",
                                                    style = AppFonts.textSmall,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(25))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .clickable { /* TODO: Open Table Of Contents */ }
                                                        .padding(
                                                            horizontal = 10.dp,
                                                            vertical = 5.dp
                                                        ),
                                                    color = MaterialTheme.colorScheme.contentColorFor(
                                                        MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                SmallFloatingActionButton(
                                                    onClick = { /*TODO: Open Table Of Contents*/ },
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.FormatListBulleted,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    FloatingActionButton(
                                        modifier = Modifier.align(Alignment.End),
                                        onClick = { viewModel.onEvent(FolioActivityEvent.ToggleMenuVisibility) },
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocalLibrary,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            BottomBar(
                                visibility = state.isAppBarsVisible,
                                currentPage = state.currentPageText,
                                onCurrentPageChange = { viewModel.onEvent(FolioActivityEvent.OnCurrentPageTextChanged(it)) },
                                onDone = {
                                  scope.launch {
                                      state.currentPageText.toIntOrNull()?.let{
                                          val page = it.coerceIn(0,state.bookPages.size-1)
                                          pagerState?.scrollToPage(page)
                                      }
                                  }
                                },
                                isPrevButtonEnabled = pagerState?.currentPage != 0,
                                isNextButtonEnabled = pagerState?.currentPage != state.bookPages.size - 1,
                                onPrevButtonClick = {
                                    pagerState?.apply {
                                        val previousPage = max(0, currentPage - 1)
                                        scope.launch { animateScrollToPage(previousPage) }
                                    }
                                },
                                onNextButtonClick = {
                                    pagerState?.apply {
                                        val nextPage =
                                            min(currentPage + 1, state.bookPages.size - 1)
                                        scope.launch { animateScrollToPage(nextPage) }
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        state.publication?.let { publication ->
                            pagerState = rememberPagerState(
                                initialPage = 0,
                                initialPageOffsetFraction = 0f,
                                pageCount = { state.bookPages.size }
                            )
                            LaunchedEffect(key1 = Unit, block = {
                                snapshotFlow { pagerState?.currentPage }.collect {page->
                                    page?.let{
                                        viewModel.onEvent(FolioActivityEvent.OnChangeSelectedPage(it))
                                    }
                                }
                            })
                            pagerState?.let { pagerState ->
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
                                        CustomWebView(
                                            url = state.bookPages[currentPageIndex],
                                            data = state.htmlData[currentPageIndex],
                                            mimeType = state.mimeType,
                                            backgroundColor = backgroundColor.toInt(),
                                            onTapped = {
                                                viewModel.onEvent(FolioActivityEvent.ToggleAppBarsVisibility)
                                            }
                                        )
                                    }

                                }
                            }
                        }
                    }
                    LoadingScreen(state.isLoading)
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
        onDone:()->Unit,
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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
                            val page = viewModel.state.value.bookPages.indexOfFirst {
                                it.contains(searchLocator.href)
                            }
                            pagerState?.scrollToPage(page)
                            searchResultToBeHighlighted = highlightSearchLocator(searchLocator)
                            Log.e(
                                LOG_TAG,
                                "searchResultToBeHighlighted:${searchResultToBeHighlighted} ",
                            )
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
                    Log.e(FolioPageFragment.LOG_TAG, "shouldInterceptRequest failed", e)
                }

            }
            return null
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.let {
                Log.e("MAH ", "onPageFinished:url($url) ")
                searchResultToBeHighlighted.let {
                    if (it.isNotBlank()) {
                        Log.e(LOG_TAG, "onPageFinished:webView.loadUrl($it) ")
                        view.loadUrl(it)
                        searchResultToBeHighlighted = ""
                    }
                }
            }

        }
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    @Composable
    private fun CustomWebView(
        url: String,
        data: String,
        mimeType: String = "application/xhtml+xml",
        backgroundColor: Int,
        onTapped: () -> Unit,
    ) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                setBackgroundColor(backgroundColor)
                settings.javaScriptEnabled = true
                settings.defaultTextEncodingName = "UTF-8"
                settings.allowFileAccess = true
                webViewClient = mMebViewClient
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun isTapped() {
                        Log.e("CustomWebView", "onClickHtml: isTapped")
                        onTapped()
                    }

                }, "CustomWebView")
                loadDataWithBaseURL(url, data, mimeType, "UTF-8", null)
            }
        }, update = {

        })
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