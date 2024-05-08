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
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.databinding.TextSelectionNewBinding
import com.folioreader.model.DisplayUnit
import com.folioreader.util.UiUtil
import com.shamela.apptheme.presentation.theme.AppFonts
import org.json.JSONObject
import kotlin.math.ceil

class CustomWebView(
    private val context: Context,
    private val isNightMode: Boolean,
    private val currentPageIndex:Int,
    private val currentPageHref:String?
) : WebView(context) {
    private var actionMode: ActionMode? = null
    private var density: Float = 0f
    private var popupWindow = PopupWindow()
    private var selectionRect = Rect()
    private val popupRect = Rect()
    private var handleHeight: Int = 0
    private lateinit var viewTextSelectionNew: View
    private lateinit var uiHandler: Handler
    private val LOG_TAG = "CustomWebView"
    val fullScreenMode = mutableStateOf(false)

    init {
        density = resources.displayMetrics.density
        uiHandler = Handler()
        initViewTextSelectionNew()
    }

    private lateinit var binding: TextSelectionNewBinding
    fun initViewTextSelectionNew() {
        Log.v(LOG_TAG, "-> initViewTextSelectionNew")

        val textSelectionMiddleDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.abc_text_select_handle_middle_mtrl_dark
        )
        handleHeight = textSelectionMiddleDrawable?.intrinsicHeight ?: (24 * density).toInt()

        val ctw = if (isNightMode) {
            ContextThemeWrapper(context, R.style.FolioNightTheme)
        } else {
            ContextThemeWrapper(context, R.style.FolioDayTheme)
        }

        val inflater = LayoutInflater.from(ctw)
        binding = TextSelectionNewBinding.inflate(inflater)
        viewTextSelectionNew = binding.root
        viewTextSelectionNew.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        viewTextSelectionNew.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

        binding.copySelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        binding.shareSelection.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        binding.addSelectionToFavirite.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
            //TODO:) this code must be changed
        }
    }


    override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode {
        actionMode = super.startActionMode(ActionModeCallBack(), type)
        actionMode?.finish()
        return actionMode as ActionMode
    }

    inner class ActionModeCallBack : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean { return true }
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom")
                )
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean { return false }

        override fun onDestroyActionMode(mode: ActionMode) { dismissPopupWindow() }

    }


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
            }

            R.id.shareSelection -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> shareSelection -> $selectedText")
                UiUtil.share(context, selectedText)
            }

            R.id.addSelectionToFavirite -> {
                Log.v(
                    LOG_TAG,
                    "-> onTextSelectionItemClicked -> addSelectionToFavorite -> $selectedText, currentPageIndex= $currentPageIndex",
                )
                selectedText?.let {

                    FolioReader.get().onAddQuoteToFavorite(
                        quote = it,
                        pageIndex = currentPageIndex,
                        pageHref = currentPageHref?:""
                    )
                }
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
        Log.v(LOG_TAG, "-> computeViewportRect");
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
        popupRect.right = popupRect.left + viewTextSelectionNew.measuredWidth
        popupRect.bottom = popupRect.top + viewTextSelectionNew.measuredHeight
        //Log.d(LOG_TAG, "-> Pre decision popupRect -> " + popupRect);

        val popupY: Int
        if (belowSelectionRect.contains(popupRect)) {
            Log.i(LOG_TAG, "-> show below")
            popupY = belowSelectionRect.top

        } else {

            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top
            popupRect.bottom = popupRect.top + viewTextSelectionNew.measuredHeight

            popupY = if (aboveSelectionRect.contains(popupRect)) {
                Log.i(LOG_TAG, "-> show above")
                aboveSelectionRect.bottom - popupRect.height()

            } else {

                Log.i(LOG_TAG, "-> show in middle")
                val popupYDiff = (viewTextSelectionNew.measuredHeight - selectionRect.height()) / 2
                selectionRect.top - popupYDiff
            }
        }

        val popupXDiff = (viewTextSelectionNew.measuredWidth - selectionRect.width()) / 2
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
            Log.i(
                LOG_TAG,
                "-> Stopped scrolling, show Popup, fullScreenMode= ${fullScreenMode.value}"
            )
            val selectedFontSize = AppFonts.selectedFontSizeCssClass()
            val currentFontSize = when (selectedFontSize) {
                "textSizeOne" -> 13
                "textSizeTwo" -> 15
                "textSizeThree" -> 17
                "textSizeFour" -> 19
                "textSizeFive" -> 21
                else -> 0
            }
            val lineOffset = (currentFontSize * density).toInt() + currentFontSize
            Log.e(LOG_TAG, "selectedFontSize: $selectedFontSize ")
            Log.e(LOG_TAG, "currentFontSize: $currentFontSize ")
            val verticalOffset = 16 + if (fullScreenMode.value) lineOffset else lineOffset * 3
            popupWindow.dismiss()
            popupWindow = PopupWindow(
                viewTextSelectionNew,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow.isClippingEnabled = false
            popupWindow.showAtLocation(
                this@CustomWebView,
                Gravity.NO_GRAVITY,
                popupRect.left,
                popupRect.top + verticalOffset
            )

        }

        uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
    }

    companion object {
        private const val IS_SCROLLING_CHECK_TIMER = 100
    }
}