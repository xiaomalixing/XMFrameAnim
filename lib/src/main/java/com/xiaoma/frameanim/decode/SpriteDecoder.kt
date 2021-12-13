package com.xiaoma.frameanim.decode

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.util.Log
import com.xiaoma.frameanim.costant.FrameConstant
import com.xiaoma.frameanim.costant.ResourceType
import com.xiaoma.frameanim.recycle.BitmapPool
import com.xiaoma.frameanim.util.BmpUtil
import com.xiaoma.frameanim.util.IOUtil
import java.io.File
import java.io.FileInputStream
import java.util.*

// 支持图集(精灵)解析
class SpriteDecoder(
    private val mContext: Context,
    private val mSpriteDir: String,
    private val mSpriteName: String?,
    private val mResourceType: ResourceType
) : FrameDecoder {
    private val mSpriteFrames: List<SpriteFrame> by lazy {
        var frames: List<SpriteFrame>? = when (mResourceType) {
            ResourceType.FILE -> loadFromFile()
            ResourceType.ASSETS -> loadFromAssets()
        }
        if (frames == null) {
            frames = emptyList()
            Log.e(TAG, String.format("spriteFrames: [ dir: %s, name: %s ] Load failed", mSpriteDir, mSpriteName))
        } else {
            // 注意: 一定要按照源文件名排序,否则帧序会乱掉
            Collections.sort(frames, SPRITE_FRAME_COMPARATOR)
        }
        frames
    }

    private val mRegionDecoderMap: MutableMap<String?, BitmapRegionDecoder> = HashMap(0)

    // 避免GC
    private val mRectTmp = Rect()

    // 避免GC
    private val mOptTmp: BitmapFactory.Options = BitmapFactory.Options()

    // 避免GC
    private val mDecodedFrameTmp = DecodedFrame()

    // 避免GC
    private val mDecodedFrameList = listOf(mDecodedFrameTmp)

    init {
        // 为了减少GC,固定一个16K的解码缓冲区; 否则每次调用decodeXXX,BitmapFactory内部都会new一个16K的缓冲区
        mOptTmp.inTempStorage = ByteArray(16 * 1024)
    }

    private fun loadFromFile(): List<SpriteFrame> {
        var frames: List<SpriteFrame>? = null
        val dataDir = File(mSpriteDir)
        // 支持解析多个精灵图集
        val files = dataDir.listFiles { pathname ->
            val fName = pathname.name
            // SpriteName为空则读取所有配置
            if (mSpriteName.isNullOrEmpty()) {
                fName.endsWith(".json")
            } else fName.startsWith(mSpriteName) && fName.endsWith(".json")
        }
        if (files.isNotEmpty()) {
            // 按文件名排序
            files.sortWith { o1, o2 -> o1.name.compareTo(o2.name) }
            frames = ArrayList()
            for (dataF in files) {
                val partFrames: List<SpriteFrame> = SpriteFrame.fromJson(IOUtil.readString(dataF))
                if (partFrames.isNotEmpty()) {
                    frames.addAll(partFrames)
                }
            }
        }
        return frames ?: emptyList()
    }

    private fun loadFromAssets(): List<SpriteFrame>? {
        var frames: MutableList<SpriteFrame>? = null
        // 多个精灵图集
        try {
            val fileNames = mContext.assets.list(mSpriteDir)
            if (fileNames?.isNotEmpty() == true) {
                val dataFiles: MutableList<String> = ArrayList()
                val spriteDirF = File(mSpriteDir)
                for (fileName in fileNames) {
                    // SpriteName为空,表示读取当前dir下的所有精灵
                    if (mSpriteName.isNullOrEmpty()) {
                        if (fileName.endsWith(".json")) {
                            dataFiles.add(File(spriteDirF, fileName).path)
                        }
                    } else {
                        if (fileName.startsWith(mSpriteName) && fileName.endsWith(".json")) {
                            dataFiles.add(File(spriteDirF, fileName).path)
                        }
                    }
                }
                if (dataFiles.isNotEmpty()) {
                    dataFiles.sortWith { o1, o2 -> o1.compareTo(o2) }
                    frames = ArrayList()
                    for (dataFile in dataFiles) {
                        val spriteFrames: List<SpriteFrame> = SpriteFrame.fromJson(
                            IOUtil.readStringFromAssets(dataFile)
                        )
                        if (spriteFrames.isNotEmpty()) {
                            frames.addAll(spriteFrames)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, String.format("loadFromAssets: [ dir: %s ] MULTI, Exception", mSpriteDir), e)
        }
        return frames
    }

    private fun getRegionDecoder(spriteFrame: SpriteFrame): BitmapRegionDecoder? {
        synchronized(mRegionDecoderMap) {
            val imageFileName = spriteFrame.imageFileName
            val decoderMap = mRegionDecoderMap
            var decoder = decoderMap[imageFileName]
            if (decoder == null) {
                val imageFile = File(mSpriteDir, imageFileName)
                try {
                    decoder = when (mResourceType) {
                        ResourceType.FILE -> BitmapRegionDecoder.newInstance(
                            FileInputStream(imageFile),
                            false
                        )
                        ResourceType.ASSETS -> BitmapRegionDecoder.newInstance(
                            mContext.assets.open(imageFile.path),
                            false
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (decoder != null) {
                    decoderMap[imageFileName] = decoder
                }
            }
            return decoder
        }
    }

    override fun getFrame(index: Int, pool: BitmapPool): List<DecodedFrame> {
        val spriteFrames = mSpriteFrames
        // 解析精灵数据文件
        if (index < 0 || index >= spriteFrames.size) {
            Log.e(TAG, String.format("getFrame: [ idx: %s, frameSz: %s ] Invalid index", index, spriteFrames.size))
            return emptyList()
        }
        return decodeFrame(index, pool)
    }

    override val frameCount: Int
        get() = mSpriteFrames.size

    private fun decodeFrame(index: Int, pool: BitmapPool): List<DecodedFrame> {
        val spriteFrame = mSpriteFrames[index]
        val regionDecoder = getRegionDecoder(spriteFrame) ?: return emptyList()
        // 先取出原图在图集中的部分
        val frameSize = spriteFrame.frame ?: return emptyList()
        // 注意: 不论是否旋转,左上角的位置不变
        mRectTmp.left = frameSize.x
        mRectTmp.top = frameSize.y
        if (spriteFrame.rotated) {
            mRectTmp.right = mRectTmp.left + frameSize.h
            mRectTmp.bottom = mRectTmp.top + frameSize.w
        } else {
            mRectTmp.right = mRectTmp.left + frameSize.w
            mRectTmp.bottom = mRectTmp.top + frameSize.h
        }
        // 注意: 因为旋转后,宽高是反的,这里要以rect为准
        val width = mRectTmp.right - mRectTmp.left
        val height = mRectTmp.bottom - mRectTmp.top
        val config = spriteFrame.config ?: FrameConstant.DEFAULT_BMP_CONFIG
        // 开始解码
        val spriteInBmp = pool.get(width, height, config)
        BmpUtil.setNonScaleOpt(mOptTmp)
        mOptTmp.inBitmap = spriteInBmp
        val spriteBmp = regionDecoder.decodeRegion(mRectTmp, mOptTmp) // 在图集中的部分(精灵)
        if (spriteInBmp != spriteBmp) {
            // 复用未能解码的inBitmap
            pool.put(spriteInBmp)
        }
        // 写入Frame字段
        mDecodedFrameTmp.run {
            frame = spriteBmp
            val spriteSourceSize = spriteFrame.spriteSourceSize
            if (spriteSourceSize != null) {
                x = spriteSourceSize.x
                y = spriteSourceSize.y
            }
            val sourceSize = spriteFrame.sourceSize
            if (sourceSize != null) {
                frameWidth = sourceSize.w
                frameHeight = sourceSize.h
            }
            rotate = if (spriteFrame.rotated) -90 else 0
        }
        return mDecodedFrameList
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        Log.e(TAG, "finalize")
        synchronized(mRegionDecoderMap) {
            for ((_, decoder) in mRegionDecoderMap) {
                decoder.recycle()
            }
            mRegionDecoderMap.clear()
        }
    }

    companion object {
        private const val TAG = "SpriteDecoder"
        private val SPRITE_FRAME_COMPARATOR = Comparator<SpriteFrame> { o1, o2 -> o1.sourceFileName!!.compareTo(o2.sourceFileName!!) }
    }
}