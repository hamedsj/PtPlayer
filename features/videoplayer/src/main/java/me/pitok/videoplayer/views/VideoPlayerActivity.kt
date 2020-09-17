package me.pitok.videoplayer.views

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.view_video_player_controller.*
import kotlinx.coroutines.launch
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.logger.Logger
import me.pitok.mvi.MviView
import me.pitok.videoplayer.R
import me.pitok.videoplayer.di.builder.VideoPlayerComponentBuilder
import me.pitok.videoplayer.intents.VideoPlayerIntent
import me.pitok.videoplayer.states.PlaybackState
import me.pitok.videoplayer.states.VideoPlayerState
import me.pitok.videoplayer.viewmodels.VideoPlayerViewModel
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity(), MviView<VideoPlayerState>, Player.EventListener {

    companion object{
        const val DATA_SOURCE_KEY = "datasource"
        const val DATA_SOURCE_TYPE_KEY = "datasourcetype"
        const val PATH_DATA_TYPE = "path"
        const val CONTROLLER_FADE_IN_ANIM_DURATION = 150L
        const val CONTROLLER_FADE_OUT_ANIM_DURATION = 250L
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private val videoPlayerViewModel: VideoPlayerViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        VideoPlayerComponentBuilder.getComponent().inject(this)
        setFullScreen(true)
        getInitialData()
        setInitialViews()
        videoPlayerViewModel.state.observe(this@VideoPlayerActivity, ::render)
        videoPlayerPv.setOnClickListener(::onControllerToggle)
        videoPlayerControllerHighlight.setOnClickListener(::onControllerToggle)
        videoPlayerControllerPlayIc.setOnClickListener(::onPlayIcClick)
        videoPlayerPv.player = exoPlayer
        exoPlayer.seekTo(0L)
        exoPlayer.prepare(videoPlayerViewModel.buildVideoSource())
        exoPlayer.addListener(this)
    }

    private fun onPlayIcClick(view: View) {
        exoPlayer.playWhenReady = exoPlayer.playWhenReady.not()
    }

    private fun getInitialData() {
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
                    videoPlayerViewModel.path = this
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
        videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
    }

    private fun onControllerToggle(view: View?){
        if (videoPlayerController.alpha != 0f){
            val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                0f)
            fadeOutObjectAnimator.interpolator = LinearInterpolator()
            fadeOutObjectAnimator.duration = CONTROLLER_FADE_IN_ANIM_DURATION
            fadeOutObjectAnimator.doOnEnd {
                setPlaybackButtonsVisibility(visible = false)
            }
            fadeOutObjectAnimator.start()
            setFullScreen(false)
        }else{
            val fadeInObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                1f)
            fadeInObjectAnimator.interpolator = LinearInterpolator()
            fadeInObjectAnimator.duration = CONTROLLER_FADE_OUT_ANIM_DURATION
            fadeInObjectAnimator.start()
            setPlaybackButtonsVisibility(visible = true)
            setFullScreen(true)
        }
    }

    private fun setFullScreen(enabled: Boolean = true){
        if (enabled){
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }else{
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
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
        if (state.playback_state is PlaybackState.Buffering){
            videoPlayerLoadingAv.visibility = View.VISIBLE
        }else{
            videoPlayerLoadingAv.visibility = View.INVISIBLE
        }
        when(state.playback_state){
            is PlaybackState.Playing -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_pause)
            }
            is PlaybackState.ReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PlaybackState.Ended -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
            is PlaybackState.NotReadyAndStoped -> {
                videoPlayerControllerPlayIc.setImageResource(R.drawable.ic_play)
            }
        }
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
                lifecycleScope.launch {
                    if (playWhenReady.not()) {
                        videoPlayerViewModel.intents.send(
                            VideoPlayerIntent.SetPlayBackState(PlaybackState.ReadyAndStoped)
                        )
                    }else {
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

}