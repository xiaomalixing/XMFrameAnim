package com.xiaoma.frameanim.renderer

import com.xiaoma.frameanim.ScaleType
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.anim.IFrameAnimSet

interface FrameRenderer {
    fun addFrameAnim(anim: IFrameAnim)
    fun removeFrameAnim(anim: IFrameAnim)
    fun addFrameAnim(aSet: IFrameAnimSet)
    fun removeFrameAnim(aSet: IFrameAnimSet)

    /**
     * 刷新率 , <=0 表示自适应刷新率
     */
    var fps: Int
    fun setScaleType(scaleType: ScaleType)
    suspend fun onDraw()
    fun startAllAnim()
    fun stopAllAnim()
    fun startRender()
    fun stopRender()
    var renderDecodeMode: RenderDecodeMode
}