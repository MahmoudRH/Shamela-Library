package com.folioreader.ui.activity

import android.graphics.Rect
import com.folioreader.Config
import com.folioreader.model.DisplayUnit
import com.folioreader.model.locators.ReadLocator
import java.lang.ref.WeakReference

interface FolioActivityCallback {
    val currentChapterIndex: Int
    val entryReadLocator: ReadLocator
    fun goToChapter(href: String): Boolean
    val direction: Config.Direction
    fun onDirectionChange(newDirection: Config.Direction)
    fun storeLastReadLocator(lastReadLocator: ReadLocator)
    fun toggleSystemUI()
    fun setDayMode()
    fun setNightMode()
    fun getTopDistraction(unit: DisplayUnit): Int
    fun getBottomDistraction(unit: DisplayUnit): Int
    fun getViewportRect(unit: DisplayUnit): Rect
    val activity: WeakReference<FolioActivity>
    val streamerUrl: String
}