package com.choryan.quan.wxclean.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

/**
 * @author Secret
 * @since 2021/4/28
 */
object ExtensionCommon {

    val Float.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )

    val Float.sp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            Resources.getSystem().displayMetrics
        )

    fun Context.deviceWidth() = this.resources.displayMetrics.widthPixels

    fun Context.deviceHeight() = this.resources.displayMetrics.heightPixels

}