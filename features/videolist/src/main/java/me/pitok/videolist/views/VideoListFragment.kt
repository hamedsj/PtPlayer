package me.pitok.videolist.views

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import kotlinx.android.synthetic.main.fragment_video_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.mvi.MviView
import me.pitok.sdkextentions.getScreenWidth
import me.pitok.videolist.R
import me.pitok.videolist.di.builder.VideoListComponentBuilder
import me.pitok.videolist.entities.FileEntity
import me.pitok.videolist.intents.VideoListIntent
import me.pitok.videolist.states.VideoListState
import me.pitok.videolist.viewmodels.VideoListViewModel
import me.pitok.videoplayer.views.VideoPlayerActivity
import javax.inject.Inject

class VideoListFragment: Fragment(R.layout.fragment_video_list), MviView<VideoListState> {

    companion object{
        const val ANIMATION_DURATION = 100L
    }

    private lateinit var videoListEpoxyController: VideoListController

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @ApplicationContext
    @Inject
    lateinit var applicationContext: Context

    @Inject
    lateinit var coilImageLoader: ImageLoader

    private val videoListViewModel: VideoListViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        VideoListComponentBuilder.getComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoListEpoxyController = VideoListController(
            ::onFileClick,
            ContextCompat.getColor(applicationContext,R.color.color_primary_light),
            coilImageLoader,
            requireActivity().getScreenWidth()
        )
        videoListRv.apply {
            layoutManager = GridLayoutManager(requireContext(),3)
            adapter = videoListEpoxyController.adapter
        }
        videoListViewModel.state.observe(this@VideoListFragment.viewLifecycleOwner, ::render)
        lifecycleScope.launch {
            videoListViewModel.intents.send(
                VideoListIntent.FetchFolders(
                    contentResolver = this@VideoListFragment.requireActivity().contentResolver
                )
            )
        }
        getStoragePermissions()
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner,true){onBackPressed()}

    }

    private fun onBackPressed(){
        if (videoListViewModel.depth != VideoListViewModel.SUB_FOLDER_DEPTH){
            requireActivity().finish()
        }else{
            lifecycleScope.launch {
                videoListViewModel
                    .intents
                    .send(
                        VideoListIntent.FetchFolders (
                            contentResolver = this@VideoListFragment.
                            requireActivity().contentResolver
                        )
                    )
            }
        }
    }

    private fun onFileClick(path: String, type: Int){
        when(type) {
            FileEntity.FOLDER_TYPE -> {
                lifecycleScope.launch {
                    delay(ANIMATION_DURATION)
                    videoListViewModel.intents.send(
                        VideoListIntent.FetchFolderVideos(
                            folderPath = path,
                            contentResolver = this@VideoListFragment
                                .requireActivity()
                                .contentResolver
                        )
                    )
                }
            }
            FileEntity.FILE_TYPE -> {
                lifecycleScope.launch {
                    delay(ANIMATION_DURATION)
                    withContext(Dispatchers.Main){
                        Intent(requireActivity(), VideoPlayerActivity::class.java).apply {
                            putExtra(
                                VideoPlayerActivity.DATA_SOURCE_TYPE_KEY,
                                VideoPlayerActivity.PATH_DATA_TYPE
                            )
                            putExtra(VideoPlayerActivity.DATA_SOURCE_KEY, path)
                            startActivity(this)
                        }
                    }
                }
            }
        }
    }

    override fun render(state: VideoListState) {
        videoListEpoxyController.items = state.items
        videoListEpoxyController.requestModelBuild()
        (videoListRv.layoutManager as GridLayoutManager).spanCount = if (state.sub_folder) 2 else 3
        videoListTitle.text = state.title
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            true
        else
            applicationContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
    }

    private fun getStoragePermissions() {
        if (checkStoragePermissions()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        requireActivity().requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 1
        )
    }

}