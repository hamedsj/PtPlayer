package me.pitok.videolist.intents

import android.content.ContentResolver
import me.pitok.mvi.MviIntent

sealed class VideoListIntent(val contentResolver: ContentResolver? = null,
                             val path: String = ""): MviIntent {
    class FetchFolders(contentResolver: ContentResolver) : VideoListIntent(contentResolver)
    class FetchFolderVideos(folderPath: String) : VideoListIntent(path = folderPath)
}