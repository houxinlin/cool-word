package com.hxl.android.xiaoan.word.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics


object ScreenUtils {

    fun getScreenHeight(context: Context): Int {
        var dm: DisplayMetrics? = null
        try {
            dm = DisplayMetrics()
            val localWindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localWindowManager.defaultDisplay.getRealMetrics(dm)
            } else {
                localWindowManager.defaultDisplay.getMetrics(dm)
            }
            return dm.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}