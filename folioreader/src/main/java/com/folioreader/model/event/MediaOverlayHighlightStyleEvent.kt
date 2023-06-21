package com.folioreader.model.event

/**
 * @author gautam chibde on 14/6/17.
 */
class MediaOverlayHighlightStyleEvent(val style: Style) {
    enum class Style {
        UNDERLINE, BACKGROUND, DEFAULT
    }

}