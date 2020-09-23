package me.pitok.videoplayer.views

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.view_video_player_controller.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.design.entity.BottomSheetItemEntity
import me.pitok.design.views.BottomSheetView
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.logger.Logger
import me.pitok.mvi.MviView
import me.pitok.player.di.IndictableSimpleExoPlayer
import me.pitok.sdkextentions.getScreenWidth
import me.pitok.videoplayer.R
import me.pitok.videoplayer.di.builder.VideoPlayerComponentBuilder
import me.pitok.videoplayer.intents.PlayerControllerCommmand
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.*
import me.pitok.videoplayer.viewmodels.VideoPlayerViewModel
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity(), MviView<VideoPlayerState>, Player.EventListener {

    companion object{
        const val DATA_SOURCE_KEY = "datasource"
        const val DATA_SOURCE_TYPE_KEY = "datasourcetype"
        const val PATH_DATA_TYPE = "path"
        const val FADE_IN_ANIM_DURATION = 150L
        const val FADE_OUT_ANIM_DURATION = 250L
        const val CHANGE_POSITION_ANIMATION_DURATION = 250L
        const val CLICK_ANIMATION_DURATION = 100L
        const val OPTIONS_MAIN_MENU = 0
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var exoPlayer: IndictableSimpleExoPlayer

    @ApplicationContext
    @Inject
    lateinit var context: Context


    private var sliderInTouch = false

    private val videoPlayerViewModel: VideoPlayerViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        VideoPlayerComponentBuilder.getComponent().inject(this)
        getInitialData()
        setInitialViews()
        val screenWidth = getScreenWidth()
        videoPlayerViewModel.state.observe(this@VideoPlayerActivity, ::render)
        videoPlayerControllerHighlight.setOnTouchListener(object: View.OnTouchListener{
            private val gestureDetector = GestureDetector(context,object:
                GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (requireNotNull(e?.x) >=  screenWidth/2f){
                        onFastForwardTapped()
                    }else{
                        onRewindTapped()
                    }
                    return super.onDoubleTap(e)
                }
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    onControllerToggle()
                    return super.onSingleTapConfirmed(e)
                }
            })
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                gestureDetector.onTouchEvent(p1)
                return true
            }
        })
        videoPlayerControllerPlayIc.setOnClickListener(::onPlayIcClick)
        videoPlayerControllerNextIc.setOnClickListener(::onNextIcClick)
        videoPlayerControllerBackIc.setOnClickListener(::onBackIcClick)
        videoPlayerControllerOptionsIc.setOnClickListener(::onOptionsIcClick)
        videoPlayerPv.player = exoPlayer
        exoPlayer.seekTo(0L)
        exoPlayer.prepare(videoPlayerViewModel.buildVideoSource())
        exoPlayer.addListener(this)
        exoPlayer.onPositionChanged = {position ->
            onProgressChanged(position.toFloat())
        }
        videoPlayerControllerSeekbar.addOnSliderTouchListener(object: Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: Slider) {
                sliderInTouch = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                sliderInTouch = false
                exoPlayer.seekTo(slider.value.toLong())
            }

        })
        videoPlayerControllerSeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (!fromUser) return@OnChangeListener
            videoPlayerControllerTimeLeft.text = miliSecToFormatedTime(value.toLong())
        })
    }

    private fun onFastForwardTapped(){
        if (exoPlayer.contentPosition >= (exoPlayer.duration-10000))
            exoPlayer.seekTo(exoPlayer.duration - 500)
        else
            exoPlayer.seekTo(exoPlayer.currentPosition + 10000)

        val fadeInObjectAnimator = ObjectAnimator.ofFloat(
            videoPlayerFastForwardHighlight,
            "alpha",
            videoPlayerFastForwardHighlight.alpha,
            1f
        )
        fadeInObjectAnimator.interpolator = AccelerateInterpolator()
        fadeInObjectAnimator.duration = FADE_IN_ANIM_DURATION
        fadeInObjectAnimator.doOnEnd {
            lifecycleScope.launch {
                delay(CHANGE_POSITION_ANIMATION_DURATION)
                withContext(Dispatchers.Main){
                    val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                        videoPlayerFastForwardHighlight,
                        "alpha",
                        videoPlayerFastForwardHighlight.alpha,
                        0f
                    )
                    fadeOutObjectAnimator.interpolator = AccelerateInterpolator()
                    fadeOutObjectAnimator.duration = FADE_OUT_ANIM_DURATION
                    fadeOutObjectAnimator.start()
                }
            }
        }
        fadeInObjectAnimator.start()
    }

    private fun onRewindTapped(){
        if (exoPlayer.contentPosition <= (10000))
            exoPlayer.seekTo(0L)
        else
            exoPlayer.seekTo(exoPlayer.currentPosition - 10000)

        val fadeInObjectAnimator = ObjectAnimator.ofFloat(
            videoPlayerRewindHighlight,
            "alpha",
            videoPlayerRewindHighlight.alpha,
            1f
        )
        fadeInObjectAnimator.interpolator = AccelerateInterpolator()
        fadeInObjectAnimator.duration = FADE_IN_ANIM_DURATION
        fadeInObjectAnimator.doOnEnd {
            lifecycleScope.launch {
                delay(CHANGE_POSITION_ANIMATION_DURATION)
                withContext(Dispatchers.Main){
                    val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                        videoPlayerRewindHighlight,
                        "alpha",
                        videoPlayerRewindHighlight.alpha,
                        0f
                    )
                    fadeOutObjectAnimator.interpolator = AccelerateInterpolator()
                    fadeOutObjectAnimator.duration = FADE_OUT_ANIM_DURATION
                    fadeOutObjectAnimator.start()
                }
            }
        }
        fadeInObjectAnimator.start()
    }

    private fun onProgressChanged(position: Float){
        if (sliderInTouch) return
        videoPlayerControllerTimeLeft.text = miliSecToFormatedTime(exoPlayer.currentPosition)
        videoPlayerControllerSeekbar.apply {
            value = if (position >= valueTo) valueTo else position
        }
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SubtitleProgressChanged(progress = exoPlayer.currentPosition)
            )
        }
    }

    private fun onPlayIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(
                    if (exoPlayer.playWhenReady) PlayerControllerCommmand.Pause
                    else PlayerControllerCommmand.Play
                )
            )
        }
    }

    private fun onNextIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommmand.Next)
            )
        }
    }

    private fun onBackIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommmand.Previous)
            )
        }
    }

    private fun onOptionsIcClick(view: View) {
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_MAIN_MENU)
            )
        }
    }

    private fun getInitialData() {
        if (intent.action != null){
            try {
                videoPlayerViewModel.datasourcetype = PATH_DATA_TYPE
                if (intent.data?.scheme == "content"){
                    videoPlayerViewModel.activePath = videoPlayerViewModel.getRealPathFromURI(contentResolver,intent.data)
                }
                Logger.v("${videoPlayerViewModel.activePath}")
                videoPlayerViewModel.getFolderVideos(contentResolver)
            }catch (e: Exception){
                Toast.makeText(context,"PtPlayer cannot play this file",Toast.LENGTH_LONG).show()
                Logger.e(e.message)
                finish()
            }
            return
        }
        try {
            videoPlayerViewModel.datasourcetype =
                requireNotNull(intent.getStringExtra(DATA_SOURCE_TYPE_KEY))
        }catch (e: Exception){
            Logger.e(e.message)
            finish()
        }
        when(videoPlayerViewModel.datasourcetype){
            PATH_DATA_TYPE -> {
                intent.getStringExtra(DATA_SOURCE_KEY)?.run{
                    videoPlayerViewModel.activePath = this
                    videoPlayerViewModel.getFolderVideos(contentResolver)
                }?:run{
                    Logger.e("video path not found")
                    finish()
                }
            }
            else -> {
                videoPlayerControllerBackIc.visibility = View.GONE
                videoPlayerControllerNextIc.visibility = View.GONE
            }
        }
    }

    private fun setInitialViews() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
    }

    private fun onControllerToggle(){
        if (videoPlayerController.alpha != 0f){
            val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                0f
            )
            fadeOutObjectAnimator.interpolator = LinearInterpolator()
            fadeOutObjectAnimator.duration = FADE_IN_ANIM_DURATION
            fadeOutObjectAnimator.doOnEnd {
                setPlaybackButtonsVisibility(visible = false)
                setFullScreen(false)
            }
            fadeOutObjectAnimator.start()
        }else{
            val fadeInObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                1f
            )
            fadeInObjectAnimator.interpolator = LinearInterpolator()
            fadeInObjectAnimator.duration = FADE_OUT_ANIM_DURATION
            fadeInObjectAnimator.doOnEnd {
                setFullScreen(true)
            }
            fadeInObjectAnimator.start()
            setPlaybackButtonsVisibility(visible = true)
        }
    }

    private fun setFullScreen(enabled: Boolean = true){
        if (enabled){
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.VISIBLE
        }else{
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val fullscreenFlags =  (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

            window.decorView.systemUiVisibility =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        (fullscreenFlags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                    }else {
                        fullscreenFlags
                    }
        }
    }

    private fun setPlaybackButtonsVisibility(visible: Boolean){
        val targetVisibility = if (visible) View.VISIBLE else View.GONE
        videoPlayerControllerPlayIc.visibility = targetVisibility
        videoPlayerControllerNextIc.visibility = targetVisibility
        videoPlayerControllerBackIc.visibility = targetVisibility
        videoPlayerControllerSeekbar.visibility = targetVisibility
    }

    override fun onResume() {
        exoPlayer.seekTo(
            videoPlayerViewModel.resumeWindow,
            videoPlayerViewModel.resumePosition
        )
        super.onResume()
    }

    override fun onPause() {
        videoPlayerViewModel.resumePosition = exoPlayer.currentPosition
        videoPlayerViewModel.resumeWindow = exoPlayer.currentWindowIndex
        exoPlayer.playWhenReady = false
        super.onPause()
    }

    override fun onDestroy() {
        exoPlayer.stop()
        super.onDestroy()
    }

    override fun render(state: VideoPlayerState) {
        Logger.e("render($state)")
        if (state is PlaybackState.Buffering){
            videoPlayerLoadingAv.visibility = View.VISIBLE
        }else{
            videoPlayerLoadingAv.visibility = View.INVISIBLE
        }
        when(state){
            is PlaybackState.Playing -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_pause)
                videoPlayerControllerDuration.text = miliSecToFormatedTime(exoPlayer.duration)
                videoPlayerControllerTimeLeft.text = miliSecToFormatedTime(exoPlayer.currentPosition)
            }
            is PlaybackState.ReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
                videoPlayerControllerDuration.text = miliSecToFormatedTime(exoPlayer.duration)
                videoPlayerControllerTimeLeft.text = miliSecToFormatedTime(exoPlayer.currentPosition)
            }
            is PlaybackState.Ended -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PlaybackState.NotReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PLayerCommand.Start -> {
                exoPlayer.playWhenReady = true
            }
            is PLayerCommand.Pause -> {
                exoPlayer.playWhenReady = false
            }
            is PLayerCommand.SeekToPosition -> {
                exoPlayer.seekTo(state.position)
            }
            is PLayerCommand.Prepare -> {
                exoPlayer.prepare(state.mediaSource)
            }
            is OptionsState.ShowMainMenu -> {
                BottomSheetView(this).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            R.drawable.ic_closed_caption,
                            R.string.subtitle,
                            ::onSubtitleClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_speed,
                            R.string.playback_speed,
                            ::onPlaybackSpeedClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_audio,
                            R.string.audio,
                            ::onAudioClick
                        )
                    )
                    show()
                }
            }
            is SubtitleState.Clear -> {
                subtitleTv.text = ""
            }
            is SubtitleState.Show -> {
                subtitleTv.text = state.subText
            }
        }
    }

    private fun onSubtitleClick(){
        SubtitleListDialogView(this){path ->

        }.apply {
            show()
        }
    }

    private fun onPlaybackSpeedClick(){
    }

    private fun onAudioClick(){
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when(playbackState){
            ExoPlayer.STATE_BUFFERING -> {
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackState.Buffering)
                    )
                }
            }
            ExoPlayer.STATE_ENDED -> {
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackState.Ended)
                    )
                }
            }
            ExoPlayer.STATE_READY -> {
                videoPlayerControllerSeekbar.valueFrom = 0f
                videoPlayerControllerSeekbar.valueTo = exoPlayer.duration.toFloat()
                lifecycleScope.launch {
                    if (playWhenReady.not()) {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackState.ReadyAndStoped)
                        )
                    } else {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackState.Playing)
                        )
                    }
                }
            }
            else -> {
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackState.WithoutVideoSource)
                    )
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Logger.e("onIsPlayingChanged( isPlaying = $isPlaying )")
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SetPlayBackState(
                    if (isPlaying){
                        PlaybackState.Playing
                    }else{
                        PlaybackState.ReadyAndStoped
                    }
                )
            )
        }
    }

    private fun miliSecToFormatedTime(miliSec: Long): String{
        val durationHourInt = (miliSec / (60*60*1000)).toInt()
        val durationMinInt = ((miliSec % (60*60*1000))/(60*1000)).toInt()
        val durationSecInt = ((miliSec % (60*1000))/1000).toInt()
        val durationHourStr = if (durationHourInt < 10) "0$durationHourInt" else "$durationHourInt"
        val durationMinStr = if (durationMinInt < 10) "0$durationMinInt" else "$durationMinInt"
        val durationSecStr = if (durationSecInt < 10) "0$durationSecInt" else "$durationSecInt"
        return if (durationHourInt != 0) "$durationHourStr:$durationMinStr:$durationSecStr"
        else "$durationMinStr:$durationSecStr"
    }

}