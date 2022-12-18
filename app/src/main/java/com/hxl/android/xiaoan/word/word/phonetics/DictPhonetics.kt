package com.hxl.android.xiaoan.word.word.phonetics

import com.hxl.android.xiaoan.word.net.Net
import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import com.hxl.android.xiaoan.word.word.example.Phonetics
import com.hxl.android.xiaoan.word.word.example.PhoneticsBean
import io.reactivex.rxjava3.core.Observable
import java.util.regex.Pattern

class DictPhonetics : Phonetics {
    override fun query(word: String): Observable<PhoneticsBean> {
        return RxJavaUtils.createObservable {
            val result = PhoneticsBean()
            val readText = Net.getHttpResponse("https://dict.eudic.net/dicts/en/${word}")
            val compile = Pattern.compile("span class=\"Phonitic\">(.*?)</span>")
            val matcher = compile.matcher(readText)

            if (matcher.find()) {
                result.phonitic = matcher.group(1)!!
            }
            if (matcher.find()) {
                result.phontype = matcher.group(1)!!
            }
            onNext(result)
        }
    }
}