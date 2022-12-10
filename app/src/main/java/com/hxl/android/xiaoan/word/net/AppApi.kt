package com.hxl.android.xiaoan.word.net

import com.hxl.android.xiaoan.word.bean.WordBean
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 应用程序API
 */
interface AppApi {
    @GET("word/list")
    fun getWordList(): Call<List<WordBean>>

    @POST("word/add")
    @FormUrlEncoded
    fun addWord(@Field("word")word:String,@Field("mean")mean:String):Call<String>
}