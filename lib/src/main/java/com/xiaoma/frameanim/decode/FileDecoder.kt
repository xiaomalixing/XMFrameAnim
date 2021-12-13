package com.xiaoma.frameanim.decode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.*

/**
 * 文件解码器, 帧序列直接存放在文件中, 一般是外部存储或内部存储
 */
class FileDecoder(framesDir: File) : BaseFrameDecoder() {
    private val mFrameFiles: MutableList<File> = mutableListOf()

    init {
        framesDir.listFiles()?.run {
            sortWith(FILE_NAME_COMPARATOR)
            mFrameFiles.addAll(asList())
        }
    }

    override fun onDecode(index: Int, opts: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeFile(mFrameFiles[index].absolutePath, opts)
    }

    override val frameCount: Int
        get() = mFrameFiles.size

    companion object {
        //private const val TAG = "FileDecoder"
        private val FILE_NAME_COMPARATOR = Comparator<File> { o1, o2 -> o1.name.compareTo(o2.name) }
    }
}