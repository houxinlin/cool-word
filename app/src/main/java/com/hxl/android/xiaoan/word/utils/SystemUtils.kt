package com.hxl.android.xiaoan.word.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import com.hxl.android.xiaoan.word.WordApplication
import java.util.*


object SystemUtils {
    private val mainHandler = Handler(Looper.getMainLooper())
    fun delayRun(timer: Long, function: () -> Unit) {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                mainHandler.post {
                    function.invoke()
                }
            }
        }, timer)
    }
    fun play(resourceId:Int){
        MediaPlayer.create(WordApplication.context,resourceId).start()
    }

}