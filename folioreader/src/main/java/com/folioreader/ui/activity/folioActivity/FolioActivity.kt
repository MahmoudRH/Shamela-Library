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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.Constants.BOOK_TITLE
import com.folioreader.Constants.CHAPTER_SELECTED
import com.folioreader.Constants.EPUB_FILE_PATH
import com.folioreader.Constants.SETTINGS_CHANGED
import com.folioreader.FolioReader
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.activity.ContentHighlightActivity
import com.folioreader.ui.activity.folioActivity.book.BookScreen
import com.folioreader.ui.activity.searchActivity.SearchActivity
import com.shamela.apptheme.presentation.common.LoadingScreen
import com.shamela.apptheme.presentation.theme.AppTheme
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


class FolioActivity : ComponentActivity() {
    private val viewModel: FolioActivityViewModel by viewModels()
    private var searchResultsFlow = emptyFlow<Pair<String, String>>()
    private var selectedChapterFlow = emptyFlow<String>()
    private var settingsChangedFlow = emptyFlow<Int>()

    companion object {
        const val LOG_TAG = "FolioActivityCompose"
        const val INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        const val EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(LOG_TAG, "-> onCreate")
        val epubFilePath = intent.getStringExtra(INTENT_EPUB_SOURCE_PATH) ?: ""
        viewModel.onEvent(FolioActivityEvent.InitializeBook(epubFilePath))
        LocalBroadcastManager.getInstance(this).registerReceiver(
            closeBroadcastReceiver,
            IntentFilter(FolioReader.ACTION_CLOSE_FOLIOREADER)
        )



        setContent {
            val state = viewModel.state.collectAsState().value
            AppTheme.ShamelaLibraryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    state.publication?.let { publication ->
                        val searchResult = searchResultsFlow.collectAsState(initial = "" to "").value
                        val selectedChapter = selectedChapterFlow.collectAsState(initial = "").value
                        val settingsChanged = settingsChangedFlow.collectAsState(initial = 0).value


                        BookScreen(
                            navigateToTableOfContent = { selectedPage ->
                                onTableOfContentSelected(
                                    publication.readingOrder[selectedPage].href ?: "",
                                    publication.metadata.title,
                                    epubFilePath
                                )
                            },
                            navigateToSettings = { selectedPage ->
                                onSettingsSelected(
                                    publication.readingOrder[selectedPage].href ?: "",
                                    publication.metadata.title,
                                    epubFilePath
                                )
                            },
                            navigateToSearchScreen = {
                                onSearchButtonClick(publication.readingOrder.size)
                            },
                            publication = publication,
                            navigateBack = { finish() },
                            streamUrl = viewModel.streamUrl,
                            searchResult = searchResult,
                            selectedChapter = selectedChapter,
                            settingsChanged = settingsChanged,
                        )
                    }

                }
                LoadingScreen(visibility = state.isLoading)
            }
        }
    }


    private fun onSearchButtonClick(spineSize: Int?) {
        val intent = Intent(this@FolioActivity, SearchActivity::class.java)
        intent.putExtra(SearchActivity.BUNDLE_SPINE_SIZE, spineSize ?: 0)
        searchLauncher.launch(intent)
    }

    private fun onTableOfContentSelected(currentHref: String, title: String, epubFilePath: String) {
        val intent = Intent(this@FolioActivity, ContentHighlightActivity::class.java)
        intent.putExtra(CHAPTER_SELECTED, currentHref)
        intent.putExtra(EPUB_FILE_PATH, epubFilePath)
        intent.putExtra(ContentHighlightActivity.SELECTED_VIEW_TYPE, ContentHighlightActivity.TableOfContent)
        intent.putExtra(BOOK_TITLE, title)
        contentHighlightLauncher.launch(intent)
    }

    private fun onSettingsSelected(currentHref: String, title: String, epubFilePath: String) {
        val intent = Intent(this@FolioActivity, ContentHighlightActivity::class.java)
        intent.putExtra(CHAPTER_SELECTED, currentHref)
        intent.putExtra(EPUB_FILE_PATH, epubFilePath)
        intent.putExtra(ContentHighlightActivity.SELECTED_VIEW_TYPE, ContentHighlightActivity.Settings)
        intent.putExtra(BOOK_TITLE, title)
        contentHighlightLauncher.launch(intent)
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
                            searchResultsFlow = flow<Pair<String, String>> {
                                emit(searchLocator.href to highlightSearchLocator(searchLocator))
                            }
                        }
                    }
                }

            }
        }

    private fun highlightSearchLocator(searchLocator: SearchLocator): String {
        return "javascript:highlightSearchLocator('${searchLocator.locations.cfi}')"
    }

    private val contentHighlightLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let {
                    data.getStringExtra(CHAPTER_SELECTED)?.let { href ->
                        Log.e(LOG_TAG, "TableOfContentsScreen: Navigating to href: $href ")
                        selectedChapterFlow = flow<String> {
                            emit(href)
                        }
                    }
                    data.getIntExtra(SETTINGS_CHANGED,0).let{ hash->
                        Log.e(LOG_TAG, "TableOfContentsScreen: SettingsChanged: $hash ")
                        settingsChangedFlow = flow<Int> {
                            emit(hash)
                        }
                    }
                }
            }
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