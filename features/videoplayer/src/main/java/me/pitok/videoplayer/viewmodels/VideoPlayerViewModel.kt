package me.pitok.videoplayer.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.videoplayer.views.VideoPlayerActivity
import java.io.File
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSourceFactory: DefaultDataSourceFactory
) : ViewModel() {

    var path : String? = null
    lateinit var datasourcetype: String

    var resumePosition = 0L
    var resumeWindow = 0


    fun buildVideoSource(): MediaSource {
        return when(datasourcetype){
            VideoPlayerActivity.PATH_DATA_TYPE -> {
                buildFromPath(requireNotNull(path))
            }
            else -> {
                buildFromPath(requireNotNull(path))
            }
        }
    }

    private fun buildFromPath(path: String): MediaSource {
        return ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(Uri.fromFile(File(path)))
    }

}