package com.folioreader.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.StateSet
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.folioreader.AppContext
import com.folioreader.R
import com.folioreader.ui.view.UnderlinedTextView
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.SoftReference
import java.util.Hashtable

/**
 * Created by mahavir on 3/30/16.
 */
object UiUtil {
    private val LOG_TAG = UiUtil::class.java.simpleName
    fun setCustomFont(
        view: View, ctx: Context, attrs: AttributeSet?,
        attributeSet: IntArray?, fontId: Int,
    ) {
        val a = ctx.obtainStyledAttributes(attrs, attributeSet!!)
        val customFont = a.getString(fontId)
        setCustomFont(view, ctx, customFont)
        a.recycle()
    }

    fun setCustomFont(view: View, ctx: Context, asset: String?): Boolean {
        if (TextUtils.isEmpty(asset)) return false
        var tf: Typeface? = null
        try {
            tf = getFont(ctx, asset)
            if (view is TextView) {
                view.typeface = tf
            } else {
                (view as Button).typeface = tf
            }
        } catch (e: Exception) {
            Log.e("AppUtil", "Could not get typface  $asset")
            return false
        }
        return true
    }

    private val fontCache = Hashtable<String?, SoftReference<Typeface?>?>()
    fun getFont(c: Context, name: String?): Typeface? {
        synchronized(fontCache) {
            if (fontCache[name] != null) {
                val ref = fontCache[name]
                if (ref!!.get() != null) {
                    return ref.get()
                }
            }
            val typeface = Typeface.createFromAsset(c.assets, name)
            fontCache[name] = SoftReference(typeface)
            return typeface
        }
    }

