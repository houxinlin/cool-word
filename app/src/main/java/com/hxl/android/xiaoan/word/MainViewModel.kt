package com.hxl.android.xiaoan.word

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import com.hxl.android.xiaoan.word.word.phonetics.DictPhonetics
import com.hxl.android.xiaoan.word.word.example.PhoneticsBean
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    companion object {
        val EMPTY_PHONETICSBEAN = PhoneticsBean()
    }
    private val localMutableLiveData = MutableLiveData<List<WordBean>>()
    private val threadPoolExecutor =
        ThreadPoolExecutor(2, 2, 2, TimeUnit.SECONDS, LinkedBlockingQueue())
    private val database = WordApplication.applicationDatabase

    private val dictPhonetics = DictPhonetics()
    fun getLocalWords(): Observable<List<WordBean>> {
        return RxJavaUtils.createObservable {
            val result = database.wordDao().getAll()
            onNext(result)
            localMutableLiveData.postValue(result)
        }
    }

    fun importWord(words: List<WordBean>): Observable<Boolean> {
        val date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        words.forEach { it.insertDate = date }
        return RxJavaUtils.createObservable {
            val needQuery = words.filter { it.id == -1 }.toCollection(mutableListOf())
            try {
                if (needQuery.isNotEmpty()) {
                    val countDownLatch = CountDownLatch(needQuery.size)
                    val result = mutableMapOf<Int, PhoneticsBean>()
                    words.forEach { runPhoneticsQuery(it, countDownLatch, result) }
                    countDownLatch.await(5, TimeUnit.SECONDS)
                    words.forEach {
                        it.phonitic = result.getOrDefault(it.id, EMPTY_PHONETICSBEAN).phonitic
                        it.phontype = result.getOrDefault(it.id, EMPTY_PHONETICSBEAN).phontype
                    }
                }
                database.wordDao().insertAll(words)
                onNext(true)
            } catch (e: InterruptedException) {
                onError(e)
            }

        }
    }

    fun loadNetworkWords(excludes: List<Int>): Observable<List<WordBean>> {
        return RxJavaUtils.createObservable {
            try {
                var listWord = Net.getAppRetrofit().listWord(intArrayOf()).execute().body()!!
                    .filterNot { excludes.contains(it.id) }
                onNext(listWord)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun runPhoneticsQuery(
        wordBean: WordBean,
        countDownLatch: CountDownLatch,
        result: MutableMap<Int, PhoneticsBean>
    ) {
        threadPoolExecutor.execute { PhoneticsQuery(countDownLatch, result).query(wordBean) }
    }

    inner class PhoneticsQuery(
        private val countDownLatch: CountDownLatch,
        private val result: MutableMap<Int, PhoneticsBean>
    ) {
        fun query(word: WordBean) {
            this@MainViewModel.dictPhonetics.query(word.wordName).subscribe({
                result[word.id] = it
            }, {}, {
                countDownLatch.countDown()
            })
        }
    }
}
