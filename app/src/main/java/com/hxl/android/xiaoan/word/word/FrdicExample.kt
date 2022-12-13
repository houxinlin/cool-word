package com.hxl.android.xiaoan.word.word

import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import io.reactivex.rxjava3.core.Observable
import java.net.URL
import java.util.regex.Pattern

class FrdicExample : Example {
    companion object {
        const val HOST = "https://www.frdic.com/liju/en/{0}"
    }

    override fun listExample(word:String): Observable<Map<String, String>> {
        return RxJavaUtils.createObservable {
            val sourceList = mutableListOf<String>()
            val targetList = mutableListOf<String>()
            val result = mutableMapOf<String, String>()
            val source = "<p class=\"line\".*?</p>"
            val target = "<p class=\"exp\".*?</p>"
            val read = URL("https://www.frdic.com/liju/en/$word").readText()
            Pattern.compile(target).matcher(read).run {
                while (this.find()) {
                    val value = this.group(0)
                        .replace("class=\"exp\"","style=\"margin-top: 10px;\"")
                    targetList.add(value)
                }
            }
            Pattern.compile(source).matcher(read).run {
                while (this.find()) {
                    val value = this.group(0)!!
                        .replace("class=\"key\"","style=\"color: #03a9f4;\"")
                    sourceList.add(value)
                }
            }
            for (i in 0 until sourceList.size) {
                result[sourceList[i]] = targetList[i]
            }
            onNext(result)
        }

    }
}