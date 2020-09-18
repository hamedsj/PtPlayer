package me.pitok.videoplayer.states

import com.google.android.exoplayer2.source.MediaSource
import me.pitok.mvi.MviState

data class VideoPlayerState (
    val playback_state: PlaybackState? = null,
    val command: PLayerCommand? = null
): MviState

sealed class PlaybackState {
    object NotReadyAndStoped: PlaybackState()
    object ReadyAndStoped: PlaybackState()
    object Playing: PlaybackState()
    object Ended: PlaybackState()
    object Buffering: PlaybackState()
    object WithoutVideoSource: PlaybackState()
}

sealed class PLayerCommand {
    object Pause: PLayerCommand()
    object Start: PLayerCommand()
    class Prepare(val mediaSource: MediaSource): PLayerCommand()
    class SeekToPosition(val position : Long): PLayerCommand()
}