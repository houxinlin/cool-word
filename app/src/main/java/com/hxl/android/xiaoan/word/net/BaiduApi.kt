package com.hxl.android.xiaoan.word.net

import com.hxl.android.xiaoan.word.bean.BaiduSuggest
import retrofit2.Call
import retrofit2.http.*

/**
 * 百度接口API
 */
interface BaiduApi {
    @POST("sug")
    @FormUrlEncoded
    fun suggest(@Field("kw") text:String): Call<BaiduSuggest>
}