package com.folioreader.mediaoverlay

/**
 * @author gautam chibde on 21/6/17.
 */
interface MediaControllerCallbacks {
    fun highLightText(text: String?)
    fun highLightTTS()
    fun resetCurrentIndex()
}