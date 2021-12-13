package com.xiaoma.frameanim.decode

import android.graphics.Bitmap
import android.support.rastermill.FrameSequenceWrapper
import android.util.Log
import com.xiaoma.frameanim.BuildConfig
import com.xiaoma.frameanim.recycle.BitmapPool
import java.io.InputStream

class WebpDecoder(input: InputStream) : FrameDecoder {
    private val mFsw: FrameSequenceWrapper = FrameSequenceWrapper.decodeStream(input)
    private val mDecodedFrame: DecodedFrame = DecodedFrame() // 防止GC
    private val mDecodedFrameList = listOf(mDecodedFrame) // 防止GC

    override val frameCount: Int = mFsw.frameCount

    override fun getFrame(index: Int, pool: BitmapPool): List<DecodedFrame> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "getFrame: i=$index")
        }
        val w = mFsw.width
        val h = mFsw.height
        val bmp = pool.get(w, h, Bitmap.Config.ARGB_8888)
        val interval = mFsw.getFrame(index, bmp)
        mDecodedFrame.run {
            frame = bmp
            frameWidth = w
            frameHeight = h
            frameInterval = interval
        }
        return mDecodedFrameList
    }

    companion object {
        private const val TAG = "WebpDecoder"
    }
}