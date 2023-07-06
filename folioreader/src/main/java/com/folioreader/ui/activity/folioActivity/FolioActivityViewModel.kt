package com.folioreader.ui.activity.folioActivity

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.ui.base.HtmlUtil
import com.folioreader.util.AppUtil
import com.folioreader.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.server.Server
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class FolioActivityViewModel : ViewModel() {
    private val TAG = "FolioActivityViewModel"
    private val _state = MutableStateFlow<FolioActivityState>(FolioActivityState())
    val state: StateFlow<FolioActivityState> = _state.asStateFlow()
    private var portNumber = Constants.DEFAULT_PORT_NUMBER
    private var server = Server(portNumber)
    fun onEvent(event: FolioActivityEvent) {
        when (event) {
            is FolioActivityEvent.InitializeEpubBook -> {
                viewModelScope.launch {
                    Log.e(
                        TAG, "onEvent: ${event.javaClass.simpleName}, filepath: ${event.filePath}",
                    )
                    _state.update { it.copy(isLoading = true) }
                    portNumber = event.portNumber
                    val bookFileName = FileUtil.getEpubFilename(event.filePath)
                    val publication = initBook(bookFileName, event.filePath)
                    val streamUrl = getStreamerUrl(bookFileName)
                    publication?.let {
                        _state.update {
                            it.copy(
                                publication = publication,
                                bookTitle = publication.metadata.title,
                                streamUrl = streamUrl,
                            )
                        }
                    }
                    onEvent(
                        FolioActivityEvent.OnChangeSelectedPage(
                            0,
                            event.fontSizeCssClass,
                            event.fontFamilyCssClass,
                            event.isNightMode,
                            event.context
                        )
                    )
                }
            }

            is FolioActivityEvent.OnChangeTaskImportance -> {
                _state.update { it.copy(taskImportance = event.taskImportance) }
            }

            is FolioActivityEvent.OnChangeTopActivity -> {
                _state.update { it.copy(isTopActivity = event.isTopActivity) }

            }

            is FolioActivityEvent.OnChangeSelectedPage -> {
                Log.e(TAG, "onEvent: ${event.javaClass.simpleName}, page: ${event.pageIndex}")
                onEvent(FolioActivityEvent.OnCurrentPageTextChanged(event.pageIndex.toString()))
                    viewModelScope.launch {
                        getBookPages(
                            context = event.context,
                            fontFamily = event.fontFamilyCssClass,
                            isNightMode = event.isNightMode,
                            fontSize = event.fontSizeCssClass,
                            pageIndex = event.pageIndex,
                            totalPages = _state.value.publication!!.readingOrder.lastIndex
                        ).collect { map->
                            if (_state.value.pagesMap[map.keys.first()] == null)
                            _state.update {
                                it.copy(
                                    pagesMap = it.pagesMap + mapOf(map.keys.first() to map.values.first()),
                                    isLoading = false
                                )
                            }
                        }
                    }
            }

            FolioActivityEvent.StopStreamerServer -> {
                server.stop()
            }

            FolioActivityEvent.ToggleAppBarsVisibility -> {
                _state.update { it.copy(isAppBarsVisible = !it.isAppBarsVisible) }
            }

            FolioActivityEvent.ToggleMenuVisibility -> {
                _state.update { it.copy(isMenuVisible = !it.isMenuVisible) }
            }

            is FolioActivityEvent.OnCurrentPageTextChanged -> {
                _state.update { it.copy(currentPageText = event.newPage) }
            }

            FolioActivityEvent.DismissMenu -> {
                _state.update { it.copy(isMenuVisible = false) }

            }
        }
    }


    private suspend fun getBookPages(
        context: Context,
        pageIndex: Int,
        totalPages:Int,
        fontFamily: String,
        isNightMode: Boolean,
        fontSize: String,
    ) = flow<Map<Int,Pair<String, String>>> {
        val range = (maxOf(0, pageIndex -2))..(minOf(pageIndex +2,totalPages) )
        state.value.publication?.let { publication ->
            range.forEach{page->
                publication.readingOrder[page].let { link ->
                    link.href?.substring(1)?.let { pageFilePath ->
                        val pageUrl = state.value.streamUrl + pageFilePath
                        val htmlContent = getHtmlData(pageUrl)
                        val pageData = HtmlUtil.getHtmlContent(
                            context = context,
                            content = htmlContent,
                            fontFamilyCssClass = fontFamily,
                            isNightMode = isNightMode,
                            fontSizeCssClass = fontSize
                        )
                    emit(mapOf(page to Pair(pageUrl,pageData)))
                    }
                }
            }
        }
    }
    private suspend fun getHtmlData(urlString: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                val inputStream = urlConnection.inputStream
                val reader = BufferedReader(
                    InputStreamReader(
                        inputStream,
                        AppUtil.charsetNameForURLConnection(urlConnection)
                    )
                )

                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let {
//                        if (!it.contains("<hr/>") && !it.contains("¦"))
                        stringBuilder.append(it).append('\n')
                    }
                }
                stringBuilder.toString()
            } catch (e: IOException) {
                Log.e(FolioActivity.LOG_TAG, "HtmlTask failed", e)
                ""
            }
        }
    }

    private suspend fun initBook(
        bookFileName: String,
        filePath: String,
    ): Publication? {
        Log.v(TAG, "-> initBook")
        Log.v(TAG, "-> bookFileName= $bookFileName")
        return withContext(Dispatchers.IO) {
            EpubParser().parse(filePath, "")?.let {
                server = Server(AppUtil.getAvailablePortNumber(portNumber))
                server.addEpub(it.publication, it.container, "/$bookFileName", null)
                server.start()
                FolioReader.initRetrofit(getStreamerUrl(bookFileName))
                val publication = it.publication
                publication
            }
        }
    }

    private fun getStreamerUrl(bookFileName: String): String {
        val url = "${Constants.LOCALHOST}:$portNumber/$bookFileName/"
        return Uri.parse(url).toString()
    }
}