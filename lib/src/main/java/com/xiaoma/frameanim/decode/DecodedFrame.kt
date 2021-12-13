package com.xiaoma.frameanim.decode

import android.graphics.Bitmap

/**
 * 帧动画的帧Model
 */
class DecodedFrame {
    /**
     * 帧位图
     */
    var frame: Bitmap? = null

    /**
     * x坐标
     */
    var x = 0

    /**
     * y坐标
     */
    var y = 0

    /**
     * 帧宽, 通常等于frame的getWidth
     */
    var frameWidth = 0

    /**
     * 帧高, 通常等于frame的getHeight
     */
    var frameHeight = 0

    /**
     * 旋转角度, 默认为0
     */
    var rotate = 0

    /**
     * 帧间隔, 单位:ms
     */
    var frameInterval = 0L
}