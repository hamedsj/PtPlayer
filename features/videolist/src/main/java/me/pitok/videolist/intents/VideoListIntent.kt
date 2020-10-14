package me.pitok.videolist.intents

import android.content.ContentResolver
import me.pitok.mvi.MviIntent

sealed class VideoListIntent(val contentResolver: ContentResolver? = null,
                             val path: String = ""): MviIntent {
    class FetchFolders(contentResolver: ContentResolver) : VideoListIntent(contentResolver)
    class FetchFolderVideos(contentResolver: ContentResolver,
                            folderPath: String) : VideoListIntent(contentResolver, folderPath)
    object ClearVideoList: VideoListIntent()
    class GoToDeepLink(val deeplink: String): VideoListIntent()
}