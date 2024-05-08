package com.folioreader.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.FocusFinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.core.os.ParcelableCompat
import androidx.core.os.ParcelableCompatCreatorCallbacks
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.VelocityTrackerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityRecordCompat
import androidx.core.widget.EdgeEffectCompat
import androidx.viewpager.widget.PagerAdapter
import com.folioreader.Config
import com.folioreader.R
import java.lang.reflect.Method
import java.util.Collections

/**
 * Created by mobisys on 10/10/2016.
 */
class DirectionalViewpager : ViewGroup {
    enum class Direction {
        HORIZONTAL, VERTICAL
    }

    /**
     * Used to track what the expected number of items in the adapter should be.
     * If the app changes this when we don't expect it, we'll throw a big obnoxious exception.
     */
    private var mExpectedAdapterCount = 0
    var mDirection: String = Direction.VERTICAL.name

    class ItemInfo {
        var `object`: Any? = null
        var position = 0
        var scrolling = false
        var widthFactor = 0f
        var heightFactor = 0f
        var offset = 0f
    }

    private val mItems = ArrayList<ItemInfo>()
    private val mTempItem = ItemInfo()
    private val mTempRect = Rect()

    /**
     * Retrieve the current adapter supplying pages.
     *
     * @return The currently registered PagerAdapter
     */
    private var mAdapter: PagerAdapter? = null
    private var mCurItem // Index of currently displayed page.
            = 0
    private var mRestoredCurItem = -1
    private var mRestoredAdapterState: Parcelable? = null
    private var mRestoredClassLoader: ClassLoader? = null
    private var mScroller: Scroller? = null
    private var mIsScrollStarted = false
    private var mObserver: PagerObserver? = null
    private var mPageMargin = 0
    private var mMarginDrawable: Drawable? = null
    private var mTopPageBounds = 0
    private var mBottomPageBounds = 0
    private var mLeftPageBounds = 0
    private var mRightPageBounds = 0

    // Offsets of the first and last items, if known.
    // Set during population, used to determine if we are at the beginning
    // or end of the pager data set during touch scrolling.
    private var mFirstOffset = -Float.MAX_VALUE
    private var mLastOffset = Float.MAX_VALUE
    private var mChildWidthMeasureSpec = 0
    private var mChildHeightMeasureSpec = 0
    private var mInLayout = false
    private var mScrollingCacheEnabled = false
    private var mPopulatePending = false
    private var mOffscreenPageLimit = DEFAULT_OFFSCREEN_PAGES
    private var mIsBeingDragged = false
    private var mIsUnableToDrag = false
    private val mIgnoreGutter = false
    private var mDefaultGutterSize = 0
    private var mGutterSize = 0
    private var mTouchSlop = 0

    /**
     * Position of the last motion event.
     */
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private var mActivePointerId = INVALID_POINTER

    /**
     * Determines speed during touch scrolling
     */
    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mFlingDistance = 0
    private var mCloseEnough = 0

    /**
     * Returns true if a fake drag is in progress.
     *
     * @return true if currently in a fake drag, false otherwise.
     * @see .beginFakeDrag
     * @see .fakeDragBy
     * @see .endFakeDrag
     */
    var isFakeDragging = false
        private set
    private var mFakeDragBeginTime: Long = 0
    private var mLeftEdge: EdgeEffectCompat? = null
    private var mRightEdge: EdgeEffectCompat? = null
    private var mTopEdge: EdgeEffectCompat? = null
    private var mBottomEdge: EdgeEffectCompat? = null
    private var mFirstLayout = true
    private var mNeedCalculatePageOffsets = false
    private var mCalledSuper = false
    private var mDecorChildCount = 0
    private var mOnPageChangeListeners: MutableList<OnPageChangeListener>? = null
    private var mOnPageChangeListener: OnPageChangeListener? = null
    private var mInternalPageChangeListener: OnPageChangeListener? = null
    private var mPageTransformer: PageTransformer? = null
    private var mSetChildrenDrawingOrderEnabled: Method? = null
    private var mDrawingOrder = 0
    private var mDrawingOrderedChildren: ArrayList<View>? = null
    private val mEndScrollRunnable = Runnable {
        setScrollState(SCROLL_STATE_IDLE)
        populate()
    }
    private var mScrollState = SCROLL_STATE_IDLE

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    interface OnPageChangeListener {
        /**
         * This method will be invoked when the current
         * page is scrolled, either as part
         * of a programmatically initiated
         * smooth scroll or a user initiated touch scroll.
         *
         * @param position             Position index of the first page currently being displayed.
         *
         *
         * Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        fun onPageSelected(position: Int)

        /**
         * Called when the scroll state changes. Useful for discovering when the user
         * begins dragging, when the pager is automatically settling to the current page,
         * or when it is fully stopped/idle.
         *
         * @param state The new scroll state.
         * @see ViewPager.SCROLL_STATE_IDLE
         *
         * @see ViewPager.SCROLL_STATE_DRAGGING
         *
         * @see ViewPager.SCROLL_STATE_SETTLING
         */
        fun onPageScrollStateChanged(state: Int)
    }

