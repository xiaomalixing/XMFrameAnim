package com.xiaoma.frameanim.anim

interface FrameAnimListener {
    /**
     * 帧动画开始
     */
    fun onStart(anim: IFrameAnim)

    /**
     * 帧动画结束
     */
    fun onStop(anim: IFrameAnim)
}