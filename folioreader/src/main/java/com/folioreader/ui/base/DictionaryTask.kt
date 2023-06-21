package com.folioreader.ui.base

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.folioreader.model.dictionary.Dictionary
import com.folioreader.network.TLSSocketFactory
import com.folioreader.util.AppUtil.Companion.charsetNameForURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @author gautam chibde on 4/7/17.
 */
class DictionaryTask(private val callBack: DictionaryCallBack) :
    AsyncTask<String?, Void?, Dictionary?>() {
    protected override fun doInBackground(vararg strings: String?): Dictionary? {
        val strUrl = strings[0]
        try {
            Log.v(TAG, "-> doInBackground -> url -> $strUrl")
            val url = URL(strUrl)
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            if (Build.VERSION.SDK_INT <= 20) httpsURLConnection.sslSocketFactory =
                TLSSocketFactory()
            val inputStream = httpsURLConnection.inputStream
            val bufferedReader = BufferedReader(
                InputStreamReader(
                    inputStream,
                    charsetNameForURLConnection(httpsURLConnection)
                )
            )
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            val objectMapper = ObjectMapper()
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            return objectMapper.readValue(stringBuilder.toString(), Dictionary::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "DictionaryTask failed", e)
        }
        return null
    }

    protected override fun onPostExecute(dictionary: Dictionary?) {
        super.onPostExecute(dictionary)
        if (dictionary != null) {
            callBack.onDictionaryDataReceived(dictionary)
        } else {
            callBack.onError()
        }
        cancel(true)
    }

    companion object {
        private const val TAG = "DictionaryTask"
    }
}