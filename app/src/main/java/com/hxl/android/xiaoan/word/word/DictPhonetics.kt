package com.hxl.android.xiaoan.word.word

import com.hxl.android.xiaoan.word.utils.RxJavaUtils
import io.reactivex.rxjava3.core.Observable
import java.net.URL
import java.util.regex.Pattern

class DictPhonetics : Phonetics {
    override fun query(word: String): Observable<PhoneticsBean> {
       return RxJavaUtils.createObservable {
           val result = PhoneticsBean()
           val readText = URL("https://dict.eudic.net/dicts/en/${word}").readText()
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