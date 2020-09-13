package me.pitok.videolist.states

import me.pitok.mvi.MviState
import me.pitok.videolist.entities.FileEntity

data class VideoListState (
    val items : List<FileEntity> = listOf(),
): MviState