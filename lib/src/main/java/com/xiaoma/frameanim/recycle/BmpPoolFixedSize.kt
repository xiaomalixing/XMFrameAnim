package com.xiaoma.frameanim.recycle

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.xiaoma.frameanim.BuildConfig
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.util.BmpUtil
import com.xiaoma.frameanim.util.BytesUtil
import com.xiaoma.frameanim.util.DeviceUtil
import java.util.*

internal object BmpPoolFixedSize : BitmapPool {
    private const val TAG = "BmpPool_FixedSize"
    private const val DEFAULT_CACHED_SCREEN_SIZE = 1
    private val maxPoolSize: Int

    init {
        // 默认最大缓存数
        val screenSize = DeviceUtil.screenSize
        maxPoolSize = if (screenSize[0] > 0 && screenSize[1] > 0) {
            DEFAULT_CACHED_SCREEN_SIZE * BmpUtil.getByteSize(
                    screenSize[0], screenSize[1], FrameConstant.DEFAULT_BMP_CONFIG)
        } else {
            8 * 1024 * 1024
        }
        Log.e(TAG, String.format("instance initializer: [ maxPoolSz: %sMB ]", BytesUtil.toMB(maxPoolSize.toLong())))
    }

    // 因为Bitmap复用机制在4.4上允许大于等于原图大小,所以采用排序结构,优先淘汰小的Size
    private val mBitmapCache = TreeMap<Int, LinkedList<Bitmap>>()

    @Volatile
    private var mPoolSize = 0
    override fun put(bitmap: Bitmap) {
        synchronized(mBitmapCache) {
            if (bitmap.isRecycled || !bitmap.isMutable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, String.format("put: [ recycled: %s, isMutable: %s ] invalid bitmap",
                            bitmap.isRecycled, bitmap.isMutable))
                }
                return
            }
            val bmpSize = bitmap.allocationByteCount
            trimForSize(bmpSize)
            var bitmaps = mBitmapCache[bmpSize]
            if (bitmaps == null) {
                bitmaps = LinkedList()
                mBitmapCache[bmpSize] = bitmaps
            }
            bitmaps.addLast(bitmap)
            mPoolSize += bmpSize
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("put: [ w: %s, h: %s cfg: %s, bmpSz: %sKB, poolSz: %sMB, max: %sMB ]",
                        bitmap.width, bitmap.height, bitmap.config,
                        BytesUtil.toKB(bmpSize.toLong()), BytesUtil.toMB(mPoolSize.toLong()), BytesUtil.toMB(maxPoolSize.toLong())))
            }
        }
    }

    // 移除策略: 直接移除同一个Size下的所有Bitmap集合
    private fun trimForSize(bmpSize: Int) {
        synchronized(mBitmapCache) {
            if (mPoolSize + bmpSize <= maxPoolSize) {
                return
            }
            val it: MutableIterator<Map.Entry<Int, LinkedList<Bitmap>>> = mBitmapCache.entries.iterator()
            var sizeMeet = false
            while (it.hasNext()) {
                val entry = it.next()
                val bitmaps = entry.value
                if (bitmaps.isEmpty()) {
                    it.remove()
                    continue
                }
                val allocBytes = entry.key
                var bmp: Bitmap
                while (bitmaps.pollLast().also { bmp = it } != null) {
                    bmp.recycle()
                    mPoolSize -= allocBytes
                    if (mPoolSize + bmpSize <= maxPoolSize) {
                        sizeMeet = true
                        break
                    }
                }
                if (bitmaps.isEmpty()) {
                    it.remove()
                }
                if (sizeMeet) {
                    break
                }
            }
            if (BuildConfig.DEBUG) {
                Log.w(TAG, String.format("trimForSize: [ bmpSz: %sKB, oldPoolSz: %sMB, poolSz: %sMB, max: %sMB ]",
                        BytesUtil.toKB(bmpSize.toLong()), BytesUtil.toMB(maxPoolSize.toLong()), BytesUtil.toMB(mPoolSize.toLong()), BytesUtil.toMB(maxPoolSize.toLong())))
            }
        }
    }

    override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        synchronized(mBitmapCache) {
            var bmp: Bitmap? = null
            val bmpSize = BmpUtil.getByteSize(width, height, config)
            val ceilingEntry = mBitmapCache.ceilingEntry(bmpSize)
            if (ceilingEntry != null) {
                val allocBytes = ceilingEntry.key
                val bitmaps = ceilingEntry.value
                if (bitmaps != null && !bitmaps.isEmpty()) {
                    bmp = bitmaps.removeLast()
                    bmp.reconfigure(width, height, config)
                    bmp.eraseColor(Color.TRANSPARENT)
                    mPoolSize -= allocBytes
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format("get: [ w: %s, h: %s cfg: %s, bmpSz: %sKB, poolSz: %sMB, max: %sMB ] Reuse",
                                width, height, config, BytesUtil.toKB(bmpSize.toLong()), BytesUtil.toMB(mPoolSize.toLong()), BytesUtil.toMB(maxPoolSize.toLong())))
                    }
                }
            }
            if (bmp == null) {
                bmp = Bitmap.createBitmap(width, height, config)
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("get: [ w: %s, h: %s cfg: %s, bmpSz: %sKB, poolSz: %sMB, max: %sMB ] New bitmap",
                            width, height, config, BytesUtil.toKB(bmpSize.toLong()), BytesUtil.toMB(mPoolSize.toLong()), BytesUtil.toMB(maxPoolSize.toLong())))
                }
            }

            // 注意: 这里一定要清除Density, 否则会导致不同Density的Bitmap绘制出问题
            bmp!!.density = Bitmap.DENSITY_NONE
            return bmp
        }
    }

    override fun clear() {
        synchronized(mBitmapCache) {
            mBitmapCache.clear()
            mPoolSize = 0
        }
    }
}