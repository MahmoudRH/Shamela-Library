package com.folioreader.model.event

/**
 * Created by Shrikant on 7/28/2017.
 */
class AnchorIdEvent {
    var href: String? = null
        private set

    constructor() {}
    constructor(href: String?) {
        this.href = href
    }
}