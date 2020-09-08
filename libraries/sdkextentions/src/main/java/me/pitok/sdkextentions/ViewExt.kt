package me.pitok.sdkextentions

import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat

fun View.setElevationByDp(elevationByDp: Float){
    val elevationByPx = (elevationByDp * Resources.getSystem().displayMetrics.density).toFloat()
    ViewCompat.setElevation(this,elevationByPx)
}