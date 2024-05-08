package com.folioreader.ui.base

import android.content.Context
import com.folioreader.R

/**
 * @author gautam chibde on 14/6/17.
 */
object HtmlUtil {
    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param content input html raw data
     * @return modified raw html string
     */
    fun getHtmlContent(
        context: Context,
        content: String,
        fontFamilyCssClass: String,
        isNightMode: Boolean,
        fontSizeCssClass: String,
    ): String {
//        Log.e("HtmlUtil", "getHtmlContent: for content:($content),\n font:($font),\n isNightMode:($isNightMode),\n  fontSize:($fontSize)\n", )
        val cssPath = String.format(
            context.getString(R.string.css_tag), "file:///android_asset/css/Style.css"
        )
        val jsPaths = listOf(
            "file:///android_asset/js/jsface.min.js",
            "file:///android_asset/js/jquery-3.4.1.min.js",
            "file:///android_asset/js/rangy-core.js",
            "file:///android_asset/js/rangy-highlighter.js",
            "file:///android_asset/js/rangy-classapplier.js",
            "file:///android_asset/js/rangy-serializer.js",
            "file:///android_asset/js/Bridge.js",
            "file:///android_asset/js/rangefix.js",
            "file:///android_asset/js/readium-cfi.umd.js"
        )

        val scriptTags = jsPaths.joinToString(separator = "\n") { path ->
            String.format(context.getString(R.string.script_tag), path)
        }

        val methodCallScriptTag = String.format(
            context.getString(R.string.script_tag_method_call),
            "setMediaOverlayStyleColors('#C0ED72','#C0ED72')"
        )

        val metaTag =
            "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />"

        val toInject = """
    |$cssPath
    |$scriptTags
    |$methodCallScriptTag
    |$metaTag
    |</head>
""".trimMargin()

        val nightModeCssClass = if (isNightMode) "nightMode" else ""
        val cssClasses = listOf(fontFamilyCssClass, fontSizeCssClass, nightModeCssClass)
            .joinToString(separator =" ")

        return StringBuilder(content).replaceFirst(
            "<html".toRegex(),
            "<html class=\"$cssClasses\" onclick=\"onClickHtml()\""
        ).replace("</head>", toInject)
    }
}