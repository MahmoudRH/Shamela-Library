package com.folioreader.ui.base

import android.content.Context
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.R

/**
 * @author gautam chibde on 14/6/17.
 */
object HtmlUtil {
    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    fun getHtmlContent(context: Context, htmlContent: String/*, config: Config*/): String {
        var htmlContent = htmlContent
        val cssPath = String.format(
            context.getString(R.string.css_tag),
            "file:///android_asset/css/Style.css"
        )
        var jsPath = String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/jsface.min.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/jquery-3.4.1.min.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/rangy-core.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/rangy-highlighter.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/rangy-classapplier.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/rangy-serializer.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/Bridge.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/rangefix.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag),
            "file:///android_asset/js/readium-cfi.umd.js"
        ) + "\n"
        jsPath = jsPath + String.format(
            context.getString(R.string.script_tag_method_call),
            "setMediaOverlayStyleColors('#C0ED72','#C0ED72')"
        ) + "\n"
        jsPath = (jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />")
        val toInject = "\n$cssPath\n$jsPath\n</head>"
        htmlContent = htmlContent.replace("</head>", toInject)
        /*var classes = ""
        when (config.font) {
            Constants.FONT_ANDADA -> classes = "andada"
            Constants.FONT_LATO -> classes = "lato"
            Constants.FONT_LORA -> classes = "lora"
            Constants.FONT_RALEWAY -> classes = "raleway"
            else -> {}
        }
        if (config.isNightMode) {
            classes += " nightMode"
        }
        when (config.fontSize) {
            0 -> classes += " textSizeOne"
            1 -> classes += " textSizeTwo"
            2 -> classes += " textSizeThree"
            3 -> classes += " textSizeFour"
            4 -> classes += " textSizeFive"
            else -> {}

        htmlContent = htmlContent.replace(
            "<html", "<html class=\"" + classes + "\"" +
                    " onclick=\"onClickHtml()\""
        )}*/
        htmlContent = htmlContent.replace(
            "<html", "<html onclick=\"onClickHtml()\""
        )
        return htmlContent
    }
}