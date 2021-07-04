package me.pitok.videolist.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.design.views.EditTextBottomSheetView
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.logger.Logger
import me.pitok.mvi.MviView
import me.pitok.navigation.observeNavigation
import me.pitok.sdkextentions.getScreenWidth
import me.pitok.sdkextentions.isValidUrlWithProtocol
import me.pitok.sdkextentions.toPx
import me.pitok.videolist.R
import me.pitok.videolist.databinding.FragmentVideoListBinding
import me.pitok.videolist.databinding.MergeVideoListDrawerBinding
import me.pitok.videolist.di.builder.VideoListComponentBuilder
import me.pitok.videolist.entities.FileEntity
import me.pitok.videolist.intents.VideoListIntent
import me.pitok.videolist.states.VideoListState
import me.pitok.videolist.viewmodels.VideoListViewModel
import me.pitok.videoplayer.views.VideoPlayerActivity
import javax.inject.Inject

@SuppressLint("RtlHardcoded")
class VideoListFragment:
    Fragment(),
    MviView<VideoListState>,
    DrawerLayout.DrawerListener {

    companion object{
        const val REQUEST_STORAGE_PERMISSION_CODE = 1001
        const val ANIMATION_DURATION = 100L
        const val CHANGE_ACTIVITY_DELAY_DURATION = 150L
    }

    private lateinit var videoListEpoxyController: VideoListController

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @ApplicationContext
    @Inject
    lateinit var applicationContext: Context

    @Inject
    lateinit var coilImageLoader: ImageLoader

    private lateinit var binding: FragmentVideoListBinding
    private lateinit var videoListDrawerBinding: MergeVideoListDrawerBinding

    private val videoListViewModel: VideoListViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        VideoListComponentBuilder.getComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoListBinding.inflate(layoutInflater, container, false)
        videoListDrawerBinding = MergeVideoListDrawerBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoListViewModel.navigationObservable.observeNavigation(this)
        binding.videoListDrawerLayout.setScrimColor(Color.TRANSPARENT);
        binding.videoListDrawerLayout.addDrawerListener(this)
        binding.videoListDrawerIc.setOnClickListener(::onDrawerIcClickListener)
        videoListDrawerBinding.videoListDrawerNetwrokStreamClickable.setOnClickListener(::onNetworkStreamClick)
        videoListDrawerBinding.videoListDrawerSettingsClickable.setOnClickListener(::onSettingsClick)
        videoListDrawerBinding.videoListDrawerFeedbackClickable.setOnClickListener(::onFeedbackClick)
        videoListDrawerBinding.videoListDrawerAboutClickable.setOnClickListener(::onAboutClick)
        binding.videoListPermitBt.setOnClickListener { getStoragePermissions() }
        binding.videoListBackIc.setOnClickListener {
            lifecycleScope.launch {
                delay(ANIMATION_DURATION)
                withContext(Dispatchers.Main) { onBackPressed() }
            }
        }
        videoListEpoxyController = VideoListController(
            ::onFileClick,
            ContextCompat.getColor(applicationContext, R.color.color_primary_light),
            coilImageLoader,
            requireActivity().getScreenWidth()
        )
        binding.videoListRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoListEpoxyController.adapter
        }
        videoListViewModel.state.observe(this@VideoListFragment.viewLifecycleOwner, ::render)
        if (!isStoragePermissionGranted()) {
            binding.videoListPermissionText.visibility = View.VISIBLE
            binding.videoListPermitBt.visibility = View.VISIBLE
            binding.videoListRv.visibility = View.INVISIBLE
        } else {
            binding.videoListPermissionText.visibility = View.INVISIBLE
            binding.videoListPermitBt.visibility = View.INVISIBLE
            binding.videoListRv.visibility = View.VISIBLE
            lifecycleScope.launch {
                videoListViewModel.intents.send(
                    VideoListIntent.FetchFolders(
                        contentResolver = this@VideoListFragment.requireActivity().contentResolver
                    )
                )
            }
        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, true){onBackPressed()}

    }

    private fun onNetworkStreamClick(view: View){
        lifecycleScope.launch {
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main) {
                binding.videoListDrawerLayout.closeDrawer(Gravity.RIGHT)
            }
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main){
                EditTextBottomSheetView(requireActivity()).apply {
                    sheetTitle = getString(R.string.network_stream)
                    primaryText = getString(R.string.play)
                    secondaryText = getString(R.string.cancel)
                    editTextHint = getString(R.string.online_video_link)
                    onSecondaryClick = { _->
                        lifecycleScope.launch {
                            delay(ANIMATION_DURATION)
                            withContext(Dispatchers.Main){
                                cancel()
                            }
                        }
                    }
                    onPrimaryClick = onPrimaryClick@ { path ->
                        Logger.e("onPrimaryClick invoked")
                        if (!path.isValidUrlWithProtocol()){
                            Logger.e("onPrimaryClick isValidUrlWithProtocol not passed")
                            Toast.makeText(
                                applicationContext,
                                "Invalid Url Path",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@onPrimaryClick
                        }
                        Logger.e("onPrimaryClick isValidUrlWithProtocol passed")
                        lifecycleScope.launch {
                            delay(ANIMATION_DURATION)
                            withContext(Dispatchers.Main){
                                Intent(requireActivity(), VideoPlayerActivity::class.java).apply {
                                    putExtra(
                                        VideoPlayerActivity.DATA_SOURCE_TYPE_KEY,
                                        VideoPlayerActivity.ONLINE_PATH_DATA_TYPE
                                    )
                                    putExtra(VideoPlayerActivity.DATA_SOURCE_KEY, path)
                                    startActivity(this)
                                }
                                dismiss()
                            }
                        }
                    }
                    show()
                }
            }
        }
    }

    private fun onSettingsClick(view: View){
        lifecycleScope.launch {
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main) {
                binding.videoListDrawerLayout.closeDrawer(Gravity.RIGHT)
            }
            delay(ANIMATION_DURATION)
            videoListViewModel.intents.send(
                VideoListIntent.GoToDeepLink(getString(R.string.deeplink_settings))
            )
        }
    }

    private fun onAboutClick(view: View){
        lifecycleScope.launch {
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main) {
                binding.videoListDrawerLayout.closeDrawer(Gravity.RIGHT)
            }
            delay(ANIMATION_DURATION)
            videoListViewModel.intents.send(
                VideoListIntent.GoToDeepLink(getString(R.string.deeplink_about))
            )
        }
    }

    private fun onFeedbackClick(view: View){
        lifecycleScope.launch {
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main) {
                binding.videoListDrawerLayout.closeDrawer(Gravity.RIGHT)
            }
            delay(ANIMATION_DURATION)
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("hamedsj5@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Suggestion for improving user exprience of PT-Player");
                    startActivity(Intent.createChooser(this, "Send Email"))
                }

        }
    }

    private fun onDrawerIcClickListener(view: View){
        lifecycleScope.launch{
            delay(ANIMATION_DURATION)
            withContext(Dispatchers.Main){
                binding.videoListDrawerLayout.openDrawer(Gravity.RIGHT)
            }
        }
    }

    private fun onBackPressed(){
        if (videoListViewModel.depth != VideoListViewModel.SUB_FOLDER_DEPTH){
            requireActivity().finish()
        }else{
            lifecycleScope.launch {
                videoListViewModel
                    .intents
                    .send(
                        VideoListIntent.FetchFolders(
                            contentResolver = this@VideoListFragment.requireActivity().contentResolver
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
                    delay(ANIMATION_DURATION + CHANGE_ACTIVITY_DELAY_DURATION)
                    withContext(Dispatchers.Main) {
                        Intent(requireActivity(), VideoPlayerActivity::class.java).apply {
                            putExtra(
                                VideoPlayerActivity.DATA_SOURCE_TYPE_KEY,
                                VideoPlayerActivity.LOCAL_PATH_DATA_TYPE
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
        if (state.sub_folder) {
            binding.videoListBackIc.visibility = View.VISIBLE
            val lp = binding.videoListTitle.layoutParams as ConstraintLayout.LayoutParams
            lp.leftMargin = 8f.toPx()
            binding.videoListTitle.layoutParams = lp
        } else {
            binding.videoListBackIc.visibility = View.GONE
            val lp = binding.videoListTitle.layoutParams as ConstraintLayout.LayoutParams
            lp.leftMargin = 24f.toPx()
            binding.videoListTitle.layoutParams = lp
        }
        (binding.videoListRv.layoutManager as GridLayoutManager).spanCount =
            if (state.sub_folder) 2 else 3
        binding.videoListTitle.text = state.title
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            true
        else
            (applicationContext
                .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) and
                    (applicationContext
                        .checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED)
    }

    private fun getStoragePermissions() {
        if (isStoragePermissionGranted()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_STORAGE_PERMISSION_CODE
        )
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        binding.videoListContent?.apply {
            translationX = -1 * drawerView.width * slideOffset
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            binding.videoListPermissionText.visibility = View.INVISIBLE
            binding.videoListPermitBt.visibility = View.INVISIBLE
            binding.videoListRv.visibility = View.VISIBLE
            lifecycleScope.launch {
                videoListViewModel.intents.send(
                    VideoListIntent.FetchFolders(
                        contentResolver = this@VideoListFragment.requireActivity().contentResolver
                    )
                )
            }
        }
        return
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerStateChanged(newState: Int) {}

}