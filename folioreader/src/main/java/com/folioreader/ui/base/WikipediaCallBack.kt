package com.folioreader.ui.base

import com.folioreader.model.dictionary.Wikipedia

/**
 * @author gautam chibde on 4/7/17.
 */
interface WikipediaCallBack : BaseMvpView {
    fun onWikipediaDataReceived(wikipedia: Wikipedia)

    //TODO
    fun playMedia(url: String?)
}