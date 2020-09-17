package me.pitok.videoplayer.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.lifecycle.update
import me.pitok.mvi.MviModel
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.VideoPlayerState
import me.pitok.videoplayer.views.VideoPlayerActivity
import java.io.File
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSourceFactory: DefaultDataSourceFactory
) : ViewModel(), MviModel<VideoPlayerState,VideoPlayerIntent> {

    var path : String? = null
    lateinit var datasourcetype: String

    var resumePosition = 0L
    var resumeWindow = 0

    override val intents: Channel<VideoPlayerIntent> = Channel(Channel.UNLIMITED)
    private val pState = MutableLiveData<VideoPlayerState>().apply { value = VideoPlayerState() }
    override val state: LiveData<VideoPlayerState>
        get() = pState

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            intents.consumeAsFlow().collect {videoPlayerIntent ->
                when (videoPlayerIntent){
                    is VideoPlayerIntent.SetPlayBackState -> {
                        pState.update {
                            copy(playback_state = videoPlayerIntent.playbackState)
                        }
                    }
                }
            }
        }
    }

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