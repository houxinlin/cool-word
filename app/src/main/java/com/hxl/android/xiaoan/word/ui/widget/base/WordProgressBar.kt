package com.hxl.android.xiaoan.word.ui.widget.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.utils.convertDpToPixel
import kotlin.math.max

class WordProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var maxValue = 0
        set(value) {
            field = value;
            invalidate()
        }
    var currentValue = 0
        set(value) {
            field = value
            invalidate()
        }
    private var oneState = 1
    private var twoState = 0
    private var threeState = 0

    fun setState(o: Int, t: Int, th: Int) {
        this.oneState = o
        this.twoState = t
        this.threeState = th
        invalidate()
    }

    private val textPaint = Paint().apply {
        textSize = 14f.convertDpToPixel(getContext())
    }
    private val onePaint = Paint().apply {
        textSize = 14f.convertDpToPixel(getContext())
        color = context.getColor(R.color.red)
    }

    private val twoPaint = Paint().apply {
        textSize = 14f.convertDpToPixel(getContext())
        color = context.getColor(R.color.blue)
    }
    private val threePaint = Paint().apply {
        textSize = 14f.convertDpToPixel(getContext())
        color = context.getColor(R.color.green)
    }


    init {
    }

    override fun onDraw(canvas: Canvas) {
        if (maxValue==0) return
        val itemWidth = measuredWidth / maxValue
        val drawItemHeight = getDrawHeight()
        var drawTop = 0f
        var drawBottom = 0f
        if (oneState > 0) {
            drawBottom = drawTop + drawItemHeight
            canvas.drawRect(0f, drawTop, oneState * itemWidth.toFloat(), drawBottom, onePaint)
            drawTop += drawItemHeight + 1
        }
        if (twoState > 0) {
            drawBottom = drawTop + drawItemHeight
            canvas.drawRect(0f, drawTop, twoState * itemWidth.toFloat(), drawBottom, twoPaint)
            drawTop += drawItemHeight + 1
        }
        if (threeState > 0) {
            drawBottom = drawTop + drawItemHeight
            canvas.drawRect(0f, drawTop, threeState * itemWidth.toFloat(), drawBottom, threePaint)
        }
        drawTextWithCenterPoint(canvas)

        super.onDraw(canvas)
    }

    private fun getDrawCount(): Int {
        var oneCount: Int = 0
        if (oneState >= 1) oneCount++
        if (twoState >= 1) oneCount++
        if (threeState >= 1) oneCount++
        return oneCount
    }

    private fun getDrawHeight(): Int {
        if (getDrawCount() == 0) return measuredHeight / 1
        return measuredHeight / getDrawCount()
    }

    private fun drawTextWithCenterPoint(
        canvas: Canvas,
    ) {
        val text = "$currentValue/$maxValue"
        val centerY = measuredHeight / 2
        val centerX = measuredWidth / 2
        val textWidth = textPaint.measureText(text)
        val fontMetrics = textPaint.fontMetrics
        val baselineY = centerY + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        canvas.drawText(text, centerX - textWidth / 2, baselineY, textPaint)
    }

}