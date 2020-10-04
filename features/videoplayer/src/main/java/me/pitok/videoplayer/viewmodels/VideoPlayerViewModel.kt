package me.pitok.videoplayer.viewmodels

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import me.pitok.datasource.ifSuccessful
import me.pitok.datasource.otherwise
import me.pitok.lifecycle.update
import me.pitok.logger.Logger
import me.pitok.mvi.MviModel
import me.pitok.sdkextentions.isValidUrlWithProtocol
import me.pitok.subtitle.datasource.SubtitleReaderType
import me.pitok.subtitle.datasource.SubtitleRequest
import me.pitok.subtitle.entity.SubtitleEntity
import me.pitok.subtitle.error.SubtitleError
import me.pitok.videometadata.datasource.FolderVideosReadType
import me.pitok.videometadata.requests.FolderVideosRequest
import me.pitok.videoplayer.intents.PlayerControllerCommmand
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.OptionsState
import me.pitok.videoplayer.states.PLayerCommandState
import me.pitok.videoplayer.states.SubtitleState
import me.pitok.videoplayer.states.VideoPlayerState
import me.pitok.videoplayer.views.VideoPlayerActivity
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class VideoPlayerViewModel @Inject constructor(
    private val dataSourceFactory: DefaultDataSourceFactory,
    private val httpDataSourceFactory: DefaultHttpDataSourceFactory,
    private val folderVideosReader: FolderVideosReadType,
    private val subtitleReader: SubtitleReaderType
) : ViewModel(), MviModel<VideoPlayerState, VideoPlayerIntent> {

    var activePath : String? = null
    lateinit var datasourcetype: String
    private var videoList = mutableListOf<String>()
    private val availibleSubtitleList = mutableListOf<SubtitleEntity>()
    private var activeSubtitlePath = ""

    var resumePosition = 0L
    var resumeWindow = 0

    override val intents: Channel<VideoPlayerIntent> = Channel(Channel.UNLIMITED)
    private val pState = MutableLiveData<VideoPlayerState>().apply { value = VideoPlayerState() }
    override val state: LiveData<VideoPlayerState>
        get() = pState

    private var job1 : CoroutineContext? = null

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch(NonCancellable) {
            intents.consumeAsFlow().collect { videoPlayerIntent ->
                when (videoPlayerIntent){
                    is VideoPlayerIntent.SetPlayBackState -> {
                        pState.update {videoPlayerIntent.playbackState}
                    }
                    is VideoPlayerIntent.SendCommand -> {
                        when (videoPlayerIntent.command) {
                            is PlayerControllerCommmand.Next -> {
                                startNextVideo()
                            }
                            is PlayerControllerCommmand.Previous -> {
                                startPreviousVideo()
                            }
                            is PlayerControllerCommmand.Play -> {
                                pState.update {PLayerCommandState.Start}
                            }
                            is PlayerControllerCommmand.Pause -> {
                                pState.update {PLayerCommandState.Pause}
                            }
                            is PlayerControllerCommmand.Prepare -> {
                                pState.update {
                                    PLayerCommandState.Prepare(buildVideoSource())
                                }
                            }
                            is PlayerControllerCommmand.ChangePlaybackSpeed -> {
                                pState.update {
                                    PLayerCommandState.ChangeSpeed(
                                        videoPlayerIntent.command.spped
                                    )
                                }
                            }
                        }
                    }
                    is VideoPlayerIntent.ShowOptions -> {
                        when(videoPlayerIntent.OptionsMenu){
                            VideoPlayerActivity.OPTIONS_MAIN_MENU ->{
                                pState.update {OptionsState.ShowMainMenu}
                            }
                            VideoPlayerActivity.OPTIONS_SUBTITLE_MENU ->{
                                pState.update {OptionsState.ShowSubtitleMenu}
                            }
                            VideoPlayerActivity.OPTIONS_SPEED_MENU ->{
                                pState.update {OptionsState.ShowPlaybackSpeedMenu}
                            }
                            VideoPlayerActivity.OPTIONS_AUDIO_MENU ->{
                                pState.update {OptionsState.ShowAudioMenu}
                            }
                        }
                    }
                    is VideoPlayerIntent.SubtitleProgressChanged -> {
                        getSubtitleContent(videoPlayerIntent.progress)?.apply {
                                withContext(Dispatchers.Main) {
                                    pState.update { SubtitleState.Show(content) }
                                }
                            } ?: run {
                                withContext(Dispatchers.Main) {
                                    pState.update { SubtitleState.Clear }
                                }
                            }
                    }
                    is VideoPlayerIntent.LoadSubtitle -> {
                        activeSubtitlePath = videoPlayerIntent.path
                        loadSubtitle()
                    }
                    is VideoPlayerIntent.RemoveSubtitle -> {
                        activeSubtitlePath = ""
                        availibleSubtitleList.clear()
                    }
                }
            }
        }
    }

    private fun buildVideoSource(): MediaSource? {
        return when(datasourcetype){
            VideoPlayerActivity.LOCAL_PATH_DATA_TYPE -> {
                buildFromPath(requireNotNull(activePath))
            }
            VideoPlayerActivity.ONLINE_PATH_DATA_TYPE -> {
                buildFromUrl(requireNotNull(activePath))
            }
            else -> {
                buildFromPath(requireNotNull(activePath))
            }
        }
    }

    private fun buildFromUrl(url: String): MediaSource? {
        Logger.e("buildFromUrl called")
        if (!url.isValidUrlWithProtocol()) return null
        val validUrl = if (!url.startsWith("http://") &&
            !url.startsWith("https://")){
            "http://$url"
        }else{
            url
        }
        Logger.e("url : $validUrl")
        return ProgressiveMediaSource
            .Factory(httpDataSourceFactory)
            .createMediaSource(Uri.parse(validUrl))
    }

    private fun buildFromPath(path: String): MediaSource {
        return ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(Uri.fromFile(File(path)))
    }

    fun getFolderVideos(contentResolver: ContentResolver){
        if(activePath == null) return
        val pathSplited = activePath?.split("/") as MutableList
        pathSplited.removeAt(pathSplited.size - 1)
        val folderPath = pathSplited.joinToString("/")
        Logger.v("folderPath : $folderPath")
        job1 = GlobalScope.launch(Dispatchers.IO) {
            folderVideosReader.read(FolderVideosRequest(folderPath, contentResolver))
                .ifSuccessful { videos ->
                    videoList.clear()
                    videoList.addAll(videos)
                }
        }
    }

    fun getRealPathFromURI(contentResolver: ContentResolver, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri!!, proj, null, null, null)
            val columnIndex: Int = requireNotNull(cursor?.getColumnIndex(MediaStore.Images.Media.DATA))
            cursor?.moveToFirst()
            cursor?.getString(columnIndex)
        } finally {
            cursor?.close()
        }
    }

    private fun startNextVideo(){
        if (videoList.isEmpty()) return
        val position = videoList.indexOf(activePath)
        val nextPosition = if (position == -1 || position == videoList.size - 1) 0 else (position + 1)
        activePath = videoList[nextPosition]
        pState.update {PLayerCommandState.Prepare(buildFromPath(requireNotNull(activePath))) }
        pState.update {PLayerCommandState.SeekToPosition(0)}
        pState.update {PLayerCommandState.Start}
    }

    private fun startPreviousVideo(){
        if (videoList.isEmpty()) return
        val position = videoList.indexOf(activePath)
        val nextPosition = if (position == -1 || position == 0) videoList.size - 1 else (position - 1)
        activePath = videoList[nextPosition]
        pState.update {PLayerCommandState.Prepare(buildFromPath(requireNotNull(activePath)))}
        pState.update {PLayerCommandState.SeekToPosition(0)}
        pState.update {PLayerCommandState.Start}
    }

    private suspend fun getSubtitleContent(currentMiliSec: Long) : SubtitleEntity? {
        if (availibleSubtitleList.isEmpty()) subtitleReader.read(SubtitleRequest(activeSubtitlePath))
        availibleSubtitleList.forEach { subtitleEntity ->
            if (subtitleEntity.fromMs <= currentMiliSec && subtitleEntity.toMs > currentMiliSec){
                return subtitleEntity
            }
        }
        return null
    }

    private suspend fun loadSubtitle(){
        if (activeSubtitlePath == "") return
        availibleSubtitleList.clear()
        subtitleReader.read(SubtitleRequest(activeSubtitlePath)).ifSuccessful { subtitle ->
            availibleSubtitleList.addAll(subtitle)
        }.otherwise { error ->
            Logger.e(error.message)
            when(error){
                is SubtitleError.SubtitleFileNotFound -> {
                    pState.update { SubtitleState.SubtitleNotFoundError }
                }
                is SubtitleError.ReadingSubtitleFileError -> {
                    pState.update { SubtitleState.SubtitleReadingError }
                }
            }
        }
    }

    fun isSubtitleReady() : Boolean{
        return availibleSubtitleList.isNotEmpty()
    }

    /**
     *  cause viewmodelScope not working with injected viewModels
     *  we should use GlobalScope and then cancel them in [onCleared()]
     *
     */
    override fun onCleared() {
        job1?.cancel()
        super.onCleared()
    }

}