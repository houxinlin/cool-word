package com.hxl.android.xiaoan.word.utils

import android.content.Context
import android.util.DisplayMetrics
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class KotlinCore {

}

fun Int.generatorRandom(size: Int): List<Int> {
    val result = mutableListOf<Int>()
    for (i in 0 until this) result.add(i)
    result.shuffle()

    if (this < size) return result
    return result.subList(0, size )

}

fun LocalDate.formatTo(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

fun Float.convertDpToPixel(context: Context): Float {
    return this * (context.resources
        .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}


fun Float.convertPixelsToDp(context: Context): Float {
    return this / (context.resources
        .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}


fun <T> List<T>.random(size: Int): List<T> {
    val result = mutableListOf<T>()
    if (this.size <= size) {
        result.addAll(this)
        result.shuffle()
        return result
    }
    for (i in 1..size) {
        result.add(this.random())
    }
    return result
}

fun <E> MutableList<E>.addNearby(max: Int, value: E): Boolean {
    val minIndex = 2
    if (this.size <= 2) return add(value)
    val maxIndex = if (this.size < max) this.size + 1 else minIndex + max
    val r = Random.nextInt(2, maxIndex)
    return insertNearby(minIndex, r + 1, value)
}

fun <E> MutableList<E>.addLastNearby(max: Int, value: E): Boolean {
    val maxIndex = this.size
    var minIndex = this.size - 1
    if (minIndex <= 2) return add(value)

    while (maxIndex - minIndex != max && minIndex != 2) {
        minIndex--
    }
    return insertNearby(minIndex, maxIndex + 1, value)
}

fun <E> MutableList<E>.insertNearby(min: Int, max: Int, value: E): Boolean {
    val r = Random.nextInt(min, max)
    this.add(r, value)
    return true
}

fun <E> MutableList<E>.addCenterNearby(max: Int, value: E): Boolean {
    if (this.size <= 2) return add(value)

    var minIndex = 3
    var maxIndex = if (minIndex + max > this.size) this.size else minIndex + max
    if (maxIndex - minIndex < max) return insertNearby(minIndex, maxIndex + 1, value)
    var leftCount = minIndex
    var rightCount = this.size - maxIndex
    if (rightCount <= 1) return insertNearby(minIndex, maxIndex, value)
    while (Math.max(leftCount, rightCount) - Math.min(leftCount, rightCount) > 1) {
        leftCount = (minIndex++)
        rightCount = this.size - (maxIndex++)
    }
    return insertNearby(minIndex, maxIndex, value)
}

fun <E> MutableList<E>.addRandom(value: E): Boolean {
    if (this.size <= 2) return add(value)
    return insertNearby(2, this.size + 1, value)
}
