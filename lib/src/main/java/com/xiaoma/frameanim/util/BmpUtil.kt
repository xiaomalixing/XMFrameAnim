package com.xiaoma.frameanim.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import com.xiaoma.frameanim.costant.FrameConstant

object BmpUtil {
    fun getConfig(options: BitmapFactory.Options?): Bitmap.Config {
        var config: Bitmap.Config? = null
        if (options != null) {
            config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                options.outConfig
            } else {
                options.inPreferredConfig
            }
        }
        if (config == null) {
            config = FrameConstant.DEFAULT_BMP_CONFIG
        }
        return config
    }

    fun setNonScaleOpt(opt: BitmapFactory.Options?) {
        if (opt != null) {
            opt.inDensity = Bitmap.DENSITY_NONE
            opt.inScreenDensity = Bitmap.DENSITY_NONE
            opt.inTargetDensity = Bitmap.DENSITY_NONE
            opt.inScaled = false
        }
    }

    fun getByteSize(width: Int, height: Int, config: Bitmap.Config): Int {
        return width * height * getBytesPerPixel(config)
    }

    private fun getBytesPerPixel(config: Bitmap.Config = FrameConstant.DEFAULT_BMP_CONFIG): Int {
        return when (config) {
            Bitmap.Config.ALPHA_8 -> 1
            Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.RGBA_F16 -> 8
            Bitmap.Config.ARGB_8888 -> 4
            else -> 4
        }
    }
}