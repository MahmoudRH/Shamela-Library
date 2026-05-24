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
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BookViewModel : ViewModel() {
    private val _state = MutableStateFlow(BookState())
    val state = _state.asStateFlow()
    private val cachedPages = mutableMapOf<Int, Pair<String, String>>()
//    val cachedWebViews = mutableStateMapOf<Int, WebView>()


    fun onEvent(event: BookEvent) {
        when (event) {
            is BookEvent.OnChangeSelectedPage -> {
                clearOldWebViews(event.pageIndex)
                onEvent(BookEvent.OnCurrentPageTextChanged(event.pageIndex.toString()))
                fetchPage(event)
            }

            is BookEvent.OnCurrentPageTextChanged -> _state.update {
                it.copy(currentPageText = event.newPage)
            }

            BookEvent.ToggleAppBarsVisibility -> _state.update {
                it.copy(isAppBarsVisible = !it.isAppBarsVisible)
            }

            BookEvent.ToggleMenuVisibility -> _state.update {
                it.copy(isMenuVisible = !it.isMenuVisible)
            }

            BookEvent.DismissMenu -> _state.update {
                it.copy(isMenuVisible = false)
            }

            BookEvent.ClearCachedPages -> {
                cachedPages.clear()
//                cachedWebViews.clear()
                _state.update { it.copy(pagesMap = emptyMap()) }
            }
        }
    }
    private fun clearOldWebViews(currentIndex: Int) {
        val maxCacheDistance = 5
//        cachedWebViews.keys.toList().forEach { key ->
//            if (abs(key - currentIndex) > maxCacheDistance) cachedWebViews.remove(key)
//        }
    }

    private fun fetchPage(event: BookEvent.OnChangeSelectedPage) {
        viewModelScope.launch {
            getBookPages(
                context = event.context,
                pageIndex = event.pageIndex,
                totalPages = event.publication.readingOrder.lastIndex,
                fontFamily = event.fontFamilyCssClass,
                publication = event.publication,
                isNightMode = event.isNightMode,
                fontSize = event.fontSizeCssClass,
                streamUrl = event.streamUrl
            ).collect { pageMap ->
                _state.update {
                    it.copy(
                        pagesMap = it.pagesMap + pageMap,
                        isLoading = false
                    )
                }
            }
        }
    }


    private fun getBookPages(
        context: Context,
        pageIndex: Int,
        totalPages: Int,
        fontFamily: String,
        publication: Publication,
        isNightMode: Boolean,
        fontSize: String,
        streamUrl: String
    ) = flow {
        val range = (maxOf(0, pageIndex - 5))..(minOf(pageIndex + 5, totalPages))
        for (page in range) {
            val cachedPage = cachedPages[page]
            if (cachedPage != null){
                emit(mapOf(page to cachedPage))
                continue
            }
            val href = publication.readingOrder[page].href?.removePrefix("/") ?: continue
            val pageUrl = "$streamUrl$href"
            val html = getHtmlData(pageUrl)
            val styledHtml = HtmlUtil.getHtmlContent(context, html, fontFamily, isNightMode, fontSize)
            val data = Pair(pageUrl, styledHtml)
            cachedPages[page] = data
            emit(mapOf(page to data))
        }
    }

    private suspend fun getHtmlData(urlString: String): String = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            BufferedReader(InputStreamReader(connection.inputStream, AppUtil.charsetNameForURLConnection(connection))).use { reader ->
                buildString {
                    reader.lineSequence().forEach { appendLine(it) }
                }
            }
        }.onFailure {
            Log.e(FolioActivity.LOG_TAG, "HtmlTask failed", it)
        }.getOrDefault("")
    }
}