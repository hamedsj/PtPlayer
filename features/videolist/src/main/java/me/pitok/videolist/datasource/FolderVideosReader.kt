package me.pitok.videolist.datasource

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import me.pitok.datasource.Failure
import me.pitok.datasource.Readable
import me.pitok.datasource.Response
import me.pitok.datasource.Success
import me.pitok.videolist.requests.FolderVideosRequest
import javax.inject.Inject

class FolderVideosReader @Inject constructor(): FolderVideosReadType{
    @SuppressLint("Recycle")
    override suspend fun read(input: FolderVideosRequest): Response<List<String>,Throwable> {
        val pathes = mutableListOf<String>()
        val columns = arrayOf(MediaStore.Video.Media.DATA,)
        val selection = "${MediaStore.Video.Media.DATA} like?"
        val selectionArgs = arrayOf("%${input.folderPath}%")
        val  cursor = input.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            columns,
            selection,
            selectionArgs,
            null
        ) ?: return Failure(Throwable())
        if (!cursor.moveToFirst()) Failure(Throwable())
        do {
            val videoFilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            pathes.add(videoFilePath)
        }while (cursor.moveToNext())
        return Success(pathes)
    }
}

typealias FolderVideosReadType = Readable.Suspendable.IO<FolderVideosRequest,
        @JvmSuppressWildcards Response<@JvmSuppressWildcards List<String>,Throwable>>