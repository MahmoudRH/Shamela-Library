package com.shamela.library.presentation.screens.about

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.shamela.library.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class AboutAppViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AboutAppState())
    val state = _state.asStateFlow()

    private var apkDownloadUrl: String? = null

    init {
        _state.update { it.copy(currentVersion = BuildConfig.VERSION_NAME) }
        onEvent(AboutAppEvent.FetchLatestRelease)
    }

    fun onEvent(event: AboutAppEvent) {
        when (event) {
            is AboutAppEvent.FetchLatestRelease -> fetchLatestRelease()
            is AboutAppEvent.DownloadAndInstall -> downloadAndInstall()
        }
    }

    private fun fetchLatestRelease() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLatestVersion = true, error = null) }
            try {
                val json = withContext(Dispatchers.IO) {
                    val url = URL("https://api.github.com/repos/MahmoudRH/Shamela-Library/releases/latest")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Accept", "application/vnd.github+json")
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    val text = connection.inputStream.bufferedReader().readText()
                    connection.disconnect()
                    text
                }
                val release = Gson().fromJson(json, GitHubRelease::class.java)
                apkDownloadUrl = release.assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl
                val version = release.tagName.removePrefix("v").trim()
                _state.update {
                    it.copy(
                        latestVersion = version,
                        releaseNotes = release.body.orEmpty(),
                        isLoadingLatestVersion = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingLatestVersion = false, error = "تعذّر التحقق من التحديثات") }
            }
        }
    }

    private fun downloadAndInstall() {
        val url = apkDownloadUrl ?: return
        _state.update { it.copy(isDownloading = true, error = null, downloadProgress = 0) }
        val context = getApplication<Application>()
        val fileName = "shamela-update.apk"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("تحديث مكتبة الشاملة")
            .setDescription("جارٍ تحميل التحديث…")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setMimeType("application/vnd.android.package-archive")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        viewModelScope.launch(Dispatchers.IO) {
            var downloading = true
            while (downloading) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                cursor.use {
                    if (it.moveToFirst()) {
                        val statusCol = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val downloadedCol = it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalCol = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        val status = if (statusCol >= 0) it.getInt(statusCol) else -1
                        val downloaded = if (downloadedCol >= 0) it.getLong(downloadedCol) else 0L
                        val total = if (totalCol >= 0) it.getLong(totalCol) else 1L
                        val progress = if (total > 0) ((downloaded * 100) / total).toInt() else 0

                        _state.update { s -> s.copy(downloadProgress = progress) }

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloading = false
                                _state.update { s -> s.copy(isDownloading = false, downloadProgress = 100) }
                                withContext(Dispatchers.Main) { installApk(context, fileName) }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                downloading = false
                                _state.update { s ->
                                    s.copy(isDownloading = false, error = "فشل التحميل، يرجى المحاولة مرة أخرى")
                                }
                            }
                            else -> delay(500)
                        }
                    } else {
                        downloading = false
                    }
                }
            }
        }
    }

    private fun installApk(context: Context, fileName: String) {
        val apkFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("body") val body: String?,
    @SerializedName("assets") val assets: List<GitHubAsset>,
)

private data class GitHubAsset(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val browserDownloadUrl: String,
)
