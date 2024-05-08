package com.folioreader.ui.base

/**
 * @author gautam chibde on 12/6/17.
 */
interface HtmlTaskCallback : BaseMvpView {
    fun onReceiveHtml(html: String?)
}