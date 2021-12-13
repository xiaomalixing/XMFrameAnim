package com.xiaoma.frameanim.anim

import com.xiaoma.frameanim.ScaleType
import com.xiaoma.frameanim.decode.FrameDecoder

interface IFrameAnimSet {
    /**
     * 添加动画
     */
    fun addFrameAnim(anim: IFrameAnim)

    /**
     * 移除动画
     */
    fun removeFrameAnim(anim: IFrameAnim)

    /**
     * 获取当前所有动画
     */
    val allFrameAnim: MutableList<IFrameAnim>

    /**
     * 开启指定动画
     *
     * @param animIndex 动画索引
     */
    fun start(animIndex: Int)

    /**
     * 停止指定动画
     *
     * @param animIndex 动画索引
     */
    fun stop(animIndex: Int)

    /**
     * 获取当前帧索引
     */
    fun getFrameIndex(animIndex: Int): Int

    /**
     * 设置所有帧动画至指定索引
     *
     * @param frameIndex 帧索引
     */
    fun setFrameIndex(frameIndex: Int)

    /**
     * 设置当前帧序列索引
     *
     * @param animIndex  动画索引
     * @param frameIndex 帧索引
     */
    fun setFrameIndex(animIndex: Int, frameIndex: Int)

    /**
     * 获取帧数
     */
    fun getFrameCount(animIndex: Int): Int

    /**
     * 获取最大帧数
     */
    fun getMaxFrameCount(): Int

    /**
     * 统一设置所有动画是否一次性播放
     *
     * @param oneshot true:只播放一次; false:循环播放
     */
    fun setOneShot(oneshot: Boolean)

    /**
     * 设置是否一次性播放
     *
     * @param animIndex 动画索引
     * @param oneshot   true:只播放一次; false:循环播放
     */
    fun setOneShot(animIndex: Int, oneshot: Boolean)

    /**
     * 是否一次性播放
     *
     * @param animIndex 动画索引
     */
    fun isOneShot(animIndex: Int): Boolean

    /**
     * 统一设置帧间隔
     */
    fun setFrameInterval(frameInterval: Long)

    /**
     * 设置帧间隔
     *
     * @param animIndex 动画索引
     */
    fun setFrameInterval(animIndex: Int, frameInterval: Long)

    /**
     * 获取帧间隔
     *
     * @param animIndex 动画索引
     */
    fun getFrameInterval(animIndex: Int): Long

    /**
     * 统一设置缩放类型, 默认为[com.xiaoma.frameanim.costant.FrameConstant.DEFAULT_SCALE_TYPE]
     *
     * @param scaleType [ScaleType]
     */
    fun setScaleType(scaleType: ScaleType)

    /**
     * 设置缩放类型, 默认为[com.xiaoma.frameanim.costant.FrameConstant.DEFAULT_SCALE_TYPE]
     *
     * @param animIndex 动画索引
     * @param scaleType [ScaleType]
     */
    fun setScaleType(animIndex: Int, scaleType: ScaleType?)

    /**
     * [ScaleType]
     *
     * @param animIndex 动画索引
     */
    fun getScaleType(animIndex: Int): ScaleType?

    /**
     * 统一注册播放状态监听
     */
    fun addFrameAnimListener(listener: FrameAnimListener)

    /**
     * 注册播放状态监听
     *
     * @param animIndex 动画索引
     */
    fun addFrameAnimListener(animIndex: Int, listener: FrameAnimListener)

    /**
     * 统一移除监听
     */
    fun removeFrameAnimListener(listener: FrameAnimListener)

    /**
     * 移除监听
     *
     * @param animIndex 动画索引
     */
    fun removeFrameAnimListener(animIndex: Int, listener: FrameAnimListener)

    /**
     * 获取解码器
     *
     * @param animIndex 动画索引
     */
    fun getFrameDecoder(animIndex: Int): FrameDecoder

    /**
     * 获取位置,缩放参数
     *
     * @param animIndex 动画索引
     */
    fun getParams(animIndex: Int): FrameAnimParams

    /**
     * 统一设置参数
     */
    fun setParams(params: FrameAnimParams)

    /**
     * 设置指定动画的参数
     *
     * @param animIndex 动画索引
     */
    fun setParams(animIndex: Int, params: FrameAnimParams)

    /**
     * 动画是否播放中
     *
     * @param animIndex 动画索引
     */
    fun isRunning(animIndex: Int): Boolean
    fun start()
    fun stop()
}