package com.hxl.android.xiaoan.word.common

import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import com.hxl.android.xiaoan.word.ui.widget.base.Option
import com.hxl.android.xiaoan.word.utils.random

object Application {
    var word: List<WordBean> = mutableListOf()

    fun generatorTestWord(size: Int): List<WordBean> {
        val words = word.shuffled()
        return if (words.size > size) words.subList(0, size) else words
    }

    fun generatorOptions(id:Int):MutableList<Option>{
       return word.filter { it.id!=id }.random(3).map { Option(it.id,it.wordMean) }.toCollection(mutableListOf())
    }

}