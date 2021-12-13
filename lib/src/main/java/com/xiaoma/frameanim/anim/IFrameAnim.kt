package com.xiaoma.frameanim.anim

import android.graphics.drawable.Animatable
import com.xiaoma.frameanim.ScaleType
import com.xiaoma.frameanim.decode.DecodedFrame
import com.xiaoma.frameanim.decode.FrameDecoder

interface IFrameAnim : Animatable {
    /**
     * 获取当前帧索引
     */
    var frameIndex: Int

    /**
     * 获取帧数
     */
    val frameCount: Int

    /**
     * 是否一次性播放
     */
    var oneshot: Boolean

    /**
     * 获取帧间隔
     */
    var frameInterval: Long

    /**
     * [ScaleType] 设置缩放类型, 默认为[com.xiaoma.frameanim.costant.FrameConstant.DEFAULT_SCALE_TYPE]
     */
    var scaleType: ScaleType?

    /**
     * 注册播放状态监听
     */
    fun addFrameAnimListener(listener: FrameAnimListener)

    /**
     * 移除监听
     */
    fun removeFrameAnimListener(listener: FrameAnimListener)

    /**
     * 获取解码器
     */
    val frameDecoder: FrameDecoder

    /**
     * 设置参数
     */
    var params: FrameAnimParams

    /**
     *
     * @return List<DecodedFrame>
     */
    fun getFrames(): List<DecodedFrame>
}