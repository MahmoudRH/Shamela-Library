package com.folioreader.model.event

/**
 * @author gautam chibde on 14/6/17.
 */
class MediaOverlaySpeedEvent(val speed: Speed) {
    enum class Speed {
        HALF, ONE, ONE_HALF, TWO
    }

}