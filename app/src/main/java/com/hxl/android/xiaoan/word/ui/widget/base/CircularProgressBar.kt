package com.hxl.android.xiaoan.word.ui.widget.base


import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import com.hxl.android.xiaoan.word.R
import kotlin.math.min


class CircularProgressBar(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        private const val DEFAULT_MAX_VALUE = 100f
        private const val DEFAULT_START_ANGLE = 270f
        private const val DEFAULT_ANIMATION_DURATION = 1500L
    }

    private var progressAnimator: ValueAnimator? = null
    private var indeterminateModeHandler: Handler? = null

    private var rectF = RectF()
    private var backgroundPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private var foregroundPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    var progress: Float = 0f
        set(value) {
            field = if (progress <= progressMax) value else progressMax
            onProgressChangeListener?.invoke(progress)
            invalidate()
        }
    var progressMax: Float = DEFAULT_MAX_VALUE
        set(value) {
            field = if (field >= 0) value else DEFAULT_MAX_VALUE
            invalidate()
        }
    var progressBarWidth: Float = 10f
        set(value) {
            field = value.dpToPx()
            foregroundPaint.strokeWidth = field
            requestLayout()
            invalidate()
        }
    var backgroundProgressBarWidth: Float = 25f
        set(value) {
            field = value.dpToPx()
            backgroundPaint.strokeWidth = field
            requestLayout()
            invalidate()
        }
    var progressBarColor: Int = Color.BLACK
        set(value) {
            field = value
            manageColor()
            invalidate()
        }
    var progressBarColorStart: Int? = null
        set(value) {
            field = value
            manageColor()
            invalidate()
        }
    var progressBarColorEnd: Int? = null
        set(value) {
            field = value
            manageColor()
            invalidate()
        }
    var progressBarColorDirection: GradientDirection = GradientDirection.LEFT_TO_RIGHT
        set(value) {
            field = value
            manageColor()
            invalidate()
        }
    var backgroundProgressBarColor: Int = Color.WHITE
        set(value) {
            field = value
            manageBackgroundProgressBarColor()
            invalidate()
        }
    var backgroundProgressBarColorStart: Int? = null
        set(value) {
            field = value
            manageBackgroundProgressBarColor()
            invalidate()
        }
    var backgroundProgressBarColorEnd: Int? = null
        set(value) {
            field = value
            manageBackgroundProgressBarColor()
            invalidate()
        }
    var backgroundProgressBarColorDirection: GradientDirection = GradientDirection.LEFT_TO_RIGHT
        set(value) {
            field = value
            manageBackgroundProgressBarColor()
            invalidate()
        }
    var roundBorder = true
        set(value) {
            field = value
            foregroundPaint.strokeCap = if (field) Paint.Cap.ROUND else Paint.Cap.BUTT
            invalidate()
        }
    var startAngle: Float = DEFAULT_START_ANGLE
        set(value) {
            var angle = value + DEFAULT_START_ANGLE
            while (angle > 360) {
                angle -= 360
            }
            field = if (angle < 0) 0f else if (angle > 360) 360f else angle
            invalidate()
        }
    var progressDirection: ProgressDirection = ProgressDirection.TO_RIGHT
        set(value) {
            field = value
            invalidate()
        }
    var indeterminateMode = false
        set(value) {
            field = value
            onIndeterminateModeChangeListener?.invoke(field)
            progressIndeterminateMode = 0f
            progressDirectionIndeterminateMode = ProgressDirection.TO_RIGHT
            startAngleIndeterminateMode = DEFAULT_START_ANGLE

            indeterminateModeHandler?.removeCallbacks(indeterminateModeRunnable)
            progressAnimator?.cancel()
            indeterminateModeHandler = Handler()

            if (field) {
                indeterminateModeHandler?.post(indeterminateModeRunnable)
            }
        }
    var onProgressChangeListener: ((Float) -> Unit)? = null
    var onIndeterminateModeChangeListener: ((Boolean) -> Unit)? = null
    //endregion

    //region Indeterminate Mode
    private var progressIndeterminateMode: Float = 0f
        set(value) {
            field = value
            invalidate()
        }
    private var progressDirectionIndeterminateMode: ProgressDirection = ProgressDirection.TO_RIGHT
        set(value) {
            field = value
            invalidate()
        }
    private var startAngleIndeterminateMode: Float = DEFAULT_START_ANGLE
        set(value) {
            field = value
            invalidate()
        }

    private val indeterminateModeRunnable = Runnable {
        if (indeterminateMode) {
            postIndeterminateModeHandler()
            // whatever you want to do below
            this@CircularProgressBar.progressDirectionIndeterminateMode = this@CircularProgressBar.progressDirectionIndeterminateMode.reverse()
            if (this@CircularProgressBar.progressDirectionIndeterminateMode.isToRight()) {
                setProgressWithAnimation(0f, 1500)
            } else {
                setProgressWithAnimation(progressMax, 1500)
            }
        }
    }

    private fun postIndeterminateModeHandler() {
        indeterminateModeHandler?.postDelayed(indeterminateModeRunnable, DEFAULT_ANIMATION_DURATION)
    }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        progress = 10f
        progressMax = 100f

        progressBarWidth = progressBarWidth.pxToDp()
        backgroundProgressBarWidth =backgroundProgressBarWidth.pxToDp()

        progressBarColor = progressBarColor

        backgroundProgressBarColor = resources.getColor(R.color.appProgressBackground,null)

        backgroundProgressBarColorDirection = backgroundProgressBarColorDirection.value.toGradientDirection()

        progressDirection =  progressDirection.value.toProgressDirection()

        roundBorder = roundBorder
        startAngle = 0f

        indeterminateMode = indeterminateMode

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
        indeterminateModeHandler?.removeCallbacks(indeterminateModeRunnable)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        manageColor()
        manageBackgroundProgressBarColor()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawOval(rectF, backgroundPaint)
        val realProgress = (if (indeterminateMode) progressIndeterminateMode else progress) * DEFAULT_MAX_VALUE / progressMax

        val isToRightFromIndeterminateMode = indeterminateMode && progressDirectionIndeterminateMode.isToRight()
        val isToRightFromNormalMode = !indeterminateMode && progressDirection.isToRight()
        val angle = (if (isToRightFromIndeterminateMode || isToRightFromNormalMode) 360 else -360) * realProgress / 100

        canvas.drawArc(rectF, if (indeterminateMode) startAngleIndeterminateMode else startAngle, angle, false, foregroundPaint)
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        backgroundProgressBarColor = backgroundColor
    }

    private fun manageColor() {
        foregroundPaint.shader = createLinearGradient(progressBarColorStart ?: progressBarColor,
            progressBarColorEnd ?: progressBarColor, progressBarColorDirection)
    }

    private fun manageBackgroundProgressBarColor() {
        backgroundPaint.shader = createLinearGradient(
            backgroundProgressBarColorStart ?: backgroundProgressBarColor,
            backgroundProgressBarColorEnd ?: backgroundProgressBarColor,
            backgroundProgressBarColorDirection)
    }

    private fun createLinearGradient(startColor: Int, endColor: Int, gradientDirection: GradientDirection): LinearGradient {
        var x0 = 0f
        var y0 = 0f
        var x1 = 0f
        var y1 = 0f
        when (gradientDirection) {
            GradientDirection.LEFT_TO_RIGHT -> x1 = width.toFloat()
            GradientDirection.RIGHT_TO_LEFT -> x0 = width.toFloat()
            GradientDirection.TOP_TO_BOTTOM -> y1 = height.toFloat()
            GradientDirection.BOTTOM_TO_END -> y0 = height.toFloat()
        }
        return LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
        val highStroke = if (progressBarWidth > backgroundProgressBarWidth) progressBarWidth else backgroundProgressBarWidth
        rectF.set(0 + highStroke / 2, 0 + highStroke / 2, min - highStroke / 2, min - highStroke / 2)
    }

    @JvmOverloads
    fun setProgressWithAnimation(progress: Float,
                                 duration: Long? = null,
                                 interpolator: TimeInterpolator? = null,
                                 startDelay: Long? = null) {
        progressAnimator?.cancel()
        progressAnimator = ValueAnimator.ofFloat(if (indeterminateMode) progressIndeterminateMode else this.progress, progress)
        duration?.also { progressAnimator?.duration = it }
        interpolator?.also { progressAnimator?.interpolator = it }
        startDelay?.also { progressAnimator?.startDelay = it }
        progressAnimator?.addUpdateListener { animation ->
            (animation.animatedValue as? Float)?.also { value ->
                if (indeterminateMode) progressIndeterminateMode = value else this.progress = value
                if (indeterminateMode) {
                    val updateAngle = value * 360 / 100
                    startAngleIndeterminateMode = DEFAULT_START_ANGLE +
                            if (progressDirectionIndeterminateMode.isToRight()) updateAngle else -updateAngle
                }
            }
        }
        progressAnimator?.start()
    }

    private fun Float.dpToPx(): Float =
        this * Resources.getSystem().displayMetrics.density

    private fun Float.pxToDp(): Float =
        this / Resources.getSystem().displayMetrics.density

    private fun Int.toProgressDirection(): ProgressDirection =
        when (this) {
            1 -> ProgressDirection.TO_RIGHT
            2 -> ProgressDirection.TO_LEFT
            else -> throw IllegalArgumentException("This value is not supported for ProgressDirection: $this")
        }

    private fun ProgressDirection.reverse(): ProgressDirection =
        if (this.isToRight()) ProgressDirection.TO_LEFT else ProgressDirection.TO_RIGHT

    private fun ProgressDirection.isToRight(): Boolean = this == ProgressDirection.TO_RIGHT

    private fun Int.toGradientDirection(): GradientDirection =
        when (this) {
            1 -> GradientDirection.LEFT_TO_RIGHT
            2 -> GradientDirection.RIGHT_TO_LEFT
            3 -> GradientDirection.TOP_TO_BOTTOM
            4 -> GradientDirection.BOTTOM_TO_END
            else -> throw IllegalArgumentException("This value is not supported for GradientDirection: $this")
        }

    enum class ProgressDirection(val value: Int) {
        TO_RIGHT(1),
        TO_LEFT(2)
    }

    enum class GradientDirection(val value: Int) {
        LEFT_TO_RIGHT(1),
        RIGHT_TO_LEFT(2),
        TOP_TO_BOTTOM(3),
        BOTTOM_TO_END(4)
    }

}