    /**
     * Simple implementation of the [OnPageChangeListener]
     * interface with stub
     * implementations of each method.
     * Extend this if you do not intend to override
     * every method of [OnPageChangeListener].
     */
    class SimpleOnPageChangeListener : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) {
            // This space for rent
        }

        override fun onPageSelected(position: Int) {
            // This space for rent
        }

        override fun onPageScrollStateChanged(state: Int) {
            // This space for rent
        }
    }

    /**
     * A PageTransformer is invoked whenever a visible/attached page is scrolled.
     * This offers an opportunity for the application to apply a custom transformation
     * to the page views using animation properties.
     *
     *
     *
     * As property animation is only supported as of Android 3.0 and forward,
     * setting a PageTransformer on a ViewPager on earlier platform versions will
     * be ignored.
     */
    interface PageTransformer {
        /**
         * Apply a property transformation to the given page.
         *
         * @param page     Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         * position of the pager. 0 is front and center. 1 is one full
         * page position to the right, and -1 is one page position to the left.
         */
        fun transformPage(page: View?, position: Float)
    }

    /**
     * Used internally to monitor when adapters are switched.
     */
    interface OnAdapterChangeListener {
        fun onAdapterChanged(oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?)
    }

    /**
     * Used internally to tag special types of child views that should be added as
     * pager decorations by default.
     */
    internal interface Decor {}

    constructor(context: Context?) : super(context) {
        initViewPager()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DirectionalViewpager)
        if (a.getString(R.styleable.DirectionalViewpager_direction) != null) {
            mDirection = a.getString(R.styleable.DirectionalViewpager_direction)!!
        }
        initViewPager()
    }

    fun initViewPager() {
        setWillNotDraw(false)
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        val context = context
        mScroller = Scroller(context, sInterpolator)
        val configuration = ViewConfiguration.get(context)
        val density = context.resources.displayMetrics.density
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration)
        mMinimumVelocity = (MIN_FLING_VELOCITY * density).toInt()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mLeftEdge = EdgeEffectCompat(context)
        mRightEdge = EdgeEffectCompat(context)
        mTopEdge = EdgeEffectCompat(context)
        mBottomEdge = EdgeEffectCompat(context)
        mFlingDistance = (MIN_DISTANCE_FOR_FLING * density).toInt()
        mCloseEnough = (CLOSE_ENOUGH * density).toInt()
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()
        ViewCompat.setAccessibilityDelegate(this, MyAccessibilityDelegate())
        if (ViewCompat.getImportantForAccessibility(this)
            == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        ) {
            ViewCompat.setImportantForAccessibility(
                this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(this,
            object : androidx.core.view.OnApplyWindowInsetsListener {
                private val mTempRect = Rect()
                override fun onApplyWindowInsets(
                    v: View,
                    originalInsets: WindowInsetsCompat,
                ): WindowInsetsCompat {
                    // First let the ViewPager itself try and consume them...
                    val applied = ViewCompat.onApplyWindowInsets(v, originalInsets)
                    if (applied.isConsumed) {
                        // If the ViewPager consumed all insets, return now
                        return applied
                    }

                    // Now we'll manually dispatch the insets to our children. Since ViewPager
                    // children are always full-height, we do not want to use the standard
                    // ViewGroup dispatchApplyWindowInsets since if child 0 consumes them,
                    // the rest of the children will not receive any insets. To workaround this
                    // we manually dispatch the applied insets, not allowing children to
                    // consume them from each other. We do however keep track of any insets
                    // which are consumed, returning the union of our children's consumption
                    val res = mTempRect
                    res.left = applied.systemWindowInsetLeft
                    res.top = applied.systemWindowInsetTop
                    res.right = applied.systemWindowInsetRight
                    res.bottom = applied.systemWindowInsetBottom
                    var i = 0
                    val count = childCount
                    while (i < count) {
                        val childInsets = ViewCompat
                            .dispatchApplyWindowInsets(getChildAt(i), applied)
                        // Now keep track of any consumed by tracking each dimension's min
                        // value
                        res.left = Math.min(
                            childInsets.systemWindowInsetLeft,
                            res.left
                        )
                        res.top = Math.min(
                            childInsets.systemWindowInsetTop,
                            res.top
                        )
                        res.right = Math.min(
                            childInsets.systemWindowInsetRight,
                            res.right
                        )
                        res.bottom = Math.min(
                            childInsets.systemWindowInsetBottom,
                            res.bottom
                        )
                        i++
                    }

                    // Now return a new WindowInsets, using the consumed window insets
                    return applied.replaceSystemWindowInsets(
                        res.left, res.top, res.right, res.bottom
                    )
                }
            })
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(mEndScrollRunnable)
        // To be on the safe side, abort the scroller
        if (mScroller != null && !mScroller!!.isFinished) {
            mScroller!!.abortAnimation()
        }
        super.onDetachedFromWindow()
    }

    private fun setScrollState(newState: Int) {
        if (mScrollState == newState) {
            return
        }
        mScrollState = newState
        if (mPageTransformer != null) {
            // PageTransformers can do complex things that benefit from hardware layers.
            enableLayers(newState != SCROLL_STATE_IDLE)
        }
        dispatchOnScrollStateChanged(newState)
    }


    private fun removeNonDecorViews() {
        var i = 0
        while (i < childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            if (!lp.isDecor) {
                removeViewAt(i)
                i--
            }
            i++
        }
    }


    private val clientWidth: Int
        private get() = measuredWidth - paddingLeft - paddingRight
    private val clientHeight: Int
        private get() = measuredHeight - paddingTop - paddingBottom

    /**
     * Set the currently selected page.
     *
     * @param item         Item index to select
     * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
     */
    fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        mPopulatePending = false
        setCurrentItemInternal(item, smoothScroll, false)
    }

    /**
     * Set the currently selected page. If the ViewPager has already been through its first
     * layout with its current adapter there will be a smooth animated transition between
     * the current item and the specified item.
     *
     * @param item Item index to select
     */
    var currentItem: Int
        get() = mCurItem
        set(item) {
            mPopulatePending = false
            setCurrentItemInternal(item, !mFirstLayout, false)
        }

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean) {
        setCurrentItemInternal(item, smoothScroll, always, 0)
    }

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean, velocity: Int) {
        var item = item
        if (mAdapter == null || mAdapter!!.count <= 0) {
            setScrollingCacheEnabled(false)
            return
        }
        if (!always && mCurItem == item && mItems.size != 0) {
            setScrollingCacheEnabled(false)
            return
        }
        if (item < 0) {
            item = 0
        } else if (item >= mAdapter!!.count) {
            item = mAdapter!!.count - 1
        }
        val pageLimit = mOffscreenPageLimit
        if (item > mCurItem + pageLimit || item < mCurItem - pageLimit) {
            // We are doing a jump by more than one page.  To avoid
            // glitches, we want to keep all current pages in the view
            // until the scroll ends.
            for (i in mItems.indices) {
                mItems[i].scrolling = true
            }
        }
        val dispatchSelected = mCurItem != item
        if (mFirstLayout) {
            // We don't have any idea how big we are yet and shouldn't have any pages either.
            // Just set things up and let the pending layout handle things.
            mCurItem = item
            if (dispatchSelected) {
                dispatchOnPageSelected(item)
            }
            requestLayout()
        } else {
            populate(item)
            scrollToItem(item, smoothScroll, velocity, dispatchSelected)
        }
    }

    private fun scrollToItem(
        item: Int, smoothScroll: Boolean, velocity: Int,
        dispatchSelected: Boolean,
    ) {
        val curInfo = infoForPosition(item)
        var destX = 0
        var destY = 0
        if (isHorizontal) {
            if (curInfo != null) {
                val width = clientWidth
                destX = (width * Math.max(
                    mFirstOffset,
                    Math.min(curInfo.offset, mLastOffset)
                )).toInt()
            }
            if (smoothScroll) {
                smoothScrollTo(destX, 0, velocity)
                if (dispatchSelected) {
                    dispatchOnPageSelected(item)
                }
            } else {
                if (dispatchSelected) {
                    dispatchOnPageSelected(item)
                }
                completeScroll(false)
                scrollTo(destX, 0)
                pageScrolled(destX, 0)
            }
        } else {
            if (curInfo != null) {
                val height = clientHeight
                destY = (height * Math.max(
                    mFirstOffset,
                    Math.min(curInfo.offset, mLastOffset)
                )).toInt()
            }
            if (smoothScroll) {
                smoothScrollTo(0, destY, velocity)
                if (dispatchSelected && mOnPageChangeListener != null) {
                    mOnPageChangeListener!!.onPageSelected(item)
                }
                if (dispatchSelected && mInternalPageChangeListener != null) {
                    mInternalPageChangeListener!!.onPageSelected(item)
                }
            } else {
                if (dispatchSelected && mOnPageChangeListener != null) {
                    mOnPageChangeListener!!.onPageSelected(item)
                }
                if (dispatchSelected && mInternalPageChangeListener != null) {
                    mInternalPageChangeListener!!.onPageSelected(item)
                }
                completeScroll(false)
                scrollTo(0, destY)
                pageScrolled(0, destY)
            }
        }
    }

    /**
     * Set a listener that will be invoked whenever the page changes or is incrementally
     * scrolled. See [OnPageChangeListener].
     *
     * @param listener Listener to set
     */
    @Deprecated(
        """Use {@link #addOnPageChangeListener(OnPageChangeListener)}
      and {@link #removeOnPageChangeListener(OnPageChangeListener)} instead."""
    )
    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    /**
     * Add a listener that will be invoked whenever the page changes or is incrementally
     * scrolled. See [OnPageChangeListener].
     *
     *
     *
     * Components that add a listener should take care to remove it when finished.
     * Other components that take ownership of a view may call [.clearOnPageChangeListeners]
     * to remove all attached listeners.
     *
     * @param listener listener to add
     */
    fun addOnPageChangeListener(listener: OnPageChangeListener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = ArrayList()
        }
        mOnPageChangeListeners!!.add(listener)
    }

    /**
     * Remove a listener that was previously added via
     * [.addOnPageChangeListener].
     *
     * @param listener listener to remove
     */
    fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners!!.remove(listener)
        }
    }

    /**
     * Remove all listeners that are notified of any changes in scroll state or position.
     */
    fun clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners!!.clear()
        }
    }

    /**
     * Set a [PageTransformer] that will be called for each attached page whenever
     * the scroll position is changed. This allows the application to apply custom property
     * transformations to each page, overriding the default sliding look and feel.
     *
     *
     *
     * *Note:* Prior to Android 3.0 the property animation APIs did not exist.
     * As a result, setting a PageTransformer prior to Android 3.0 (API 11) will have no effect.
     *
     * @param reverseDrawingOrder true if the supplied PageTransformer requires page views
     * to be drawn from last to first instead of first to last.
     * @param transformer         PageTransformer that will modify each page's animation properties
     */
    fun setPageTransformer(reverseDrawingOrder: Boolean, transformer: PageTransformer?) {
        if (Build.VERSION.SDK_INT >= 11) {
            val hasTransformer = transformer != null
            val needsPopulate = hasTransformer != (mPageTransformer != null)
            mPageTransformer = transformer
            setChildrenDrawingOrderEnabledCompat(hasTransformer)
            mDrawingOrder = if (hasTransformer) {
                if (reverseDrawingOrder) DRAW_ORDER_REVERSE else DRAW_ORDER_FORWARD
            } else {
                DRAW_ORDER_DEFAULT
            }
            if (needsPopulate) populate()
        }
    }

    fun setChildrenDrawingOrderEnabledCompat(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= 7) {
            if (mSetChildrenDrawingOrderEnabled == null) {
                try {
                    mSetChildrenDrawingOrderEnabled = ViewGroup::class.java.getDeclaredMethod(
                        "setChildrenDrawingOrderEnabled", *arrayOf<Class<*>>(java.lang.Boolean.TYPE)
                    )
                } catch (e: NoSuchMethodException) {
                    Log.e(TAG, "Can't find setChildrenDrawingOrderEnabled", e)
                }
            }
            try {
                mSetChildrenDrawingOrderEnabled
                    ?.invoke(this, enable)
            } catch (e: Exception) {
                Log.e(TAG, "Error changing children drawing order", e)
            }
        }
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        val index = if (mDrawingOrder
            == DRAW_ORDER_REVERSE
        ) childCount - 1 - i else i
        return (mDrawingOrderedChildren!![index].layoutParams as LayoutParams).childIndex
    }

    /**
     * Set a separate OnPageChangeListener for internal use by the support library.
     *
     * @param listener Listener to set
     * @return The old listener that was set, if any.
     */
    fun setInternalPageChangeListener(listener: OnPageChangeListener?): OnPageChangeListener? {
        val oldListener = mInternalPageChangeListener
        mInternalPageChangeListener = listener
        return oldListener
    }
    /**
     * Returns the number of pages that will be retained to either side of the
     * current page in the view hierarchy in an idle state. Defaults to 1.
     *
     * @return How many pages will be kept offscreen on either side
     * @see .setOffscreenPageLimit
     */
    /**
     * Set the number of pages that should be
     * retained to either side of the
     * current page in the view hierarchy
     * in an idle state. Pages beyond this
     * limit will be recreated from the adapter when needed.
     *
     *
     *
     * This is offered as an optimization.
     * If you know in advance the number
     * of pages you will need to support or
     * have lazy-loading mechanisms in place
     * on your pages, tweaking this setting
     * can have benefits in perceived smoothness
     * of paging animations and interaction.
     * If you have a small number of pages (3-4)
     * that you can keep active all at once,
     * less time will be spent in layout for
     * newly created view subtrees as the
     * user pages back and forth.
     *
     *
     *
     * You should keep this limit low,
     * especially if your pages have complex layouts.
     * This setting defaults to 1.
     *
     * @param limit How many pages will be kept offscreen in an idle state.
     */
    var offscreenPageLimit: Int
        get() = mOffscreenPageLimit
        set(limit) {
            var limit = limit
            if (limit < DEFAULT_OFFSCREEN_PAGES) {
                Log.w(
                    TAG, "Requested offscreen page limit " + limit + " too small; defaulting to " +
                            DEFAULT_OFFSCREEN_PAGES
                )
                limit = DEFAULT_OFFSCREEN_PAGES
            }
            if (limit != mOffscreenPageLimit) {
                mOffscreenPageLimit = limit
                populate()
            }
        }
    /**
     * Return the margin between pages.
     *
     * @return The size of the margin in pixels
     */
    /**
     * Set the margin between pages.
     *
     * @param marginPixels Distance between adjacent pages in pixels
     * @see .getPageMargin
     * @see .setPageMarginDrawable
     * @see .setPageMarginDrawable
     */
    var pageMargin: Int
        get() = mPageMargin
        set(marginPixels) {
            val oldMargin = mPageMargin
            mPageMargin = marginPixels
            if (isHorizontal) {
                val width = width
                recomputeScrollPosition(width, width, marginPixels, oldMargin, 0, 0)
            } else {
                val height = height
                recomputeScrollPosition(0, 0, marginPixels, oldMargin, height, height)
            }
            requestLayout()
        }

    /**
     * Set a drawable that will be used to fill the margin between pages.
     *
     * @param d Drawable to display between pages
     */
    fun setPageMarginDrawable(d: Drawable?) {
        mMarginDrawable = d
        if (d != null) refreshDrawableState()
        setWillNotDraw(d == null)
        invalidate()
    }

    /**
     * Set a drawable that will be used to fill the margin between pages.
     *
     * @param resId Resource ID of a drawable to display between pages
     */
    fun setPageMarginDrawable(@DrawableRes resId: Int) {
        setPageMarginDrawable(context.resources.getDrawable(resId))
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mMarginDrawable
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val d = mMarginDrawable
        if (d != null && d.isStateful) {
            d.state = drawableState
        }
    }

    // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= (0.3f * Math.PI / 2.0f).toFloat()
        return Math.sin(f.toDouble()).toFloat()
    }
    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param x        the number of pixels to scroll by on the X axis
     * @param y        the number of pixels to scroll by on the Y axis
     * @param velocity the velocity associated with a fling, if applicable. (0 otherwise)
     */
    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param x the number of pixels to scroll by on the X axis
     * @param y the number of pixels to scroll by on the Y axis
     */
    @JvmOverloads
    fun smoothScrollTo(x: Int, y: Int, velocity: Int = 0) {
        var velocity = velocity
        if (childCount == 0) {
            // Nothing to do.
            setScrollingCacheEnabled(false)
            return
        }
        val sx: Int
        if (isHorizontal) {
            val wasScrolling = mScroller != null && !mScroller!!.isFinished
            if (wasScrolling) {
                // We're in the middle of a previously initiated scrolling. Check to see
                // whether that scrolling has actually started (if we always call getStartX
                // we can get a stale value from the scroller if it hadn't yet had its first
                // computeScrollOffset call) to decide what is the current scrolling position.
                sx = if (mIsScrollStarted) mScroller!!.currX else mScroller!!.startX
                // And abort the current scrolling.
                mScroller!!.abortAnimation()
                setScrollingCacheEnabled(false)
            } else {
                sx = scrollX
            }
        } else {
            sx = scrollX
        }
        val sy = scrollY
        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            completeScroll(false)
            populate()
            setScrollState(SCROLL_STATE_IDLE)
            return
        }
        setScrollingCacheEnabled(true)
        setScrollState(SCROLL_STATE_SETTLING)
        var duration = 0
        if (isHorizontal) {
            val width = clientWidth
            val halfWidth = width / 2
            val distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width)
            val distance = halfWidth + halfWidth *
                    distanceInfluenceForSnapDuration(distanceRatio)
            velocity = Math.abs(velocity)
            duration = if (velocity > 0) {
                4 * Math.round(1000 * Math.abs(distance / velocity))
            } else {
                val pageWidth = width * mAdapter!!.getPageWidth(mCurItem)
                val pageDelta = Math.abs(dx).toFloat() / (pageWidth + mPageMargin)
                ((pageDelta + 1) * 100).toInt()
            }
        } else {
            val height = clientHeight
            val halfHeight = height / 2
            val distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / height)
            val distance = halfHeight + halfHeight *
                    distanceInfluenceForSnapDuration(distanceRatio)
            duration = 0
            velocity = Math.abs(velocity)
            duration = if (velocity > 0) {
                4 * Math.round(1000 * Math.abs(distance / velocity))
            } else {
                val pageHeight = height * mAdapter!!.getPageWidth(mCurItem)
                val pageDelta = Math.abs(dx).toFloat() / (pageHeight + mPageMargin)
                ((pageDelta + 1) * 100).toInt()
            }
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION)

        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
        if (isHorizontal) {
            mIsScrollStarted = false
        }
        mScroller!!.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private val isHorizontal: Boolean
        private get() = mDirection.equals(Direction.HORIZONTAL.name, ignoreCase = true)

    fun addNewItem(position: Int, index: Int): ItemInfo {
        val ii = ItemInfo()
        ii.position = position
        ii.`object` = mAdapter!!.instantiateItem(this, position)
        if (isHorizontal) {
            ii.widthFactor = mAdapter!!.getPageWidth(position)
        } else {
            ii.heightFactor = mAdapter!!.getPageWidth(position)
        }
        if (index < 0 || index >= mItems.size) {
            mItems.add(ii)
        } else {
            mItems.add(index, ii)
        }
        return ii
    }

    fun dataSetChanged() {
        // This method only gets called if our observer is attached, so mAdapter is non-null.
        val adapterCount = mAdapter!!.count
        mExpectedAdapterCount = adapterCount
        var needPopulate = mItems.size < mOffscreenPageLimit * 2 + 1 &&
                mItems.size < adapterCount
        var newCurrItem = mCurItem
        var isUpdating = false
        var i = 0
        while (i < mItems.size) {
            val ii = mItems[i]
            val newPos = mAdapter!!.getItemPosition(ii.`object`!!)
            if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                i++
                continue
            }
            if (newPos == PagerAdapter.POSITION_NONE) {
                mItems.removeAt(i)
                i--
                if (!isUpdating) {
                    mAdapter!!.startUpdate(this)
                    isUpdating = true
                }
                mAdapter!!.destroyItem(this, ii.position, ii.`object`!!)
                needPopulate = true
                if (mCurItem == ii.position) {
                    // Keep the current item in the valid range
                    newCurrItem = Math.max(0, Math.min(mCurItem, adapterCount - 1))
                    needPopulate = true
                }
                i++
                continue
            }
            if (ii.position != newPos) {
                if (ii.position == mCurItem) {
                    // Our current item changed position. Follow it.
                    newCurrItem = newPos
                }
                ii.position = newPos
                needPopulate = true
            }
            i++
        }
        if (isUpdating) {
            mAdapter!!.finishUpdate(this)
        }
        Collections.sort(mItems, COMPARATOR)
        if (needPopulate) {
            // Reset our known page widths; populate will recompute them.
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                if (!lp.isDecor) {
                    if (isHorizontal) {
                        lp.widthFactor = 0f
                    } else {
                        lp.heightFactor = 0f
                    }
                }
            }
            setCurrentItemInternal(newCurrItem, false, true)
            requestLayout()
        }
    }

    @JvmOverloads
    fun populate(newCurrentItem: Int = mCurItem) {
        var oldCurInfo: ItemInfo? = null
        var focusDirection = FOCUS_FORWARD
        if (mCurItem != newCurrentItem) {
            focusDirection = if (mCurItem < newCurrentItem) FOCUS_DOWN else FOCUS_UP
            oldCurInfo = infoForPosition(mCurItem)
            mCurItem = newCurrentItem
        }
        if (mAdapter == null) {
            sortChildDrawingOrder()
            return
        }

        // Bail now if we are waiting to populate.  This is to hold off
        // on creating views from the time the user releases their finger to
        // fling to a new position until we have finished the scroll to
        // that position, avoiding glitches from happening at that point.
        if (mPopulatePending) {
            if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...")
            sortChildDrawingOrder()
            return
        }

        // Also, don't populate until we are attached to a window.  This is to
        // avoid trying to populate before we have restored our view hierarchy
        // state and conflicting with what is restored.
        if (windowToken == null) {
            return
        }
        mAdapter!!.startUpdate(this)
        val pageLimit = mOffscreenPageLimit
        val startPos = Math.max(0, mCurItem - pageLimit)
        val N = mAdapter!!.count
        val endPos = Math.min(N - 1, mCurItem + pageLimit)
        if (N != mExpectedAdapterCount) {
            val resName: String
            resName = try {
                resources.getResourceName(id)
            } catch (e: NotFoundException) {
                Integer.toHexString(id)
            }
            throw IllegalStateException(
                "The application's PagerAdapter changed the adapter's" +
                        " contents without calling PagerAdapter#notifyDataSetChanged!" +
                        " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N +
                        " Pager id: " + resName +
                        " Pager class: " + javaClass +
                        " Problematic adapter: " + mAdapter!!.javaClass
            )
        }

        // Locate the currently focused item or add it if needed.
        var curIndex = -1
        var curItem: ItemInfo? = null
        curIndex = 0
        while (curIndex < mItems.size) {
            val ii = mItems[curIndex]
            if (ii.position >= mCurItem) {
                if (ii.position == mCurItem) curItem = ii
                break
            }
            curIndex++
        }
        if (curItem == null && N > 0) {
            curItem = addNewItem(mCurItem, curIndex)
        }

        // Fill 3x the available width or up to the number of offscreen
        // pages requested to either side, whichever is larger.
        // If we have no current item we have no work to do.
        if (curItem != null) {
            if (isHorizontal) {
                var extraWidthLeft = 0f
                var itemIndex = curIndex - 1
                var ii = if (itemIndex >= 0) mItems[itemIndex] else null
                val clientWidth = clientWidth
                val leftWidthNeeded: Float =
                    if (clientWidth <= 0) 0F else 2f - curItem.widthFactor + paddingLeft.toFloat() / clientWidth.toFloat()
                for (pos in mCurItem - 1 downTo 0) {
                    if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                        if (ii == null) {
                            break
                        }
                        if (pos == ii.position && !ii.scrolling) {
                            mItems.removeAt(itemIndex)
                            mAdapter!!.destroyItem(this, pos, ii.`object`!!)
                            if (DEBUG) {
                                Log.i(TAG, logDestroyItem(pos, ii.`object` as View?))
                            }
                            itemIndex--
                            curIndex--
                            ii = if (itemIndex >= 0) mItems[itemIndex] else null
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraWidthLeft += ii.widthFactor
                        itemIndex--
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    } else {
                        ii = addNewItem(pos, itemIndex + 1)
                        extraWidthLeft += ii.widthFactor
                        curIndex++
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    }
                }
                var extraWidthRight = curItem.widthFactor
                itemIndex = curIndex + 1
                if (extraWidthRight < 2f) {
                    ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    val rightWidthNeeded: Float =
                        if (clientWidth <= 0) 0f else paddingRight.toFloat() / clientWidth.toFloat() + 2f
                    for (pos in mCurItem + 1 until N) {
                        if (extraWidthRight >= rightWidthNeeded && pos > endPos) {
                            if (ii == null) {
                                break
                            }
                            if (pos == ii.position && !ii.scrolling) {
                                mItems.removeAt(itemIndex)
                                mAdapter!!.destroyItem(this, pos, ii.`object`!!)
                                if (DEBUG) {
                                    Log.i(TAG, logDestroyItem(pos, ii.`object` as View?))
                                }
                                ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                            }
                        } else if (ii != null && pos == ii.position) {
                            extraWidthRight += ii.widthFactor
                            itemIndex++
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        } else {
                            ii = addNewItem(pos, itemIndex)
                            itemIndex++
                            extraWidthRight += ii.widthFactor
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        }
                    }
                }
            } else {
                var extraHeightTop = 0f
                var itemIndex = curIndex - 1
                var ii = if (itemIndex >= 0) mItems[itemIndex] else null
                val clientHeight = clientHeight
                val topHeightNeeded: Float =
                    if (clientHeight <= 0) 0f else (2f - curItem.heightFactor
                            + paddingLeft.toFloat() / clientHeight.toFloat())
                for (pos in mCurItem - 1 downTo 0) {
                    if (extraHeightTop >= topHeightNeeded && pos < startPos) {
                        if (ii == null) {
                            break
                        }
                        if (pos == ii.position && !ii.scrolling) {
                            mItems.removeAt(itemIndex)
                            mAdapter!!.destroyItem(this, pos, ii.`object`!!)
                            if (DEBUG) {
                                Log.i(TAG, logDestroyItem(pos, ii.`object` as View?))
                            }
                            itemIndex--
                            curIndex--
                            ii = if (itemIndex >= 0) mItems[itemIndex] else null
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraHeightTop += ii.heightFactor
                        itemIndex--
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    } else {
                        ii = addNewItem(pos, itemIndex + 1)
                        extraHeightTop += ii.heightFactor
                        curIndex++
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    }
                }
                var extraHeightBottom = curItem.heightFactor
                itemIndex = curIndex + 1
                if (extraHeightBottom < 2f) {
                    ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    val bottomHeightNeeded: Float =
                        if (clientHeight <= 0) 0f else paddingRight.toFloat() / clientHeight.toFloat() + 2f
                    for (pos in mCurItem + 1 until N) {
                        if (extraHeightBottom >= bottomHeightNeeded && pos > endPos) {
                            if (ii == null) {
                                break
                            }
                            if (pos == ii.position && !ii.scrolling) {
                                mItems.removeAt(itemIndex)
                                mAdapter!!.destroyItem(this, pos, ii.`object`!!)
                                if (DEBUG) {
                                    Log.i(TAG, logDestroyItem(pos, ii.`object` as View?))
                                }
                                ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                            }
                        } else if (ii != null && pos == ii.position) {
                            extraHeightBottom += ii.heightFactor
                            itemIndex++
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        } else {
                            ii = addNewItem(pos, itemIndex)
                            itemIndex++
                            extraHeightBottom += ii.heightFactor
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        }
                    }
                }
            }
            calculatePageOffsets(curItem, curIndex, oldCurInfo)
        }
        if (DEBUG) {
            Log.i(TAG, "Current page list:")
            for (i in mItems.indices) {
                Log.i(TAG, "#" + i + ": page " + mItems[i].position)
            }
        }
        mAdapter!!.setPrimaryItem(this, mCurItem, curItem?.`object`!!)
        mAdapter!!.finishUpdate(this)

        // Check width measurement of current pages and drawing sort order.
        // Update LayoutParams as needed.
        val childCount = childCount
        if (isHorizontal) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                lp.childIndex = i
                if (!lp.isDecor && lp.widthFactor == 0f) {
                    // 0 means requery the adapter for this, it doesn't have a valid width.
                    val ii = infoForChild(child)
                    if (ii != null) {
                        lp.widthFactor = ii.widthFactor
                        lp.position = ii.position
                    }
                }
            }
            sortChildDrawingOrder()
            if (hasFocus()) {
                val currentFocused = findFocus()
                var ii = currentFocused?.let { infoForAnyChild(it) }
                if (ii == null || ii.position != mCurItem) {
                    for (i in 0 until getChildCount()) {
                        val child = getChildAt(i)
                        ii = infoForChild(child)
                        if (ii != null && ii.position == mCurItem &&
                            child.requestFocus(FOCUS_FORWARD)
                        ) {
                            break
                        }
                    }
                }
            }
        } else {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                lp.childIndex = i
                if (!lp.isDecor && lp.heightFactor == 0f) {
                    val ii = infoForChild(child)
                    if (ii != null) {
                        lp.heightFactor = ii.heightFactor
                        lp.position = ii.position
                    }
                }
            }
            sortChildDrawingOrder()
            if (hasFocus()) {
                val currentFocused = findFocus()
                var ii = currentFocused?.let { infoForAnyChild(it) }
                if (ii == null || ii.position != mCurItem) {
                    for (i in 0 until getChildCount()) {
                        val child = getChildAt(i)
                        ii = infoForChild(child)
                        if (ii != null && ii.position == mCurItem && child.requestFocus(
                                focusDirection
                            )
                        ) {
//                        if (child.requestFocus(focusDirection)) {
                            break
                            // }
                        }
                    }
                }
            }
        }
    }

    private fun sortChildDrawingOrder() {
        if (mDrawingOrder != DRAW_ORDER_DEFAULT) {
            if (mDrawingOrderedChildren == null) {
                mDrawingOrderedChildren = ArrayList()
            } else {
                mDrawingOrderedChildren!!.clear()
            }
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                mDrawingOrderedChildren!!.add(child)
            }
            Collections.sort(mDrawingOrderedChildren, sPositionComparator)
        }
    }

    private fun calculatePageOffsets(curItem: ItemInfo, curIndex: Int, oldCurInfo: ItemInfo?) {
        val N = mAdapter!!.count
        if (isHorizontal) {
            val width = clientWidth
            val marginOffset: Float = if (width > 0) mPageMargin.toFloat() / width else 0f
            // Fix up offsets for later layout.
            if (oldCurInfo != null) {
                val oldCurPosition = oldCurInfo.position
                // Base offsets off of oldCurInfo.
                if (oldCurPosition < curItem.position) {
                    var itemIndex = 0
                    var ii: ItemInfo? = null
                    var offset = oldCurInfo.offset + oldCurInfo.widthFactor + marginOffset
                    var pos = oldCurPosition + 1
                    while (pos <= curItem.position && itemIndex < mItems.size) {
                        ii = mItems[itemIndex]
                        while (pos > ii!!.position && itemIndex < mItems.size - 1) {
                            itemIndex++
                            ii = mItems[itemIndex]
                        }
                        while (pos < ii.position) {
                            // We don't have an item populated for this,
                            // ask the adapter for an offset.
                            offset += mAdapter!!.getPageWidth(pos) + marginOffset
                            pos++
                        }
                        ii.offset = offset
                        offset += ii.widthFactor + marginOffset
                        pos++
                    }
                } else if (oldCurPosition > curItem.position) {
                    var itemIndex = mItems.size - 1
                    var ii: ItemInfo? = null
                    var offset = oldCurInfo.offset
                    var pos = oldCurPosition - 1
                    while (pos >= curItem.position && itemIndex >= 0) {
                        ii = mItems[itemIndex]
                        while (pos < ii!!.position && itemIndex > 0) {
                            itemIndex--
                            ii = mItems[itemIndex]
                        }
                        while (pos > ii.position) {
                            // We don't have an item populated for this,
                            // ask the adapter for an offset.
                            offset -= mAdapter!!.getPageWidth(pos) + marginOffset
                            pos--
                        }
                        offset -= ii.widthFactor + marginOffset
                        ii.offset = offset
                        pos--
                    }
                }
            }

            // Base all offsets off of curItem.
            val itemCount = mItems.size
            var offset = curItem.offset
            var pos = curItem.position - 1
            mFirstOffset = if (curItem.position == 0) curItem.offset else -Float.MAX_VALUE
            mLastOffset =
                if (curItem.position == N - 1) curItem.offset + curItem.widthFactor - 1 else Float.MAX_VALUE
            // Previous pages
            run {
                var i = curIndex - 1
                while (i >= 0) {
                    val ii = mItems[i]
                    while (pos > ii.position) {
                        offset -= mAdapter!!.getPageWidth(pos--) + marginOffset
                    }
                    offset -= ii.widthFactor + marginOffset
                    ii.offset = offset
                    if (ii.position == 0) mFirstOffset = offset
                    i--
                    pos--
                }
            }
            offset = curItem.offset + curItem.widthFactor + marginOffset
            pos = curItem.position + 1
            // Next pages
            var i = curIndex + 1
            while (i < itemCount) {
                val ii = mItems[i]
                while (pos < ii.position) {
                    offset += mAdapter!!.getPageWidth(pos++) + marginOffset
                }
                if (ii.position == N - 1) {
                    mLastOffset = offset + ii.widthFactor - 1
                }
                ii.offset = offset
                offset += ii.widthFactor + marginOffset
                i++
                pos++
            }
        } else {
            val height = clientHeight
            val marginOffset: Float = if (height > 0) mPageMargin.toFloat() / height else 0f
            // Fix up offsets for later layout.
            if (oldCurInfo != null) {
                val oldCurPosition = oldCurInfo.position
                // Base offsets off of oldCurInfo.
                if (oldCurPosition < curItem.position) {
                    var itemIndex = 0
                    var ii: ItemInfo? = null
                    var offset = oldCurInfo.offset + oldCurInfo.heightFactor + marginOffset
                    var pos = oldCurPosition + 1
                    while (pos <= curItem.position && itemIndex < mItems.size) {
                        ii = mItems[itemIndex]
                        while (pos > ii!!.position && itemIndex < mItems.size - 1) {
                            itemIndex++
                            ii = mItems[itemIndex]
                        }
                        while (pos < ii.position) {
                            // We don't have an item populated for this,
                            // ask the adapter for an offset.
                            offset += mAdapter!!.getPageWidth(pos) + marginOffset
                            pos++
                        }
                        ii.offset = offset
                        offset += ii.heightFactor + marginOffset
                        pos++
                    }
                } else if (oldCurPosition > curItem.position) {
                    var itemIndex = mItems.size - 1
                    var ii: ItemInfo? = null
                    var offset = oldCurInfo.offset
                    var pos = oldCurPosition - 1
                    while (pos >= curItem.position && itemIndex >= 0) {
                        ii = mItems[itemIndex]
                        while (pos < ii!!.position && itemIndex > 0) {
                            itemIndex--
                            ii = mItems[itemIndex]
                        }
                        while (pos > ii.position) {
                            // We don't have an item populated for this,
                            // ask the adapter for an offset.
                            offset -= mAdapter!!.getPageWidth(pos) + marginOffset
                            pos--
                        }
                        offset -= ii.heightFactor + marginOffset
                        ii.offset = offset
                        pos--
                    }
                }
            }

            // Base all offsets off of curItem.
            val itemCount = mItems.size
            var offset = curItem.offset
            var pos = curItem.position - 1
            mFirstOffset = if (curItem.position == 0) curItem.offset else -Float.MAX_VALUE
            mLastOffset =
                if (curItem.position == N - 1) curItem.offset + curItem.heightFactor - 1 else Float.MAX_VALUE
            // Previous pages
            run {
                var i = curIndex - 1
                while (i >= 0) {
                    val ii = mItems[i]
                    while (pos > ii.position) {
                        offset -= mAdapter!!.getPageWidth(pos--) + marginOffset
                    }
                    offset -= ii.heightFactor + marginOffset
                    ii.offset = offset
                    if (ii.position == 0) mFirstOffset = offset
                    i--
                    pos--
                }
            }
            offset = curItem.offset + curItem.heightFactor + marginOffset
            pos = curItem.position + 1
            // Next pages
            var i = curIndex + 1
            while (i < itemCount) {
                val ii = mItems[i]
                while (pos < ii.position) {
                    offset += mAdapter!!.getPageWidth(pos++) + marginOffset
                }
                if (ii.position == N - 1) {
                    mLastOffset = offset + ii.heightFactor - 1
                }
                ii.offset = offset
                offset += ii.heightFactor + marginOffset
                i++
                pos++
            }
        }
        mNeedCalculatePageOffsets = false
    }

    /**
     * This is the persistent state that is saved by ViewPager.  Only needed
     * if you are creating a sublass of ViewPager that must save its own
     * state, in which case it should implement a subclass of this which
     * contains that state.
     */
    class SavedState : BaseSavedState {
        var position = 0
        var adapterState: Parcelable? = null
        var loader: ClassLoader? = null

        constructor(superState: Parcelable?) : super(superState) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(position)
            out.writeParcelable(adapterState, flags)
        }

        override fun toString(): String {
            return ("FragmentPager.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " position=" + position + "}")
        }

        internal constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`) {
            var loader = loader
            if (loader == null) {
                loader = javaClass.classLoader
            }
            position = `in`.readInt()
            adapterState = `in`.readParcelable(loader)
            this.loader = loader
        }

        companion object {
            @JvmField
            val CREATOR = ParcelableCompat
                .newCreator(object : ParcelableCompatCreatorCallbacks<SavedState?> {
                    override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                        return SavedState(`in`, loader)
                    }

                    override fun newArray(size: Int): Array<SavedState> {
                        return arrayOf()
                    }
                })
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.position = mCurItem
        if (mAdapter != null) {
            ss.adapterState = mAdapter!!.saveState()
        }
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        super.onRestoreInstanceState(ss.superState)
        if (mAdapter != null) {
            mAdapter!!.restoreState(ss.adapterState, ss.loader)
            setCurrentItemInternal(ss.position, false, true)
        } else {
            mRestoredCurItem = ss.position
            mRestoredAdapterState = ss.adapterState
            mRestoredClassLoader = ss.loader
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        var params = params
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params)
        }
        val lp = params as LayoutParams
        lp.isDecor = lp.isDecor or (child is Decor)
        if (mInLayout) {
            check(!(lp != null && lp.isDecor)) { "Cannot add pager decor view during layout" }
            lp.needsMeasure = true
            addViewInLayout(child, index, params)
        } else {
            super.addView(child, index, params)
        }
        if (USE_CACHE) {
            if (child.visibility != GONE) {
                child.isDrawingCacheEnabled = mScrollingCacheEnabled
            } else {
                child.isDrawingCacheEnabled = false
            }
        }
    }

    override fun removeView(view: View) {
        if (mInLayout) {
            removeViewInLayout(view)
        } else {
            super.removeView(view)
        }
    }

    fun infoForChild(child: View?): ItemInfo? {
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (mAdapter!!.isViewFromObject(child!!, ii.`object`!!)) {
                return ii
            }
        }
        return null
    }

    fun infoForAnyChild(child: View): ItemInfo? {
        var child = child
        var parent: ViewParent?
        while (child.parent.also { parent = it } !== this) {
            if (parent == null || parent !is View) {
                return null
            }
            child = parent as View
        }
        return infoForChild(child)
    }

    fun infoForPosition(position: Int): ItemInfo? {
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.position == position) {
                return ii
            }
        }
        return null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // For simple implementation, our internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view.  We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
        setMeasuredDimension(
            getDefaultSize(0, widthMeasureSpec),
            getDefaultSize(0, heightMeasureSpec)
        )
        var childWidthSize = 0
        var childHeightSize = 0
        if (isHorizontal) {
            val measuredWidth = measuredWidth
            val maxGutterSize = measuredWidth / 10
            mGutterSize = Math.min(maxGutterSize, mDefaultGutterSize)

            // Children are just made to fill our space.
            childWidthSize = measuredWidth - paddingLeft - paddingRight
            childHeightSize = measuredHeight - paddingTop - paddingBottom
        } else {
            val measuredHeight = measuredHeight
            val maxGutterSize = measuredHeight / 10
            mGutterSize = Math.min(maxGutterSize, mDefaultGutterSize)

            // Children are just made to fill our space.
            childWidthSize = measuredWidth - paddingLeft - paddingRight
            childHeightSize = measuredHeight - paddingTop - paddingBottom
        }

        /*
         * Make sure all children have been properly measured. Decor views first.
         * Right now we cheat and make this less complicated by assuming decor
         * views won't intersect. We will pin to edges based on gravity.
         */
        var size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                if (lp != null && lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    var widthMode = MeasureSpec.AT_MOST
                    var heightMode = MeasureSpec.AT_MOST
                    val consumeVertical = vgrav == Gravity.TOP || vgrav == Gravity.BOTTOM
                    val consumeHorizontal = hgrav == Gravity.LEFT || hgrav == Gravity.RIGHT
                    if (consumeVertical) {
                        widthMode = MeasureSpec.EXACTLY
                    } else if (consumeHorizontal) {
                        heightMode = MeasureSpec.EXACTLY
                    }
                    var widthSize = childWidthSize
                    var heightSize = childHeightSize
                    if (lp.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        widthMode = MeasureSpec.EXACTLY
                        if (lp.width != ViewGroup.LayoutParams.FILL_PARENT) {
                            widthSize = lp.width
                        }
                    }
                    if (lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        heightMode = MeasureSpec.EXACTLY
                        if (lp.height != ViewGroup.LayoutParams.FILL_PARENT) {
                            heightSize = lp.height
                        }
                    }
                    val widthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode)
                    val heightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode)
                    child.measure(widthSpec, heightSpec)
                    if (consumeVertical) {
                        childHeightSize -= child.measuredHeight
                    } else if (consumeHorizontal) {
                        childWidthSize -= child.measuredWidth
                    }
                }
            }
        }
        mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
        mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY)

        // Make sure we have created all fragments that we need to have shown.
        mInLayout = true
        populate()
        mInLayout = false

        // Page views next.
        size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                if (DEBUG) Log.v(
                    TAG, "Measuring #" + i + " " + child
                            + ": " + mChildWidthMeasureSpec
                )
                val lp = child.layoutParams as LayoutParams
                if (lp == null || !lp.isDecor) {
                    if (isHorizontal) {
                        val widthSpec = MeasureSpec.makeMeasureSpec(
                            (childWidthSize * lp.widthFactor).toInt(),
                            MeasureSpec.EXACTLY
                        )
                        child.measure(widthSpec, mChildHeightMeasureSpec)
                    } else {
                        val heightSpec = MeasureSpec.makeMeasureSpec(
                            (childHeightSize * lp.heightFactor).toInt(),
                            MeasureSpec.EXACTLY
                        )
                        child.measure(mChildWidthMeasureSpec, heightSpec)
                    }
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Make sure scroll position is set correctly.
        if (isHorizontal) {
            if (w != oldw) {
                recomputeScrollPosition(w, oldw, mPageMargin, mPageMargin, 0, 0)
            }
        } else {
            if (h != oldh) {
                recomputeScrollPosition(0, 0, mPageMargin, mPageMargin, h, oldh)
            }
        }
    }

    private fun recomputeScrollPosition(
        width: Int, oldWidth: Int, margin: Int,
        oldMargin: Int, height: Int, oldHeight: Int,
    ) {
        if (isHorizontal) {
            if (oldWidth > 0 && !mItems.isEmpty()) {
                if (!mScroller!!.isFinished) {
                    mScroller!!.finalX = currentItem * clientWidth
                } else {
                    val widthWithMargin = width - paddingLeft - paddingRight + margin
                    val oldWidthWithMargin = (oldWidth - paddingLeft - paddingRight
                            + oldMargin)
                    val xpos = scrollX
                    val pageOffset = xpos.toFloat() / oldWidthWithMargin
                    val newOffsetPixels = (pageOffset * widthWithMargin).toInt()
                    scrollTo(newOffsetPixels, scrollY)
                }
            } else {
                val ii = infoForPosition(mCurItem)
                val scrollOffset: Float = if (ii != null) Math.min(ii.offset, mLastOffset) else 0f
                val scrollPos = (scrollOffset *
                        (width - paddingLeft - paddingRight)).toInt()
                if (scrollPos != scrollX) {
                    completeScroll(false)
                    scrollTo(scrollPos, scrollY)
                }
            }
        } else {
            val heightWithMargin = height - paddingTop - paddingBottom + margin
            val oldHeightWithMargin = (oldHeight - paddingTop - paddingBottom
                    + oldMargin)
            val ypos = scrollY
            val pageOffset = ypos.toFloat() / oldHeightWithMargin
            val newOffsetPixels = (pageOffset * heightWithMargin).toInt()
            scrollTo(scrollX, newOffsetPixels)
            if (!mScroller!!.isFinished) {
                // We now return to your regularly scheduled scroll, already in progress.
                val newDuration = mScroller!!.duration - mScroller!!.timePassed()
                val targetInfo = infoForPosition(mCurItem)
                mScroller!!.startScroll(
                    0, newOffsetPixels,
                    0, (targetInfo!!.offset * height).toInt(), newDuration
                )
            } else {
                val ii = infoForPosition(mCurItem)
                val scrollOffset: Float = if (ii != null) Math.min(ii.offset, mLastOffset) else 0f
                val scrollPos = (scrollOffset *
                        (height - paddingTop - paddingBottom)).toInt()
                if (scrollPos != scrollY) {
                    completeScroll(false)
                    scrollTo(scrollX, scrollPos)
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        val height = b - t
        var paddingLeft = paddingLeft
        var paddingTop = paddingTop
        var paddingRight = paddingRight
        var paddingBottom = paddingBottom
        val scrollX = scrollX
        val scrollY = scrollY
        var decorCount = 0

        // First pass - decor views. We need to do this in two passes so that
        // we have the proper offsets for non-decor views later.
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                var childLeft = 0
                var childTop = 0
                if (lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    when (hgrav) {
                        Gravity.LEFT -> {
                            childLeft = paddingLeft
                            paddingLeft += child.measuredWidth
                        }

                        Gravity.CENTER_HORIZONTAL -> childLeft = Math.max(
                            (width - child.measuredWidth) / 2,
                            paddingLeft
                        )

                        Gravity.RIGHT -> {
                            childLeft = width - paddingRight - child.measuredWidth
                            paddingRight += child.measuredWidth
                        }

                        else -> childLeft = paddingLeft
                    }
                    when (vgrav) {
                        Gravity.TOP -> {
                            childTop = paddingTop
                            paddingTop += child.measuredHeight
                        }

                        Gravity.CENTER_VERTICAL -> childTop = Math.max(
                            (height - child.measuredHeight) / 2,
                            paddingTop
                        )

                        Gravity.BOTTOM -> {
                            childTop = height - paddingBottom - child.measuredHeight
                            paddingBottom += child.measuredHeight
                        }

                        else -> childTop = paddingTop
                    }
                    if (isHorizontal) {
                        childLeft += scrollX
                    } else {
                        childTop += scrollY
                    }
                    child.layout(
                        childLeft, childTop,
                        childLeft + child.measuredWidth,
                        childTop + child.measuredHeight
                    )
                    decorCount++
                }
            }
        }
        if (isHorizontal) {
            val childWidth = width - paddingLeft - paddingRight
            // Page views. Do this once we have the right padding offsets from above.
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child.visibility != GONE) {
                    val lp = child.layoutParams as LayoutParams
                    var ii: ItemInfo? = null
                    if (!lp.isDecor && infoForChild(child).also { ii = it!! } != null) {
                        val loff = (childWidth * ii!!.offset).toInt()
                        val childLeft = paddingLeft + loff
                        val childTop = paddingTop
                        if (lp.needsMeasure) {
                            // This was added during layout and needs measurement.
                            // Do it now that we know what we're working with.
                            lp.needsMeasure = false
                            val widthSpec = MeasureSpec.makeMeasureSpec(
                                (childWidth * lp.widthFactor).toInt(),
                                MeasureSpec.EXACTLY
                            )
                            val heightSpec = MeasureSpec.makeMeasureSpec(
                                (height - paddingTop - paddingBottom),
                                MeasureSpec.EXACTLY
                            )
                            child.measure(widthSpec, heightSpec)
                        }
                        if (DEBUG) Log.v(
                            TAG, "Positioning #" + i + " " + child + " f=" + ii!!.`object`
                                    + ":" + childLeft + "," + childTop + " " + child.measuredWidth
                                    + "x" + child.measuredHeight
                        )
                        child.layout(
                            childLeft, childTop,
                            childLeft + child.measuredWidth,
                            childTop + child.measuredHeight
                        )
                    }
                }
            }
            mTopPageBounds = paddingTop
            mBottomPageBounds = height - paddingBottom
        } else {
            val childHeight = height - paddingTop - paddingBottom
            // Page views. Do this once we have the right padding offsets from above.
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child.visibility != GONE) {
                    val lp = child.layoutParams as LayoutParams
                    var ii: ItemInfo? = null
                    if (!lp.isDecor && infoForChild(child).also { ii = it!! } != null) {
                        val toff = (childHeight * ii!!.offset).toInt()
                        val childLeft = paddingLeft
                        val childTop = paddingTop + toff
                        if (lp.needsMeasure) {
                            // This was added during layout and needs measurement.
                            // Do it now that we know what we're working with.
                            lp.needsMeasure = false
                            val widthSpec = MeasureSpec.makeMeasureSpec(
                                (width - paddingLeft - paddingRight),
                                MeasureSpec.EXACTLY
                            )
                            val heightSpec = MeasureSpec.makeMeasureSpec(
                                (childHeight * lp.heightFactor).toInt(),
                                MeasureSpec.EXACTLY
                            )
                            child.measure(widthSpec, heightSpec)
                        }
                        if (DEBUG) Log.v(
                            TAG, "Positioning #" + i + " " + child + " f=" + ii!!.`object`
                                    + ":" + childLeft + "," + childTop + " " + child.measuredWidth
                                    + "x" + child.measuredHeight
                        )
                        child.layout(
                            childLeft, childTop,
                            childLeft + child.measuredWidth,
                            childTop + child.measuredHeight
                        )
                    }
                }
            }
            mLeftPageBounds = paddingLeft
            mRightPageBounds = width - paddingRight
        }
        mDecorChildCount = decorCount
        if (mFirstLayout) {
            scrollToItem(mCurItem, false, 0, false)
        }
        mFirstLayout = false
    }

    override fun computeScroll() {
        mIsScrollStarted = true
        if (!mScroller!!.isFinished && mScroller!!.computeScrollOffset()) {
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller!!.currX
            val y = mScroller!!.currY
            if (oldX != x || oldY != y) {
                scrollTo(x, y)
                if (isHorizontal) {
                    if (!pageScrolled(x, 0)) {
                        mScroller!!.abortAnimation()
                        scrollTo(0, y)
                    }
                } else {
                    if (!pageScrolled(0, y)) {
                        mScroller!!.abortAnimation()
                        scrollTo(x, 0)
                    }
                }
            }

            // Keep on drawing until the animation has finished.
            ViewCompat.postInvalidateOnAnimation(this)
            return
        }

        // Done with scroll, clean up state.
        completeScroll(true)
    }

    private fun pageScrolled(xpos: Int, ypos: Int): Boolean {
        if (mItems.size == 0) {
            if (mFirstLayout) {
                // If we haven't been laid out yet, we probably just haven't been populated yet.
                // Let's skip this call since it doesn't make sense in this state
                return false
            }
            mCalledSuper = false
            onPageScrolled(0, 0f, 0)
            check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
            return false
        }
        val ii = infoForCurrentScrollPosition()
        var currentPage = 0
        var pageOffset = 0f
        var offsetPixels = 0
        if (isHorizontal) {
            val width = clientWidth
            val widthWithMargin = width + mPageMargin
            val marginOffset = mPageMargin.toFloat() / width
            currentPage = ii!!.position
            pageOffset = (xpos.toFloat() / width - ii.offset) /
                    (ii.widthFactor + marginOffset)
            offsetPixels = (pageOffset * widthWithMargin).toInt()
        } else {
            val height = clientHeight
            val heightWithMargin = height + mPageMargin
            val marginOffset = mPageMargin.toFloat() / height
            currentPage = ii!!.position
            pageOffset = (ypos.toFloat() / height - ii.offset) /
                    (ii.heightFactor + marginOffset)
            offsetPixels = (pageOffset * heightWithMargin).toInt()
        }
        mCalledSuper = false
        onPageScrolled(currentPage, pageOffset, offsetPixels)
        check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
        return true
    }

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     * If you override this method you must call through to the superclass implementation
     * (e.g. super.onPageScrolled(position, offset, offsetPixels)) before onPageScrolled
     * returns.
     *
     * @param position     Position index of the first page currently being displayed.
     * Page position+1 will be visible if positionOffset is nonzero.
     * @param offset       Value from [0, 1) indicating the offset from the page at position.
     * @param offsetPixels Value in pixels indicating the offset from position.
     */
    @CallSuper
    protected fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        // Offset any decor views if needed - keep them on-screen at all times.
        if (isHorizontal) {
            if (mDecorChildCount > 0) {
                val scrollX = scrollX
                var paddingLeft = paddingLeft
                var paddingRight = paddingRight
                val width = width
                val childCount = childCount
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val lp = child.layoutParams as LayoutParams
                    if (!lp.isDecor) continue
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    var childLeft = 0
                    when (hgrav) {
                        Gravity.LEFT -> {
                            childLeft = paddingLeft
                            paddingLeft += child.width
                        }

                        Gravity.CENTER_HORIZONTAL -> childLeft = Math.max(
                            (width - child.measuredWidth) / 2,
                            paddingLeft
                        )

                        Gravity.RIGHT -> {
                            childLeft = width - paddingRight - child.measuredWidth
                            paddingRight += child.measuredWidth
                        }

                        else -> childLeft = paddingLeft
                    }
                    childLeft += scrollX
                    val childOffset = childLeft - child.left
                    if (childOffset != 0) {
                        child.offsetLeftAndRight(childOffset)
                    }
                }
            }
            dispatchOnPageScrolled(position, offset, offsetPixels)
            if (mPageTransformer != null) {
                val scrollX = scrollX
                val childCount = childCount
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val lp = child.layoutParams as LayoutParams
                    if (lp.isDecor) continue
                    val transformPos = (child.left - scrollX).toFloat() / clientWidth
                    mPageTransformer!!.transformPage(child, transformPos)
                }
            }
        } else {
            if (mDecorChildCount > 0) {
                val scrollY = scrollY
                var paddingTop = paddingTop
                var paddingBottom = paddingBottom
                val height = height
                val childCount = childCount
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val lp = child.layoutParams as LayoutParams
                    if (!lp.isDecor) continue
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    var childTop = 0
                    when (vgrav) {
                        Gravity.TOP -> {
                            childTop = paddingTop
                            paddingTop += child.height
                        }

                        Gravity.CENTER_VERTICAL -> childTop = Math.max(
                            (height - child.measuredHeight) / 2,
                            paddingTop
                        )

                        Gravity.BOTTOM -> {
                            childTop = height - paddingBottom - child.measuredHeight
                            paddingBottom += child.measuredHeight
                        }

                        else -> childTop = paddingTop
                    }
                    childTop += scrollY
                    val childOffset = childTop - child.top
                    if (childOffset != 0) {
                        child.offsetTopAndBottom(childOffset)
                    }
                }
            }
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
            }
            if (mInternalPageChangeListener != null) {
                mInternalPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
            }
            if (mPageTransformer != null) {
                val scrollY = scrollY
                val childCount = childCount
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val lp = child.layoutParams as LayoutParams
                    if (lp.isDecor) continue
                    val transformPos = (child.top - scrollY).toFloat() / clientHeight
                    mPageTransformer!!.transformPage(child, transformPos)
                }
            }
        }
        mCalledSuper = true
    }

    private fun dispatchOnPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
        }
        if (mOnPageChangeListeners != null) {
            var i = 0
            val z = mOnPageChangeListeners!!.size
            while (i < z) {
                val listener = mOnPageChangeListeners!![i]
                if (listener != null) {
                    listener.onPageScrolled(position, offset, offsetPixels)
                }
                i++
            }
        }
        if (mInternalPageChangeListener != null) {
            mInternalPageChangeListener!!.onPageScrolled(position, offset, offsetPixels)
        }
    }

    private fun dispatchOnPageSelected(position: Int) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageSelected(position)
        }
        if (mOnPageChangeListeners != null) {
            var i = 0
            val z = mOnPageChangeListeners!!.size
            while (i < z) {
                val listener = mOnPageChangeListeners!![i]
                if (listener != null) {
                    listener.onPageSelected(position)
                }
                i++
            }
        }
        if (mInternalPageChangeListener != null) {
            mInternalPageChangeListener!!.onPageSelected(position)
        }
    }

    private fun dispatchOnScrollStateChanged(state: Int) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener!!.onPageScrollStateChanged(state)
        }
        if (mOnPageChangeListeners != null) {
            var i = 0
            val z = mOnPageChangeListeners!!.size
            while (i < z) {
                val listener = mOnPageChangeListeners!![i]
                if (listener != null) {
                    listener.onPageScrollStateChanged(state)
                }
                i++
            }
        }
        if (mInternalPageChangeListener != null) {
            mInternalPageChangeListener!!.onPageScrollStateChanged(state)
        }
    }

    private fun completeScroll(postEvents: Boolean) {
        var needPopulate = mScrollState == SCROLL_STATE_SETTLING
        if (needPopulate) {
            // Done with scroll, no longer want to cache view drawing.
            setScrollingCacheEnabled(false)
            val wasScrolling = !mScroller!!.isFinished
            if (wasScrolling) {
                mScroller!!.abortAnimation()
                val oldX = scrollX
                val oldY = scrollY
                val x = mScroller!!.currX
                val y = mScroller!!.currY
                if (oldX != x || oldY != y) {
                    scrollTo(x, y)
                    if (isHorizontal && x != oldX) {
                        pageScrolled(x, 0)
                    }
                }
            }
        }
        mPopulatePending = false
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.scrolling) {
                needPopulate = true
                ii.scrolling = false
            }
        }
        if (needPopulate) {
            if (postEvents) {
                ViewCompat.postOnAnimation(this, mEndScrollRunnable)
            } else {
                mEndScrollRunnable.run()
            }
        }
    }

    private fun isGutterDrag(x: Float, dx: Float, y: Float, dy: Float): Boolean {
        return if (isHorizontal) {
            x < mGutterSize && dx > 0 || x > width - mGutterSize && dx < 0
        } else {
            y < mGutterSize && dy > 0 || y > height - mGutterSize && dy < 0
        }
    }

    @SuppressLint("WrongConstant")
    private fun enableLayers(enable: Boolean) {
        val childCount = childCount
        for (i in 0 until childCount) {
            val layerType =
                if (enable) ViewCompat.LAYER_TYPE_HARDWARE else ViewCompat.LAYER_TYPE_NONE
            ViewCompat.setLayerType(getChildAt(i), layerType, null)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */
        val action = ev.action and MotionEventCompat.ACTION_MASK

        // Always take care of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the drag.
            if (DEBUG) Log.v(TAG, "Intercept done!")
            if (isHorizontal) {
                resetTouch()
            } else {
                mIsBeingDragged = false
                mIsUnableToDrag = false
                mActivePointerId = INVALID_POINTER
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.recycle()
                    mVelocityTracker = null
                }
            }
            return false
        }

        // Nothing more to do here if we have decided whether or not we
        // are dragging.
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) {
                if (DEBUG) Log.v(TAG, "Intercept returning true!")
                return true
            }
            if (mIsUnableToDrag) {
                if (DEBUG) Log.v(TAG, "Intercept returning false!")
                return false
            }
        }
        if (isHorizontal) {
            when (action) {
                MotionEvent.ACTION_MOVE -> {

                    /*
                     * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                     * whether the user has moved far enough from his original down touch.
                     */

                    /*
                     * Locally do absolute value. mLastMotionY is set to the y value
                     * of the down event.
                     */
                    val activePointerId = mActivePointerId
                    if (activePointerId != INVALID_POINTER) {


                        val pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)
                        val x = MotionEventCompat.getX(ev, pointerIndex)
                        val dx = x - mLastMotionX
                        val xDiff = Math.abs(dx)
                        val y = MotionEventCompat.getY(ev, pointerIndex)
                        val yDiff = Math.abs(y - mInitialMotionY)
                        if (dx != 0f && !isGutterDrag(mLastMotionX, dx, 0f, 0f) &&
                            canScroll(this, false, dx.toInt(), 0, x.toInt(), y.toInt())
                        ) {
                            // Nested view has scrollable
                            // area under this point. Let it be handled there.
                            mLastMotionX = x
                            mLastMotionY = y
                            mIsUnableToDrag = true
                            return false
                        }
                        if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                            if (DEBUG) Log.v(TAG, context.getString(R.string.debug_start_drag))
                            mIsBeingDragged = true
                            requestParentDisallowInterceptTouchEvent(true)
                            setScrollState(SCROLL_STATE_DRAGGING)
                            mLastMotionX =
                                if (dx > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                            mLastMotionY = y
                            setScrollingCacheEnabled(true)
                        } else if (yDiff > mTouchSlop) {
                            // The finger has moved enough in the vertical
                            // direction to be counted as a drag...  abort
                            // any attempt to drag horizontally, to work correctly
                            // with children that have scrolling containers.
                            if (DEBUG) Log.v(
                                TAG,
                                context.getString(R.string.debug_start_unable_drag)
                            )
                            mIsUnableToDrag = true
                        }
                        if (mIsBeingDragged && performDrag(x, 0f)) {
                            // Scroll to follow the motion event
                            ViewCompat.postInvalidateOnAnimation(this)
                        }
                    }
                }

                MotionEvent.ACTION_DOWN -> {

                    /*
                     * Remember location of down touch.
                     * ACTION_DOWN always refers to pointer index 0.
                     */mInitialMotionX = ev.x
                    mLastMotionX = mInitialMotionX
                    mInitialMotionY = ev.y
                    mLastMotionY = mInitialMotionY
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                    mIsUnableToDrag = false
                    mIsScrollStarted = true
                    mScroller!!.computeScrollOffset()
                    if (mScrollState == SCROLL_STATE_SETTLING &&
                        Math.abs(mScroller!!.finalX - mScroller!!.currX) > mCloseEnough
                    ) {
                        // Let the user 'catch' the pager as it animates.
                        mScroller!!.abortAnimation()
                        mPopulatePending = false
                        populate()
                        mIsBeingDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        setScrollState(SCROLL_STATE_DRAGGING)
                    } else {
                        completeScroll(false)
                        mIsBeingDragged = false
                    }
                    if (DEBUG) Log.v(
                        TAG, "Down at " + mLastMotionX + "," + mLastMotionY
                                + " mIsBeingDragged=" + mIsBeingDragged
                                + "mIsUnableToDrag=" + mIsUnableToDrag
                    )
                }

                MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            }

            /* if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
*/
            /*
             * The only time we want to intercept motion events is if we are in the
             * drag mode.
             */
        } else {
            when (action) {
                MotionEvent.ACTION_MOVE -> {

                    /*
                     * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                     * whether the user has moved far enough from his original down touch.
                     */

                    /*
                     * Locally do absolute value. mLastMotionY is set to the y value
                     * of the down event.
                     */
                    val activePointerId = mActivePointerId
                    if (activePointerId != INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.


                        val pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId)
                        val y = MotionEventCompat.getY(ev, pointerIndex)
                        val dy = y - mLastMotionY
                        val yDiff = Math.abs(dy)
                        val x = MotionEventCompat.getX(ev, pointerIndex)
                        val xDiff = Math.abs(x - mInitialMotionX)
                        if (dy != 0f && !isGutterDrag(0f, 0f, mLastMotionY, dy) &&
                            canScroll(this, false, 0, dy.toInt(), x.toInt(), y.toInt())
                        ) {
                            // Nested view has scrollable
                            // area under this point.
                            // Let it be handled there.
                            mLastMotionX = x
                            mLastMotionY = y
                            mIsUnableToDrag = true
                            return false
                        }
                        if (yDiff > mTouchSlop && yDiff * 0.5f > xDiff) {
                            if (DEBUG) Log.v(TAG, context.getString(R.string.debug_start_drag))
                            mIsBeingDragged = true
                            requestParentDisallowInterceptTouchEvent(true)
                            setScrollState(SCROLL_STATE_DRAGGING)
                            mLastMotionY =
                                if (dy > 0) mInitialMotionY + mTouchSlop else mInitialMotionY - mTouchSlop
                            mLastMotionX = x
                            setScrollingCacheEnabled(true)
                        } else if (xDiff > mTouchSlop) {
                            // The finger has moved enough in the vertical
                            // direction to be counted as a drag...  abort
                            // any attempt to drag horizontally, to work correctly
                            // with children that have scrolling containers.
                            if (DEBUG) Log.v(
                                TAG,
                                context.getString(R.string.debug_start_unable_drag)
                            )
                            mIsUnableToDrag = true
                        }
                        if (mIsBeingDragged && performDrag(0f, y)) {
                            // Scroll to follow the motion event
                            ViewCompat.postInvalidateOnAnimation(this)
                        }
                    }
                }

                MotionEvent.ACTION_DOWN -> {

                    /*
                     * Remember location of down touch.
                     * ACTION_DOWN always refers to pointer index 0.
                     */mInitialMotionX = ev.x
                    mLastMotionX = mInitialMotionX
                    mInitialMotionY = ev.y
                    mLastMotionY = mInitialMotionY
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                    mIsUnableToDrag = false
                    mScroller!!.computeScrollOffset()
                    if (mScrollState == SCROLL_STATE_SETTLING &&
                        Math.abs(mScroller!!.finalY - mScroller!!.currY) > mCloseEnough
                    ) {
                        // Let the user 'catch' the pager as it animates.
                        mScroller!!.abortAnimation()
                        mPopulatePending = false
                        populate()
                        mIsBeingDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        setScrollState(SCROLL_STATE_DRAGGING)
                    } else {
                        completeScroll(false)
                        mIsBeingDragged = false
                    }
                    if (DEBUG) Log.v(
                        TAG, "Down at " + mLastMotionX + "," + mLastMotionY
                                + " mIsBeingDragged=" + mIsBeingDragged
                                + "mIsUnableToDrag=" + mIsUnableToDrag
                    )
                }

                MotionEventCompat.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            }
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isFakeDragging) {
            // A fake drag is in progress already, ignore this real one
            // but still eat the touch events.
            // (It is likely that the user is multi-touching the screen.)
            return true
        }
        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false
        }
        if (mAdapter == null || mAdapter!!.count == 0) {
            // Nothing to present or scroll; nothing to touch.
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)
        val action = ev.action
        var needsInvalidate = false
        if (isHorizontal) {
            when (action and MotionEventCompat.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    mScroller!!.abortAnimation()
                    mPopulatePending = false
                    populate()

                    // Remember where the motion event started
                    mInitialMotionX = ev.x
                    mLastMotionX = mInitialMotionX
                    mInitialMotionY = ev.y
                    mLastMotionY = mInitialMotionY
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!mIsBeingDragged) {
                        val pointerIndex = MotionEventCompat.findPointerIndex(
                            ev,
                            mActivePointerId
                        )
                        if (pointerIndex == -1) {
                            // A child has consumed some
                            // touch events and put us into an inconsistent state.
                            needsInvalidate = resetTouch()
                        } else {


                            val x = MotionEventCompat.getX(ev, pointerIndex)
                            val xDiff = Math.abs(x - mLastMotionX)
                            val y = MotionEventCompat.getY(
                                ev,
                                pointerIndex
                            )
                            val yDiff = Math.abs(y - mLastMotionY)
                            if (xDiff > mTouchSlop && xDiff > yDiff) {
                                if (DEBUG) Log.v(TAG, context.getString(R.string.debug_start_drag))
                                mIsBeingDragged = true
                                requestParentDisallowInterceptTouchEvent(true)
                                mLastMotionX =
                                    if (x - mInitialMotionX > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                                mLastMotionY = y
                                setScrollState(SCROLL_STATE_DRAGGING)
                                setScrollingCacheEnabled(true)

                                // Disallow Parent Intercept, just in case
                                val parent = parent
                                parent?.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                        // Not else! Note that mIsBeingDragged can be set above.
                        if (mIsBeingDragged) {
                            // Scroll to follow the motion event
                            val activePointerIndex = MotionEventCompat.findPointerIndex(
                                ev, mActivePointerId
                            )
                            val x = MotionEventCompat.getX(ev, activePointerIndex)
                            needsInvalidate = needsInvalidate or performDrag(x, 0f)
                        }
                    }
                }

                MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                    val velocityTracker = mVelocityTracker
                    velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity = VelocityTrackerCompat.getXVelocity(
                        velocityTracker, mActivePointerId
                    ).toInt()
                    mPopulatePending = true
                    val width = clientWidth
                    val scrollX = scrollX
                    val ii = infoForCurrentScrollPosition()
                    val marginOffset = mPageMargin.toFloat() / width
                    val currentPage = ii!!.position
                    val pageOffset = ((scrollX.toFloat() / width - ii.offset)
                            / (ii.widthFactor + marginOffset))
                    val activePointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                    val x = MotionEventCompat.getX(ev, activePointerIndex)
                    val totalDelta = (x - mInitialMotionX).toInt()
                    val nextPage = determineTargetPage(
                        currentPage, pageOffset, initialVelocity,
                        totalDelta, 0
                    )
                    setCurrentItemInternal(nextPage, true, true, initialVelocity)
                    needsInvalidate = resetTouch()
                }

                MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                    scrollToItem(mCurItem, true, 0, false)
                    needsInvalidate = resetTouch()
                }

                MotionEventCompat.ACTION_POINTER_DOWN -> {
                    val index = MotionEventCompat.getActionIndex(ev)
                    val x = MotionEventCompat.getX(ev, index)
                    mLastMotionX = x
                    mActivePointerId = MotionEventCompat.getPointerId(ev, index)
                }

                MotionEventCompat.ACTION_POINTER_UP -> {
                    onSecondaryPointerUp(ev)
                    mLastMotionX = MotionEventCompat.getX(
                        ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                    )
                }
            }
        } else {
            when (action and MotionEventCompat.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    mScroller!!.abortAnimation()
                    mPopulatePending = false
                    populate()

                    // Remember where the motion event started
                    mInitialMotionX = ev.x
                    mLastMotionX = mInitialMotionX
                    mInitialMotionY = ev.y
                    mLastMotionY = mInitialMotionY
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!mIsBeingDragged) {
                        val pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                        val y = MotionEventCompat.getY(ev, pointerIndex)
                        val yDiff = Math.abs(y - mLastMotionY)
                        val x = MotionEventCompat.getX(ev, pointerIndex)
                        val xDiff = Math.abs(x - mLastMotionX)
                        if (yDiff > mTouchSlop && yDiff > xDiff) {
                            if (DEBUG) Log.v(TAG, context.getString(R.string.debug_start_drag))
                            mIsBeingDragged = true
                            requestParentDisallowInterceptTouchEvent(true)
                            mLastMotionY =
                                if (y - mInitialMotionY > 0) mInitialMotionY + mTouchSlop else mInitialMotionY - mTouchSlop
                            mLastMotionX = x
                            setScrollState(SCROLL_STATE_DRAGGING)
                            setScrollingCacheEnabled(true)

                            // Disallow Parent Intercept, just in case
                            val parent = parent
                            parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    // Not else! Note that mIsBeingDragged can be set above.
                    if (mIsBeingDragged) {
                        // Scroll to follow the motion event
                        val activePointerIndex = MotionEventCompat.findPointerIndex(
                            ev, mActivePointerId
                        )
                        val y = MotionEventCompat.getY(ev, activePointerIndex)
                        needsInvalidate = needsInvalidate or performDrag(0f, y)
                    }
                }

                MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                    val velocityTracker = mVelocityTracker
                    velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity = VelocityTrackerCompat.getYVelocity(
                        velocityTracker, mActivePointerId
                    ).toInt()
                    mPopulatePending = true
                    val height = clientHeight
                    val scrollY = scrollY
                    val ii = infoForCurrentScrollPosition()
                    val currentPage = ii!!.position
                    val pageOffset = (scrollY.toFloat() / height - ii.offset) / ii.heightFactor
                    val activePointerIndex =
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                    val y = MotionEventCompat.getY(ev, activePointerIndex)
                    val totalDelta = (y - mInitialMotionY).toInt()
                    val nextPage = determineTargetPage(
                        currentPage, pageOffset, initialVelocity,
                        0, totalDelta
                    )
                    setCurrentItemInternal(nextPage, true, true, initialVelocity)
                    mActivePointerId = INVALID_POINTER
                    endDrag()
                    needsInvalidate = mTopEdge!!.onRelease() or mBottomEdge!!.onRelease()
                }

                MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                    scrollToItem(mCurItem, true, 0, false)
                    mActivePointerId = INVALID_POINTER
                    endDrag()
                    needsInvalidate = mTopEdge!!.onRelease() or mBottomEdge!!.onRelease()
                }

                MotionEventCompat.ACTION_POINTER_DOWN -> {
                    val index = MotionEventCompat.getActionIndex(ev)
                    val y = MotionEventCompat.getY(ev, index)
                    mLastMotionY = y
                    mActivePointerId = MotionEventCompat.getPointerId(ev, index)
                }

                MotionEventCompat.ACTION_POINTER_UP -> {
                    onSecondaryPointerUp(ev)
                    mLastMotionY = MotionEventCompat.getY(
                        ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId)
                    )
                }
            }
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        return true
    }

    private fun resetTouch(): Boolean {
        val needsInvalidate: Boolean
        mActivePointerId = INVALID_POINTER
        endDrag()
        needsInvalidate = mLeftEdge!!.onRelease() or mRightEdge!!.onRelease()
        return needsInvalidate
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun performDrag(x: Float, y: Float): Boolean {
        var needsInvalidate = false
        if (isHorizontal) {
            val deltaX = mLastMotionX - x
            mLastMotionX = x
            val oldScrollX = scrollX.toFloat()
            var scrollX = oldScrollX + deltaX
            val width = clientWidth
            var leftBound = width * mFirstOffset
            var rightBound = width * mLastOffset
            var leftAbsolute = true
            var rightAbsolute = true
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                leftAbsolute = false
                leftBound = firstItem.offset * width
            }
            if (lastItem.position != mAdapter!!.count - 1) {
                rightAbsolute = false
                rightBound = lastItem.offset * width
            }
            if (scrollX < leftBound) {
                if (leftAbsolute) {
                    val over = leftBound - scrollX
                    needsInvalidate = mLeftEdge!!.onPull(Math.abs(over) / width)
                }
                scrollX = leftBound
            } else if (scrollX > rightBound) {
                if (rightAbsolute) {
                    val over = scrollX - rightBound
                    needsInvalidate = mRightEdge!!.onPull(Math.abs(over) / width)
                }
                scrollX = rightBound
            }
            // Don't lose the rounded component
            mLastMotionX += scrollX - scrollX.toInt()
            scrollTo(scrollX.toInt(), scrollY)
            pageScrolled(scrollX.toInt(), 0)
        } else {
            val deltaY = mLastMotionY - y
            mLastMotionY = y
            val oldScrollY = scrollY.toFloat()
            var scrollY = oldScrollY + deltaY
            val height = clientHeight
            var topBound = height * mFirstOffset
            var bottomBound = height * mLastOffset
            var topAbsolute = true
            var bottomAbsolute = true
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                topAbsolute = false
                topBound = firstItem.offset * height
            }
            if (lastItem.position != mAdapter!!.count - 1) {
                bottomAbsolute = false
                bottomBound = lastItem.offset * height
            }
            if (scrollY < topBound) {
                if (topAbsolute) {
                    val over = topBound - scrollY
                    needsInvalidate = mTopEdge!!.onPull(Math.abs(over) / height)
                }
                scrollY = topBound
            } else if (scrollY > bottomBound) {
                if (bottomAbsolute) {
                    val over = scrollY - bottomBound
                    needsInvalidate = mBottomEdge!!.onPull(Math.abs(over) / height)
                }
                scrollY = bottomBound
            }
            // Don't lose the rounded component
            mLastMotionX += scrollY - scrollY.toInt()
            scrollTo(scrollX, scrollY.toInt())
            pageScrolled(0, scrollY.toInt())
        }
        return needsInvalidate
    }

    /**
     * @return Info about the page at the current scroll position.
     * This can be synthetic for a missing middle page; the 'object' field can be null.
     */
    private fun infoForCurrentScrollPosition(): ItemInfo? {
        var lastItem: ItemInfo? = null
        if (isHorizontal) {
            val width = clientWidth
            val scrollOffset: Float = if (width > 0) scrollX.toFloat() / width else 0f
            val marginOffset: Float = if (width > 0) mPageMargin.toFloat() / width else 0f
            var lastPos = -1
            var lastOffset = 0f
            var lastWidth = 0f
            var first = true
            var i = 0
            while (i < mItems.size) {
                var ii = mItems[i]
                var offset: Float
                if (!first && ii.position != lastPos + 1) {
                    // Create a synthetic item for a missing page.
                    ii = mTempItem
                    ii.offset = lastOffset + lastWidth + marginOffset
                    ii.position = lastPos + 1
                    ii.widthFactor = mAdapter!!.getPageWidth(ii.position)
                    i--
                }
                offset = ii.offset
                val leftBound = offset
                val rightBound = offset + ii.widthFactor + marginOffset
                if (first || scrollOffset >= leftBound) {
                    if (scrollOffset < rightBound || i == mItems.size - 1) {
                        return ii
                    }
                } else {
                    return lastItem
                }
                first = false
                lastPos = ii.position
                lastOffset = offset
                lastWidth = ii.widthFactor
                lastItem = ii
                i++
            }
        } else {
            val height = clientHeight
            val scrollOffset: Float = if (height > 0) scrollY.toFloat() / height else 0f
            val marginOffset: Float = if (height > 0) mPageMargin.toFloat() / height else 0f
            var lastPos = -1
            var lastOffset = 0f
            var lastHeight = 0f
            var first = true
            var i = 0
            while (i < mItems.size) {
                var ii = mItems[i]
                var offset: Float
                if (!first && ii.position != lastPos + 1) {
                    // Create a synthetic item for a missing page.
                    ii = mTempItem
                    ii.offset = lastOffset + lastHeight + marginOffset
                    ii.position = lastPos + 1
                    ii.heightFactor = mAdapter!!.getPageWidth(ii.position)
                    i--
                }
                offset = ii.offset
                val topBound = offset
                val bottomBound = offset + ii.heightFactor + marginOffset
                if (first || scrollOffset >= topBound) {
                    if (scrollOffset < bottomBound || i == mItems.size - 1) {
                        return ii
                    }
                } else {
                    return lastItem
                }
                first = false
                lastPos = ii.position
                lastOffset = offset
                lastHeight = ii.heightFactor
                lastItem = ii
                i++
            }
        }
        return lastItem
    }

    private fun determineTargetPage(
        currentPage: Int, pageOffset: Float,
        velocity: Int, deltaX: Int, deltaY: Int,
    ): Int {
        var targetPage: Int
        targetPage = if (isHorizontal) {
            if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
                if (velocity > 0) currentPage else currentPage + 1
            } else {
                val truncator = if (currentPage >= mCurItem) 0.4f else 0.6f
                (currentPage + pageOffset + truncator).toInt()
            }
        } else {
            if (Math.abs(deltaY) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
                if (velocity > 0) currentPage else currentPage + 1
            } else {
                val truncator = if (currentPage >= mCurItem) 0.4f else 0.6f
                (currentPage + pageOffset + truncator).toInt()
            }
        }
        if (mItems.size > 0) {
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]

            // Only let the user target pages we have items for
            targetPage = Math.max(firstItem.position, Math.min(targetPage, lastItem.position))
        }
        return targetPage
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var needsInvalidate = false
        val overScrollMode = ViewCompat.getOverScrollMode(this)
        if ((overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS || overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && mAdapter != null && (mAdapter!!.count) > 1)) {
            if (isHorizontal) {
                if (!mLeftEdge!!.isFinished) {
                    val restoreCount = canvas.save()
                    val height = height - paddingTop - paddingBottom
                    val width = width
                    canvas.rotate(270f)
                    canvas.translate((-height + paddingTop).toFloat(), mFirstOffset * width)
                    mLeftEdge!!.setSize(height, width)
                    needsInvalidate = needsInvalidate or mLeftEdge!!.draw(canvas)
                    canvas.restoreToCount(restoreCount)
                }
                if (!mRightEdge!!.isFinished) {
                    val restoreCount = canvas.save()
                    val width = width
                    val height = height - paddingTop - paddingBottom
                    canvas.rotate(90f)
                    canvas.translate(-paddingTop.toFloat(), -(mLastOffset + 1) * width)
                    mRightEdge!!.setSize(height, width)
                    needsInvalidate = needsInvalidate or mRightEdge!!.draw(canvas)
                    canvas.restoreToCount(restoreCount)
                } else {
                    mLeftEdge!!.finish()
                    mRightEdge!!.finish()
                }
            } else {
                if (!mTopEdge!!.isFinished) {
                    val restoreCount = canvas.save()
                    val height = height
                    val width = width - paddingLeft - paddingRight
                    canvas.translate(paddingLeft.toFloat(), mFirstOffset * height)
                    mTopEdge!!.setSize(width, height)
                    needsInvalidate = needsInvalidate or mTopEdge!!.draw(canvas)
                    canvas.restoreToCount(restoreCount)
                }
                if (!mBottomEdge!!.isFinished) {
                    val restoreCount = canvas.save()
                    val height = height
                    val width = width - paddingLeft - paddingRight
                    canvas.rotate(180f)
                    canvas.translate((-width - paddingLeft).toFloat(), -(mLastOffset + 1) * height)
                    mBottomEdge!!.setSize(width, height)
                    needsInvalidate = needsInvalidate or mBottomEdge!!.draw(canvas)
                    canvas.restoreToCount(restoreCount)
                } else {
                    mTopEdge!!.finish()
                    mBottomEdge!!.finish()
                }
            }
        }
        if (needsInvalidate) {
            // Keep animating
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the margin drawable between pages if needed.
        if (mPageMargin > 0 && mMarginDrawable != null && mItems.size > 0 && mAdapter != null) {
            if (isHorizontal) {
                val scrollX = scrollX
                val width = width
                val marginOffset = mPageMargin.toFloat() / width
                var itemIndex = 0
                var ii = mItems[0]
                var offset = ii.offset
                val itemCount = mItems.size
                val firstPos = ii.position
                val lastPos = mItems[itemCount - 1].position
                for (pos in firstPos until lastPos) {
                    while (pos > ii.position && itemIndex < itemCount) {
                        ii = mItems[++itemIndex]
                    }
                    var drawAt: Float
                    if (pos == ii.position) {
                        drawAt = (ii.offset + ii.widthFactor) * width
                        offset = ii.offset + ii.widthFactor + marginOffset
                    } else {
                        val widthFactor = mAdapter!!.getPageWidth(pos)
                        drawAt = (offset + widthFactor) * width
                        offset += widthFactor + marginOffset
                    }
                    if (drawAt + mPageMargin > scrollX) {
                        mMarginDrawable!!.setBounds(
                            Math.round(drawAt), mTopPageBounds,
                            Math.round(drawAt + mPageMargin), mBottomPageBounds
                        )
                        mMarginDrawable!!.draw(canvas)
                    }
                    if (drawAt > scrollX + width) {
                        break // No more visible, no sense in continuing
                    }
                }
            } else {
                val scrollY = scrollY
                val height = height
                val marginOffset = mPageMargin.toFloat() / height
                var itemIndex = 0
                var ii = mItems[0]
                var offset = ii.offset
                val itemCount = mItems.size
                val firstPos = ii.position
                val lastPos = mItems[itemCount - 1].position
                for (pos in firstPos until lastPos) {
                    while (pos > ii.position && itemIndex < itemCount) {
                        ii = mItems[++itemIndex]
                    }
                    var drawAt: Float
                    if (pos == ii.position) {
                        drawAt = (ii.offset + ii.heightFactor) * height
                        offset = ii.offset + ii.heightFactor + marginOffset
                    } else {
                        val heightFactor = mAdapter!!.getPageWidth(pos)
                        drawAt = (offset + heightFactor) * height
                        offset += heightFactor + marginOffset
                    }
                    if (drawAt + mPageMargin > scrollY) {
                        mMarginDrawable!!.setBounds(
                            mLeftPageBounds, drawAt.toInt(),
                            mRightPageBounds, (drawAt + mPageMargin + 0.5f).toInt()
                        )
                        mMarginDrawable!!.draw(canvas)
                    }
                    if (drawAt > scrollY + height) {
                        break // No more visible, no sense in continuing
                    }
                }
            }
        }
    }

    /**
     * Start a fake drag of the pager.
     *
     *
     *
     * A fake drag can be useful if you want to synchronize the motion of the ViewPager
     * with the touch scrolling of another view, while still letting the ViewPager
     * control the snapping motion and fling behavior. (e.g. parallax-scrolling tabs.)
     * Call [.fakeDragBy] to simulate the actual drag motion. Call
     * [.endFakeDrag] to complete the fake drag and fling as necessary.
     *
     *
     *
     * During a fake drag the ViewPager will ignore all touch events. If a real drag
     * is already in progress, this method will return false.
     *
     * @return true if the fake drag began successfully, false if it could not be started.
     * @see .fakeDragBy
     * @see .endFakeDrag
     */
    fun beginFakeDrag(): Boolean {
        if (mIsBeingDragged) {
            return false
        }
        isFakeDragging = true
        setScrollState(SCROLL_STATE_DRAGGING)
        if (isHorizontal) {
            mLastMotionX = 0f
            mInitialMotionX = mLastMotionX
        } else {
            mLastMotionY = 0f
            mInitialMotionY = mLastMotionY
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker!!.clear()
        }
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        mVelocityTracker!!.addMovement(ev)
        ev.recycle()
        mFakeDragBeginTime = time
        return true
    }

    /**
     * End a fake drag of the pager.
     *
     * @see .beginFakeDrag
     * @see .endFakeDrag
     */
    fun endFakeDrag() {
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }
        if (mAdapter != null) {
            if (isHorizontal) {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = VelocityTrackerCompat.getXVelocity(
                    velocityTracker, mActivePointerId
                ).toInt()
                mPopulatePending = true
                val width = clientWidth
                val scrollX = scrollX
                val ii = infoForCurrentScrollPosition()
                val currentPage = ii!!.position
                val pageOffset = (scrollX.toFloat() / width - ii.offset) / ii.widthFactor
                val totalDelta = (mLastMotionX - mInitialMotionX).toInt()
                val nextPage = determineTargetPage(
                    currentPage, pageOffset, initialVelocity,
                    totalDelta, 0
                )
                setCurrentItemInternal(nextPage, true, true, initialVelocity)
            } else {
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = VelocityTrackerCompat.getYVelocity(
                    velocityTracker, mActivePointerId
                ).toInt()
                mPopulatePending = true
                val height = clientHeight
                val scrollY = scrollY
                val ii = infoForCurrentScrollPosition()
                val currentPage = ii!!.position
                val pageOffset = (scrollY.toFloat() / height - ii.offset) / ii.heightFactor
                val totalDelta = (mLastMotionY - mInitialMotionY).toInt()
                val nextPage = determineTargetPage(
                    currentPage, pageOffset, initialVelocity,
                    0, totalDelta
                )
                setCurrentItemInternal(nextPage, true, true, initialVelocity)
            }
        }
        endDrag()
        isFakeDragging = false
    }

    /**
     * Fake drag by an offset in pixels. You must have called [.beginFakeDrag] first.
     *
     * @param xOffset Offset in pixels to drag by.
     * @see .beginFakeDrag
     * @see .endFakeDrag
     */
    fun fakeDragBy(xOffset: Float, yOffset: Float) {
        var ev: MotionEvent? = null
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }
        if (mAdapter == null) {
            return
        }
        if (isHorizontal) {
            mLastMotionX += xOffset
            val oldScrollX = scrollX.toFloat()
            var scrollX = oldScrollX - xOffset
            val width = clientWidth
            var leftBound = width * mFirstOffset
            var rightBound = width * mLastOffset
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                leftBound = firstItem.offset * width
            }
            if (lastItem.position != mAdapter!!.count - 1) {
                rightBound = lastItem.offset * width
            }
            if (scrollX < leftBound) {
                scrollX = leftBound
            } else if (scrollX > rightBound) {
                scrollX = rightBound
            }
            // Don't lose the rounded component
            mLastMotionX += scrollX - scrollX.toInt()
            scrollTo(scrollX.toInt(), scrollY)
            pageScrolled(scrollX.toInt(), 0)

            // Synthesize an event for the VelocityTracker.
            val time = SystemClock.uptimeMillis()
            ev = MotionEvent.obtain(
                mFakeDragBeginTime, time, MotionEvent.ACTION_MOVE,
                mLastMotionX, 0f, 0
            )
        } else {
            mLastMotionY += yOffset
            val oldScrollY = scrollY.toFloat()
            var scrollY = oldScrollY - yOffset
            val height = clientHeight
            var topBound = height * mFirstOffset
            var bottomBound = height * mLastOffset
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                topBound = firstItem.offset * height
            }
            if (lastItem.position != mAdapter!!.count - 1) {
                bottomBound = lastItem.offset * height
            }
            if (scrollY < topBound) {
                scrollY = topBound
            } else if (scrollY > bottomBound) {
                scrollY = bottomBound
            }
            // Don't lose the rounded component
            mLastMotionY += scrollY - scrollY.toInt()
            scrollTo(scrollX, scrollY.toInt())
            pageScrolled(0, scrollY.toInt())

            // Synthesize an event for the VelocityTracker.
            val time = SystemClock.uptimeMillis()
            ev = MotionEvent.obtain(
                mFakeDragBeginTime, time, MotionEvent.ACTION_MOVE,
                0f, mLastMotionY, 0
            )
        }
        mVelocityTracker!!.addMovement(ev)
        ev.recycle()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = MotionEventCompat.getPointerId(ev, pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            if (isHorizontal) {
                mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex)
            } else {
                mLastMotionY = MotionEventCompat.getY(ev, newPointerIndex)
            }
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
            if (mVelocityTracker != null) {
                mVelocityTracker!!.clear()
            }
        }
    }

    private fun endDrag() {
        mIsBeingDragged = false
        mIsUnableToDrag = false
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun setScrollingCacheEnabled(enabled: Boolean) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled
            if (USE_CACHE) {
                val size = childCount
                for (i in 0 until size) {
                    val child = getChildAt(i)
                    if (child.visibility != GONE) {
                        child.isDrawingCacheEnabled = enabled
                    }
                }
            }
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        if (mAdapter == null) {
            return false
        }
        val width = clientWidth
        val scrollX = scrollX
        return if (direction < 0) {
            scrollX > (width * mFirstOffset).toInt()
        } else if (direction > 0) {
            scrollX < (width * mLastOffset).toInt()
        } else {
            false
        }
    }

    fun internalCanScrollVertically(direction: Int): Boolean {
        if (mAdapter == null) {
            return false
        }
        val height = clientHeight
        val scrollY = scrollY
        return if (direction < 0) {
            scrollY > (height * mFirstOffset).toInt()
        } else if (direction > 0) {
            scrollY < (height * mLastOffset).toInt()
        } else {
            false
        }
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected fun canScroll(v: View, checkV: Boolean, dx: Int, dy: Int, x: Int, y: Int): Boolean {
        return if (v is ViewGroup) {
            if (isHorizontal) {
                val group = v
                val scrollX = v.getScrollX()
                val scrollY = v.getScrollY()
                val count = group.childCount
                // Count backwards - let topmost views consume scroll distance first.
                for (i in count - 1 downTo 0) {
                    // TODO: Add versioned support here for transformed views.
                    // This will not work for transformed views in Honeycomb+
                    val child = group.getChildAt(i)
                    if (x + scrollX >= child.left && x + scrollX < child.right && y + scrollY >= child.top && y + scrollY < child.bottom &&
                        canScroll(
                            child, true, dx, 0, x + scrollX - child.left,
                            y + scrollY - child.top
                        )
                    ) {
                        return true
                    }
                }
                checkV && ViewCompat.canScrollHorizontally(v, -dx)
            } else {
                val group = v
                val scrollX = v.getScrollX()
                val scrollY = v.getScrollY()
                val count = group.childCount
                // Count backwards - let topmost views consume scroll distance first.
                for (i in count - 1 downTo 0) {
                    // TODO: Add versioned support here for transformed views.
                    // This will not work for transformed views in Honeycomb+
                    val child = group.getChildAt(i)
                    if (y + scrollY >= child.top && y + scrollY < child.bottom && x + scrollX >= child.left && x + scrollX < child.right &&
                        canScroll(
                            child, true, 0, dy, x + scrollX - child.left,
                            y + scrollY - child.top
                        )
                    ) {
                        return true
                    }
                }
                checkV && ViewCompat.canScrollVertically(v, -dy)
            }
        } else false
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Let the focused view and/or our descendants get the key first
        return super.dispatchKeyEvent(event) || executeKeyEvent(event)
    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    fun executeKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> handled = arrowScroll(FOCUS_LEFT)
                KeyEvent.KEYCODE_DPAD_RIGHT -> handled = arrowScroll(FOCUS_RIGHT)
                KeyEvent.KEYCODE_TAB -> if (Build.VERSION.SDK_INT >= 11) {
                    // The focus finder had a bug handling FOCUS_FORWARD and FOCUS_BACKWARD
                    // before Android 3.0. Ignore the tab key on those devices.
                    if (KeyEvent.metaStateHasNoModifiers(event.metaState)) {
                        handled = arrowScroll(FOCUS_FORWARD)
                    } else if (KeyEvent.metaStateHasNoModifiers(event.metaState)) {
                        handled = arrowScroll(FOCUS_BACKWARD)
                    }
                }
            }
        }
        return handled
    }

    fun arrowScroll(direction: Int): Boolean {
        var currentFocused = findFocus()
        if (currentFocused === this) {
            currentFocused = null
        } else if (currentFocused != null) {
            var isChild = false
            var parent = currentFocused.parent
            while (parent is ViewGroup) {
                if (parent === this) {
                    isChild = true
                    break
                }
                parent = parent.getParent()
            }
            if (!isChild) {
                // This would cause the focus search down below to fail in fun ways.
                val sb = StringBuilder()
                sb.append(currentFocused.javaClass.simpleName)
                var parent = currentFocused.parent
                while (parent is ViewGroup) {
                    sb.append(" => ").append(parent.javaClass.simpleName)
                    parent = parent.getParent()
                }
                Log.e(
                    TAG, "arrowScroll tried to find focus based on non-child " +
                            "current focused view " + sb.toString()
                )
                currentFocused = null
            }
        }
        var handled = false
        val nextFocused = FocusFinder.getInstance().findNextFocus(
            this, currentFocused,
            direction
        )
        if (nextFocused != null && nextFocused !== currentFocused) {
            if (isHorizontal) {
                if (direction == FOCUS_LEFT) {
                    // If there is nothing
                    // to the left, or this is causing us to
                    // jump to the right,
                    // then what we really want to do is page left.
                    val nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left
                    val currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left
                    handled = if (currentFocused != null && nextLeft >= currLeft) {
                        pageLeft()
                    } else {
                        nextFocused.requestFocus()
                    }
                } else if (direction == FOCUS_RIGHT) {
                    // If there is nothing to the right,
                    // or this is causing us to
                    // jump to the left,
                    // then what we really
                    // want to do is page right.
                    val nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left
                    val currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left
                    handled = if (currentFocused != null && nextLeft <= currLeft) {
                        pageRight()
                    } else {
                        nextFocused.requestFocus()
                    }
                } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
                    // Trying to move left and nothing there; try to page.
                    handled = pageLeft()
                } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {
                    // Trying to move right and nothing there; try to page.
                    handled = pageRight()
                }
            } else {
                if (direction == FOCUS_UP) {
                    // If there is nothing to the left,
                    // or this is causing us to
                    // jump to the right,
                    // then what we really want to do is page left.
                    val nextTop = getChildRectInPagerCoordinates(mTempRect, nextFocused).top
                    val currTop = getChildRectInPagerCoordinates(mTempRect, currentFocused).top
                    handled = if (currentFocused != null && nextTop >= currTop) {
                        pageUp()
                    } else {
                        nextFocused.requestFocus()
                    }
                } else if (direction == FOCUS_DOWN) {
                    val nextDown = getChildRectInPagerCoordinates(mTempRect, nextFocused).bottom
                    val currDown = getChildRectInPagerCoordinates(mTempRect, currentFocused).bottom
                    handled = if (currentFocused != null && nextDown <= currDown) {
                        pageDown()
                    } else {
                        nextFocused.requestFocus()
                    }
                } else if (direction == FOCUS_UP || direction == FOCUS_BACKWARD) {
                    // Trying to move left and nothing there; try to page.
                    handled = pageUp()
                } else if (direction == FOCUS_DOWN || direction == FOCUS_FORWARD) {
                    // Trying to move right and nothing there; try to page.
                    handled = pageDown()
                }
            }
            if (handled) {
                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction))
            }
            return handled
        }
        return handled
    }

    private fun getChildRectInPagerCoordinates(outRect: Rect, child: View?): Rect {
        var outRect: Rect? = outRect
        if (outRect == null) {
            outRect = Rect()
        }
        if (child == null) {
            outRect[0, 0, 0] = 0
            return outRect
        }
        outRect.left = child.left
        outRect.right = child.right
        outRect.top = child.top
        outRect.bottom = child.bottom
        var parent = child.parent
        while (parent is ViewGroup && parent !== this) {
            val group = parent
            outRect.left += group.left
            outRect.right += group.right
            outRect.top += group.top
            outRect.bottom += group.bottom
            parent = group.parent
        }
        return outRect
    }

    fun pageLeft(): Boolean {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true)
            return true
        }
        return false
    }

    fun pageRight(): Boolean {
        if (mAdapter != null && mCurItem < mAdapter!!.count - 1) {
            setCurrentItem(mCurItem + 1, true)
            return true
        }
        return false
    }

    fun pageUp(): Boolean {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true)
            return true
        }
        return false
    }

    fun pageDown(): Boolean {
        if (mAdapter != null && mCurItem < mAdapter!!.count - 1) {
            setCurrentItem(mCurItem + 1, true)
            return true
        }
        return false
    }

    /**
     * We only want the current page that is being shown to be focusable.
     */
    override fun addFocusables(views: ArrayList<View>, direction: Int, focusableMode: Int) {
        val focusableCount = views.size
        val descendantFocusability = descendantFocusability
        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.visibility == VISIBLE) {
                    val ii = infoForChild(child)
                    if (ii != null && ii.position == mCurItem) {
                        child.addFocusables(views, direction, focusableMode)
                    }
                }
            }
        }

        // we add ourselves (if focusable) in all cases except for when we are
        // FOCUS_AFTER_DESCENDANTS and there are some descendants focusable.  this is
        // to avoid the focus search finding layouts when a more precise search
        // among the focusable children would be more interesting.
        if (descendantFocusability != FOCUS_AFTER_DESCENDANTS || focusableCount == views.size) {
            // Note that we can't call the superclass here, because it will
            // add all views in.  So we need to do the same thing View does.
            if (!isFocusable) {
                return
            }
            if (focusableMode and FOCUSABLES_TOUCH_MODE == FOCUSABLES_TOUCH_MODE &&
                isInTouchMode && !isFocusableInTouchMode
            ) {
                return
            }
            if (views != null) {
                views.add(this)
            }
        }
    }

    /**
     * We only want the current page that is being shown to be touchable.
     */
    override fun addTouchables(views: ArrayList<View>) {
        // Note that we don't call super.addTouchables(), which means that
        // we don't call View.addTouchables().  This is okay because a ViewPager
        // is itself not touchable.
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem) {
                    child.addTouchables(views)
                }
            }
        }
    }

    /**
     * We only want the current page that is being shown to be focusable.
     */
    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?,
    ): Boolean {
        val index: Int
        val increment: Int
        val end: Int
        val count = childCount
        if (direction and FOCUS_FORWARD != 0) {
            index = 0
            increment = 1
            end = count
        } else {
            index = count - 1
            increment = -1
            end = -1
        }
        var i = index
        while (i != end) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                val ii = infoForChild(child)?.let{
                    if ((it.position == mCurItem) && child.requestFocus(
                            direction,
                            previouslyFocusedRect
                        )
                    ) {
                        return true
                    }
                }
            }
            i += increment
        }
        return false
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        // Dispatch scroll events from this ViewPager.
        if (event.eventType == AccessibilityEventCompat.TYPE_VIEW_SCROLLED) {
            return super.dispatchPopulateAccessibilityEvent(event)
        }

        // Dispatch all other accessibility events from the current page.
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem &&
                    child.dispatchPopulateAccessibilityEvent(event)
                ) {
                    return true
                }
            }
        }
        return false
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return generateDefaultLayoutParams()
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    internal inner class MyAccessibilityDelegate : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            event.className = DirectionalViewpager::class.java.name
            var recordCompat: AccessibilityRecordCompat? = null
            recordCompat = if (isHorizontal) {
                AccessibilityEventCompat.asRecord(event)
            } else {
                AccessibilityRecordCompat.obtain()
            }
            recordCompat.isScrollable = canScroll()
            if (event.eventType == AccessibilityEventCompat.TYPE_VIEW_SCROLLED
                && mAdapter != null
            ) {
                recordCompat.itemCount = mAdapter!!.count
                recordCompat.fromIndex = mCurItem
                recordCompat.toIndex = mCurItem
            }
        }

        override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat,
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = DirectionalViewpager::class.java.name
            info.isScrollable = canScroll()
            if (isHorizontal) {
                if (canScrollHorizontally(1)) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
                }
                if (canScrollHorizontally(-1)) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
                }
            } else {
                if (internalCanScrollVertically(1)) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
                }
                if (internalCanScrollVertically(-1)) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
                }
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            if (super.performAccessibilityAction(host, action, args)) {
                return true
            }
            if (isHorizontal) {
                when (action) {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                        run {
                            if (canScrollHorizontally(1)) {
                                currentItem = mCurItem + 1
                                return true
                            }
                        }
                        return false
                    }

                    AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                        run {
                            if (canScrollHorizontally(-1)) {
                                currentItem = mCurItem - 1
                                return true
                            }
                        }
                        return false
                    }
                }
            } else {
                when (action) {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                        run {
                            if (internalCanScrollVertically(1)) {
                                currentItem = mCurItem + 1
                                return true
                            }
                        }
                        return false
                    }

                    AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                        run {
                            if (internalCanScrollVertically(-1)) {
                                currentItem = mCurItem - 1
                                return true
                            }
                        }
                        return false
                    }
                }
            }
            return false
        }

        private fun canScroll(): Boolean {
            return mAdapter != null && mAdapter!!.count > 1
        }
    }

    private inner class PagerObserver : DataSetObserver() {
        override fun onChanged() {
            dataSetChanged()
        }

        override fun onInvalidated() {
            dataSetChanged()
        }
    }

    /**
     * Layout parameters that should be supplied for views added to a
     * ViewPager.
     */
    class LayoutParams : ViewGroup.LayoutParams {
        /**
         * true if this view is a decoration on the pager itself and not
         * a view supplied by the adapter.
         */
        var isDecor = false

        /**
         * Gravity setting for use on decor views only:
         * Where to position the view page within the overall ViewPager
         * container; constants are defined in [android.view.Gravity].
         */
        var gravity = 0

        /**
         * Width as a 0-1 multiplier of the measured pager width
         */
        var widthFactor = 0f
        var heightFactor = 0f

        /**
         * true if this view was added during layout and needs to be measured
         * before being positioned.
         */
        var needsMeasure = false

        /**
         * Adapter position this view is for if !isDecor
         */
        var position = 0

        /**
         * Current child index within the ViewPager that this view occupies
         */
        var childIndex = 0

        constructor() : super(FILL_PARENT, FILL_PARENT) {}
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
            gravity = a.getInteger(0, Gravity.TOP)
            a.recycle()
        }
    }

    internal class ViewPositionComparator : Comparator<View> {
        override fun compare(lhs: View, rhs: View): Int {
            val llp = lhs.layoutParams as LayoutParams
            val rlp = rhs.layoutParams as LayoutParams
            return if (llp.isDecor != rlp.isDecor) {
                if (llp.isDecor) 1 else -1
            } else llp.position - rlp.position
        }
    }

    fun setDirection(direction: Direction) {
        mDirection = direction.name
        initViewPager()
    }

    fun setDirection(direction: Config.Direction) {
        mDirection = direction.name
        initViewPager()
    }

    private fun logDestroyItem(pos: Int, `object`: View?): String {
        return "populate() - destroyItem() with pos: $pos view: $`object`"
    }

    companion object {
        private const val TAG = "ViewPager"
        private const val DEBUG = false
        private const val USE_CACHE = false
        private const val DEFAULT_OFFSCREEN_PAGES = 1
        private const val MAX_SETTLE_DURATION = 600 // ms
        private const val MIN_DISTANCE_FOR_FLING = 25 // dips
        private const val DEFAULT_GUTTER_SIZE = 16 // dips
        private const val MIN_FLING_VELOCITY = 400 // dips
        private val LAYOUT_ATTRS = intArrayOf(
            android.R.attr.layout_gravity
        )
        private val COMPARATOR: Comparator<ItemInfo> =
            Comparator { lhs, rhs -> lhs.position - rhs.position }
        private val sInterpolator = Interpolator { tx ->
            var t = tx
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }

        /**
         * Sentinel value for no current active pointer.
         * Used by [.mActivePointerId].
         */
        private const val INVALID_POINTER = -1

        // If the pager is at least this close to its final position, complete the scroll
        // on touch down and let the user interact with the content inside instead of
        // "catching" the flinging pager.
        private const val CLOSE_ENOUGH = 2 // dp
        private const val DRAW_ORDER_DEFAULT = 0
        private const val DRAW_ORDER_FORWARD = 1
        private const val DRAW_ORDER_REVERSE = 2
        private val sPositionComparator = ViewPositionComparator()

        /**
         * Indicates that the pager is in an idle, settled state. The current page
         * is fully in view and no animation is in progress.
         */
        const val SCROLL_STATE_IDLE = 0

        /**
         * Indicates that the pager is currently being dragged by the user.
         */
        const val SCROLL_STATE_DRAGGING = 1

        /**
         * Indicates that the pager is in the process of settling to a final position.
         */
        const val SCROLL_STATE_SETTLING = 2
    }
}