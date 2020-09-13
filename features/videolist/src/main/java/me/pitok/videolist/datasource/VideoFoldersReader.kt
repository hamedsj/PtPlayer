package me.pitok.videolist.datasource

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import me.pitok.datasource.Failure
import me.pitok.datasource.Readable
import me.pitok.datasource.Response
import me.pitok.datasource.Success
import javax.inject.Inject

class VideoFoldersReader @Inject constructor(): VideoFoldersReadType{
    @SuppressLint("Recycle")
    override suspend fun read(input: ContentResolver): Response<Map<String,Int>,Throwable> {
        val pathes = mutableMapOf<String, Int>()
        val columns = arrayOf(MediaStore.Video.Media.DATA,)
        val  cursor = input.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            columns,
            null,
            null,
            null
        ) ?: return Failure(Throwable())
        if (!cursor.moveToFirst()) Failure(Throwable())
        do {
            val videoFilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            val videoFilePathSplited = videoFilePath.split("/") as MutableList
            videoFilePathSplited.removeAt(videoFilePathSplited.size-1)
            val videoFolderPath = videoFilePathSplited.joinToString("/")
            if (pathes.containsKey(videoFolderPath)){
                pathes[videoFolderPath] = requireNotNull(pathes[videoFolderPath]).plus(1)
            }else{
                pathes[videoFolderPath] = 1
            }
        }while (cursor.moveToNext())
        return Success(pathes)
    }
}

typealias VideoFoldersReadType = Readable.Suspendable.IO<ContentResolver,
        @JvmSuppressWildcards Response<@JvmSuppressWildcards Map<String,Int>,Throwable>>