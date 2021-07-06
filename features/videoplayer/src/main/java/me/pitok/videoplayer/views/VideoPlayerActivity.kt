package me.pitok.videoplayer.views

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
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
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.material.slider.Slider
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
import me.pitok.videoplayer.databinding.ActivityVideoPlayerBinding
import me.pitok.videoplayer.di.builder.VideoPlayerComponentBuilder
import me.pitok.videoplayer.intents.PlayerControllerCommand
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.SubtitleState
import me.pitok.videoplayer.states.VideoPlayerState
import me.pitok.videoplayer.states.VideoPlayerState.Companion.OptionMenus
import me.pitok.videoplayer.states.VideoPlayerState.Companion.PlaybackStatus
import me.pitok.videoplayer.viewmodels.VideoPlayerViewModel
import javax.inject.Inject


class VideoPlayerActivity :
    AppCompatActivity(),
    MviView<VideoPlayerState>,
    Player.Listener {

    companion object {
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

    private lateinit var binding: ActivityVideoPlayerBinding

    private var sliderInTouch = false
    private var durationSet = false

    private val videoPlayerViewModel: VideoPlayerViewModel by viewModels { viewModelFactory }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Logger.w("OnCreate")
        VideoPlayerComponentBuilder.getComponent().inject(this)
        getInitialData(savedInstanceState)
        setInitialViews(savedInstanceState)
        val screenWidth = getScreenWidth()
        videoPlayerViewModel.state.observe(this@VideoPlayerActivity, ::render)
        videoPlayerViewModel.subtitleObservable.observe(
            this@VideoPlayerActivity,
            ::observeSubtitleEvents
        )
        binding.playerController.videoPlayerController.setOnTouchListener(object :
            View.OnTouchListener {
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
        binding.playerController.videoPlayerControllerPlayClick.setOnClickListener(::onPlayIcClick)
        binding.playerController.videoPlayerControllerNextClick.setOnClickListener(::onNextIcClick)
        binding.playerController.videoPlayerControllerBackClick.setOnClickListener(::onBackIcClick)
        binding.playerController.videoPlayerControllerOptionsIc.setOnClickListener(::onOptionsIcClick)
        binding.playerController.videoPlayerControllerNavigateBackIc.setOnClickListener(::onNavigateBackIcClick)
        binding.videoPlayerPv.player = exoPlayer
        exoPlayer.seekTo(0L)
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommand.Prepare)
            )
        }
        exoPlayer.addListener(this)
        exoPlayer.onPositionChanged = { position ->
            onProgressChanged(position.toFloat())
        }
        binding.playerController.videoPlayerControllerSeekbar.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                sliderInTouch = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                sliderInTouch = false
                if (!durationSet) return
                exoPlayer.seekTo(slider.value.toLong())
            }

        })
        binding.playerController.videoPlayerControllerSeekbar.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (!fromUser || !durationSet) return@OnChangeListener
            binding.playerController.videoPlayerControllerTimeLeft.text =
                milliSecToFormattedTime(value.toLong())
        })
    }

    private fun onFastForwardTapped() {
        if (exoPlayer.duration == C.TIME_UNSET) return
        if (exoPlayer.contentPosition >= (exoPlayer.duration - 10000))
            exoPlayer.seekTo(exoPlayer.duration - 500)
        else
            exoPlayer.seekTo(exoPlayer.currentPosition + 10000)

        val fadeInObjectAnimator = ObjectAnimator.ofFloat(
            binding.videoPlayerFastForwardHighlight,
            "alpha",
            binding.videoPlayerFastForwardHighlight.alpha,
            1f
        )
        fadeInObjectAnimator.interpolator = AccelerateInterpolator()
        fadeInObjectAnimator.duration = FADE_IN_ANIM_DURATION
        fadeInObjectAnimator.doOnEnd {
            lifecycleScope.launch {
                delay(CHANGE_POSITION_ANIMATION_DURATION)
                withContext(Dispatchers.Main) {
                    val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                        binding.videoPlayerFastForwardHighlight,
                        "alpha",
                        binding.videoPlayerFastForwardHighlight.alpha,
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

    private fun onRewindTapped() {
        if (exoPlayer.duration == C.TIME_UNSET) return
        if (exoPlayer.contentPosition <= (10000))
            exoPlayer.seekTo(0L)
        else
            exoPlayer.seekTo(exoPlayer.currentPosition - 10000)

        val fadeInObjectAnimator = ObjectAnimator.ofFloat(
            binding.videoPlayerRewindHighlight,
            "alpha",
            binding.videoPlayerRewindHighlight.alpha,
            1f
        )
        fadeInObjectAnimator.interpolator = AccelerateInterpolator()
        fadeInObjectAnimator.duration = FADE_IN_ANIM_DURATION
        fadeInObjectAnimator.doOnEnd {
            lifecycleScope.launch {
                delay(CHANGE_POSITION_ANIMATION_DURATION)
                withContext(Dispatchers.Main) {
                    val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                        binding.videoPlayerRewindHighlight,
                        "alpha",
                        binding.videoPlayerRewindHighlight.alpha,
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

    private fun onProgressChanged(position: Float) {
        if (sliderInTouch) return
        binding.playerController.videoPlayerControllerTimeLeft.text =
            milliSecToFormattedTime(exoPlayer.currentPosition)
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SubtitleProgressChanged(progress = exoPlayer.currentPosition)
            )
        }
        if (!durationSet) return
        binding.playerController.videoPlayerControllerSeekbar.apply {
            value = if (position >= valueTo) valueTo else position
        }
    }

    private fun onPlayIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(
                    if (exoPlayer.playWhenReady) PlayerControllerCommand.Pause
                    else PlayerControllerCommand.Play
                )
            )
        }
    }

    private fun onNextIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommand.Next)
            )
        }
    }

    private fun onBackIcClick(view: View) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(PlayerControllerCommand.Previous)
            )
        }
    }

    private fun onNavigateBackIcClick(view: View) {
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            super.onBackPressed()
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
        if (intent.action != null) {
            try {
                videoPlayerViewModel.datasourcetype = LOCAL_PATH_DATA_TYPE
                if (intent.data?.scheme == "content") {
                    videoPlayerViewModel.activePath = videoPlayerViewModel.getRealPathFromURI(
                        contentResolver,
                        intent.data
                    )
                }
                Logger.v("${videoPlayerViewModel.activePath}")
                videoPlayerViewModel.getFolderVideos(contentResolver)
            } catch (e: Exception) {
                Toast.makeText(context, "PtPlayer cannot play this file", Toast.LENGTH_LONG).show()
                Logger.e(e.message)
                finish()
            }
            return
        }
        try {
            videoPlayerViewModel.datasourcetype =
                requireNotNull(intent.getStringExtra(DATA_SOURCE_TYPE_KEY))
        } catch (e: Exception) {
            Logger.e(e.message)
            finish()
        }
        when (videoPlayerViewModel.datasourcetype) {
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
                binding.playerController.videoPlayerControllerBackClick.visibility = View.GONE
                binding.playerController.videoPlayerControllerNextClick.visibility = View.GONE
                binding.playerController.videoPlayerControllerBackIc.visibility = View.GONE
                binding.playerController.videoPlayerControllerNextIc.visibility = View.GONE
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
        setFullScreen(true)
        binding.playerController.videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
        val lp = binding.playerController.videoPlayerControllerSeekbar.layoutParams
                as ConstraintLayout.LayoutParams
        binding.playerController.videoPlayerControllerSeekbar.layoutParams =
            when (requestedOrientation) {
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

    private fun onControllerToggle() {
        if (binding.playerController.videoPlayerController.alpha != 0f) {
            val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                binding.playerController.videoPlayerController,
                "alpha",
                binding.playerController.videoPlayerController.alpha,
                0f
            )
            fadeOutObjectAnimator.interpolator = LinearInterpolator()
            fadeOutObjectAnimator.duration = FADE_IN_ANIM_DURATION
            fadeOutObjectAnimator.doOnEnd {
                setPlaybackButtonsVisibility(visible = false)
                setSubtitleBottomMargin(12f)
            }
            fadeOutObjectAnimator.start()
        } else {
            val fadeInObjectAnimator = ObjectAnimator.ofFloat(
                binding.playerController.videoPlayerController,
                "alpha",
                binding.playerController.videoPlayerController.alpha,
                1f
            )
            fadeInObjectAnimator.interpolator = LinearInterpolator()
            fadeInObjectAnimator.duration = FADE_OUT_ANIM_DURATION
            fadeInObjectAnimator.doOnEnd {
//                setFullScreen(false)
                setSubtitleBottomMargin(24f)
            }
            fadeInObjectAnimator.start()
            setPlaybackButtonsVisibility(visible = true)
        }
    }

    private fun setSubtitleBottomMargin(bottomMargin: Float) {
        val lp = binding.subtitleTv.layoutParams as ConstraintLayout.LayoutParams
        lp.bottomMargin = bottomMargin.toPx()
        binding.subtitleTv.layoutParams = lp
    }

    private fun setFullScreen(enabled: Boolean = true) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            val fullscreenFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)

            window.decorView.systemUiVisibility =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    (fullscreenFlags or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                } else {
                    fullscreenFlags
                }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.decorView.systemUiVisibility = View.VISIBLE
        }
    }

    private fun setPlaybackButtonsVisibility(visible: Boolean) {
        val targetVisibility = if (visible) View.VISIBLE else View.GONE
        binding.playerController.videoPlayerControllerPlayClick.visibility = targetVisibility
        binding.playerController.videoPlayerControllerNextClick.visibility = targetVisibility
        binding.playerController.videoPlayerControllerBackClick.visibility = targetVisibility
        binding.playerController.videoPlayerControllerPlayIc.visibility = targetVisibility
        binding.playerController.videoPlayerControllerNextIc.visibility = targetVisibility
        binding.playerController.videoPlayerControllerBackIc.visibility = targetVisibility
        binding.playerController.videoPlayerControllerSeekbar.visibility = targetVisibility
        binding.playerController.videoPlayerControllerOptionsIc.visibility = targetVisibility
        binding.playerController.videoPlayerControllerSeekbar.isEnabled = visible
        binding.playerController.videoPlayerControllerPlayClick.setOnClickListener(
            if (visible) {
                ::onPlayIcClick
            } else {
                { onControllerToggle() }
            }
        )
        binding.playerController.videoPlayerControllerNextClick.setOnClickListener(
            if (visible) {
                ::onNextIcClick
            } else {
                { onControllerToggle() }
            }
        )
        binding.playerController.videoPlayerControllerBackClick.setOnClickListener(
            if (visible) {
                ::onBackIcClick
            } else {
                { onControllerToggle() }
            }
        )
        binding.playerController.videoPlayerControllerOptionsIc.setOnClickListener(
            if (visible) {
                ::onOptionsIcClick
            } else {
                { onControllerToggle() }
            }
        )
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

    private fun observeSubtitleEvents(event: SubtitleState) {
        when (event) {
            is SubtitleState.Clear -> {
                binding.subtitleTv.text = ""
            }
            is SubtitleState.Show -> {
                binding.subtitleTv.setTextColor(getSubtitleTextColor())
                binding.subtitleTv.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    videoPlayerViewModel.subtitleTextSize.toFloat()
                )
                binding.subtitleTv.text = getSubtitleSpannableFromString(event.subText)
            }
            is SubtitleState.SubtitleReadingError -> {
                Toast.makeText(context, "Error in reading subtitle file!", Toast.LENGTH_LONG).show()
            }
            is SubtitleState.SubtitleNotFoundError -> {
                Toast.makeText(context, "Subtitle file not found!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun render(state: VideoPlayerState) {
        state.playbackStatus?.ifNotHandled { status ->
            when (status) {
                is PlaybackStatus.Playing -> {
                    endBuffering()
                    binding.playerController.videoPlayerControllerPlayIc
                        .setImageResource(R.drawable.ic_pause)
                    binding.playerController.videoPlayerControllerDuration.text =
                        milliSecToFormattedTime(exoPlayer.duration)
                    binding.playerController.videoPlayerControllerTimeLeft.text =
                        milliSecToFormattedTime(exoPlayer.currentPosition)
                }
                is PlaybackStatus.ReadyAndStopped -> {
                    endBuffering()
                    binding.playerController.videoPlayerControllerPlayIc
                        .setImageResource(R.drawable.ic_play)
                    binding.playerController.videoPlayerControllerDuration.text =
                        milliSecToFormattedTime(exoPlayer.duration)
                    binding.playerController.videoPlayerControllerTimeLeft.text =
                        milliSecToFormattedTime(exoPlayer.currentPosition)
                }
                is PlaybackStatus.Ended -> {
                    endBuffering()
                    binding.playerController.videoPlayerControllerPlayIc
                        .setImageResource(R.drawable.ic_play)
                    lifecycleScope.launch {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SendCommand(PlayerControllerCommand.Next)
                        )
                    }
                }
                is PlaybackStatus.NotReadyAndStopped -> {
                    endBuffering()
                    binding.playerController.videoPlayerControllerPlayIc
                        .setImageResource(R.drawable.ic_play)
                }
                is PlaybackStatus.Buffering -> {
                    binding.playerController.videoPlayerControllerPlayClick.isEnabled = false
                    binding.playerController.videoPlayerControllerBackClick.isEnabled = false
                    binding.playerController.videoPlayerControllerNextClick.isEnabled = false
                    binding.playerController.videoPlayerLoadingAv.visibility = View.VISIBLE
                }
                else -> {
                }
            }
        }

        state.play?.ifNotHandled {
            exoPlayer.playWhenReady = true
        }

        state.pause?.ifNotHandled {
            exoPlayer.playWhenReady = false
        }

        state.seekToPosition?.ifNotHandled { position ->
            exoPlayer.seekTo(position)
        }

        state.prepare?.ifNotHandled { mediaSource ->
            mediaSource?.apply {
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackStatus.Buffering)
                    )
                }
                exoPlayer.setMediaSource(this)
            } ?: apply {
                Toast.makeText(context, "Invalid Url Path!", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        state.changeSpeed?.ifNotHandled { speed ->
            exoPlayer.playbackParameters = PlaybackParameters(speed)
        }

        state.changeSpeakerVolume?.ifNotHandled { speakerVolume ->
            exoPlayer.volume =
                when {
                    speakerVolume > 1f -> 1f
                    speakerVolume < 0f -> 0f
                    else -> speakerVolume
                }
        }

        state.changeOrientation?.ifNotHandled { orientation ->
            if (orientation == VideoPlayerViewModel.LANDSCAPE_ORIENTATION &&
                requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ) {
                videoPlayerViewModel.playerOrientation =
                    VideoPlayerViewModel.LANDSCAPE_ORIENTATION
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else if (orientation == VideoPlayerViewModel.PORTRAIT_ORIENTATION &&
                requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ) {
                videoPlayerViewModel.playerOrientation =
                    VideoPlayerViewModel.PORTRAIT_ORIENTATION
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        state.showOptionsMenu?.ifNotHandled { menu ->
            when (menu) {
                is OptionMenus.MainMenu -> showMainMenu()
                is OptionMenus.SubtitleMenu -> showSubtitleMenu()
                is OptionMenus.SpeedMenu -> showSpeedMenu()
                is OptionMenus.AudioMenu -> showAudioMenu()
            }
        }

        state.setName?.ifNotHandled { name ->
            name?.let {
                binding.playerController.videoPlayerControllerVideoName.text = it
            } ?: let {
                binding.playerController.videoPlayerControllerVideoName.text = ""
            }
        }
    }

    private fun endBuffering() {
        binding.playerController.videoPlayerControllerPlayClick.visibility = View.VISIBLE
        binding.playerController.videoPlayerControllerBackClick.visibility = View.VISIBLE
        binding.playerController.videoPlayerControllerNextClick.visibility = View.VISIBLE
        binding.playerController.videoPlayerControllerPlayClick.isEnabled = true
        binding.playerController.videoPlayerControllerBackClick.isEnabled = true
        binding.playerController.videoPlayerControllerNextClick.isEnabled = true
        binding.playerController.videoPlayerLoadingAv.visibility = View.INVISIBLE
    }

    private fun showMainMenu() {
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

    private fun showSubtitleMenu() {
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

    private fun showSpeedMenu() {
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

    private fun showAudioMenu() {
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
                        { exoPlayer.disableAudioRenderer() },
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
                                { exoPlayer.selectTrackGroup(groupIndex) },
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

    private fun onScreenRotationOptionClick() {
        Logger.d("onScreenRotationOptionClick called : $requestedOrientation")
        videoPlayerViewModel.resumePosition = exoPlayer.currentPosition
        videoPlayerViewModel.resumeWindow = exoPlayer.currentWindowIndex
        when (requestedOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> {
                videoPlayerViewModel.playerOrientation =
                    VideoPlayerViewModel.PORTRAIT_ORIENTATION
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            else -> {
                videoPlayerViewModel.playerOrientation =
                    VideoPlayerViewModel.LANDSCAPE_ORIENTATION
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    private fun getSubtitleTextColor(): Int {
        videoPlayerViewModel.subtitleTextColor?.let {
            return it
        } ?: let {
            return ContextCompat.getColor(applicationContext, R.color.colorSubtitleWhite)
        }
    }

    private fun getSubtitleHighlightColor(): Int {
        videoPlayerViewModel.subtitleHighlightColor?.let {
            return it
        } ?: let {
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

    private fun onSubtitleOptionClick() {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_SUBTITLE_MENU)
            )
        }
    }

    private fun onPlaybackSpeedOptionClick() {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_SPEED_MENU)
            )
        }
    }

    private fun onAudioOptionClick() {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.ShowOptions(OPTIONS_AUDIO_MENU)
            )
        }
    }

    private fun onChangePlayBackSpeed(speed: Float) {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SendCommand(
                    PlayerControllerCommand.ChangePlaybackSpeed(speed)
                )
            )
        }
    }

    private fun onDeleteSubtitle() {
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.RemoveSubtitle
            )
        }
    }

    private fun onSubtitleClick() {
        SubtitleListDialogView(this) { path ->
            lifecycleScope.launch {
                videoPlayerViewModel.intents.send(
                    VideoPlayerIntent.LoadSubtitle(path)
                )
            }
        }.apply {
            show()
        }
    }

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        Logger.d("onVideoSizeChanged/: width =$width & height =$height")
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.VideoSizeChanged(width, height)
            )
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Logger.e("playWhenReady = $playWhenReady")
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                Logger.e("ExoPlayer.STATE_BUFFERING")
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackStatus.Buffering)
                    )
                }
            }
            ExoPlayer.STATE_ENDED -> {
                Logger.e("ExoPlayer.STATE_ENDED")
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackStatus.Ended)
                    )
                }
            }
            ExoPlayer.STATE_READY -> {
                Logger.e("ExoPlayer.STATE_READY")
                binding.playerController.videoPlayerControllerSeekbar.valueFrom = 0f
                if (exoPlayer.duration != C.TIME_UNSET) {
                    durationSet = true
                    binding.playerController.videoPlayerControllerSeekbar.valueTo =
                        exoPlayer.duration.toFloat()
                } else {
                    durationSet = false
                    binding.playerController.videoPlayerControllerSeekbar.valueTo =
                        (Long.MAX_VALUE).toFloat()
                }
                lifecycleScope.launch {
                    if (playWhenReady.not()) {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackStatus.ReadyAndStopped)
                        )
                    } else {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackStatus.Playing)
                        )
                    }
                }
            }
            else -> {
                Logger.e("ExoPlayer.STATE_IDLE")
                lifecycleScope.launch {
                    videoPlayerViewModel.intents.send(
                        VideoPlayerIntent.SetPlayBackState(PlaybackStatus.WithoutVideoSource)
                    )
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Logger.e("onIsPlayingChanged( isPlaying = $isPlaying )")
        if (isPlaying) {
            try {
                requestAudioFocus()
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        lifecycleScope.launch {
            videoPlayerViewModel.intents.send(
                VideoPlayerIntent.SetPlayBackState(
                    if (isPlaying) {
                        PlaybackStatus.Playing
                    } else {
                        PlaybackStatus.ReadyAndStopped
                    }
                )
            )
        }
    }

    private fun milliSecToFormattedTime(milliSec: Long): String {
        if (milliSec < 0) return "--:--"
        val durationHourInt = (milliSec / (60 * 60 * 1000)).toInt()
        val durationMinInt = ((milliSec % (60 * 60 * 1000)) / (60 * 1000)).toInt()
        val durationSecInt = ((milliSec % (60 * 1000)) / 1000).toInt()
        val durationHourStr = if (durationHourInt < 10) "0$durationHourInt" else "$durationHourInt"
        val durationMinStr = if (durationMinInt < 10) "0$durationMinInt" else "$durationMinInt"
        val durationSecStr = if (durationSecInt < 10) "0$durationSecInt" else "$durationSecInt"
        return if (durationHourInt != 0) "$durationHourStr:$durationMinStr:$durationSecStr"
        else "$durationMinStr:$durationSecStr"
    }

    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            {},
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
    }
}