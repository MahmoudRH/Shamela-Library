package com.folioreader.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.folioreader.R

/**
 * Created by mobisys on 7/4/2016.
 */
class UnderlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private var mRect: Rect? = null
    private var mPaint: Paint? = null
    private var mColor = 0
    private var mDensity = 0f
    private var mStrokeWidth = 0f

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attributeSet: AttributeSet?, defStyle: Int) {
        mDensity = context.resources.displayMetrics.density
        val typedArray = context.obtainStyledAttributes(
            attributeSet, R.styleable.UnderlinedTextView,
            defStyle, 0
        )
        mStrokeWidth = typedArray.getDimension(
            R.styleable.UnderlinedTextView_underlineWidth,
            mDensity * 2
        )
        typedArray.recycle()
        mRect = Rect()
        mPaint = Paint()
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.color = mColor //line mColor
        mPaint!!.strokeWidth = mStrokeWidth
    }

    //line mColor
    var underLineColor: Int
        get() = mColor
        set(mColor) {
            this.mColor = mColor
            mRect = Rect()
            mPaint = Paint()
            mPaint!!.style = Paint.Style.STROKE
            mPaint!!.color = mColor //line mColor
            mPaint!!.strokeWidth = mStrokeWidth
            postInvalidate()
        }
    var underlineWidth: Float
        get() = mStrokeWidth
        set(mStrokeWidth) {
            this.mStrokeWidth = mStrokeWidth
            postInvalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val count = lineCount
        val layout = layout
        var xStart: Float
        var xStop: Float
        var xDiff: Float
        var firstCharInLine: Int
        var lastCharInLine: Int
        for (i in 0 until count) {
            val baseline = getLineBounds(i, mRect)
            firstCharInLine = layout.getLineStart(i)
            lastCharInLine = layout.getLineEnd(i)
            xStart = layout.getPrimaryHorizontal(firstCharInLine)
            xDiff = layout.getPrimaryHorizontal(firstCharInLine + 1) - xStart
            xStop = layout.getPrimaryHorizontal(lastCharInLine - 1) + xDiff
            canvas.drawLine(
                xStart,
                baseline + mStrokeWidth,
                xStop,
                baseline + mStrokeWidth,
                mPaint!!
            )
        }
        super.onDraw(canvas)
    }
}