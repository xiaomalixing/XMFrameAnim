package com.xiaoma.frameanim.util

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

val Context.coroutineScope: CoroutineScope
    get() = if (this is LifecycleOwner) {
        lifecycleScope
    } else {
        GlobalScope
    }