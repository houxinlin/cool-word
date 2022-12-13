package com.hxl.android.xiaoan.word.utils.orm

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hxl.android.xiaoan.word.bean.WordBean

@Database(entities = [WordBean::class], version = 1, exportSchema = false)
open abstract class AppDatabase: RoomDatabase() {

    abstract fun wordDao(): WordDao
}