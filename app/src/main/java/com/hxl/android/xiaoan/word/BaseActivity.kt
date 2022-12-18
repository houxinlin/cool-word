package com.hxl.android.xiaoan.word

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.hxl.android.xiaoan.word.ui.widget.base.BaseProgressDialog
import com.hxl.android.xiaoan.word.utils.PlayLan
import com.hxl.android.xiaoan.word.utils.tts


open class BaseActivity : AppCompatActivity() {
    protected lateinit var configPreferences: SharedPreferences
    private  lateinit var progressDialog :BaseProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun ttsPlayBy(text: String, playLan: PlayLan) {
        text.tts(playLan)
    }

    fun ttsAuto(text: String) {
        configPreferences.getString("play_type", "0")?.toInt().run {
            ttsPlayBy(text, if (this == 0) PlayLan.UK else PlayLan.EN)
        }
    }

    fun showLoading() {
        if (!::progressDialog.isInitialized)        progressDialog = BaseProgressDialog(this)
        progressDialog.start()
    }

    fun cancelDialog() {
        progressDialog.stop()
    }

    fun waitUserContinue(): Boolean {
        return getBooleanConfig("wait_next", false)
    }

    fun autoShowOption(): Boolean {
        return getBooleanConfig("auto_show_option", false)
    }

    fun isAutoPlayTTS(): Boolean {
        return getBooleanConfig("auto_play", true)
    }

    fun getBooleanConfig(key: String, default: Boolean = false): Boolean {
        return configPreferences.getBoolean(key, default)
    }
}