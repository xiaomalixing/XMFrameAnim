package com.xiaoma.frameanim.renderer

/**
 * 渲染器的对动画的解码模式
 */
enum class RenderDecodeMode {
    /**
     * 自动模式, 根据fps自行选择 同步解码 或 并发解码
     */
    AUTO,

    /**
     * 同步模式, 单线程解码
     */
    SYNC,

    /**
     * 并发模式, 多线程解码
     */
    PARALLEL
}