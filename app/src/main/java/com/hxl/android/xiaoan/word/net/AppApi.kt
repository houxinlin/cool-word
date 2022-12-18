package com.hxl.android.xiaoan.word.net

import com.hxl.android.xiaoan.word.bean.WordBean
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 应用程序API
 */
interface AppApi {
    @POST("word/list")
    @FormUrlEncoded
    fun listWord(@Field("exclude") exclude: IntArray): Call<List<WordBean>>
}