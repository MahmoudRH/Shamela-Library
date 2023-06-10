package com.shamela.library.presentaion.screens.download


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor():ViewModel() {
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState())
    val downloadState = _downloadState.asStateFlow()


    fun onEvent(event: DownloadEvent) {
      when (event) {
          is DownloadEvent.SampleEvent -> {
             _downloadState.update { it.copy(example = event.newText) }
           }
        }
   }

}