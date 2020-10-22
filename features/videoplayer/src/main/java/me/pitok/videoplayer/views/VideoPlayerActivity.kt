package me.pitok.videoplayer.views

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.view_video_player_controller.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.design.entity.BottomSheetItemEntity
import me.pitok.design.views.ChooserBottomSheetView
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.logger.Logger
import me.pitok.mvi.MviView
import me.pitok.player.di.IndictableSimpleExoPlayer
import me.pitok.sdkextentions.getScreenWidth
import me.pitok.sdkextentions.toPx
import me.pitok.subtitle.components.SubtitleBackgroundColorSpan
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
        const val LOCAL_PATH_DATA_TYPE = "localpath"
        const val ONLINE_PATH_DATA_TYPE = "onlinepath"
        const val FADE_IN_ANIM_DURATION = 150L
        const val FADE_OUT_ANIM_DURATION = 250L
        const val CHANGE_POSITION_ANIMATION_DURATION = 250L
        const val CLICK_ANIMATION_DURATION = 100L
        const val OPTIONS_MAIN_MENU = 0
        const val OPTIONS_SUBTITLE_MENU = 1
        const val OPTIONS_SPEED_MENU = 2
        const val OPTIONS_AUDIO_MENU = 3
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var exoPlayer: IndictableSimpleExoPlayer

    @ApplicationContext
    @Inject
    lateinit var context: Context

    private var sliderInTouch = false
    private var durationSet = false

    private val videoPlayerViewModel: VideoPlayerViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        Logger.w("OnCreate")
        VideoPlayerComponentBuilder.getComponent().inject(this)
        getInitialData(savedInstanceState)
        setInitialViews(savedInstanceState)
        val screenWidth = getScreenWidth()
        videoPlayerViewModel.state.observe(this@VideoPlayerActivity, ::render)
        videoPlayerControllerHighlight.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(context, object :
                GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (requireNotNull(e?.x) >= screenWidth / 2f) {
                        onFastForwardTapped()
                    } else {
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
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommmand.Prepare)
            )
        }
        exoPlayer.addListener(this)
        exoPlayer.onPositionChanged = { position ->
            onProgressChanged(position.toFloat())
        }
        videoPlayerControllerSeekbar.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                sliderInTouch = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                sliderInTouch = false
                if (!durationSet)return
                exoPlayer.seekTo(slider.value.toLong())
            }

        })
        videoPlayerControllerSeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (!fromUser || !durationSet) return@OnChangeListener
            videoPlayerControllerTimeLeft.text = miliSecToFormatedTime(value.toLong())
        })
    }

    private fun onFastForwardTapped(){
        if (exoPlayer.duration == C.TIME_UNSET) return
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
        if (exoPlayer.duration == C.TIME_UNSET) return
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
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SubtitleProgressChanged(progress = exoPlayer.currentPosition)
            )
        }
        if (!durationSet) return
        videoPlayerControllerSeekbar.apply {
            value = if (position >= valueTo) valueTo else position
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

    private fun getInitialData(savedInstanceState: Bundle?) {
        Logger.e("getInitialData called")
        if (savedInstanceState == null) {
            videoPlayerViewModel.resumePosition = 0L
            videoPlayerViewModel.resumeWindow = 0
        }
        if (intent.action != null){
            try {
                videoPlayerViewModel.datasourcetype = LOCAL_PATH_DATA_TYPE
                if (intent.data?.scheme == "content"){
                    videoPlayerViewModel.activePath = videoPlayerViewModel.getRealPathFromURI(
                        contentResolver,
                        intent.data
                    )
                }
                Logger.v("${videoPlayerViewModel.activePath}")
                videoPlayerViewModel.getFolderVideos(contentResolver)
            }catch (e: Exception){
                Toast.makeText(context, "PtPlayer cannot play this file", Toast.LENGTH_LONG).show()
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
            LOCAL_PATH_DATA_TYPE -> {
                intent.getStringExtra(DATA_SOURCE_KEY)?.run {
                    videoPlayerViewModel.activePath = this
                    videoPlayerViewModel.getFolderVideos(contentResolver)
                } ?: run {
                    Logger.e("video path not found")
                    finish()
                }
            }
            ONLINE_PATH_DATA_TYPE -> {
                Logger.e("ONLINE_PATH_DATA_TYPE processing")
                intent.getStringExtra(DATA_SOURCE_KEY)?.run {
                    Logger.e("videoPlayerViewModel.activePath = $this")
                    videoPlayerViewModel.activePath = this
                } ?: run {
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

    private fun setInitialViews(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                delay(CLICK_ANIMATION_DURATION)
                videoPlayerViewModel.intents.send(
                    VideoPlayerIntent.SetInitialConfigsIntent
                )
            }
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
        val lp = videoPlayerControllerSeekbar.layoutParams as ConstraintLayout.LayoutParams
        videoPlayerControllerSeekbar.layoutParams = when (requestedOrientation){
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                lp.bottomMargin = 8f.toPx()
                lp
            }
            else -> {
                lp.bottomMargin = 32f.toPx()
                lp
            }
        }
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
                setSubtitleBottomMargin(12f)
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
                setSubtitleBottomMargin(24f)
            }
            fadeInObjectAnimator.start()
            setPlaybackButtonsVisibility(visible = true)
        }
    }

    private fun setSubtitleBottomMargin(bottomMargin: Float){
        val lp = subtitleTv.layoutParams as ConstraintLayout.LayoutParams
        lp.bottomMargin = bottomMargin.toPx()
        subtitleTv.layoutParams = lp
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
        videoPlayerControllerOptionsIc.visibility = targetVisibility
        videoPlayerControllerPlayIc.isEnabled = visible
        videoPlayerControllerNextIc.isEnabled = visible
        videoPlayerControllerBackIc.isEnabled = visible
        videoPlayerControllerSeekbar.isEnabled = visible
        videoPlayerControllerOptionsIc.isEnabled = visible

    }

    override fun onResume() {
        Logger.w("OnResume")
        exoPlayer.seekTo(
            videoPlayerViewModel.resumeWindow,
            videoPlayerViewModel.resumePosition
        )
        super.onResume()
    }

    override fun onPause() {
        Logger.w("OnPause")
        videoPlayerViewModel.resumePosition = exoPlayer.currentPosition
        videoPlayerViewModel.resumeWindow = exoPlayer.currentWindowIndex
        exoPlayer.playWhenReady = false
        super.onPause()
    }

    override fun onDestroy() {
        Logger.w("OnDestroy")
        exoPlayer.stop()
        super.onDestroy()
    }

    override fun render(state: VideoPlayerState) {
        if (state !is SubtitleState)
            Logger.e("render($state)")
        if (state !is PlaybackState.Buffering){
            videoPlayerControllerPlayIc.visibility = View.VISIBLE
            videoPlayerControllerBackIc.visibility = View.VISIBLE
            videoPlayerControllerNextIc.visibility = View.VISIBLE
            videoPlayerLoadingAv.visibility = View.INVISIBLE
        }
        when(state){
            is PlaybackState.Playing -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_pause)
                videoPlayerControllerDuration.text = miliSecToFormatedTime(exoPlayer.duration)
                videoPlayerControllerTimeLeft.text =
                    miliSecToFormatedTime(exoPlayer.currentPosition)
            }
            is PlaybackState.ReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
                videoPlayerControllerDuration.text = miliSecToFormatedTime(exoPlayer.duration)
                videoPlayerControllerTimeLeft.text =
                    miliSecToFormatedTime(exoPlayer.currentPosition)
            }
            is PlaybackState.Ended -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PlaybackState.NotReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PlaybackState.Buffering -> {
                videoPlayerControllerPlayIc.visibility = View.INVISIBLE
                videoPlayerControllerBackIc.visibility = View.INVISIBLE
                videoPlayerControllerNextIc.visibility = View.INVISIBLE
                videoPlayerLoadingAv.visibility = View.VISIBLE
            }
            is PLayerCommandState.Start -> {
                exoPlayer.playWhenReady = true
            }
            is PLayerCommandState.Pause -> {
                exoPlayer.playWhenReady = false
            }
            is PLayerCommandState.SeekToPosition -> {
                exoPlayer.seekTo(state.position)
            }
            is PLayerCommandState.Prepare -> {
                state.mediaSource?.apply {
                    lifecycleScope.launch {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackState.Buffering)
                        )
                    }
                    exoPlayer.prepare(this)
                }?:apply {
                    Toast.makeText(context,"Invalid Url Path!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            is PLayerCommandState.ChangeSpeed -> {
                exoPlayer.setPlaybackParameters(
                    PlaybackParameters(
                        state.speed,
                        exoPlayer.playbackParameters.pitch,
                        exoPlayer.playbackParameters.skipSilence
                    )
                )
            }
            is PLayerCommandState.ChangeSpeakerVolume -> {
                exoPlayer.volume =
                    if (state.speakerVolume>1f) 1f
                    else if (state.speakerVolume<0f) 0f
                    else state.speakerVolume
            }
            is OptionsState.ShowMainMenu -> {
                ChooserBottomSheetView(this).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            R.drawable.ic_closed_caption,
                            itemSecondaryIconResource = null,
                            R.string.subtitle,
                            if (videoPlayerViewModel.isSubtitleReady())
                                ::onSubtitleOptionClick
                            else
                                ::onSubtitleClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_speed,
                            itemSecondaryIconResource = null,
                            R.string.playback_speed,
                            ::onPlaybackSpeedOptionClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_audio,
                            itemSecondaryIconResource = null,
                            R.string.audio,
                            ::onAudioOptionClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_screen_rotation,
                            itemSecondaryIconResource = null,
                            R.string.rotate_screen,
                            ::onScreenRotationOptionClick
                        )
                    )
                    show()
                }
            }
            is OptionsState.ShowSubtitleMenu -> {
                ChooserBottomSheetView(this).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            R.drawable.ic_closed_caption,
                            itemSecondaryIconResource = null,
                            R.string.choose_subtitle,
                            ::onSubtitleClick
                        ),
                        BottomSheetItemEntity(
                            R.drawable.ic_delete,
                            itemSecondaryIconResource = null,
                            R.string.remove_subtitle,
                            ::onDeleteSubtitle
                        )
                    )
                    show()
                }
            }
            is OptionsState.ShowPlaybackSpeedMenu -> {
                ChooserBottomSheetView(this).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 0.25f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_25x,
                            { onChangePlayBackSpeed(0.25f) }
                        ),
                        BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 0.5f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_50x,
                            { onChangePlayBackSpeed(0.5f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 0.75f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._0_75x,
                            { onChangePlayBackSpeed(0.75f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 1f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1x,
                            { onChangePlayBackSpeed(1f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 1.25f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_25x,
                            { onChangePlayBackSpeed(1.25f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 1.5f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_50x,
                            { onChangePlayBackSpeed(1.5f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 1.75f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._1_75x,
                            { onChangePlayBackSpeed(1.75f) }
                        ), BottomSheetItemEntity(
                            if (exoPlayer.playbackParameters.speed == 2f)
                                R.drawable.ic_check
                            else
                                null,
                            itemSecondaryIconResource = null,
                            R.string._2x,
                            { onChangePlayBackSpeed(2f) }
                        )
                    )
                    show()
                }
            }
            is OptionsState.ShowAudioMenu -> {
                exoPlayer.trackSelector
                    .currentMappedTrackInfo
                    ?.getTrackGroups(C.TRACK_TYPE_AUDIO)
                    ?.apply {
                        val audioTrackList = mutableListOf<BottomSheetItemEntity>()
                        audioTrackList.add(
                            BottomSheetItemEntity(
                                if (!exoPlayer.isAudioRendererEnabled())
                                    R.drawable.ic_check
                                else
                                    null,
                                itemSecondaryIconResource = null,
                                R.string.disable_audio,
                                {exoPlayer.disableAudioRenderer()},
                                null
                            )
                        )
                        for (groupIndex in 0 until length) {
                            for (formatIndex in 0 until get(groupIndex).length) {
                                audioTrackList.add(
                                    BottomSheetItemEntity(
                                        if (exoPlayer.isGroupIndexSelected(groupIndex))
                                            R.drawable.ic_check
                                        else
                                            null,
                                        itemSecondaryIconResource = null,
                                        null,
                                        {exoPlayer.selectTrackGroup(groupIndex)},
                                        DefaultTrackNameProvider(resources).getTrackName(
                                            get(groupIndex).getFormat(formatIndex)
                                        )
                                    )
                                )
                            }
                        }
                        ChooserBottomSheetView(this@VideoPlayerActivity).apply {
                            sheetTitle = ""
                            sheetItems = audioTrackList
                            show()
                        }
                    }
            }
            is OptionsState.ChangeOrientation -> {
                if (state.landscape &&
                    requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    videoPlayerViewModel.playerLandscapeOrientation = true
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else if(!state.landscape &&
                    requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    videoPlayerViewModel.playerLandscapeOrientation = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
            is SubtitleState.Clear -> {
                subtitleTv.text = ""
            }
            is SubtitleState.Show -> {
                Logger.e("Subtitle::: ${state.subText}")
                subtitleTv.setTextColor(getSubtitleTextColor())
                subtitleTv.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    videoPlayerViewModel.subtitleTextSize.toFloat()
                )
                subtitleTv.text = getSubtitleSpannableFromString(state.subText)
            }
            is SubtitleState.SubtitleReadingError -> {
                Toast.makeText(context, "Error in reading subtitle file!", Toast.LENGTH_LONG).show()
            }
            is SubtitleState.SubtitleNotFoundError -> {
                Toast.makeText(context, "Subtitle file not found!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onScreenRotationOptionClick(){
        videoPlayerViewModel.resumePosition = exoPlayer.currentPosition
        videoPlayerViewModel.resumeWindow = exoPlayer.currentWindowIndex
        when (requestedOrientation){
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> {
                videoPlayerViewModel.playerLandscapeOrientation = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            else -> {
                videoPlayerViewModel.playerLandscapeOrientation = true
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    private fun getSubtitleTextColor(): Int{
        videoPlayerViewModel.subtitleTextColor?.let {
            return it
        }?:let{
            return ContextCompat.getColor(applicationContext,R.color.colorSubtitleWhite)
        }
    }

    private fun getSubtitleHighlightColor(): Int{
        videoPlayerViewModel.subtitleHighlightColor?.let{
            return it
        }?:let {
            return ContextCompat.getColor(context, R.color.colorHighlightText)
        }
    }

    private fun getSubtitleSpannableFromString(str: String): SpannableString {
        val subtitleBackgroundColorSpan = SubtitleBackgroundColorSpan(
            getSubtitleHighlightColor(),
            8f.toPx().toFloat(),
            4f.toPx().toFloat()
        )
        val spannableString = SpannableString(str)
        spannableString.setSpan(
            subtitleBackgroundColorSpan,
            0,
            str.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun onSubtitleOptionClick(){
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_SUBTITLE_MENU)
            )
        }
    }

    private fun onPlaybackSpeedOptionClick(){
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_SPEED_MENU)
            )
        }
    }

    private fun onAudioOptionClick(){
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_AUDIO_MENU)
            )
        }
    }

    private fun onChangePlayBackSpeed(speed: Float){
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(
                    PlayerControllerCommmand.ChangePlaybackSpeed(speed)
                )
            )
        }
    }

    private fun onDeleteSubtitle(){
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.RemoveSubtitle
            )
        }
    }

    private fun onSubtitleClick(){
        SubtitleListDialogView(this){ path ->
            lifecycleScope.launch {
                videoPlayerViewModel.intents.send(
                    VideoPlayerIntent.LoadSubtitle(path)
                )
            }
        }.apply {
            show()
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Logger.e("playWhenReady = $playWhenReady")
        when(playbackState){
            ExoPlayer.STATE_BUFFERING -> {
                Logger.e("ExoPlayer.STATE_BUFFERING")
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackState.Buffering)
                    )
                }
            }
            ExoPlayer.STATE_ENDED -> {
                Logger.e("ExoPlayer.STATE_ENDED")
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackState.Ended)
                    )
                }
            }
            ExoPlayer.STATE_READY -> {
                Logger.e("ExoPlayer.STATE_READY")
                videoPlayerControllerSeekbar.valueFrom = 0f
                if (exoPlayer.duration != C.TIME_UNSET){
                    durationSet = true
                    videoPlayerControllerSeekbar.valueTo = exoPlayer.duration.toFloat()
                }else{
                    durationSet = false
                    videoPlayerControllerSeekbar.valueTo = (Long.MAX_VALUE).toFloat()
                }
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
                Logger.e("ExoPlayer.STATE_IDLE")
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
        if (isPlaying){
            try {
                requestAudioFocus()
            }catch (e: Exception){
                Logger.e(e.message)
            }
        }
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SetPlayBackState(
                    if (isPlaying) {
                        PlaybackState.Playing
                    } else {
                        PlaybackState.ReadyAndStoped
                    }
                )
            )
        }
    }

    private fun miliSecToFormatedTime(miliSec: Long): String{
        if (miliSec < 0) return "--:--"
        val durationHourInt = (miliSec / (60*60*1000)).toInt()
        val durationMinInt = ((miliSec % (60*60*1000))/(60*1000)).toInt()
        val durationSecInt = ((miliSec % (60*1000))/1000).toInt()
        val durationHourStr = if (durationHourInt < 10) "0$durationHourInt" else "$durationHourInt"
        val durationMinStr = if (durationMinInt < 10) "0$durationMinInt" else "$durationMinInt"
        val durationSecStr = if (durationSecInt < 10) "0$durationSecInt" else "$durationSecInt"
        return if (durationHourInt != 0) "$durationHourStr:$durationMinStr:$durationSecStr"
        else "$durationMinStr:$durationSecStr"
    }

    private fun requestAudioFocus() : Boolean{
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus({}, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }
}