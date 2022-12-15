package com.hxl.android.xiaoan.word.ui.widget.base

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import com.hxl.android.xiaoan.word.R
import com.hxl.android.xiaoan.word.utils.SystemUtils

data class Option(var id: Int, var value: String) {}
class OptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var rootView: LinearLayout
    private lateinit var listener: (Option) -> Unit
    private var rightOptionId = -1

    private var showOptions = false

    init {
        LayoutInflater.from(context).inflate(R.layout.option_view, this, true)

    }

    fun setOptions(rightOptionId: Int, options: List<Option>) {
        if (options.size <= 4) {
            rootView.isVisible = true
            this.rightOptionId = rightOptionId
            for (i in options.indices) {
                (rootView.getChildAt(i) as TextView).apply {
                    text = options[i].value
                    background = resources.getDrawable(R.drawable.shape_option_view_text, null)
                    tag = options[i]
                }
            }
        }
        //设置选项时候，如果没有显示过，则进行一个动画
        if (!showOptions){
            val viewAnimator: (View, Int, Int) -> Unit = { view: View, from: Int, to: Int ->
                ObjectAnimator.ofFloat(view, "translationX", from.toFloat(), to.toFloat()).apply {
                    this.interpolator = OvershootInterpolator()
                    duration = 900
                    start()
                }
            }
            viewAnimator.invoke(rootView.getChildAt(0), -getItemWidth(0),0)
            SystemUtils.delayRun(50) { viewAnimator.invoke(rootView.getChildAt(1), -getItemWidth(1), 0) }
            SystemUtils.delayRun(100) { viewAnimator.invoke(rootView.getChildAt(2), -getItemWidth(2), 0) }
            SystemUtils.delayRun(150) { viewAnimator.invoke(rootView.getChildAt(3), -getItemWidth(3), 0) }
            showOptions = true
        }
    }

    private fun getItemWidth(index: Int):Int{
        return rootView.getChildAt(index).measuredWidth
    }

    /**
     * 单击后显示结果
     */
    private fun showResult(option: Option) {
        for (i in 0..3) {
            (rootView.getChildAt(i) as TextView).apply {
                if ((tag as Option).id == rightOptionId) {
                    background = resources.getDrawable(R.drawable.shape_option_view_text_right, null)
                } else {
                    if (option.id != rightOptionId) {
                        background = resources.getDrawable(R.drawable.shape_option_view_text_error, null)
                    }
                }
            }
        }
    }

    private fun onOptionClick(view:View){
        (view.tag as Option).let {
            showResult(it)
            listener.invoke(it)
        }
    }


    fun setClickOption(function: (Option) -> Unit) {
        listener = function
    }

    /**
     * 重置
     */
    fun reset() {
        rootView.isVisible = false
        showOptions = false
        for (i in 0..3) {
            (rootView.getChildAt(i) as TextView).apply {
                setOnClickListener { onOptionClick(it) }
                translationX=-this.measuredWidth.toFloat() //准备下一个动画
            }
        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        rootView = getChildAt(0) as LinearLayout
        rootView.viewTreeObserver.addOnGlobalLayoutListener( object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                for (i in 0..3) {
                    (rootView.getChildAt(i) as TextView).apply {
                        setOnClickListener { onOptionClick(it) }
                        translationX=-this.measuredWidth.toFloat()
                    }
                }
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}