package com.hxl.android.xiaoan.word.common

import com.hxl.android.xiaoan.word.bean.WordBean

object Application {
    var word: List<WordBean> = mutableListOf()

    fun generatorTestWord(size: Int): List<WordBean> {
        val words = word.shuffled()
        return if (words.size > size) words.subList(0, size) else words
    }
}