    fun getColorList(
        @ColorInt selectedColor: Int,
        @ColorInt unselectedColor: Int,
    ): ColorStateList {
        val states =
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf())
        val colors = intArrayOf(
            selectedColor,
            unselectedColor
        )
        return ColorStateList(states, colors)
    }

    fun keepScreenAwake(enable: Boolean, context: Context) {
        if (enable) {
            (context as Activity)
                .window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            (context as Activity)
                .window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun setBackColorToTextView(textView: UnderlinedTextView, type: String?) {
        val context = textView.context
        if (type == "highlight_yellow") {
            setUnderLineColor(textView, context, R.color.highlight_yellow, R.color.highlight_yellow)
        } else if (type == "highlight_green") {
            setUnderLineColor(textView, context, R.color.highlight_green, R.color.highlight_green)
        } else if (type == "highlight_blue") {
            setUnderLineColor(textView, context, R.color.highlight_blue, R.color.highlight_blue)
        } else if (type == "highlight_pink") {
            setUnderLineColor(textView, context, R.color.highlight_pink, R.color.highlight_pink)
        } else if (type == "highlight_underline") {
            setUnderLineColor(
                textView,
                context,
                android.R.color.transparent,
                android.R.color.holo_red_dark
            )
            textView.underlineWidth = 2.0f
        }
    }

    private fun setUnderLineColor(
        underlinedTextView: UnderlinedTextView,
        context: Context,
        background: Int,
        underlinecolor: Int,
    ) {
        underlinedTextView.setBackgroundColor(
            ContextCompat.getColor(
                context,
                background
            )
        )
        underlinedTextView.underLineColor = ContextCompat.getColor(
            context,
            underlinecolor
        )
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun copyToClipboard(context: Context, text: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copy", text)
        clipboard.setPrimaryClip(clip)
    }

    fun share(context: Context, text: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.type = "text/plain"
        context.startActivity(
            Intent.createChooser(
                sendIntent,
                context.resources.getText(R.string.send_to)
            )
        )
    }

    fun setColorIntToDrawable(@ColorInt color: Int, drawable: Drawable?) {
        drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun setColorResToDrawable(@ColorRes colorResId: Int, drawable: Drawable) {
        try {
            val color = ContextCompat.getColor(AppContext.Companion.get()!!, colorResId)
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        } catch (e: NotFoundException) {
            Log.e(LOG_TAG, "-> Exception in setColorResToDrawable -> ", e)
        }
    }

    fun setEditTextCursorColor(editText: EditText, @ColorInt color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(editText)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(editText.context, drawableResId)
            drawable!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf<Drawable?>(drawable, drawable)
            if (Build.VERSION.SDK_INT <= 27) {
                // Get the editor
                field = TextView::class.java.getDeclaredField("mEditor")
                field.isAccessible = true
                val editor = field[editText]
                // Set the drawables
                field = editor.javaClass.getDeclaredField("mCursorDrawable")
                field.isAccessible = true
                field[editor] = drawables
            } else if (Build.VERSION.SDK_INT >= 28) {
                // TODO -> Not working for 28
                // Get the editor
                field = TextView::class.java.getDeclaredField("mEditor")
                field.isAccessible = true
                val editor = field[editText]
                // Set the drawables
                field = editor.javaClass.getDeclaredField("mDrawableForCursor")
                field.isAccessible = true
                field[editor] = drawables[0]
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
        }
    }

    fun setEditTextHandleColor(editText: EditText, @ColorInt color: Int) {
        try {
            // Get the cursor resource id
            val fieldLeftRes = TextView::class.java.getDeclaredField("mTextSelectHandleLeftRes")
            fieldLeftRes.isAccessible = true
            val leftDrawableResId = fieldLeftRes.getInt(editText)
            val fieldRightRes = TextView::class.java.getDeclaredField("mTextSelectHandleRightRes")
            fieldRightRes.isAccessible = true
            val rightDrawableResId = fieldRightRes.getInt(editText)
            val fieldCenterRes = TextView::class.java.getDeclaredField("mTextSelectHandleRes")
            fieldCenterRes.isAccessible = true
            val centerDrawableResId = fieldCenterRes.getInt(editText)

            // Get the drawable and set a color filter
            val drawableLeft = ContextCompat.getDrawable(editText.context, leftDrawableResId)
            drawableLeft!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawableRight = ContextCompat.getDrawable(editText.context, rightDrawableResId)
            drawableRight!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawableCenter = ContextCompat.getDrawable(editText.context, centerDrawableResId)
            drawableCenter!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)

            // Get the editor
            val fieldEditor = TextView::class.java.getDeclaredField("mEditor")
            fieldEditor.isAccessible = true
            val editor = fieldEditor[editText]

            // Set the drawables
            val fieldLeft = editor.javaClass.getDeclaredField("mSelectHandleLeft")
            fieldLeft.isAccessible = true
            fieldLeft[editor] = drawableLeft
            val fieldRight = editor.javaClass.getDeclaredField("mSelectHandleRight")
            fieldRight.isAccessible = true
            fieldRight[editor] = drawableRight
            val fieldCenter = editor.javaClass.getDeclaredField("mSelectHandleCenter")
            fieldCenter.isAccessible = true
            fieldCenter[editor] = drawableCenter

        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> ", e)
        }
    }

    fun createStateDrawable(
        @ColorInt colorSelected: Int,
        @ColorInt colorNormal: Int,
    ): StateListDrawable {
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(
            intArrayOf(android.R.attr.state_selected),
            ColorDrawable(colorSelected)
        )
        stateListDrawable.addState(StateSet.WILD_CARD, ColorDrawable(colorNormal))
        return stateListDrawable
    }

    fun getShapeDrawable(@ColorInt color: Int): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.setStroke(pxToDp(2), color)
        gradientDrawable.setColor(color)
        gradientDrawable.cornerRadius = pxToDp(3).toFloat()
        return gradientDrawable
    }

    fun setShapeColor(view: View?, @ColorInt color: Int) {
        (view!!.background as GradientDrawable).setColor(color)
    }

    fun pxToDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    fun setStatusBarColor(window: Window, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun rectToDOMRectJson(rect: Rect): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("x", rect.left)
            jsonObject.put("y", rect.top)
            jsonObject.put("width", rect.width())
            jsonObject.put("height", rect.height())
            return jsonObject.toString()
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "-> ", e)
        }
        return ""
    }
}