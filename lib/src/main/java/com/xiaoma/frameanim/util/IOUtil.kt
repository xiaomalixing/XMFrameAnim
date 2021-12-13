package com.xiaoma.frameanim.util

import androidx.annotation.RawRes
import java.io.*

object IOUtil {
    fun readString(f: File?): String? {
        try {
            return readString(FileInputStream(f))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun readStringFromAssets(fileName: String?): String? {
        try {
            return readString(AppUtil.application.assets.open(fileName))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun readStringFromRaw(@RawRes rawId: Int): String? {
        try {
            return readString(AppUtil.application.resources.openRawResource(rawId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun readString(`in`: InputStream?): String? {
        if (`in` == null) return null
        try {
            InputStreamReader(`in`).use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(8 * 1024)
                var len: Int
                while (reader.read(buf).also { len = it } > 0) {
                    sb.append(buf, 0, len)
                }
                return sb.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}