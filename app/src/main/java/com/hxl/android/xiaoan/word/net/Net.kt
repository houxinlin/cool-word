package com.hxl.android.xiaoan.word.net

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object Net {
    private const val HOST = "https://www.houxinlin.com/xiaoan-word/"

    private val okHttpClient = OkHttpClient()
        .newBuilder()
        .connectTimeout(3,TimeUnit.SECONDS)
        .readTimeout(3,TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val baiduRetrofit = Retrofit.Builder()
        .baseUrl("https://fanyi.baidu.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val appRetrofit = Retrofit.Builder()
        .baseUrl(HOST)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    fun getBaiduRetrofitApi(): BaiduApi {
        return baiduRetrofit.create(BaiduApi::class.java)
    }

    fun getAppRetrofit(): AppApi {
        return appRetrofit.create(AppApi::class.java)
    }

    fun download(url: String): ByteArray? {
        val request: Request = Request.Builder().url(url).build()
        val response: Response = okHttpClient.newCall(request).execute()

        if (response.code!=200 || response.body==null) return null
        return response.body!!.byteStream().readBytes()
    }
}