package com.folioreader.ui.activity.folioActivity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.folioreader.Constants
import com.folioreader.FolioReader
import com.folioreader.util.AppUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.server.Server


class FolioActivityViewModel : ViewModel() {
    private val TAG = "FolioActivityViewModel"
    private val _state = MutableStateFlow<FolioActivityState>(FolioActivityState())
    val state: StateFlow<FolioActivityState> = _state.asStateFlow()
    private val _searchResult = MutableStateFlow("" to "")
    val searchResult: StateFlow<Pair<String, String>> = _searchResult.asStateFlow()
    private val _selectedChapter = MutableStateFlow("")
    val selectedChapter: StateFlow<String> = _selectedChapter.asStateFlow()
    private val _settingsChanged = MutableStateFlow(0)
    val settingsChanged: StateFlow<Int> = _settingsChanged.asStateFlow()
    private var server = Server(Constants.DEFAULT_PORT_NUMBER)
    var streamUrl = ""
    fun onEvent(event: FolioActivityEvent) {
        when (event) {
            is FolioActivityEvent.OnChangeTaskImportance -> {
                _state.update { it.copy(taskImportance = event.taskImportance) }
            }

            is FolioActivityEvent.OnChangeTopActivity -> {
                _state.update { it.copy(isTopActivity = event.isTopActivity) }
            }

            is FolioActivityEvent.InitializeBook -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val publication = initBook(event.filePath)
                    _state.update { it.copy(publication = publication, isLoading = false) }
                }
            }

            is FolioActivityEvent.OnSearchResult -> {
                _searchResult.update { event.href to event.jsCall }
            }

            is FolioActivityEvent.OnSelectedChapter -> {
                _selectedChapter.update { event.href }
            }

            is FolioActivityEvent.OnSettingsChanged -> {
                _settingsChanged.update { event.hash }
            }

            FolioActivityEvent.StopStreamerServer -> server.stop()
        }
    }

    private suspend fun initBook(
        filePath: String,
    ): Publication? {
        Log.v(TAG, "-> initBook")
        return try{
            withContext(Dispatchers.IO) {
                EpubParser().parse(filePath, "")?.let {
                    val publication = it.publication
                    val portNumber = AppUtil.getAvailablePortNumber(Constants.DEFAULT_PORT_NUMBER)
                    try {
                        server.stop()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping old server", e)
                    }
                    server = Server(portNumber)
                    server.addEpub(
                        it.publication,
                        it.container,
                        "/${publication.metadata.title.hashCode()}",
                        null
                    )
                    server.start()
                    streamUrl = AppUtil.getStreamerUrl(
                        publication.metadata.title.hashCode().toString(),
                        portNumber
                    )
                    FolioReader.initRetrofit(streamUrl)
                    Log.v(TAG, "initBook [streamUrl]: $streamUrl, ${server.isAlive}")
                    publication
                }
            }
        }catch (e: Exception){
            Log.e(TAG, "initBook: ${e.message}", )
            null
        }    }

    override fun onCleared() {
        super.onCleared()
        try {
            server.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server on cleared", e)
        }
    }
}