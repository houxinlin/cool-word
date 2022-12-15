package com.hxl.android.xiaoan.word.word

import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.common.Application
import com.hxl.android.xiaoan.word.ui.activity.WordTestActivity
import java.util.concurrent.LinkedBlockingDeque

class WordTestQueue: LinkedBlockingDeque<WordBean>() {
    /**
     * 单词队列根据排序策略重新排序
     */
    fun wordQueueSort(wordBean: WordBean, sortStrategy: (MutableList<WordBean>, WordBean) -> Unit) {
        val list = this.toMutableList() //获取当前队列集合
        this.clear()  //队列清空
        sortStrategy.invoke(list, wordBean) //执行排序策略
        list.forEach(this::addLast) //直接排序策略后重新生成单词队列
    }

     fun initTestWord(size: Int,success:(List<WordBean>)->Unit) {
        //生成测试的单词
        val tempWords = Application.generatorTestWord(size)
        tempWords.forEach { addFirst(it) }
         success.invoke(tempWords)
    }


    /**
     * 获取当前单词
     */
     fun getCurrentWord(): WordBean? {
        return this.peekFirst()
    }
}