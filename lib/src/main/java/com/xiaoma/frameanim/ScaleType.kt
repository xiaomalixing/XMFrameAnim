package com.xiaoma.frameanim

/**
 * 缩放模式,与[android.widget.ImageView]的缩放模式相同
 */
enum class ScaleType(val nativeInt: Int) {
    /**
     * 参考[android.widget.ImageView.ScaleType.MATRIX]
     */
    MATRIX(0),

    /**
     * 参考[android.widget.ImageView.ScaleType.FIT_XY]
     */
    FIT_XY(1),

    /**
     * 参考[android.widget.ImageView.ScaleType.FIT_START]
     */
    FIT_START(2),

    /**
     * 参考[android.widget.ImageView.ScaleType.FIT_CENTER]
     */
    FIT_CENTER(3),

    /**
     * 参考[android.widget.ImageView.ScaleType.FIT_END]
     */
    FIT_END(4),

    /**
     * 参考[android.widget.ImageView.ScaleType.CENTER]
     */
    CENTER(5),

    /**
     * 参考[android.widget.ImageView.ScaleType.CENTER_CROP]
     */
    CENTER_CROP(6),

    /**
     * 参考[android.widget.ImageView.ScaleType.CENTER_INSIDE]
     */
    CENTER_INSIDE(7);
}