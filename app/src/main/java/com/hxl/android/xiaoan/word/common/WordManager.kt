package com.hxl.android.xiaoan.word.common

import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import com.hxl.android.xiaoan.word.ui.widget.base.Option
import com.hxl.android.xiaoan.word.utils.random

object WordManager {
    var word: List<WordBean> = mutableListOf()

    /**
     * 生成测试单词
     */
    fun generatorTestWord(size: Int): List<WordBean> {
        val words = word.sortedBy { it.wordLevel }
        val result = if (words.size > size) words.subList(0, size) else words
        return result.shuffled()
    }

    /**
     * 生成选项
     */
    fun generatorOptions(id:Int):MutableList<Option>{
       return word.filter { it.id!=id }
           .random(3)
           .map { Option(it.id,it.wordMean) }
           .toCollection(mutableListOf())
    }

}