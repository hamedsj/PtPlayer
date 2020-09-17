package me.pitok.videometadata.requests

import android.content.ContentResolver

data class FolderVideosRequest(val folderPath: String,
                               val contentResolver: ContentResolver)