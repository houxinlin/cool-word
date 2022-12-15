package com.hxl.android.xiaoan.word

import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.hxl.android.xiaoan.word.utils.PlayLan
import com.hxl.android.xiaoan.word.utils.TxtPlayer
import com.hxl.android.xiaoan.word.utils.tts

open class BaseActivity: AppCompatActivity() {
    protected lateinit var configPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }
    private fun ttsPlayBy(text:String, playLan: PlayLan){
        text.tts(playLan)
    }
    fun ttsAuto(text:String){
        configPreferences.getString("play_type", "0")?.toInt().run {
            ttsPlayBy(text, if (this == 0) PlayLan.UK else PlayLan.EN)
        }
    }

    fun waitUserContinue():Boolean{
        return getBooleanConfig("wait_next",false)
    }
    fun autoShowOption():Boolean{
        return getBooleanConfig("auto_show_option",false)
    }
    fun isAutoPlayTTS():Boolean{
        return getBooleanConfig("auto_play",true)
    }
    fun getBooleanConfig(key:String,default:Boolean=false):Boolean{
        return configPreferences.getBoolean(key,default)
    }
}