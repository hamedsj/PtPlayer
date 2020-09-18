package me.pitok.videoplayer.viewmodels

import android.content.ContentResolver
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.datasource.ifSuccessful
import me.pitok.lifecycle.update
import me.pitok.logger.Logger
import me.pitok.mvi.MviModel
import me.pitok.videometadata.datasource.FolderVideosReadType
import me.pitok.videometadata.requests.FolderVideosRequest
import me.pitok.videoplayer.intents.PlayerControllerCommmand
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.PLayerCommand
import me.pitok.videoplayer.states.VideoPlayerState
import me.pitok.videoplayer.views.VideoPlayerActivity
import java.io.File
import javax.inject.Inject

class VideoPlayerViewModel @Inject constructor(
    private val dataSourceFactory: DefaultDataSourceFactory,
    private val folderVideosReader: FolderVideosReadType
) : ViewModel(), MviModel<VideoPlayerState,VideoPlayerIntent> {

    var activePath : String? = null
    lateinit var datasourcetype: String
    private var videoList = mutableListOf<String>()

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
                    is VideoPlayerIntent.SendCommand -> {
                        when(videoPlayerIntent.command){
                            is PlayerControllerCommmand.Next -> {
                                startNextVideo()
                            }
                            is PlayerControllerCommmand.Previous -> {
                                startPreviousVideo()
                            }
                            is PlayerControllerCommmand.Play -> {
                                pState.update {
                                    copy(command = PLayerCommand.Start)
                                }
                            }
                            is PlayerControllerCommmand.Pause -> {
                                pState.update {
                                    copy(command = PLayerCommand.Pause)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun buildVideoSource(): MediaSource {
        return when(datasourcetype){
            VideoPlayerActivity.PATH_DATA_TYPE -> {
                buildFromPath(requireNotNull(activePath))
            }
            else -> {
                buildFromPath(requireNotNull(activePath))
            }
        }
    }

    private fun buildFromPath(path: String): MediaSource {
        return ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(Uri.fromFile(File(path)))
    }

    fun getFolderVideos(contentResolver: ContentResolver){
        if(activePath == null) return
        val pathSplited = activePath?.split("/") as MutableList
        pathSplited.removeAt(pathSplited.size -1)
        val folderPath = pathSplited.joinToString("/")
        viewModelScope.launch(Dispatchers.IO) {
            folderVideosReader.read(FolderVideosRequest(folderPath, contentResolver))
                .ifSuccessful { videos ->
                    videoList.clear()
                    videoList.addAll(videos)
                }
        }
    }

    private fun startNextVideo(){
        if (videoList.isEmpty()) return
        val position = videoList.indexOf(activePath)
        val nextPosition = if (position == -1 || position == videoList.size - 1) 0 else (position + 1)
        activePath = videoList[nextPosition]
        pState.update {
            copy(command = PLayerCommand.Prepare(buildFromPath(requireNotNull(activePath))))
        }
        pState.update { copy(command = PLayerCommand.SeekToPosition(0)) }
        pState.update { copy(command = PLayerCommand.Start) }
    }

    private fun startPreviousVideo(){
        if (videoList.isEmpty()) return
        val position = videoList.indexOf(activePath)
        val nextPosition = if (position == -1 || position == 0) videoList.size - 1 else (position - 1)
        activePath = videoList[nextPosition]
        pState.update {
            copy(command = PLayerCommand.Prepare(buildFromPath(requireNotNull(activePath))))
        }
        pState.update { copy(command = PLayerCommand.SeekToPosition(0)) }
        pState.update { copy(command = PLayerCommand.Start) }
    }

}