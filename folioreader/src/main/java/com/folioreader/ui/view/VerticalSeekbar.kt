package com.folioreader.ui.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * Created by priyank on 4/19/16.
 */
class VerticalSeekbar : AppCompatSeekBar {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(90f)
        c.translate(0f, -width.toFloat())
        super.onDraw(c)
    }

    private var mOnChangeListener: OnSeekBarChangeListener? = null
    override fun setOnSeekBarChangeListener(mOnChangeListener: OnSeekBarChangeListener) {
        this.mOnChangeListener = mOnChangeListener
    }

    private var mLastProgress = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mOnChangeListener != null) mOnChangeListener!!.onStartTrackingTouch(this)
                isPressed = true
                isSelected = true
            }

            MotionEvent.ACTION_MOVE -> {
                super.onTouchEvent(event)
                var progress = max - (max * event.y / height).toInt()

                // Ensure progress stays within boundaries
                if (progress < 0) {
                    progress = 0
                }
                if (progress > max) {
                    progress = max
                }
                setProgress(progress) // Draw progress
                if (progress != mLastProgress) {
                    // Only enact listener if the progress has actually changed
                    mLastProgress = progress
                    if (mOnChangeListener != null) mOnChangeListener!!.onProgressChanged(
                        this,
                        progress,
                        true
                    )
                }
                onSizeChanged(width, height, 0, 0)
                isPressed = true
                isSelected = true
            }

            MotionEvent.ACTION_UP -> {
                if (mOnChangeListener != null) mOnChangeListener!!.onStopTrackingTouch(this)
                isPressed = false
                isSelected = false
            }

            MotionEvent.ACTION_CANCEL -> {
                super.onTouchEvent(event)
                isPressed = false
                isSelected = false
            }
        }
        return true
    }

    @Synchronized
    fun setProgressAndThumb(progress: Int) {
        setProgress(progress)
        onSizeChanged(width, height, 0, 0)
        if (progress != mLastProgress) {
            // Only enact listener if the progress has actually changed
            mLastProgress = progress
            if (mOnChangeListener != null) mOnChangeListener!!.onProgressChanged(
                this,
                progress,
                true
            )
        }
    }

    @get:Synchronized
    @set:Synchronized
    var maximum: Int
        get() = max
        set(maximum) {
            max = maximum
        }
}