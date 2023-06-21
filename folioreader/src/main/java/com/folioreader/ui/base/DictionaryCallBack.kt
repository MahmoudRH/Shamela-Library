package com.folioreader.ui.base

import com.folioreader.model.dictionary.Dictionary

/**
 * @author gautam chibde on 4/7/17.
 */
interface DictionaryCallBack : BaseMvpView {
    fun onDictionaryDataReceived(dictionary: Dictionary)

    //TODO
    fun playMedia(url: String?)
}