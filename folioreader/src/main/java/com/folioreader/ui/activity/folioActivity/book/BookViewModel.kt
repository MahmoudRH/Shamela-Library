package com.folioreader.ui.activity.folioActivity.book


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.ui.activity.folioActivity.FolioActivity
import com.folioreader.ui.base.HtmlUtil
import com.folioreader.util.AppUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Publication
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BookViewModel : ViewModel() {
    private val _state = MutableStateFlow<BookState>(BookState())
    val state = _state.asStateFlow()
    private val TAG = "BookViewModel"
    fun onEvent(event: BookEvent) {
        when (event) {
            is BookEvent.OnChangeSelectedPage -> {
                Log.e(TAG, "onEvent: ${event.javaClass.simpleName}, page: ${event.pageIndex}")
                onEvent(BookEvent.OnCurrentPageTextChanged(event.pageIndex.toString()))
                viewModelScope.launch {
                    getBookPages(
                        context = event.context,
                        fontFamily = event.fontFamilyCssClass,
                        isNightMode = event.isNightMode,
                        fontSize = event.fontSizeCssClass,
                        pageIndex = event.pageIndex,
                        publication = event.publication,
                        totalPages = event.publication.readingOrder.lastIndex,
                        streamUrl = event.streamUrl
                    ).collect { map ->
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

            is BookEvent.OnCurrentPageTextChanged -> _state.update { it.copy(currentPageText = event.newPage) }
            BookEvent.ToggleAppBarsVisibility -> _state.update { it.copy(isAppBarsVisible = !it.isAppBarsVisible) }
            BookEvent.ToggleMenuVisibility -> _state.update { it.copy(isMenuVisible = !it.isMenuVisible) }
            BookEvent.DismissMenu -> _state.update { it.copy(isMenuVisible = false) }
        }
    }

    private suspend fun getBookPages(
        context: Context,
        pageIndex: Int,
        totalPages: Int,
        fontFamily: String,
        publication: Publication,
        isNightMode: Boolean,
        fontSize: String,
        streamUrl:String
    ) = flow<Map<Int, Pair<String, String>>> {
        val range = (maxOf(0, pageIndex - 2))..(minOf(pageIndex + 2, totalPages))
//        val streamUrl = AppUtil.getStreamerUrl(publication.metadata.title)

        range.forEach { page ->
            publication.readingOrder[page].let { link ->
                link.href?.substring(1)?.let { pageFilePath ->
                    val pageUrl = streamUrl + pageFilePath
                    Log.d(TAG, "getBookPages() pageUrl returned: $pageUrl")
                    val htmlContent = getHtmlData(pageUrl)
                    val pageData = HtmlUtil.getHtmlContent(
                        context = context,
                        content = htmlContent,
                        fontFamilyCssClass = fontFamily,
                        isNightMode = isNightMode,
                        fontSizeCssClass = fontSize
                    )
                    emit(mapOf(page to Pair(pageUrl, pageData)))
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

}