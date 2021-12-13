package com.xiaoma.frameanim.decode

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

class AssetsDecoder(context: Context, path: String) : BaseFrameDecoder() {
    private val mAssets: AssetManager = context.assets
    private val mFiles = mutableListOf<String>()

    init {
        try {
            // 路径格式归一化, 避免多个'/'或少个'/'产生问题
            val dirPath = File(path).path
            val relativePath = mAssets.list(dirPath)
            if (relativePath?.isNotEmpty() == true) {
                relativePath.forEach {
                    mFiles += File(dirPath, it).path
                }
                mFiles.sort()
            }
        } catch (e: Exception) {
            Log.w(TAG, String.format("AssetsDecoder: init Exception", path), e)
        }
    }

    override fun onDecode(index: Int, opts: BitmapFactory.Options): Bitmap? {
        var frame: Bitmap? = null
        try {
            frame = BitmapFactory.decodeStream(mAssets.open(mFiles[index]), null, opts)
        } catch (e: Exception) {
            Log.w(TAG, "onDecode: decodeStream Exception", e)
        }
        return frame
    }

    override val frameCount: Int
        get() = mFiles.size

    companion object {
        private const val TAG = "AssetsDecoder"
    }
}