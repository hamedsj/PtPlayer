package me.pitok.videoplayer.views

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.view_video_player_controller.*
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.logger.Logger
import me.pitok.videoplayer.R
import me.pitok.videoplayer.di.builder.VideoPlayerComponentBuilder
import me.pitok.videoplayer.viewmodels.VideoPlayerViewModel
import javax.inject.Inject

class VideoPlayerActivity : AppCompatActivity() {

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
        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        getInitialData()
        setInitialViews()
        videoPlayerPv.setOnClickListener(::onControllerToggle)
        videoPlayerControllerHighlight.setOnClickListener(::onControllerToggle)
        videoPlayerPv.player = exoPlayer
        exoPlayer.seekTo(0L)
        exoPlayer.prepare(videoPlayerViewModel.buildVideoSource())
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

    private fun onControllerToggle(view: View){
        if (videoPlayerController.alpha != 0f){
            val fadeInObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                0f)
            fadeInObjectAnimator.interpolator = LinearInterpolator()
            fadeInObjectAnimator.duration = CONTROLLER_FADE_IN_ANIM_DURATION
            fadeInObjectAnimator.start()
        }else{
            val fadeOutObjectAnimator = ObjectAnimator.ofFloat(
                videoPlayerController,
                "alpha",
                videoPlayerController.alpha,
                1f)
            fadeOutObjectAnimator.interpolator = LinearInterpolator()
            fadeOutObjectAnimator.duration = CONTROLLER_FADE_OUT_ANIM_DURATION
            fadeOutObjectAnimator.start()
        }
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

}