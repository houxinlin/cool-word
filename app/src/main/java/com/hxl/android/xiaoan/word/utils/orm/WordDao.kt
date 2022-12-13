package com.hxl.android.xiaoan.word.utils.orm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hxl.android.xiaoan.word.bean.WordBean
@Dao
interface WordDao {
    @Query("SELECT * FROM words")
    fun getAll(): List<WordBean>

    @Insert
    fun insertAll(vararg users: WordBean)

}