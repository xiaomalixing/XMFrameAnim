package com.xiaoma.frameanim.recycle

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.xiaoma.frameanim.util.AppUtil

object FrameAnimBmpPool : BitmapPool {
    private const val TAG = "FrameAnimBmpPool"
    private val mImpl: BitmapPool

    init {
        // Android 8.0 (API 26)以上, Bitmap存储在Native层, 不会占用Java堆内存, 因此无须担心GC带来的性能问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mImpl = BmpPoolNoCache
        } else {
            mImpl = BmpPoolFixedSize
            AppUtil.application.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    Log.e(TAG, String.format("onTrimMemory: [ level: %s ]", level))
                    clear()
                }

                override fun onConfigurationChanged(newConfig: Configuration) {
                    Log.d(TAG, String.format("onConfigurationChanged: [ cfg: %s ]", newConfig))
                }

                override fun onLowMemory() {
                    Log.e(TAG, "onLowMemory")
                    clear()
                }
            })
        }
    }

    override fun put(bitmap: Bitmap) {
        mImpl.put(bitmap)
    }

    override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        return mImpl.get(width, height, config)
    }

    override fun clear() {
        Log.e(TAG, "clear")
        mImpl.clear()
    }
}