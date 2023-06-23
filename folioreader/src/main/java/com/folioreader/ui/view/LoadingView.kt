package com.folioreader.ui.view

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.webkit.JavascriptInterface
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil

class LoadingView : ConstraintLayout {
    private var progressBar: ProgressBar? = null
    var maxVisibleDuration = -1
    private var handler: Handler? = null
    private val hideRunnable = Runnable { hide() }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_loading, this)
        if (isInEditMode) return
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingView,
            0, 0
        )
        maxVisibleDuration = typedArray.getInt(R.styleable.LoadingView_maxVisibleDuration, -1)
        handler = Handler()
        progressBar = findViewById(R.id.progressBar)
        isClickable = true
        isFocusable = true
        updateTheme()
        if (visibility == VISIBLE) show()
    }

    fun updateTheme() {
        var config = AppUtil.getSavedConfig(context)
        if (config == null) config = Config()
        UiUtil.setColorIntToDrawable(config.themeColor, progressBar!!.indeterminateDrawable)
        if (config.isNightMode) {
            setBackgroundColor(ContextCompat.getColor(context, R.color.night_background_color))
        } else {
            setBackgroundColor(ContextCompat.getColor(context, R.color.day_background_color))
        }
    }

    @JavascriptInterface
    fun show() {
        //Log.d(LOG_TAG, "-> show");
        handler!!.removeCallbacks(hideRunnable)
        handler!!.post { visibility = VISIBLE }
        if (maxVisibleDuration > -1) handler!!.postDelayed(
            hideRunnable,
            maxVisibleDuration.toLong()
        )
    }

    @JavascriptInterface
    fun hide() {
        //Log.d(LOG_TAG, "-> hide");
        handler!!.removeCallbacks(hideRunnable)
        handler!!.post { visibility = INVISIBLE }
    }

    @JavascriptInterface
    fun visible() {
        //Log.d(LOG_TAG, "-> visible");
        handler!!.post { visibility = VISIBLE }
    }

    @JavascriptInterface
    fun invisible() {
        //Log.d(LOG_TAG, "-> invisible");
        handler!!.post { visibility = INVISIBLE }
    }

    companion object {
        private val LOG_TAG = LoadingView::class.java.simpleName
    }
}