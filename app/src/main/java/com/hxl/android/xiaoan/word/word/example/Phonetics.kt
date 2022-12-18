package com.hxl.android.xiaoan.word.word.example

import com.hxl.android.xiaoan.word.word.example.PhoneticsBean
import io.reactivex.rxjava3.core.Observable

interface Phonetics {
    fun query(word:String):Observable<PhoneticsBean>
}