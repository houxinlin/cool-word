package com.hxl.android.xiaoan.word.ui.widget.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import com.google.android.material.card.MaterialCardView

class ScaleMaterialCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    private var lastClick: Long = 0
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN) {
            startScale(this)
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action==MotionEvent.ACTION_DOWN){
            if (System.currentTimeMillis()-lastClick<800){
                return true
            }
            lastClick =System.currentTimeMillis()
        }
        return super.onTouchEvent(event)
    }


    private fun startScale(view: View) {
        val animationSet = AnimatorSet()
        animationSet.interpolator = OvershootInterpolator()
        animationSet.duration = 300
        animationSet.playTogether(
            ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f, 1f),
            ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f, 1f)
        )
        animationSet.start()
    }

}
