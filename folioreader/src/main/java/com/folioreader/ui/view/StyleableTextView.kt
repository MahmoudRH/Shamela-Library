package com.folioreader.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.folioreader.R
import com.folioreader.util.UiUtil

class StyleableTextView : AppCompatTextView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        UiUtil.setCustomFont(
            this, context, attrs,
            R.styleable.StyleableTextView,
            R.styleable.StyleableTextView_folio_font
        )
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        UiUtil.setCustomFont(
            this, context, attrs,
            R.styleable.StyleableTextView,
            R.styleable.StyleableTextView_folio_font
        )
    }
}