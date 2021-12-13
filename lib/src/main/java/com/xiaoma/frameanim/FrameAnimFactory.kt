package com.xiaoma.frameanim

import android.content.Context
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.costant.ResourceType
import com.xiaoma.frameanim.decode.AssetsDecoder
import com.xiaoma.frameanim.decode.ResourceDecoder
import com.xiaoma.frameanim.decode.SpriteDecoder
import com.xiaoma.frameanim.decode.WebpDecoder
import com.xiaoma.frameanim.util.coroutineScope
import kotlinx.coroutines.CoroutineScope
import java.io.InputStream

/**
 * FrameAnim实例工厂类
 */
object FrameAnimFactory {
    private const val DEFAULT_FRAME_INTERVAL = 1000L / FrameConstant.DEFAULT_FPS

    /**
     * 通过[android.content.res.Resources]创建帧动画
     *
     * @param context       [Context]
     * @param frameResIds   帧序列的资源ID
     * @param frameInterval 帧间隔, 单位:ms
     * @param oneshot       是否只播放一次, false表示循环播放
     */
    @JvmOverloads
    @JvmStatic
    fun createWithResIds(
        context: Context,
        frameResIds: IntArray,
        frameInterval: Long = DEFAULT_FRAME_INTERVAL,
        oneshot: Boolean = false,
        scope: CoroutineScope = context.coroutineScope
    ): IFrameAnim = FrameAnim(
        scope,
        ResourceDecoder(context, frameResIds),
        frameInterval,
        oneshot
    )

    /**
     * 通过[android.content.res.AssetManager]下的目录创建帧动画
     *
     * @param context       [Context]
     * @param path          帧序列的路径
     * @param frameInterval 帧间隔, 单位:ms
     * @param oneshot       是否只播放一次, false表示循环播放
     */
    @JvmOverloads
    @JvmStatic
    fun createWithAssets(
        context: Context,
        path: String,
        frameInterval: Long = DEFAULT_FRAME_INTERVAL,
        oneshot: Boolean = false,
        scope: CoroutineScope = context.coroutineScope,
    ): IFrameAnim = FrameAnim(
        scope,
        AssetsDecoder(context, path),
        frameInterval,
        oneshot
    )

    /**
     * 创建精灵帧动画
     *
     * @param spriteDir     精灵所在的目录
     * @param spriteName    精灵的名称, 通常是文件名(不带后缀), 如为null或空串, 则默认读取当前目录下的所有图集
     * @param frameInterval 帧间隔, 单位:ms
     * @param oneshot       是否只播放一次, false表示循环播放
     * @param resourceType  [ResourceType]
     */
    @JvmOverloads
    @JvmStatic
    fun createWithSprite(
        context: Context,
        spriteDir: String,
        resourceType: ResourceType,
        spriteName: String? = null,
        frameInterval: Long = DEFAULT_FRAME_INTERVAL,
        oneshot: Boolean = false,
        scope: CoroutineScope = context.coroutineScope,
    ): IFrameAnim = FrameAnim(
        scope,
        SpriteDecoder(context, spriteDir, spriteName, resourceType),
        frameInterval,
        oneshot
    )

    @JvmOverloads
    @JvmStatic
    fun createWithWebp(
        scope: CoroutineScope,
        input: InputStream,
        frameInterval: Long = DEFAULT_FRAME_INTERVAL,
        oneshot: Boolean = false,
    ): IFrameAnim = FrameAnim(
        scope,
        WebpDecoder(input),
        frameInterval,
        oneshot
    )
}