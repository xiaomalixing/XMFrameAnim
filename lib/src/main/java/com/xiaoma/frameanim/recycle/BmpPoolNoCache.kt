package com.xiaoma.frameanim.recycle

import android.graphics.Bitmap

internal object BmpPoolNoCache : BitmapPool {
    override fun put(bitmap: Bitmap) {
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        return Bitmap.createBitmap(width, height, config)
    }

    override fun clear() {
        // 因为无缓存, 无须clear
    }
}