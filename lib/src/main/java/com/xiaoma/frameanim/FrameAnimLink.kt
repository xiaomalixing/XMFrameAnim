package com.xiaoma.frameanim

import com.xiaoma.frameanim.anim.IFrameAnim
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.decode.DecodedFrame
import com.xiaoma.frameanim.decode.FrameDecoder
import com.xiaoma.frameanim.recycle.BitmapPool
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * 可以将多个帧动画合并为1个帧动画, 比如有两个帧动画A(30帧) 和 B(50帧)  合并为一个Link之后就是 A+B (80帧)
 */
class FrameAnimLink @JvmOverloads constructor(
    scope: CoroutineScope,
    anims: Collection<Any> = emptyList(),
    frameInterval: Long = 1000L / FrameConstant.DEFAULT_FPS,
    oneshot: Boolean = false,
) : FrameAnim(scope, LinkingDecoder(anims), frameInterval, oneshot) {

    fun addFrameAnim(anim: Any?) {
        (frameDecoder as LinkingDecoder).addFrameAnim(anim)
    }

    fun removeFrameAnim(anim: Any?) {
        (frameDecoder as LinkingDecoder).removeFrameAnim(anim)
    }

    private class LinkingDecoder(anims: Collection<Any>) : FrameDecoder {
        private val mAnims: MutableList<Any> = CopyOnWriteArrayList()
        private val mFrameCount = AtomicInteger()
        val mDecodedList: MutableList<DecodedFrame> = CopyOnWriteArrayList()
        override fun getFrame(index: Int, pool: BitmapPool): List<DecodedFrame> {
            mDecodedList.clear()
            var pos = 0
            for (anim in mAnims) {
                val seekTo = pos + getFrameCountByObj(anim)
                if (seekTo > index) {
                    val realIndex = index - pos
                    if (anim is IFrameAnim) {
                        decodeAndFillInList(anim, realIndex, pool, mDecodedList)
                    } else if (anim is FrameAnimSet) {
                        val subAnims = anim.allFrameAnim
                        if (!subAnims.isEmpty()) {
                            for (subAnim in subAnims) {
                                decodeAndFillInList(subAnim, realIndex, pool, mDecodedList)
                            }
                        }
                    }
                    break
                }
                pos = seekTo
            }
            return mDecodedList
        }

        private fun decodeAndFillInList(anim: IFrameAnim, index: Int, pool: BitmapPool, outList: MutableList<DecodedFrame>) {
            anim.frameDecoder.getFrame(index, pool).let {
                outList.addAll(it)
            }
        }

        override val frameCount: Int
            get() = mFrameCount.get()

        fun addFrameAnim(anim: Any?) {
            if (anim != null) {
                mAnims.add(anim)
                mFrameCount.addAndGet(getFrameCountByObj(anim))
            }
        }

        fun removeFrameAnim(anim: Any?) {
            if (anim != null) {
                mFrameCount.addAndGet(-getFrameCountByObj(anim))
                mAnims.remove(anim)
            }
        }

        companion object {
            private fun getFrameCountByObj(anim: Any): Int {
                if (anim is IFrameAnim) {
                    return (anim as FrameAnim).frameCount
                } else if (anim is FrameAnimSet) {
                    // 获取帧数最多的为Set的帧数, 避免其中某个动画无帧导致整个动画不播放
                    return anim.getMaxFrameCount()
                }
                return 0
            }
        }

        init {
            mAnims.addAll(anims)
            var frameCount = 0
            for (anim in anims) {
                frameCount += getFrameCountByObj(anim)
            }
            mFrameCount.set(frameCount)
        }
    }

    companion object {
        private const val TAG = "FrameAnimLinks"
    }
}