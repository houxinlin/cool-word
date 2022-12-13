package com.hxl.android.xiaoan.word.word

import io.reactivex.rxjava3.core.Observable

interface Example {
    fun  listExample(word:String):Observable<Map<String,String>>
}