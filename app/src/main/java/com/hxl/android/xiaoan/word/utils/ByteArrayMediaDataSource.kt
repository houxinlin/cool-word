package com.hxl.android.xiaoan.word.utils

import android.media.MediaDataSource
import java.io.IOException

class ByteArrayMediaDataSource(private var data: ByteArray) : MediaDataSource() {

    private val TAG = ByteArrayMediaDataSource::class.java.simpleName

    @Throws(IOException::class)
    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        var  mSize = size
        if (position >= data.size) {
            return -1
        }
        if (position + size > data.size) {
            mSize -= (position + size - data.size).toInt()
        }
        System.arraycopy(data, position.toInt(), buffer, offset, size)
        return size
    }


    @Throws(IOException::class)
    override fun getSize(): Long {
        return data.size.toLong()
    }

    @Throws(IOException::class)
    override fun close() {
    }
}