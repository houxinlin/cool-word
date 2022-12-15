package com.hxl.android.xiaoan.word

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hxl.android.xiaoan.word.utils.SharedPreferencesUtils
import com.hxl.android.xiaoan.word.utils.orm.AppDatabase

class WordApplication:Application() {
    companion object{
         @SuppressLint("StaticFieldLeak")
         lateinit var context:Context

         lateinit var applicationDatabase: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesUtils.init(this)
        context=this
        applicationDatabase =Room.databaseBuilder(applicationContext, AppDatabase::class.java, "word-database").build()
        MultiDex.install(this)
    }
}