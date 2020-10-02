package me.pitok.videoplayer.states

sealed class PlaybackState: VideoPlayerState() {
    object NotReadyAndStoped: PlaybackState()
    object ReadyAndStoped: PlaybackState()
    object Playing: PlaybackState()
    object Ended: PlaybackState()
    object Buffering: PlaybackState()
    object WithoutVideoSource: PlaybackState()
}