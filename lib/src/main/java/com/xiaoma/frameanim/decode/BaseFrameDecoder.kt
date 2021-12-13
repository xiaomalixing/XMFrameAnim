package com.xiaoma.frameanim.decode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.xiaoma.frameanim.BuildConfig
import com.xiaoma.frameanim.recycle.BitmapPool
import com.xiaoma.frameanim.util.BmpUtil

abstract class BaseFrameDecoder : FrameDecoder {
    // 避免GC
    private val mOptsTmp: BitmapFactory.Options = BitmapFactory.Options().apply {
        // 为了减少GC,固定一个16K的解码缓冲区; 否则每次调用decodeXXX,BitmapFactory内部都会new一个16K的缓冲区
        inTempStorage = ByteArray(16 * 1024)
    }

    // 避免GC
    private val mDecodedFrameTmp: DecodedFrame = DecodedFrame()

    // 避免GC
    private val mDecodedFrameList = listOf(mDecodedFrameTmp)

    protected abstract fun onDecode(index: Int, opts: BitmapFactory.Options): Bitmap?

    override fun getFrame(index: Int, pool: BitmapPool): List<DecodedFrame> {
        if (index < 0 || index >= frameCount) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, String.format("getFrame: Invalid index: %s", index))
            }
            return emptyList()
        }
        BmpUtil.setNonScaleOpt(mOptsTmp)
        // 先解析大小和Config
        mOptsTmp.inJustDecodeBounds = true
        mOptsTmp.inBitmap = null
        onDecode(index, mOptsTmp)
        val w = mOptsTmp.outWidth
        val h = mOptsTmp.outHeight
        if (w <= 0 || h <= 0) {
            Log.e(TAG, String.format("getFrame: Invalid bitmap size: %s x %s", w, h))
            return emptyList()
        }
        val cfg = BmpUtil.getConfig(mOptsTmp)
        val inBmp = pool.get(w, h, cfg)
        // 在inBitmap上真正解码
        mOptsTmp.inJustDecodeBounds = false
        mOptsTmp.inBitmap = inBmp
        val decodedBmp = onDecode(index, mOptsTmp)
        if (inBmp != decodedBmp) {
            pool.put(inBmp) // 复用未能解码的容器inBitmap
        }
        mDecodedFrameTmp.run {
            frame = decodedBmp
            frameWidth = w
            frameHeight = h
        }
        return mDecodedFrameList
    }

    companion object {
        private const val TAG = "BaseFrameDecoder"
    }
}