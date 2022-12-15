package com.hxl.android.xiaoan.word.word

import com.hxl.android.xiaoan.word.bean.WordBean

abstract class IWordRecord {
    private val wordRecord = mutableMapOf<WordBean, Int>()
    abstract fun getDefaultCount(): Int

    fun inc(wordBean: WordBean) {
        val count = wordRecord.getOrDefault(wordBean, 0)
        wordRecord[wordBean] = count + 1
    }

    fun count(predicate: (WordBean) -> Boolean): Int {
        return wordRecord.keys.count { predicate.invoke(it) }
    }

     fun getOrDefault(key: WordBean, i: Int): Int{
         return wordRecord.getOrDefault(key,i)
     }
}