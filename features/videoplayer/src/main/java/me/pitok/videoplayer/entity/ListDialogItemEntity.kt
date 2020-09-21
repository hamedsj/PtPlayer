package me.pitok.videoplayer.entity

import androidx.annotation.DrawableRes

data class ListDialogItemEntity(
    val itemUnique: String?,
    @DrawableRes val itemIconResource: Int?,
    val itemTitleResource: String,
    val itemOnClickListener: (String?) -> Unit
)