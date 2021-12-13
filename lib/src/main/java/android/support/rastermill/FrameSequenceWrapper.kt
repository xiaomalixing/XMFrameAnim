package android.support.rastermill

import android.graphics.Bitmap
import android.util.Log
import com.xiaoma.frameanim.BuildConfig
import java.io.InputStream

class FrameSequenceWrapper private constructor(private val mFrameSequence: FrameSequence) {
    val width: Int
        get() = mFrameSequence.width
    val height: Int
        get() = mFrameSequence.height
    val frameCount: Int
        get() = mFrameSequence.frameCount
    val isOpaque: Boolean
        get() = mFrameSequence.isOpaque

    private val mFrameSequenceState by lazy { mFrameSequence.createState() }

    fun getFrame(index: Int, output: Bitmap): Long =
        mFrameSequenceState.getFrame(index, output, index - 1)

    override fun toString(): String {
        return """{
            |w=$width, h=$height, opaque=$isOpaque
            |frameCount=$frameCount, loopCount=${mFrameSequence.defaultLoopCount} 
            |}""".trimMargin()
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "finalize: $this")
        }
        mFrameSequenceState?.destroy()
    }

    companion object {
        private const val TAG = "FrameSequenceWrapper"

        @JvmStatic
        fun decodeStream(input: InputStream): FrameSequenceWrapper =
            FrameSequenceWrapper(FrameSequence.decodeStream(input))
    }
}