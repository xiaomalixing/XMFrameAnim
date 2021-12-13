package com.xiaoma.frameanim

import android.util.Log
import com.xiaoma.frameanim.anim.FrameAnimListener
import com.xiaoma.frameanim.anim.FrameAnimParams
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.decode.DecodedFrame
import com.xiaoma.frameanim.decode.FrameDecoder
import com.xiaoma.frameanim.recycle.FrameAnimBmpPool
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 帧动画Model, 可以通过工厂类实例化[FrameAnimFactory]
 */
open class FrameAnim @JvmOverloads constructor(
    private val mScope: CoroutineScope,
    override val frameDecoder: FrameDecoder,
    override var frameInterval: Long = 1000L / FrameConstant.DEFAULT_FPS,
    override var oneshot: Boolean = false,
) : IFrameAnim {

    private val mFrameAnimListeners: MutableList<FrameAnimListener> = CopyOnWriteArrayList()
    private var mAnimJob: Job? = null

    @InternalCoroutinesApi
    override fun start() {
        synchronized(this) {
            if (isRunning) {
                Log.w(TAG, "start: but running, return")
                return
            }
            mAnimJob?.run {
                if (!isCompleted && !isCancelled) return
                cancel()
            }
            mAnimJob = mScope.launch(Dispatchers.Default) {
                // 调用start时, 如果处于最后一帧, 则回到一帧, 避免start没有反应
                if (frameIndex >= frameCount - 1) {
                    frameIndex = 0
                }
                for (l in mFrameAnimListeners) {
                    l.onStart(this@FrameAnim)
                }
                while (true) {
                    doNextFrame()
                    delay(frameInterval)
                }
            }.apply {
                invokeOnCompletion(onCancelling = true) {
                    for (l in mFrameAnimListeners) {
                        l.onStop(this@FrameAnim)
                    }
                }
            }
        }
    }

    override fun stop() {
        synchronized(this) {
            mAnimJob?.cancel()
        }
    }

    override fun isRunning(): Boolean = mAnimJob?.run {
        !isCancelled && !isCompleted
    } ?: false

    override val frameCount: Int
        get() = frameDecoder.frameCount

    @Volatile
    override var frameIndex: Int = 0

    override var scaleType: ScaleType? = null

    override var params: FrameAnimParams = FrameAnimParams()

    override fun getFrames(): List<DecodedFrame> {
        val frames = frameDecoder.getFrame(frameIndex, FrameAnimBmpPool)
        if (frames.isNotEmpty()) {
            val interval = frames[0].frameInterval
            if (interval > 0) {
                frameInterval = interval
            }
        }
        return frames
    }

    override fun addFrameAnimListener(listener: FrameAnimListener) {
        mFrameAnimListeners.add(listener)
    }

    override fun removeFrameAnimListener(listener: FrameAnimListener) {
        mFrameAnimListeners.remove(listener)
    }

    private fun doNextFrame() {
        if (frameCount <= 0) {
            // 没有帧数,主动停止动画
            stop()
            return
        }
        frameIndex = (frameIndex + 1) % frameCount
        if (oneshot && frameIndex == 0) {
            stop()
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "finalize: $this")
        }
        stop()
    }

    companion object {
        private const val TAG = "FrameAnimImpl"
    }
}