package com.xiaoma.frameanim.util

object BytesUtil {
    fun toKB(bytes: Long): Float {
        return bytes / 1024f
    }

    fun toMB(bytes: Long): Float {
        return bytes / 1024f / 1024f
    }
}