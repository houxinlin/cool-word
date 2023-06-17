package com.hxl.android.xiaoan.word.bean

import android.widget.ImageView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.afollestad.materialdialogs.bottomsheets.GridItem

@Entity(tableName = "words")
class WordBean {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var keyId: Int = 0

    @ColumnInfo(name = "insert_id")
    var id: Int = 0

    @ColumnInfo(name = "insert_date")
    var insertDate: String = ""

    @ColumnInfo(name = "word_mean")
    var wordMean: String = ""

    @ColumnInfo(name = "word_name")
    var wordName: String = ""

    @ColumnInfo(name = "word_level")
    var wordLevel: Int = -1

    @ColumnInfo(name = "phonitic")
    var phonitic: String = ""

    @ColumnInfo(name = "phontype")
    var phontype: String = ""

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

fun WordBean.meanKey(): String {
    return "mean-${this.id}"
}

fun WordBean.wordKey(): String {
    return "word-${this.id}"
}