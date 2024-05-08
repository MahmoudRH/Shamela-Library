package com.folioreader.util

import com.folioreader.model.HighLight
import com.folioreader.model.HighLight.HighLightAction
import com.folioreader.model.HighlightImpl

/**
 * Interface to convey highlight events.
 *
 * @author gautam chibde on 26/9/17.
 */
interface OnHighlightListener {
    /**
     * This method will be invoked when a highlight is created, deleted or modified.
     *
     * @param highlight meta-data for created highlight [HighlightImpl].
     * @param type      type of event e.g new,edit or delete [com.folioreader.model.HighlightImpl.HighLightAction].
     */
    fun onHighlight(highlight: HighLight, type: HighLightAction)
}