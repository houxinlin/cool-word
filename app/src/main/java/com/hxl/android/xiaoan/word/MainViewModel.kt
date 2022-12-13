package com.hxl.android.xiaoan.word

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hxl.android.xiaoan.word.bean.WordBean
import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import io.reactivex.rxjava3.core.Observable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {
    val localMutableLiveData = MutableLiveData<List<WordBean>>()

    private val database = WordApplication.applicationDatabase

    fun getLocalWords(): Observable<List<WordBean>> {
        return RxJavaUtils.createObservable {
            val result =database.wordDao().getAll()
            onNext(result)
            localMutableLiveData.postValue(result)
        }
    }

    fun importWord(words: List<WordBean>, includeIndex: IntArray): Observable<Boolean> {
        val result = mutableListOf<WordBean>()
        val date = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        for (includeIndex in includeIndex) result.add(words[includeIndex].apply {
            this.insertDate = date
        })
        return RxJavaUtils.createObservable {
            database.wordDao().insertAll(*result.toTypedArray())
            onNext(true)
        }
    }

    fun loadNetworkWords(excludes:List<Int>): Observable<List<WordBean>> {
        return RxJavaUtils.createObservable {
            try {
                var listWord = Net.getAppRetrofit().listWord(intArrayOf()).execute().body()!!
                    .filterNot {excludes.contains(it.id)  }
                onNext(listWord)
            }catch (e:Exception){
                onError(e)
            }
        }
    }

}