package com.folioreader.util

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.folioreader.R

class StyleableTextView : AppCompatTextView {
    constructor(context: Context, font: String) : super(context) {
        setCustomFont(context, font)
    }

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

    private fun setCustomFont(context: Context, font: String) {
        UiUtil.setCustomFont(this, context, font)
    }
}