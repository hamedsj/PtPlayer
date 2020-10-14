package me.pitok.videolist.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import me.pitok.datasource.ifSuccessful
import me.pitok.lifecycle.SingleLiveData
import me.pitok.lifecycle.update
import me.pitok.mvi.MviModel
import me.pitok.navigation.Navigate
import me.pitok.videolist.entities.FileEntity
import me.pitok.videolist.entities.FileEntity.Companion.FILE_TYPE
import me.pitok.videolist.entities.FileEntity.Companion.FOLDER_TYPE
import me.pitok.videolist.intents.VideoListIntent
import me.pitok.videolist.states.VideoListState
import me.pitok.videometadata.datasource.FolderVideosReadType
import me.pitok.videometadata.datasource.VideoFoldersReadType
import me.pitok.videometadata.requests.FolderVideosRequest
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class VideoListViewModel @Inject constructor(
    private val videoFoldersReader: VideoFoldersReadType,
    private val folderVideosReader: FolderVideosReadType,
): ViewModel(), MviModel<VideoListState,VideoListIntent> {

    companion object{
        const val ALL_FOLDER_DEPTH = 0
        const val SUB_FOLDER_DEPTH = 1
    }

    override val intents: Channel<VideoListIntent> = Channel(Channel.UNLIMITED)
    private val pState = MutableLiveData<VideoListState>().apply { value = VideoListState() }
    override val state: LiveData<VideoListState>
        get() = pState

    private val pNavigationObservable = SingleLiveData<Navigate>()
    val navigationObservable : LiveData<Navigate> = pNavigationObservable

    var depth = ALL_FOLDER_DEPTH

    private var job1 : CoroutineContext? = null
    private var job2 : CoroutineContext? = null

    init {
        handleIntent()
    }

    private fun handleIntent(){
        GlobalScope.launch(NonCancellable) {
            intents.consumeAsFlow().collect {videoListIntent ->
                when(videoListIntent){
                    is VideoListIntent.FetchFolders -> {
                        getFolders(
                            requireNotNull(videoListIntent.contentResolver)
                        )
                        depth = ALL_FOLDER_DEPTH
                    }
                    is VideoListIntent.FetchFolderVideos -> {
                        getFolderVideos(
                            videoListIntent.path,
                            requireNotNull(videoListIntent.contentResolver)
                        )
                        depth = SUB_FOLDER_DEPTH
                    }
                    is VideoListIntent.ClearVideoList -> {
                        clearVideoList()
                        depth = ALL_FOLDER_DEPTH
                    }
                    is VideoListIntent.GoToDeepLink -> {
                        withContext(Dispatchers.Main) {
                            pNavigationObservable.value =
                                Navigate.ToDeepLink(deepLink = videoListIntent.deeplink)
                        }
                    }
                }
            }
        }
    }

    private fun getFolders(contentResolver: ContentResolver){
        job1 = GlobalScope.launch(Dispatchers.IO) {
            videoFoldersReader.read(contentResolver).ifSuccessful {videos ->
                withContext(Dispatchers.Main) {
                    pState.update {
                        val res = mutableListOf<FileEntity>()
                        videos.forEach { video ->
                            val splitedPath = video.key.split("/")
                            val folderName = splitedPath[splitedPath.size - 1]
                            res.add(
                                FileEntity(
                                    path = video.key,
                                    type = FOLDER_TYPE,
                                    name = folderName,
                                    details = if (video.value == 1)
                                        "${video.value} video"
                                    else
                                        "${video.value} videos"
                                )
                            )
                        }
                        copy(items = res, sub_folder = false, title = "ALL")
                    }
                }
            }
        }
    }

    private fun getFolderVideos(path: String, contentResolver: ContentResolver){
        job2 = GlobalScope.launch(Dispatchers.IO) {
            folderVideosReader.read(FolderVideosRequest(path, contentResolver))
                .ifSuccessful { videos ->
                    val pathSplited = path.split("/")
                    val folderName = pathSplited[pathSplited.size-1]
                    withContext(Dispatchers.Main){
                        pState.update {
                            val res = mutableListOf<FileEntity>()
                            videos.forEach{video ->
                                val videoPathSplited = video.split("/")
                                val videoName = videoPathSplited[videoPathSplited.size-1]
                                res.add(
                                    FileEntity(
                                        video,
                                        FILE_TYPE,
                                        videoName
                                    )
                                )
                            }
                            copy(items = res,
                                sub_folder = true,
                                title = folderName
                            )
                        }
                    }
                }
        }
    }

    private fun clearVideoList(){
        pState.update {
            copy(items = listOf(), sub_folder = false)
        }
    }

    /**
     *  cause viewmodelScope not working with injected viewModels
     *  we should use GlobalScope and then cancel them in [onCleared()]
     *
     */
    override fun onCleared() {
        job1?.cancel()
        job2?.cancel()
        super.onCleared()
    }
}