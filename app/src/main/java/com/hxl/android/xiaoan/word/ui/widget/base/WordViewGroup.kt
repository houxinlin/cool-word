package com.hxl.android.xiaoan.word.ui.widget.base

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import android.widget.Scroller
import kotlin.math.abs
import kotlin.math.absoluteValue


class WordViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    val TAG = "TAG"
    private var downX: Float = 0f
    private var scroll: Scroller = Scroller(getContext())
    private val MAX_SCALE = 0.2f

    var isOpen: Boolean = false
    private var twoLeft: Float = 0f
    private var moveDistance: Float = 0f
    private var downMill: Long = 0

    var callback: (Float) -> Unit = {}
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            downX = event.x
            downMill = System.currentTimeMillis()
            twoLeft = getChildAt(1).x - (downX - event.x)
        }
        if (event.action == MotionEvent.ACTION_MOVE) {

            if (abs(event.x - downX) > 20) {
                moveDistance = (downX - event.x)

                var left = (twoLeft.toInt() - moveDistance.toInt())
                if (left > measuredWidth) left = measuredWidth
                if (left <= 0) left = 0
                getChildAt(1).layout(left, 0, (twoLeft + measuredWidth).toInt(), measuredHeight)
                setFirstViewScale()
            }
        }
        if (event.action == MotionEvent.ACTION_UP) {
            twoLeft = getChildAt(1).x
            isOpen = if ((moveDistance > -150 && isOpen) ||
                (!isOpen && System.currentTimeMillis() - downMill < 100 && moveDistance.absoluteValue > 100) ||
                (moveDistance > 150 && !isOpen)
            ) {
                scroll.startScroll(twoLeft.toInt(), 0, -twoLeft.toInt(), 0, 400)
                true
            } else {
                scroll.startScroll(twoLeft.toInt(), 0, measuredWidth - twoLeft.toInt(), 0, 400)
                false
            }
            postInvalidate()
        }
        return true
    }

    override fun computeScroll() {
        if (scroll.computeScrollOffset()) {
            getChildAt(1).layout(scroll.currX, 0, (twoLeft + measuredWidth).toInt(), measuredHeight)
            setFirstViewScale()
            invalidate()
        }

    }

    private fun setFirstViewScale() {
        callback.invoke(getChildAt(1).x / measuredWidth)
        val sx: Float = MAX_SCALE - (MAX_SCALE * (getChildAt(1).x / measuredWidth))
        getChildAt(0).scaleX = 1 - sx
        getChildAt(0).scaleY = 1 - sx
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChildAt(0).layout(0, t, r, b)
        getChildAt(1).layout(r, t, r * 2, b)
    }


    fun close() {
        if (!isOpen) return
        scroll.startScroll(0, 0, measuredWidth, 0, 400)
        isOpen = false
        postInvalidate()
    }
}


