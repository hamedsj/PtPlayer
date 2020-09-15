package me.pitok.videolist.states

import me.pitok.mvi.MviState
import me.pitok.videolist.entities.FileEntity

data class VideoListState (
    val items : List<FileEntity> = listOf(),
    val sub_folder: Boolean = false,
    val title: String = "ALL"
): MviState