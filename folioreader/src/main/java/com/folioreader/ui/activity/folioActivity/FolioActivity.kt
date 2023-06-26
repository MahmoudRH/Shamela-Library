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
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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


class FolioActivity : ComponentActivity() {
    private val viewModel: FolioActivityViewModel by viewModels()
    private var searchResultToBeHighlighted = ""

    @OptIn(ExperimentalFoundationApi::class)
    lateinit var pagerState: PagerState

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
        setContent {
            LaunchedEffect(
                key1 = Unit
            ) {
                viewModel.onEvent(
                    FolioActivityEvent.InitializeEpubBook(
                        epubFilePath,
                        Constants.DEFAULT_PORT_NUMBER
                    )
                )
            }
            val state = viewModel.state.collectAsState().value
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        topBar = {
                            DefaultTopBar(
                                title = state.bookTitle,
                                actionIcon = Icons.Outlined.Search,
                                onActionClick = {
                                    val intent = Intent(this, SearchActivity::class.java)
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
                    ) { paddingValues ->
                        state.publication?.let {
                            pagerState = rememberPagerState(
                                initialPage = 0,
                                initialPageOffsetFraction = 0f
                            ) {
                                state.bookPages.size
                            }
                            LaunchedEffect(key1 = Unit, block = {
                                snapshotFlow { pagerState.currentPage }.collect {
                                    viewModel.onEvent(FolioActivityEvent.OnChangeSelectedPage(it))
                                }
                            })
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = paddingValues,
                                modifier = Modifier.fillMaxSize(),
                            ) { currentPageIndex ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(
                                            rememberScrollState()
                                        )
                                ) {
                                    CustomWebView(
                                        url = state.bookPages[currentPageIndex],
                                        data = HtmlUtil.getHtmlContent(
                                            context = this@FolioActivity,
                                            content = state.htmlData[currentPageIndex],
                                            fontFamilyCssClass = fontFamilyCss,
                                            isNightMode = isDarkTheme,
                                            fontSizeCssClass = fontSizeCss
                                        ),
                                        mimeType = state.mimeType,
                                    )
                                }
                            }
                        }
                    }
                    LoadingScreen(state.isLoading)
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
                            pagerState.scrollToPage(page)
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
            view?.let{
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

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun CustomWebView(
        url: String,
        data: String,
        mimeType: String = "application/xhtml+xml",
    ) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.defaultTextEncodingName = "UTF-8"
                settings.allowFileAccess = true
                webViewClient = mMebViewClient
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