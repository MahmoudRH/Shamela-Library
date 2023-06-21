package com.folioreader.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created by arthur on 06/10/16.
 */
class ScreenUtils(private val ctx: Context) {
    private val metrics: DisplayMetrics

    init {
        val wm = ctx
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        metrics = DisplayMetrics()
        display.getMetrics(metrics)
    }

    val height: Int
        get() = metrics.heightPixels
    val width: Int
        get() = metrics.widthPixels
    val realHeight: Int
        get() = metrics.heightPixels / metrics.densityDpi
    val realWidth: Int
        get() = metrics.widthPixels / metrics.densityDpi
    val density: Int
        get() = metrics.densityDpi

    fun getScale(picWidth: Int): Int {
        val display = (ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay
        val width = display.width
        var `val` = (width / picWidth).toDouble()
        `val` = `val` * 100.0
        return `val`.toInt()
    }
}