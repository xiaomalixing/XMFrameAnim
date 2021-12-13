package com.xiaoma.frameanim

import com.xiaoma.frameanim.anim.FrameAnimListener
import com.xiaoma.frameanim.anim.FrameAnimParams
import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.anim.IFrameAnimSet
import com.xiaoma.frameanim.decode.FrameDecoder
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 帧动画集合, 维护多个[FrameAnim], 和原生Android的[android.view.animation.AnimationSet]特性类似.
 * 对于各个状态相同的动画可以直接通过动画集合来控制, 后者可以通过动画集合来定义图元.
 */
class FrameAnimSet : IFrameAnimSet {
    private var mMaxFrameCount = 0

    constructor(vararg anims: IFrameAnim) {
        allFrameAnim.addAll(anims)
        findMaxFrameCount()
    }

    constructor(anims: Collection<IFrameAnim>) {
        allFrameAnim.addAll(anims)
        findMaxFrameCount()
    }

    override val allFrameAnim: MutableList<IFrameAnim> = CopyOnWriteArrayList()

    override fun getMaxFrameCount() = mMaxFrameCount

    override fun addFrameAnim(anim: IFrameAnim) {
        allFrameAnim += anim
        findMaxFrameCount()
    }

    override fun removeFrameAnim(anim: IFrameAnim) {
        allFrameAnim -= anim
        findMaxFrameCount()
    }


    override fun start(animIndex: Int) {
        try {
            allFrameAnim[animIndex].start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop(animIndex: Int) {
        try {
            allFrameAnim[animIndex].stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getFrameIndex(animIndex: Int): Int {
        try {
            return allFrameAnim[animIndex].frameIndex
        } catch (ignored: Exception) {
        }
        return -1
    }

    override fun setFrameIndex(frameIndex: Int) {
        for (anim in allFrameAnim) {
            anim.frameIndex = frameIndex
        }
    }

    override fun setFrameIndex(animIndex: Int, frameIndex: Int) {
        try {
            allFrameAnim[animIndex].frameIndex = frameIndex
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getFrameCount(animIndex: Int): Int {
        try {
            return allFrameAnim[animIndex].frameCount
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    override fun setOneShot(oneshot: Boolean) {
        for (anim in allFrameAnim) {
            anim.oneshot = oneshot
        }
    }

    override fun setOneShot(animIndex: Int, oneshot: Boolean) {
        try {
            allFrameAnim[animIndex].oneshot = oneshot
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun isOneShot(animIndex: Int): Boolean {
        try {
            return allFrameAnim[animIndex].oneshot
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun setFrameInterval(frameInterval: Long) {
        for (anim in allFrameAnim) {
            anim.frameInterval = frameInterval
        }
    }

    override fun setFrameInterval(animIndex: Int, frameInterval: Long) {
        try {
            allFrameAnim[animIndex].frameInterval = frameInterval
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getFrameInterval(animIndex: Int): Long {
        try {
            return allFrameAnim[animIndex].frameInterval
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1L
    }

    override fun setScaleType(scaleType: ScaleType) {
        for (anim in allFrameAnim) {
            anim.scaleType = scaleType
        }
    }

    override fun setScaleType(animIndex: Int, scaleType: ScaleType?) {
        try {
            allFrameAnim[animIndex].scaleType = scaleType
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getScaleType(animIndex: Int): ScaleType? = allFrameAnim[animIndex].scaleType

    override fun addFrameAnimListener(listener: FrameAnimListener) {
        for (anim in allFrameAnim) {
            anim.addFrameAnimListener(listener)
        }
    }

    override fun addFrameAnimListener(animIndex: Int, listener: FrameAnimListener) {
        try {
            allFrameAnim[animIndex].addFrameAnimListener(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun removeFrameAnimListener(listener: FrameAnimListener) {
        for (anim in allFrameAnim) {
            anim.removeFrameAnimListener(listener)
        }
    }

    override fun removeFrameAnimListener(animIndex: Int, listener: FrameAnimListener) {
        try {
            allFrameAnim[animIndex].removeFrameAnimListener(listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getFrameDecoder(animIndex: Int): FrameDecoder = allFrameAnim[animIndex].frameDecoder

    override fun getParams(animIndex: Int): FrameAnimParams = allFrameAnim[animIndex].params

    override fun setParams(params: FrameAnimParams) {
        for (anim in allFrameAnim) {
            anim.params = params
        }
    }

    override fun setParams(animIndex: Int, params: FrameAnimParams) {
        try {
            allFrameAnim[animIndex].params = params
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        for (anim in allFrameAnim) {
            anim.start()
        }
    }

    override fun stop() {
        for (anim in allFrameAnim) {
            anim.stop()
        }
    }

    override fun isRunning(animIndex: Int): Boolean {
        try {
            return allFrameAnim[animIndex].isRunning
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun findMaxFrameCount() {
        var max = 0
        for (anim in allFrameAnim) {
            val count = anim.frameCount
            if (count > max) {
                max = count
            }
        }
        mMaxFrameCount = max
    }
}