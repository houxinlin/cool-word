package com.hxl.android.xiaoan.word.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {
    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences
    fun  init(context: Context){
        this.context=context
        sharedPreferences =this.context.getSharedPreferences("WORD",Context.MODE_PRIVATE)
    }
    fun getBoolean(key:String,def:Boolean=false):Boolean{
        return sharedPreferences.getBoolean(key,def)
    }
    fun setBoolean(key:String,value:Boolean){
        sharedPreferences.edit().putBoolean(key,value).commit()
    }
}