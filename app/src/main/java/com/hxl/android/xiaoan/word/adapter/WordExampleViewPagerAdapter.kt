package com.hxl.android.xiaoan.word.adapter

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.hxl.android.xiaoan.word.utils.convertDpToPixel

class WordExampleViewPagerAdapter(private val example:List<Spanned>, private val context: Context):PagerAdapter() {
    override fun getCount(): Int {
        return example.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view==`object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val textView = TextView(context).apply {
            setTextColor(Color.BLACK)
            textSize=5f.convertDpToPixel(context)
            text = example[position]
        }
        container.addView(textView)
        return textView
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)

    }
}