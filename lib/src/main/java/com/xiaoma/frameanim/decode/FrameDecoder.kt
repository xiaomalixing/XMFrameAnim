package com.xiaoma.frameanim.decode

import com.xiaoma.frameanim.recycle.BitmapPool

/**
 * 帧动画的解码器
 */
interface FrameDecoder {
    /**
     * 获取解码帧
     *
     * @param index 帧索引
     * @param pool  位图复用池
     * @return [DecodedFrame]
     */
    fun getFrame(index: Int, pool: BitmapPool): List<DecodedFrame>

    /**
     * 获取帧数
     */
    val frameCount: Int
}