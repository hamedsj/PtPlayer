package me.pitok.videoplayer.states

sealed class OptionsState: VideoPlayerState() {
    class ChangeOrientation(val orientation: Int): OptionsState()
    object ShowMainMenu: OptionsState()
    object ShowSubtitleMenu: OptionsState()
    object ShowPlaybackSpeedMenu: OptionsState()
    object ShowAudioMenu: OptionsState()
}