package me.pitok.videoplayer.states

import com.google.android.exoplayer2.source.MediaSource
import me.pitok.mvi.MviState

open class VideoPlayerState: MviState

sealed class PlaybackState: VideoPlayerState() {
    object NotReadyAndStoped: PlaybackState()
    object ReadyAndStoped: PlaybackState()
    object Playing: PlaybackState()
    object Ended: PlaybackState()
    object Buffering: PlaybackState()
    object WithoutVideoSource: PlaybackState()
}

sealed class PLayerCommand: VideoPlayerState() {
    object Pause: PLayerCommand()
    object Start: PLayerCommand()
    class Prepare(val mediaSource: MediaSource): PLayerCommand()
    class SeekToPosition(val position : Long): PLayerCommand()
}

sealed class OptionsState: VideoPlayerState() {
    object ShowMainMenu: OptionsState()
}