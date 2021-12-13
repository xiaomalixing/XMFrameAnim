package com.xiaoma.frameanim.util

import android.app.Application

object AppUtil {
    /**
     * 获取当前应用的Application对象
     */
    val application: Application by lazy {
        val clz = Class.forName("android.app.ActivityThread")
        // 拿到静态方法currentActivityThread
        val curActThread = clz.getDeclaredMethod("currentActivityThread")
        curActThread.isAccessible = true
        // 获取当前ActivityThread对象
        val actThread = curActThread.invoke(clz)
        // 拿到成员方法getApplication
        val getApp = clz.getDeclaredMethod("getApplication")
        getApp.isAccessible = true
        getApp.invoke(actThread) as Application
    }
}