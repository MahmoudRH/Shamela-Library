package com.folioreader.ui.view

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ActionMode
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.folioreader.R
import com.folioreader.databinding.TextSelectionBinding
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighlightImpl
import com.folioreader.util.UiUtil
import org.json.JSONObject
import kotlin.math.ceil

class CustomWebView(private val context: Context, private val isNightMode:Boolean) : WebView(context) {
    private var actionMode: ActionMode? = null
    private var density: Float = 0f
    private var popupWindow = PopupWindow()
    private var selectionRect = Rect()
    private val popupRect = Rect()
    private var handleHeight: Int = 0
    private lateinit var viewTextSelection: View
    private lateinit var uiHandler: Handler
    private val LOG_TAG = "CustomWebView"
    init {
        density = resources.displayMetrics.density
        uiHandler = Handler()
        initViewTextSelection()
    }
    private lateinit var binding: TextSelectionBinding
    fun initViewTextSelection() {
        Log.v(LOG_TAG, "-> initViewTextSelection")

        val textSelectionMiddleDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.abc_text_select_handle_middle_mtrl_dark
        )
        handleHeight = textSelectionMiddleDrawable?.intrinsicHeight ?: (24 * density).toInt()

//        val config = AppUtil.getSavedConfig(context)!!
        val ctw = if (isNightMode) {
            ContextThemeWrapper(context, R.style.FolioNightTheme)
        } else {
            ContextThemeWrapper(context, R.style.FolioDayTheme)
        }

        val inflater = LayoutInflater.from(ctw)
        binding = TextSelectionBinding.inflate(inflater)
        viewTextSelection = binding.root
        viewTextSelection.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        viewTextSelection.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

