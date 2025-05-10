package com.folioreader.ui.activity.folioActivity.book

import android.content.Context
import org.readium.r2.shared.Publication


sealed class BookEvent {
    class OnChangeSelectedPage(
        val pageIndex: Int,
        val fontSizeCssClass: String,
        val fontFamilyCssClass: String,
        val isNightMode: Boolean,
        val context: Context,
        val publication: Publication,
        val streamUrl:String
    ) : BookEvent()

    class OnCurrentPageTextChanged(val newPage: String) : BookEvent()

    object ToggleAppBarsVisibility : BookEvent()
    object ToggleMenuVisibility : BookEvent()
    object DismissMenu : BookEvent()
    object ClearCachedPages : BookEvent()
}