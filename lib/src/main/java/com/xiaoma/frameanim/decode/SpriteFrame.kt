package com.xiaoma.frameanim.decode

import android.graphics.Bitmap
import org.json.JSONObject
import java.util.*

/**
 * 精灵帧
 */
internal class SpriteFrame private constructor() {
    /**
     * 在图集里的区域
     */
    var frame: Size? = null

    /**
     * 是否经过旋转
     */
    var rotated = false

    /**
     * 是否经过剪裁
     */
    var trimmed = false

    /**
     * 精灵文件名
     */
    var imageFileName: String? = null

    /**
     * 图片的Config
     */
    var config: Bitmap.Config? = null

    /**
     * 有效区域相对原图的位置(剪裁后的区域)
     */
    var spriteSourceSize: Size? = null

    /**
     * 原图的尺寸
     */
    var sourceSize: Size? = null

    /**
     * 源文件文件名
     */
    var sourceFileName: String? = null

    // 可以没有xy
    // 但是不能没有w h
    internal class Size(json: JSONObject) {
        val x: Int = json.optInt("x")
        val y: Int = json.optInt("y")
        val w: Int = json.getInt("w")
        val h: Int = json.getInt("h")
    }

    companion object {
        fun fromJson(json: String?): List<SpriteFrame> {
            var spriteFrames: List<SpriteFrame>? = null
            try {
                val obj = JSONObject(json)
                val arr = obj.getJSONArray("frames")
                val metaObj = obj.getJSONObject("meta")
                val imageFileName = metaObj.getString("image")
                val bmpConfig = parseBmpConfig(metaObj.getString("format"))
                val frameCount = arr.length()
                spriteFrames = ArrayList(frameCount)
                for (i in 0 until frameCount) {
                    val spriteObj = arr.getJSONObject(i)
                    val frame = SpriteFrame()
                    frame.frame = Size(spriteObj.getJSONObject("frame"))
                    frame.rotated = spriteObj.getBoolean("rotated")
                    frame.trimmed = spriteObj.getBoolean("trimmed")
                    frame.imageFileName = imageFileName
                    frame.config = bmpConfig
                    frame.spriteSourceSize = Size(spriteObj.getJSONObject("spriteSourceSize"))
                    frame.sourceSize = Size(spriteObj.getJSONObject("sourceSize"))
                    frame.sourceFileName = spriteObj.getString("filename")
                    spriteFrames.add(frame)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return spriteFrames ?: emptyList()
        }

        private fun parseBmpConfig(format: String): Bitmap.Config? {
            if ("RGBA8888".equals(format, ignoreCase = true)) {
                return Bitmap.Config.ARGB_8888
            }
            if ("RGBA4444".equals(format, ignoreCase = true)) {
                return Bitmap.Config.ARGB_4444
            }
            return if ("RGB565".equals(format, ignoreCase = true)) {
                Bitmap.Config.RGB_565
            } else null
        }
    }
}