package com.hxl.android.xiaoan.word.bean

import android.widget.ImageView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afollestad.materialdialogs.bottomsheets.GridItem
@Entity(tableName = "words")
class WordBean {
    @PrimaryKey
    var id: Int = 0
    var insertDate: String = ""

    @ColumnInfo(name = "word_mean")
    var wordMean: String = ""

    @ColumnInfo(name = "word_name")
    var wordName: String = ""

    @ColumnInfo(name = "word_live")
    var wordLive:Int =-1
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WordBean

        if (id != other.id) return false
        if (insertDate != other.insertDate) return false
        if (wordMean != other.wordMean) return false
        if (wordName != other.wordName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + wordName.hashCode()
        return result
    }

    override fun toString(): String {
        return "('$wordName')"
    }


}