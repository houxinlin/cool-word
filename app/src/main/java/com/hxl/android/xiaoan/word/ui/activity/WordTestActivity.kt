package com.hxl.android.xiaoan.word.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hxl.android.xiaoan.word.R

class WordTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_test)
    }

    companion object {
        fun getStartIntent(size: Int, context: Context): Intent {
            return Intent(context, WordTestActivity::class.java).apply {
                this.putExtra("size", size)
            }
        }
    }
}