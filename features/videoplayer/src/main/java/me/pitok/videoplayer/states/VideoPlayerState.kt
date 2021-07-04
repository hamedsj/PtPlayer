package me.pitok.videoplayer.states

import com.google.android.exoplayer2.source.MediaSource
import me.pitok.mvi.MviState
import me.pitok.sdkextentions.EmptySingleEvent
import me.pitok.sdkextentions.SingleEvent

data class VideoPlayerState(
    val pause: EmptySingleEvent? = null,
    val play: EmptySingleEvent? = null,
    val prepare: SingleEvent<MediaSource?>? = null,
    val seekToPosition: SingleEvent<Long>? = null,
    val changeSpeed: SingleEvent<Float>? = null,
    val changeSpeakerVolume: SingleEvent<Float>? = null,
    val changeOrientation: SingleEvent<Int>? = null,
    val showOptionsMenu: SingleEvent<OptionMenus>? = null,
    val playbackNotReady: EmptySingleEvent? = null,
    val playbackStatus: SingleEvent<PlaybackStatus>? = null,
) : MviState {

    companion object {

        sealed class OptionMenus {
            object MainMenu : OptionMenus()
            object SubtitleMenu : OptionMenus()
            object SpeedMenu : OptionMenus()
            object AudioMenu : OptionMenus()
        }

        sealed class PlaybackStatus {
            object NotReadyAndStopped : PlaybackStatus()
            object ReadyAndStopped : PlaybackStatus()
            object Playing : PlaybackStatus()
            object Ended : PlaybackStatus()
            object Buffering : PlaybackStatus()
            object WithoutVideoSource : PlaybackStatus()
        }

    }
}