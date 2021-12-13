package com.xiaoma.frameanim

import android.content.Context
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.anim.IFrameAnimSet
import com.xiaoma.frameanim.renderer.FrameRenderer
import com.xiaoma.frameanim.renderer.RenderDecodeMode
import com.xiaoma.frameanim.util.coroutineScope
import kotlinx.coroutines.CoroutineScope

/**
 * 支持帧动画的SurfaceView ,一般用来播放帧数很多,图片很大的帧动画,可以防止OOM
 */
class FrameAnimSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    scope: CoroutineScope = context.coroutineScope
) : SurfaceView(context, attrs, defStyleAttr), FrameRenderer, SurfaceHolder.Callback {

    private val mFrameRenderer = object : FrameRendererImpl(
        context,
        attrs,
        defStyleAttr,
        0,
        scope
    ) {
        private val mHolder = holder
        override fun onLockCanvas(): Canvas? = mHolder.lockCanvas()
        override fun onUnlockCanvasAndPost(canvas: Canvas) {
            mHolder.unlockCanvasAndPost(canvas)
        }
    }

    init {
        // 解决黑屏问题
        setZOrderOnTop(true)
        val holder = holder
        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(this)
    }

    override var renderDecodeMode: RenderDecodeMode
        get() = mFrameRenderer.renderDecodeMode
        set(value) {
            mFrameRenderer.renderDecodeMode = value
        }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceCreated")
        }
        mFrameRenderer.startRender()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("surfaceChanged: [ format: %s, w: %s, h: %s ]", format, width, height))
        }
        mFrameRenderer.setSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "surfaceDestroyed")
        }
        mFrameRenderer.stopRender()
    }

    override fun addFrameAnim(anim: IFrameAnim) {
        mFrameRenderer.addFrameAnim(anim)
    }

    override fun removeFrameAnim(anim: IFrameAnim) {
        mFrameRenderer.removeFrameAnim(anim)
    }

    override fun addFrameAnim(aSet: IFrameAnimSet) {
        mFrameRenderer.addFrameAnim(aSet)
    }

    override fun removeFrameAnim(aSet: IFrameAnimSet) {
        mFrameRenderer.removeFrameAnim(aSet)
    }

    override var fps: Int
        get() = mFrameRenderer.fps
        set(value) {
            mFrameRenderer.fps = value
        }

    override fun setScaleType(scaleType: ScaleType) {
        mFrameRenderer.setScaleType(scaleType)
    }

    override suspend fun onDraw() {
        mFrameRenderer.onDraw()
    }

    override fun startAllAnim() {
        mFrameRenderer.startAllAnim()
    }

    override fun stopAllAnim() {
        mFrameRenderer.stopAllAnim()
    }

    override fun startRender() {
        mFrameRenderer.startRender()
    }

    override fun stopRender() {
        mFrameRenderer.stopRender()
    }

    companion object {
        private const val TAG = "FrameAnimSurfaceView"
    }
}