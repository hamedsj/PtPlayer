package me.pitok.videoplayer.states

import me.pitok.mvi.MviState

data class VideoPlayerState (
    val playback_state: PlaybackState = PlaybackState.NotReadyAndStoped,
): MviState

sealed class PlaybackState {
    object NotReadyAndStoped: PlaybackState()
    object ReadyAndStoped: PlaybackState()
    object Playing: PlaybackState()
    object Ended: PlaybackState()
    object Buffering: PlaybackState()
    object WithoutVideoSource: PlaybackState()
}