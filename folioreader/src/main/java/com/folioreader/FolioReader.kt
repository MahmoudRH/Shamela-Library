package com.folioreader

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.model.HighLight
import com.folioreader.model.HighLight.HighLightAction
import com.folioreader.model.HighlightImpl
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.sqlite.DbAdapter
import com.folioreader.network.QualifiedTypeConverterFactory
import com.folioreader.network.R2StreamerApi
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.base.OnSaveHighlight
import com.folioreader.ui.base.SaveReceivedHighlightTask
import com.folioreader.util.OnHighlightListener
import com.folioreader.util.ReadLocatorListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.readium.r2.streamer.parser.EpubParser
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by avez raj on 9/13/2017.
 */
class FolioReader private constructor(private var context: Context) {
    private var config: Config? = null
    private var overrideConfig = false
    private var portNumber = Constants.DEFAULT_PORT_NUMBER
    private var onHighlightListener: OnHighlightListener? = null
    private var readLocatorListener: ReadLocatorListener? = null
    private var onClosedListener: OnClosedListener? = null
    private var readLocator: ReadLocator? = null
    var retrofit: Retrofit? = null
    var r2StreamerApi: R2StreamerApi? = null

    interface OnClosedListener {
        /**
         * You may call [FolioReader.clear] in this method, if you wouldn't require to open
         * an epub again from the current activity.
         * Or you may call [FolioReader.stop] in this method, if you wouldn't require to open
         * an epub again from your application.
         */
        fun onFolioReaderClosed()
    }

