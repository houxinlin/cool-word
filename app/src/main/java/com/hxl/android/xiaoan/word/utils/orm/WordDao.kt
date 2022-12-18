package com.hxl.android.xiaoan.word.utils.orm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hxl.android.xiaoan.word.bean.WordBean

@Dao
interface WordDao {
    @Query("SELECT * FROM words order by id desc")
    fun getAll(): List<WordBean>

    @Insert
    fun insertAll( wordBean: List<WordBean>)

    @Update()
    fun updata( wordBean: List<WordBean> )

}