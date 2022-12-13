package com.hxl.android.xiaoan.word.word

import io.reactivex.rxjava3.core.Observable

interface Phonetics {
    fun query(word:String):Observable<PhoneticsBean>
}