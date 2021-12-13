package com.xiaoma.frameanim

import android.content.Context
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.anim.IFrameAnimSet
import com.xiaoma.frameanim.renderer.FrameRenderer
import com.xiaoma.frameanim.renderer.RenderDecodeMode
import com.xiaoma.frameanim.util.coroutineScope
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * 支持帧动画的TextureView ,一般用来播放帧数很多,图片很大的帧动画,可以防止OOM
 *
 *
 * 为什么有了SurfaceView的实现, 还要再实现一个TextureView ?
 * SurfaceView拥有单独的Surface,虽然性能更好,但是存在覆盖层级的问题; TextureView用来解决覆盖问题
 */
class FrameAnimTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    scope: CoroutineScope = context.coroutineScope
) : TextureView(context, attrs, defStyleAttr), FrameRenderer {

    private lateinit var mFrameRenderer: FrameRendererImpl
    private val mTextureLock: Lock = ReentrantLock()
    private var mSurfaceTextureListener: SurfaceTextureListener? = null

    init {
        super.setSurfaceTextureListener(object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("onSurfaceTextureAvailable: [ surface: %s, w: %s, h: %s ]", surface, width, height))
                }
                if (mSurfaceTextureListener != null) {
                    mSurfaceTextureListener!!.onSurfaceTextureAvailable(surface, width, height)
                }
                try {
                    mTextureLock.lock()
                    mFrameRenderer.setSize(width, height)
                    mFrameRenderer.startRender()
                } finally {
                    mTextureLock.unlock()
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("onSurfaceTextureSizeChanged: [ surface: %s, w: %s, h: %s ]", surface, width, height))
                }
                mFrameRenderer.setSize(width, height)
                if (mSurfaceTextureListener != null) {
                    mSurfaceTextureListener!!.onSurfaceTextureSizeChanged(surface, width, height)
                }
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("onSurfaceTextureDestroyed: [ surface: %s ]", surface))
                }
                if (mSurfaceTextureListener != null) {
                    mSurfaceTextureListener!!.onSurfaceTextureDestroyed(surface)
                }
                return try {
                    mTextureLock.lock()
                    mFrameRenderer.stopRender()
                    // 返回true,以释放底层绘制资源
                    true
                } finally {
                    mTextureLock.unlock()
                }
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                /*if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("onSurfaceTextureUpdated: [ surface: %s ]", surface));
                }*/
                if (mSurfaceTextureListener != null) {
                    mSurfaceTextureListener!!.onSurfaceTextureUpdated(surface)
                }
            }
        })
        mFrameRenderer = object : FrameRendererImpl(
            context,
            attrs,
            defStyleAttr,
            0,
            scope
        ) {
            override fun onLockCanvas(): Canvas? {
                var canvas: Canvas? = null
                return try {
                    mTextureLock.lock()
                    lockCanvas().apply { canvas = this }
                } finally {
                    // 如未能获取到Canvas, 则不会执行onUnlockCanvasAndPost, 因此这里要直接释放锁
                    mTextureLock.takeIf { canvas == null }?.unlock()
                }
            }

            override fun onUnlockCanvasAndPost(canvas: Canvas) {
                try {
                    unlockCanvasAndPost(canvas)
                } finally {
                    mTextureLock.unlock()
                }
            }
        }
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

    override var renderDecodeMode: RenderDecodeMode
        get() = mFrameRenderer.renderDecodeMode
        set(value) {
            mFrameRenderer.renderDecodeMode = value
        }

    override fun setSurfaceTextureListener(listener: SurfaceTextureListener) {
        mSurfaceTextureListener = listener
    }

    override fun getSurfaceTextureListener(): SurfaceTextureListener {
        return mSurfaceTextureListener!!
    }

    companion object {
        private const val TAG = "FrameAnimTextureView"
    }
}