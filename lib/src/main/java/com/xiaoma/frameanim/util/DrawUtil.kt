package com.xiaoma.frameanim.util

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.Paint
import android.graphics.RectF
import com.xiaoma.frameanim.ScaleType
import com.xiaoma.frameanim.decode.DecodedFrame
import kotlin.math.min
import kotlin.math.roundToInt

object DrawUtil {
    private val sMatrixTmp = Matrix()
    private val sBmpRectTmp = RectF()
    private val sOutRectTmp = RectF()
    private val sPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG)

    // 根据ScaleType绘制帧
    fun drawWithScaleType(canvas: Canvas,
                          outWidth: Int, outHeight: Int,
                          decodedFrame: DecodedFrame,
                          scaleType: ScaleType) {
        val bmp = decodedFrame.frame ?: return
        val frameWidth = decodedFrame.frameWidth
        val frameHeight = decodedFrame.frameHeight
        val saveCount = canvas.saveCount
        canvas.save()

        when (scaleType) {
            ScaleType.MATRIX -> {
            }
            ScaleType.FIT_XY -> fitMatrix(canvas,
                    frameWidth, frameHeight,
                    outWidth, outHeight,
                    ScaleToFit.FILL)
            ScaleType.FIT_START -> fitMatrix(canvas,
                    frameWidth, frameHeight,
                    outWidth, outHeight,
                    ScaleToFit.START)
            ScaleType.FIT_CENTER -> fitMatrix(canvas,
                    frameWidth, frameHeight,
                    outWidth, outHeight,
                    ScaleToFit.CENTER)
            ScaleType.FIT_END -> fitMatrix(canvas,
                    frameWidth, frameHeight,
                    outWidth, outHeight,
                    ScaleToFit.END)
            ScaleType.CENTER -> canvas.translate(((outWidth - frameWidth) * 0.5f).roundToInt().toFloat(),
                    ((outHeight - frameHeight) * 0.5f).roundToInt().toFloat())
            ScaleType.CENTER_CROP -> {
                val scale: Float
                var dx = 0f
                var dy = 0f
                if (frameWidth * outHeight > outWidth * frameHeight) {
                    scale = outHeight.toFloat() / frameHeight.toFloat()
                    dx = (outWidth - frameWidth * scale) * 0.5f
                } else {
                    scale = outWidth.toFloat() / frameWidth.toFloat()
                    dy = (outHeight - frameHeight * scale) * 0.5f
                }
                canvas.scale(scale, scale)
                canvas.translate(dx.roundToInt().toFloat(), dy.roundToInt().toFloat())
            }
            ScaleType.CENTER_INSIDE -> {
                val scale: Float = if (frameWidth <= outWidth && frameHeight <= outHeight) {
                    1.0f
                } else {
                    min(outWidth.toFloat() / frameWidth.toFloat(),
                            outHeight.toFloat() / frameHeight.toFloat())
                }
                val dx = ((outWidth - frameWidth * scale) * 0.5f).roundToInt().toFloat()
                val dy = ((outHeight - frameHeight * scale) * 0.5f).roundToInt().toFloat()
                canvas.scale(scale, scale)
                canvas.translate(dx, dy)
            }
        }

        // 根据坐标,旋转角度进行变换
        if (decodedFrame.x != 0 || decodedFrame.y != 0) {
            canvas.translate(decodedFrame.x.toFloat(), decodedFrame.y.toFloat())
        }
        if (decodedFrame.rotate != 0) {
            canvas.rotate(decodedFrame.rotate.toFloat())
            canvas.translate(-bmp.width.toFloat(), 0f) // 因为旋转之后,相对位置发生变化(宽高反了),所以要重新平移回去
        }
        canvas.drawBitmap(bmp, 0f, 0f, sPaint)
        canvas.restoreToCount(saveCount)
    }

    private fun fitMatrix(canvas: Canvas,
                          bmpW: Int, bmpH: Int,
                          outWidth: Int, outHeight: Int,
                          fit: ScaleToFit) {
        val m = sMatrixTmp
        val bmpRect = sBmpRectTmp
        bmpRect[0f, 0f, bmpW.toFloat()] = bmpH.toFloat()
        val outRect = sOutRectTmp
        outRect[0f, 0f, outWidth.toFloat()] = outHeight.toFloat()
        m.setRectToRect(bmpRect, outRect, fit)
        canvas.concat(m)
    }
}