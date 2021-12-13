package com.xiaoma.frameanim.util

import android.app.Service
import android.graphics.Point
import android.util.Log
import android.view.WindowManager
import androidx.annotation.Size
import com.xiaoma.frameanim.costant.FrameConstant

object DeviceUtil {
    private const val TAG = "DeviceUtil"

    val refreshRate: Int
        get() = try {
            val fps = winMgr.defaultDisplay.refreshRate.toInt()
            Log.e(TAG, "getRefreshRate: $fps")
            fps
        } catch (e: Exception) {
            Log.e(TAG, "getRefreshRate: Exception", e)
            FrameConstant.DEFAULT_FPS
        }

    @get:Size(2)
    val screenSize: IntArray
        get() {
            val sizeArr = IntArray(2)
            try {
                val size = Point()
                winMgr.defaultDisplay.getSize(size)
                sizeArr[0] = size.x
                sizeArr[1] = size.y
                Log.e(TAG, "getScreenSize: $size")
            } catch (e: Exception) {
                Log.e(TAG, "getScreenSize: Exception", e)
            }
            return sizeArr
        }

    private val winMgr: WindowManager by lazy {
        AppUtil.application.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    }
}