package com.folioreader.ui.activity.folioActivity

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.util.AppUtil
import com.folioreader.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
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
                    _state.update {
                        it.copy(
                            publication = publication,
                            bookTitle = publication?.metadata?.title ?: "الشاملة",
                            streamUrl = streamUrl,
                            isLoading = false,
                        )
                    }
                    launch {
                        getBookPages().collect { (pageUrl, pageData) ->
                            _state.update { it.copy(bookPages = it.bookPages + pageUrl, htmlData = it.htmlData + pageData) }
                        }
                    }
                }
            }

            is FolioActivityEvent.OnChangeTaskImportance -> {
                _state.update { it.copy(taskImportance = event.taskImportance) }
            }

            is FolioActivityEvent.OnChangeTopActivity -> {
                _state.update { it.copy(isTopActivity = event.isTopActivity) }

            }

            is FolioActivityEvent.OnChangeSelectedPage -> {
                Log.e(TAG, "onEvent: ${event.javaClass.simpleName}, page: ${event.newPage}")
                /*                viewModelScope.launch {
                                    getHtmlData(state.value.bookPages[event.newPage]).let { htmlData ->
                                        //-> mimeType is statically set...
                                        _state.update {
                                            it.copy(
                                                htmlData = htmlData,
                                                mimeType = "application/xhtml+xml"
                                            )
                                        }
                                    }
                                }*/
            }

            FolioActivityEvent.StopStreamerServer -> {
                server.stop()
            }
        }
    }

    private suspend fun getBookPages() = flow<Pair<String, String>> {
        state.value.publication?.let { publication ->
            publication.readingOrder.forEach { link ->
                link.href?.substring(1)?.let { pageFilePath ->
                    val pageUrl = state.value.streamUrl + pageFilePath
                    val htmlContent = getHtmlData(pageUrl)
//                    val pageData = HtmlUtil.getHtmlContent(htmlContent,"tajawal",true,2)
                    emit(pageUrl to htmlContent)
                }
            }
        }
    }

    private suspend fun getHtmlData(urlString: String): String {
//        Log.e(FolioActivity.LOG_TAG, "Getting html data for $urlString")

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
                    stringBuilder.append(line).append('\n')
                }
//                Log.e(FolioActivity.LOG_TAG, "Html data for $urlString is:\n $stringBuilder")
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
        val url = String.format(
            Constants.STREAMER_URL_TEMPLATE,
            Constants.LOCALHOST,
            portNumber,
            bookFileName
        )
        return Uri.parse(url).toString()
    }
}