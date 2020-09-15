package me.pitok.sdkextentions

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics

fun Float.toPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun Int.toDp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Activity.getScreenWidth(): Int{
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun Activity.getScreenHeight(): Int{
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}