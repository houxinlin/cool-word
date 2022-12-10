package com.hxl.android.xiaoan.word.utils

import android.content.Context
import io.reactivex.rxjava3.functions.Consumer

class ExceptionHandler(private val context: Context) : Consumer<Throwable> {
    override fun accept(t: Throwable?) {

    }
}