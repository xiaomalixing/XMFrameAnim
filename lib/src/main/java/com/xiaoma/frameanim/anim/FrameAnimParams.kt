package com.xiaoma.frameanim.anim

/**
 * 帧动画参数
 */
class FrameAnimParams {
    /**
     * x轴坐标
     */
    var x = 0

    /**
     * y轴坐标
     */
    var y = 0

    /**
     * x轴缩放因子, 默认 1
     */
    var scaleX = 1.0f

    /**
     * y轴缩放因子, 默认 1
     */
    var scaleY = 1.0f

    /**
     * 旋转角度, 默认 0
     */
    var rotation = 0f

    /**
     * 锚点x坐标, 相对坐标, 取值范围: 0 ~ 1f
     */
    var pivX = 0.5f

    /**
     * 锚点y坐标, 相对坐标, 取值范围: 0 ~ 1f
     */
    var pivY = 0.5f
}