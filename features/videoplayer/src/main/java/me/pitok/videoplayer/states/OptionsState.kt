package me.pitok.videoplayer.states

sealed class OptionsState: VideoPlayerState() {
    object ShowMainMenu: OptionsState()
    object ShowSubtitleMenu: OptionsState()
    object ShowPlaybackSpeedMenu: OptionsState()
    object ShowAudioMenu: OptionsState()
}