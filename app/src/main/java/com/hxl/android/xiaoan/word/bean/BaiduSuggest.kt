package com.hxl.android.xiaoan.word.bean

/**
 * 百度翻译的单词推荐结果
 */
 class BaiduSuggest{
      var `data`: List<Data>? =null
      var errno: Int =-1
 }

 class Data{
     lateinit var k: String
     lateinit var v: String
 }