    private val highlightReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= 33) {
                val highlightImpl =
                    intent.getParcelableExtra(
                        HighlightImpl.Companion.INTENT,
                        HighlightImpl::class.java
                    )
                val action =
                    intent.getSerializableExtra(
                        HighLightAction::class.java.name,
                        HighLightAction::class.java
                    )
                if (onHighlightListener != null && highlightImpl != null && action != null) {
                    onHighlightListener!!.onHighlight(highlightImpl, action)
                }
            } else {
                val highlightImpl =
                    intent.getParcelableExtra<HighlightImpl>(HighlightImpl.Companion.INTENT)
                val action =
                    intent.getSerializableExtra(HighLightAction::class.java.name) as HighLightAction?
                if (onHighlightListener != null && highlightImpl != null && action != null) {
                    onHighlightListener!!.onHighlight(highlightImpl, action)
                }
            }
        }
    }

    private val readLocatorReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val readLocator = intent.getSerializableExtra(EXTRA_READ_LOCATOR) as ReadLocator?
            if (readLocatorListener != null) readLocator?.let {
                readLocatorListener!!.saveReadLocator(
                    it
                )
            }
        }
    }
    private val closedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (onClosedListener != null) onClosedListener!!.onFolioReaderClosed()
        }
    }

    init {
        DbAdapter.Companion.initialize(context)
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager.registerReceiver(
            highlightReceiver,
            IntentFilter(HighlightImpl.Companion.BROADCAST_EVENT)
        )
        localBroadcastManager.registerReceiver(
            readLocatorReceiver,
            IntentFilter(ACTION_SAVE_READ_LOCATOR)
        )
        localBroadcastManager.registerReceiver(
            closedReceiver,
            IntentFilter(ACTION_FOLIOREADER_CLOSED)
        )
    }

    /** @return : pair of author-name and pageCount */
    suspend fun parseEpub(file: File): Pair<String, Int>? {
        return withContext(Dispatchers.IO) {
            val bookTitle = file.name.removeSuffix(".epub")
            EpubParser().parse(file.path)?.let {
                val publication = it.publication
                val authorName = publication.metadata.authors.first().name ?: "-"
                val pageCount = publication.readingOrder.size
                authorName to pageCount
            }
        }
    }

    fun openBook(assetOrSdcardPath: String?): FolioReader? {
        val intent = getIntentFromUrl(assetOrSdcardPath, 0)
        context.startActivity(intent)
        return singleton
    }

    fun openBook(rawId: Int): FolioReader? {
        val intent = getIntentFromUrl(null, rawId)
        context.startActivity(intent)
        return singleton
    }

    fun openBook(assetOrSdcardPath: String?, bookId: String?): FolioReader? {
        val intent = getIntentFromUrl(assetOrSdcardPath, 0)
        intent.putExtra(EXTRA_BOOK_ID, bookId)
        context.startActivity(intent)
        return singleton
    }

    fun openBook(rawId: Int, bookId: String?): FolioReader? {
        val intent = getIntentFromUrl(null, rawId)
        intent.putExtra(EXTRA_BOOK_ID, bookId)
        context.startActivity(intent)
        return singleton
    }

    private fun getIntentFromUrl(assetOrSdcardPath: String?, rawId: Int): Intent {
        val intent = Intent(context, FolioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra(Config.INTENT_CONFIG, config)
        intent.putExtra(Config.EXTRA_OVERRIDE_CONFIG, overrideConfig)
        intent.putExtra(EXTRA_PORT_NUMBER, portNumber)
        intent.putExtra(FolioActivity.EXTRA_READ_LOCATOR, readLocator as Parcelable?)
        intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, assetOrSdcardPath)
        return intent
    }

    /**
     * Pass your configuration and choose to override it every time or just for first execution.
     *
     * @param config         custom configuration.
     * @param overrideConfig true will override the config, false will use either this
     * config if it is null in application context or will fetch previously
     * saved one while execution.
     */
    fun setConfig(config: Config?, overrideConfig: Boolean): FolioReader? {
        this.config = config
        this.overrideConfig = overrideConfig
        return singleton
    }

    fun setPortNumber(portNumber: Int): FolioReader? {
        this.portNumber = portNumber
        return singleton
    }

    fun setOnHighlightListener(onHighlightListener: OnHighlightListener?): FolioReader {
        this.onHighlightListener = onHighlightListener
        return singleton!!
    }

    fun setReadLocatorListener(readLocatorListener: ReadLocatorListener?): FolioReader {
        this.readLocatorListener = readLocatorListener
        return singleton!!
    }

    fun setOnClosedListener(onClosedListener: OnClosedListener?): FolioReader {
        this.onClosedListener = onClosedListener
        return singleton!!
    }

    fun setReadLocator(readLocator: ReadLocator?): FolioReader {
        this.readLocator = readLocator
        return singleton!!
    }

    fun saveReceivedHighLights(
        highlights: List<HighLight>,
        onSaveHighlight: OnSaveHighlight,
    ) {
        SaveReceivedHighlightTask(onSaveHighlight, highlights).execute()
    }

    /**
     * Closes all the activities related to FolioReader.
     * After closing all the activities of FolioReader, callback can be received in
     * [OnClosedListener.onFolioReaderClosed] if implemented.
     * Developer is still bound to call [.clear] or [.stop]
     * for clean up if required.
     */
    fun close() {
        val intent = Intent(ACTION_CLOSE_FOLIOREADER)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun unregisterListeners() {
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager.unregisterReceiver(highlightReceiver)
        localBroadcastManager.unregisterReceiver(readLocatorReceiver)
        localBroadcastManager.unregisterReceiver(closedReceiver)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var singleton: FolioReader? = null
        const val EXTRA_BOOK_ID = "com.folioreader.extra.BOOK_ID"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        const val EXTRA_PORT_NUMBER = "com.folioreader.extra.PORT_NUMBER"
        const val ACTION_SAVE_READ_LOCATOR = "com.folioreader.action.SAVE_READ_LOCATOR"
        const val ACTION_CLOSE_FOLIOREADER = "com.folioreader.action.CLOSE_FOLIOREADER"
        const val ACTION_FOLIOREADER_CLOSED = "com.folioreader.action.FOLIOREADER_CLOSED"
        fun get(): FolioReader {
            if (singleton == null) {
                synchronized(FolioReader::class.java) {
                    if (singleton == null) {
                        checkNotNull(AppContext.get()) { "-> context == null" }
                        AppContext.get()?.let {
                            singleton = FolioReader(it)
                        }
                    }
                }
            }
            return singleton!!
        }

        fun initRetrofit(streamerUrl: String?) {
            if (singleton == null || singleton!!.retrofit != null) return
            val client = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build()
            singleton!!.retrofit = Retrofit.Builder()
                .baseUrl(streamerUrl)
                .addConverterFactory(
                    QualifiedTypeConverterFactory(
                        JacksonConverterFactory.create(),
                        GsonConverterFactory.create()
                    )
                )
                .client(client)
                .build()
            singleton!!.r2StreamerApi = singleton!!.retrofit?.create(
                R2StreamerApi::class.java
            )
        }

        /**
         * Nullifies readLocator and listeners.
         * This method ideally should be used in onDestroy() of Activity or Fragment.
         * Use this method if you want to use FolioReader singleton instance again in the application,
         * else use [.stop] which destruct the FolioReader singleton instance.
         */
        @Synchronized
        fun clear() {
            if (singleton != null) {
                singleton!!.readLocator = null
                singleton!!.onHighlightListener = null
                singleton!!.readLocatorListener = null
                singleton!!.onClosedListener = null
            }
        }

        /**
         * Destructs the FolioReader singleton instance.
         * Use this method only if you are sure that you won't need to use
         * FolioReader singleton instance again in application, else use [.clear].
         */
        @Synchronized
        fun stop() {
            if (singleton != null) {
                DbAdapter.Companion.terminate()
                singleton!!.unregisterListeners()
                singleton = null
            }
        }
    }
}