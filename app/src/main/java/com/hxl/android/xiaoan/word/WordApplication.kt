package com.hxl.android.xiaoan.word

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.hxl.android.xiaoan.word.utils.SharedPreferencesUtils

class WordApplication:Application() {
    companion object{
         @SuppressLint("StaticFieldLeak")
         lateinit var context:Context
    }
    override fun onCreate() {
        super.onCreate()
        SharedPreferencesUtils.init(this)
        context=this
    }
}