        binding.yellowHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> yellowHighlight")
            onHighlightColorItemsClicked(HighlightImpl.HighlightStyle.Yellow, false)
        }
        binding.greenHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> greenHighlight")
            onHighlightColorItemsClicked(HighlightImpl.HighlightStyle.Green, false)
        }
        binding.blueHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> blueHighlight")
            onHighlightColorItemsClicked(HighlightImpl.HighlightStyle.Blue, false)
        }
        binding.pinkHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> pinkHighlight")
            onHighlightColorItemsClicked(HighlightImpl.HighlightStyle.Pink, false)
        }
        binding.deleteHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> deleteHighlight")
            dismissPopupWindow()
            loadUrl("javascript:clearSelection()")
            loadUrl("javascript:deleteThisHighlight()")
        }

        binding.copySelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        binding.shareSelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
    }
    private fun onHighlightColorItemsClicked(style: HighlightImpl.HighlightStyle, isAlreadyCreated: Boolean) {
        highlight(style, isAlreadyCreated)
        dismissPopupWindow()
    }

    fun highlight(style: HighlightImpl.HighlightStyle, isAlreadyCreated: Boolean) {
        if (!isAlreadyCreated) {
            this.loadUrl(
                String.format(
                    "javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s');}",
                    HighlightImpl.HighlightStyle.classForStyle(style)
                )
            )
        } else {
            this.loadUrl(
                String.format(
                    "javascript:setHighlightStyle('%s')",
                    HighlightImpl.HighlightStyle.classForStyle(style)
                )
            )
        }
    }


 /*   private inner class TextSelectionCallback : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onCreateActionMode")
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onPrepareActionMode")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom")
                )
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Log.d(LOG_TAG, "-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode")
//            dismissPopupWindow()
        }
    }

    private inner class TextSelectionCb2 : ActionMode.Callback2() {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onCreateActionMode")
            menu.clear()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onPrepareActionMode")
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Log.d(LOG_TAG, "-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode")
//            dismissPopupWindow()
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            Log.d(LOG_TAG, "-> onGetContentRect")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom")
                )
            }
        }
    }

    override fun startActionMode(callback: ActionMode.Callback): ActionMode {
        Log.d(LOG_TAG, "-> startActionMode")

        actionMode = super.startActionMode(TextSelectionCallback())
        actionMode?.finish()

        *//*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Log.w(LOG_TAG, "-> startActionMode -> Failed to apply theme colors to selection " +
                    "handles", e)
        }*//*

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback)
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode {
        Log.d(LOG_TAG, "-> startActionMode")

        actionMode = super.startActionMode(TextSelectionCb2(), type)
        actionMode?.finish()

        *//*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Log.w(LOG_TAG, "-> startActionMode -> Failed to apply theme colors to selection " +
                    "handles", e)
        }*//*

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback, type)
    }*/
    @JavascriptInterface
    fun setSelectionRect(left: Int, top: Int, right: Int, bottom: Int) {

        val currentSelectionRect = Rect()
        currentSelectionRect.left = (left * density).toInt()
        currentSelectionRect.top = (top * density).toInt()
        currentSelectionRect.right = (right * density).toInt()
        currentSelectionRect.bottom = (bottom * density).toInt()
        Log.d(LOG_TAG, "-> setSelectionRect -> $currentSelectionRect")

        computeTextSelectionRect(currentSelectionRect)
        uiHandler.post { showTextSelectionPopup() }
    }

    @JavascriptInterface
    fun onTextSelectionItemClicked(id: Int, selectedText: String?) {

        uiHandler.post { loadUrl("javascript:clearSelection()") }

        when (id) {
            R.id.copySelection -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> copySelection -> $selectedText")
                UiUtil.copyToClipboard(context, selectedText)
                Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
            R.id.shareSelection -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> shareSelection -> $selectedText")
                UiUtil.share(context, selectedText)
            }
            else -> {
                Log.w(LOG_TAG, "-> onTextSelectionItemClicked -> unknown id = $id")
            }
        }
    }
    @JavascriptInterface
    fun isPopupShowing(): Boolean {
        return popupWindow.isShowing
    }
    @JavascriptInterface
    fun dismissPopupWindow(): Boolean {
//        Log.d(LOG_TAG, "-> dismissPopupWindow -> " + parentFragment.spineItem.href)
        val wasShowing = popupWindow.isShowing
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            popupWindow.dismiss()
        } else {
            uiHandler.post { popupWindow.dismiss() }
        }
        selectionRect = Rect()
        return wasShowing
    }

    private fun computeViewportRect(): Rect {
        //Log.v(LOG_TAG, "-> computeViewportRect");
        val displayMetrics = resources.displayMetrics
        val viewportRect = Rect()

            viewportRect.left = 0
        viewportRect.top = 0
//        if (distractionFreeMode) {
            viewportRect.right = displayMetrics!!.widthPixels
//        } else {
//            viewportRect.right = displayMetrics!!.widthPixels - viewportRect.right
//        }
        viewportRect.bottom = displayMetrics.heightPixels

        return viewportRect
    }


    private fun getViewportRect(unit: DisplayUnit): Rect {

        val viewportRect = computeViewportRect()
        when (unit) {
            DisplayUnit.PX -> return viewportRect

            DisplayUnit.DP -> {
                viewportRect.left /= density.toInt()
                viewportRect.top /= density.toInt()
                viewportRect.right /= density.toInt()
                viewportRect.bottom /= density.toInt()
                return viewportRect
            }

            DisplayUnit.CSS_PX -> {
                viewportRect.left = ceil((viewportRect.left / density).toDouble()).toInt()
                viewportRect.top = ceil((viewportRect.top / density).toDouble()).toInt()
                viewportRect.right = ceil((viewportRect.right / density).toDouble()).toInt()
                viewportRect.bottom = ceil((viewportRect.bottom / density).toDouble()).toInt()
                return viewportRect
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }

    private fun computeTextSelectionRect(currentSelectionRect: Rect) {
        Log.v(LOG_TAG, "-> computeTextSelectionRect")

        val viewportRect = getViewportRect(DisplayUnit.PX)
        Log.d(LOG_TAG, "-> viewportRect -> $viewportRect")

        if (!Rect.intersects(viewportRect, currentSelectionRect)) {
            Log.i(LOG_TAG, "-> currentSelectionRect doesn't intersects viewportRect")
            uiHandler.post {
                popupWindow.dismiss()
            }
            return
        }
        Log.i(LOG_TAG, "-> currentSelectionRect intersects viewportRect")

        if (selectionRect == currentSelectionRect) {
            Log.i(
                LOG_TAG, "-> setSelectionRect -> currentSelectionRect is equal to previous " +
                        "selectionRect so no need to computeTextSelectionRect and show popupWindow again"
            )
            return
        }

        Log.i(
            LOG_TAG, "-> setSelectionRect -> currentSelectionRect is not equal to previous " +
                    "selectionRect so computeTextSelectionRect and show popupWindow"
        )
        selectionRect = currentSelectionRect

        val aboveSelectionRect = Rect(viewportRect)
        aboveSelectionRect.bottom = selectionRect.top - (8 * density).toInt()
        val belowSelectionRect = Rect(viewportRect)
        belowSelectionRect.top = selectionRect.bottom + handleHeight


        // Priority to show popupWindow will be as following -
        // 1. Show popupWindow below selectionRect, if space available
        // 2. Show popupWindow above selectionRect, if space available
        // 3. Show popupWindow in the middle of selectionRect

        //popupRect initialisation for belowSelectionRect
        popupRect.left = viewportRect.left
        popupRect.top = belowSelectionRect.top
        popupRect.right = popupRect.left + viewTextSelection.measuredWidth
        popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight
        //Log.d(LOG_TAG, "-> Pre decision popupRect -> " + popupRect);

        val popupY: Int
        if (belowSelectionRect.contains(popupRect)) {
            Log.i(LOG_TAG, "-> show below")
            popupY = belowSelectionRect.top

        } else {

            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top
            popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight

            popupY = if (aboveSelectionRect.contains(popupRect)) {
                Log.i(LOG_TAG, "-> show above")
                aboveSelectionRect.bottom - popupRect.height()

            } else {

                Log.i(LOG_TAG, "-> show in middle")
                val popupYDiff = (viewTextSelection.measuredHeight - selectionRect.height()) / 2
                selectionRect.top - popupYDiff
            }
        }

        val popupXDiff = (viewTextSelection.measuredWidth - selectionRect.width()) / 2
        val popupX = selectionRect.left - popupXDiff

        popupRect.offsetTo(popupX, popupY)
        //Log.d(LOG_TAG, "-> Post decision popupRect -> " + popupRect);

        // Check if popupRect left side is going outside of the viewportRect
        if (popupRect.left < viewportRect.left) {
            popupRect.right += 0 - popupRect.left
            popupRect.left = 0
        }

        // Check if popupRect right side is going outside of the viewportRect
        if (popupRect.right > viewportRect.right) {
            val dx = popupRect.right - viewportRect.right
            popupRect.left -= dx
            popupRect.right -= dx
        }
    }
    private fun showTextSelectionPopup() {
        Log.v(LOG_TAG, "-> showTextSelectionPopup")
        Log.d(LOG_TAG, "-> showTextSelectionPopup -> To be laid out popupRect -> $popupRect")

        popupWindow.dismiss()

        val isScrollingRunnable = Runnable {
                Log.i(LOG_TAG, "-> Stopped scrolling, show Popup")
                popupWindow.dismiss()
                popupWindow = PopupWindow(viewTextSelection, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                popupWindow.isClippingEnabled = false
                popupWindow.showAtLocation(this@CustomWebView, Gravity.NO_GRAVITY, popupRect.left, popupRect.top)

        }

            uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
    }

    companion object{
        private const val IS_SCROLLING_CHECK_TIMER = 100
    }
}