package com.xiaoma.frameanim.recycle

import android.graphics.Bitmap

interface BitmapPool {
    fun put(bitmap: Bitmap)
    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap
    fun clear()
}