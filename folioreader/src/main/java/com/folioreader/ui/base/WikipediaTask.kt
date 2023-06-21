package com.folioreader.ui.base

import android.os.AsyncTask
import android.util.Log
import com.folioreader.model.dictionary.Wikipedia
import com.folioreader.util.AppUtil.Companion.charsetNameForURLConnection
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * @author gautam chibde on 4/7/17.
 */
class WikipediaTask(private val callBack: WikipediaCallBack) :
    AsyncTask<String?, Void?, Wikipedia?>() {
    protected override fun doInBackground(vararg strings: String?): Wikipedia? {
        val strUrl = strings[0]
        try {
            Log.v(TAG, "-> doInBackground -> url -> $strUrl")
            val url = URL(strUrl)
            val urlConnection = url.openConnection()
            val inputStream = urlConnection.getInputStream()
            val bufferedReader = BufferedReader(
                InputStreamReader(
                    inputStream,
                    charsetNameForURLConnection(urlConnection)
                )
            )
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            return try {
                val array = JSONArray(stringBuilder.toString())
                if (array.length() == 4) {
                    try {
                        val wikipedia = Wikipedia()
                        wikipedia.word = array[0].toString()
                        val defs = array[2] as JSONArray
                        wikipedia.definition = defs[0].toString()
                        val links = array[3] as JSONArray
                        wikipedia.link = links[0].toString()
                        wikipedia
                    } catch (e: Exception) {
                        Log.e(TAG, "WikipediaTask failed", e)
                        null
                    }
                } else {
                    null
                }
            } catch (e: JSONException) {
                Log.e(TAG, "WikipediaTask failed", e)
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "WikipediaTask failed", e)
        }
        return null
    }

    protected override fun onPostExecute(wikipedia: Wikipedia?) {
        super.onPostExecute(wikipedia)
        if (wikipedia != null) {
            callBack.onWikipediaDataReceived(wikipedia)
        } else {
            callBack.onError()
        }
        cancel(true)
    }

    companion object {
        private const val TAG = "WikipediaTask"
    }
}