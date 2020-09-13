package me.pitok.videolist.viewmodels

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.datasource.ifSuccessful
import me.pitok.lifecycle.update
import me.pitok.mvi.MviModel
import me.pitok.videolist.datasource.VideoFoldersReadType
import me.pitok.videolist.entities.FileEntity
import me.pitok.videolist.entities.FileEntity.Companion.FOLDER_TYPE
import me.pitok.videolist.intents.VideoListIntent
import me.pitok.videolist.states.VideoListState
import javax.inject.Inject

class VideoListViewModel @Inject constructor(
    private val videoFoldersReader: VideoFoldersReadType
): ViewModel(), MviModel<VideoListState,VideoListIntent> {

    override val intents: Channel<VideoListIntent> = Channel(Channel.UNLIMITED)
    private val pState = MutableLiveData<VideoListState>().apply { value = VideoListState() }
    override val state: LiveData<VideoListState>
        get() = pState

    init {
        handleIntent()
    }

    private fun handleIntent(){
        viewModelScope.launch {
            intents.consumeAsFlow().collect {videoListIntent ->
                when(videoListIntent){
                    is VideoListIntent.FetchFolders -> {
                        getFolders(
                            requireNotNull(videoListIntent.contentResolver)
                        )
                    }
                }
            }
        }
    }

    private fun getFolders(contentResolver: ContentResolver){
        viewModelScope.launch(Dispatchers.IO) {
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
                        copy(items = res)
                    }
                }
            }
        }
    }
}