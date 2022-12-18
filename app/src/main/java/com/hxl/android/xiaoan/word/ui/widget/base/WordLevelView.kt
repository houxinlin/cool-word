package com.hxl.android.xiaoan.word.ui.widget.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hxl.android.xiaoan.word.R

class WordLevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        private const val SIZE=10f
    }
    private val paintNotKnow=Paint().apply {
        setWillNotDraw(false)
        this.style=Paint.Style.FILL
        this.color=resources.getColor(R.color.red,null)

    }
    private val paintKnow=Paint().apply {
        setWillNotDraw(false)
        this.style=Paint.Style.FILL
        this.color=resources.getColor(R.color.green,null)

    }
    private val paintVagueKnow=Paint().apply {
        setWillNotDraw(false)
        this.style=Paint.Style.FILL
        this.color=resources.getColor(R.color.blue,null)

    }
     var levelValue =-1
     set(value) {
         field=value
         invalidate()
     }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paint =when(levelValue){
            in -1..3->paintNotKnow
            in 4 ..5->paintVagueKnow
            else ->paintKnow
        }
        canvas.drawCircle(measuredWidth/2- SIZE/2,(measuredHeight/2- SIZE/2)- SIZE*2,10f,paint)

    }
}