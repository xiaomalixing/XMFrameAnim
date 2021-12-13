package com.xiaoma.frameanim.costant

enum class ResourceType {
    /**
     * 文件[java.io.File]目录下的资源, 一般是sdcard或内部存储
     */
    FILE,

    /**
     * 通过[android.content.res.AssetManager]读取的资源
     */
    ASSETS
}