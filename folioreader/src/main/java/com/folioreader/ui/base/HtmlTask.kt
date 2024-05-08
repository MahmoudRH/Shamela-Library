package com.folioreader.ui.base

import android.os.AsyncTask
import android.util.Log
import com.folioreader.util.AppUtil
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

/**
 * Background async task which downloads the html content of a web page
 * from server
 *
 * @author by gautam on 12/6/17.
 */
class HtmlTask(private val callback: HtmlTaskCallback) : AsyncTask<String?, Void?, String?>() {
    protected override fun doInBackground(vararg urls: String?): String? {
        val strUrl = urls[0]
        try {
            val url = URL(strUrl)
            val urlConnection = url.openConnection()
            val inputStream = urlConnection.getInputStream()
            val bufferedReader = BufferedReader(
                InputStreamReader(
                    inputStream,
                    AppUtil.charsetNameForURLConnection(urlConnection)
                )
            )
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append('\n')
            }
            if (stringBuilder.isNotEmpty()) stringBuilder.deleteCharAt(stringBuilder.length - 1)
            return stringBuilder.toString()
        } catch (e: IOException) {
            Log.e(TAG, "HtmlTask failed", e)
        }
        return null
    }

    protected override fun onPostExecute(htmlString: String?) {
        if (htmlString != null) {
            callback.onReceiveHtml(htmlString)
        } else {
            callback.onError()
        }
        cancel(true)
    }

    companion object {
        private const val TAG = "HtmlTask"
    }
}