package com.xiaoma.frameanim.decode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Android原生资源的帧序列解码器,支持drawable, raw
 */
class ResourceDecoder(context: Context, frameResIds: IntArray) : BaseFrameDecoder() {
    private val mContext: Context
    private val mFrameResIds: MutableList<Int> = mutableListOf()

    init {
        if (frameResIds.isEmpty()) {
            throw NullPointerException("Invalid  frameResIds: ${frameResIds.contentToString()}")
        }
        mContext = context
        mFrameResIds.addAll(frameResIds.asList())
    }

    override fun onDecode(index: Int, opts: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeResource(mContext.resources, mFrameResIds[index], opts)
    }

    override val frameCount: Int
        get() = mFrameResIds.size

    /*companion object {
        private const val TAG = "ResourceDecoder"
    }*/
}