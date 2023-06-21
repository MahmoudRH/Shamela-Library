package com.folioreader.ui.base

import android.os.AsyncTask
import com.folioreader.model.HighLight
import com.folioreader.model.sqlite.HighLightTable

/**
 * Background task to save received highlights.
 *
 *
 * Created by gautam on 10/10/17.
 */
class SaveReceivedHighlightTask(
    private val onSaveHighlight: OnSaveHighlight,
    private val highLights: List<HighLight>
) : AsyncTask<Void?, Void?, Void?>() {
    protected override fun doInBackground(vararg voids: Void?): Void? {
        for (highLight in highLights) {
            HighLightTable.saveHighlightIfNotExists(highLight)
        }
        return null
    }

    protected override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
        onSaveHighlight.onFinished()
    }
}