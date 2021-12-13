package com.xiaoma.frameanim

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.anim.IFrameAnimSet
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.decode.DecodedFrame
import com.xiaoma.frameanim.recycle.FrameAnimBmpPool
import com.xiaoma.frameanim.renderer.FrameRenderer
import com.xiaoma.frameanim.renderer.RenderDecodeMode
import com.xiaoma.frameanim.util.DeviceUtil
import com.xiaoma.frameanim.util.DrawUtil
import com.xiaoma.frameanim.util.coroutineScope
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

internal abstract class FrameRendererImpl(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
    private val mScope: CoroutineScope = context.coroutineScope
) : FrameRenderer, LifecycleObserver {

    private val mLastDecodeTime = AtomicLong(-1)
    private val mFrameAnims: MutableList<IFrameAnim> by lazy { CopyOnWriteArrayList() }
    private val mDecodeJobs: MutableList<Deferred<List<DecodedFrame>>> by lazy { CopyOnWriteArrayList() }

    @Volatile
    private var mScaleType: ScaleType? = null

    @Volatile
    private var mWidth = 0

    @Volatile
    private var mHeight = 0

    @Volatile
    private var mDirty = false

    @Volatile
    override var renderDecodeMode = RenderDecodeMode.AUTO

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.XMFrameAnim,
            defStyleAttr,
            defStyleRes
        )
        val index = a.getInt(R.styleable.XMFrameAnim_scaleType, -1)
        if (index >= 0 && index < sScaleTypeArray.size) {
            mScaleType = sScaleTypeArray[index]
        }
        Log.d(TAG, "init: i=$index, st=$mScaleType")
        a.recycle()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        Log.e(TAG, "onDestroy")
        synchronized(mFrameAnims) {
            // 当组件被销毁时,移除所有动画
            mFrameAnims.forEach { removeFrameAnim(it) }
        }
    }

    override fun addFrameAnim(anim: IFrameAnim) {
        synchronized(mFrameAnims) {
            mFrameAnims.add(anim)
            mDirty = true
        }
    }

    override fun removeFrameAnim(anim: IFrameAnim) {
        synchronized(mFrameAnims) {
            val idx = mFrameAnims.indexOf(anim)
            if (idx >= 0) {
                anim.stop()
                mFrameAnims.removeAt(idx)
            }
            mDirty = true
        }
    }

    override var fps: Int = FPS_AUTO

    override fun setScaleType(scaleType: ScaleType) {
        mScaleType = scaleType
        mDirty = true
    }

    protected abstract fun onLockCanvas(): Canvas?
    protected abstract fun onUnlockCanvasAndPost(canvas: Canvas)
    override suspend fun onDraw() {
        val t0 = SystemClock.uptimeMillis()
        mDirty = false
        var modeDesc: String? = null
        when (renderDecodeMode) {
            RenderDecodeMode.AUTO -> {
                val sync = if (mFrameAnims.size <= 1) {
                    // 如果只有一个动画, 则无法进行多线程优化
                    true
                } else {
                    // 根据上一次解码的耗时, 自动选择采用单线程或多线程绘制
                    mLastDecodeTime.get() in 1..mLastFrameDelay
                }
                modeDesc = if (sync) {
                    drawSync()
                    "Auto -> Sync"
                } else {
                    drawParallel()
                    "Auto -> Parallel"
                }
            }
            RenderDecodeMode.SYNC -> {
                drawSync()
                modeDesc = "Sync"
            }
            RenderDecodeMode.PARALLEL -> {
                drawParallel()
                modeDesc = "Parallel"
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG, "onDraw: [ fps: $fps, decodeTime: ${mLastDecodeTime.get()}, " +
                        "onDrawTime: ${SystemClock.uptimeMillis() - t0}, mode: $modeDesc ]"
            )
        }
    }

    private fun drawSync() {
        val canvas = onLockCanvas() ?: return
        val t0 = SystemClock.uptimeMillis()
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            mFrameAnims.forEach { anim ->
                anim.getFrames().forEach {
                    drawFrameAnim(it, anim, canvas)
                }
            }
        } finally {
            onUnlockCanvasAndPost(canvas)
        }
        mLastDecodeTime.set(SystemClock.uptimeMillis() - t0)
    }

    private suspend fun drawParallel() {
        // 重置解码时间
        mLastDecodeTime.set(0)
        // 开始异步解码
        val anims = mFrameAnims
        val decodeJobs = mDecodeJobs.apply { clear() }
        anims.forEach { anim ->
            decodeJobs += mScope.async(Dispatchers.Default) {
                val t0 = SystemClock.uptimeMillis()
                try {
                    anim.getFrames()
                } finally {
                    mLastDecodeTime.addAndGet(SystemClock.uptimeMillis() - t0) // 累加解码时间
                }
            }
        }
        val decodedFrame = decodeJobs.awaitAll()
        // 开始绘制
        val canvas = onLockCanvas() ?: return
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            decodedFrame.forEachIndexed { index, frames ->
                frames.forEach { drawFrameAnim(it, anims[index], canvas) }
            }
        } finally {
            onUnlockCanvasAndPost(canvas)
        }
    }

    private fun drawFrameAnim(frame: DecodedFrame, anim: IFrameAnim, canvas: Canvas) {
        val bmp = frame.frame
        if (bmp == null || bmp.isRecycled) {
            return
        }
        // 开始绘制
        canvas.let { c ->
            val sc = c.saveCount
            c.save()
            anim.params.let { p ->
                c.translate(p.x.toFloat(), p.y.toFloat())
                c.scale(p.scaleX, p.scaleY)
                c.rotate(p.rotation, frame.frameWidth * p.pivX, frame.frameHeight * p.pivY)
            }
            // 优先使用动画自身的ScaleType
            val st = anim.scaleType ?: mScaleType ?: FrameConstant.DEFAULT_SCALE_TYPE
            DrawUtil.drawWithScaleType(c, mWidth, mHeight, frame, st)
            c.restoreToCount(sc)
        }
        // 绘制完毕, 复用图像
        FrameAnimBmpPool.put(bmp)
    }

    override fun addFrameAnim(aSet: IFrameAnimSet) {
        aSet.allFrameAnim.forEach { addFrameAnim(it) }
    }

    override fun removeFrameAnim(aSet: IFrameAnimSet) {
        aSet.allFrameAnim.forEach { removeFrameAnim(it) }
    }

    override fun startAllAnim() {
        synchronized(mFrameAnims) {
            mFrameAnims.forEach { it.start() }
            mDirty = true
        }
    }

    override fun stopAllAnim() {
        synchronized(mFrameAnims) {
            mFrameAnims.forEach { it.stop() }
            mDirty = true
        }
    }

    private var mRenderJob: Job? = null
    private var mLastFrameDelay: Long = 0L

    @Synchronized
    override fun startRender() {
        synchronized(this) {
            if (mRenderJob == null) {
                mRenderJob = mScope.launch(Dispatchers.Default) {
                    // 循环绘制
                    while (true) {
                        // 计算帧间隔
                        val delayMs = fps.let { fps ->
                            if (fps <= 0) {
                                // 自适应刷新率
                                if (mFrameAnims.isEmpty()) {
                                    1000L / DeviceUtil.refreshRate
                                } else {
                                    mFrameAnims.minOf {
                                        val interval = it.frameInterval
                                        if (interval > 0) interval
                                        else 1000L / DeviceUtil.refreshRate
                                    }
                                }
                            } else {
                                // 固定刷新率
                                1000L / fps
                            }
                        }
                        // 判断是否需要重绘
                        var dirty = mDirty
                        if (!dirty) {
                            for (anim in mFrameAnims) {
                                if (anim.isRunning) {
                                    dirty = true
                                    break
                                }
                            }
                        }
                        // 开始重绘
                        if (dirty) onDraw()
                        if (BuildConfig.DEBUG) Log.d(TAG, "delay: $delayMs")
                        delay(delayMs.apply { mLastFrameDelay = this })
                    }
                }
            }
        }
    }

    @Synchronized
    override fun stopRender() {
        synchronized(this) {
            mRenderJob?.cancel()
            mRenderJob = null
        }
    }

    fun setSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        mDirty = true
    }

    companion object {
        /**
         * <=0 表示自适应刷新率
         */
        const val FPS_AUTO = -1

        private const val TAG = "FrameRendererImpl"
        private val sScaleTypeArray = arrayOf(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )
    }
}