package com.hxl.android.xiaoan.word.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KotlinCore {

}
fun LocalDate.formatTo():String{
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}