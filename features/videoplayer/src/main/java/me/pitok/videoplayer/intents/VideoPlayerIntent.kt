package me.pitok.videoplayer.intents

import me.pitok.mvi.MviIntent
import me.pitok.videoplayer.states.VideoPlayerState.Companion.PlaybackStatus

sealed class VideoPlayerIntent : MviIntent {
    object SetInitialConfigsIntent : VideoPlayerIntent()
    class SetPlayBackState(val playbackStatus: PlaybackStatus) : VideoPlayerIntent()
    class SendCommand(val command: PlayerControllerCommand) : VideoPlayerIntent()
    class ShowOptions(val OptionsMenu: Int) : VideoPlayerIntent()
    class LoadSubtitle(val path: String) : VideoPlayerIntent()
    object RemoveSubtitle : VideoPlayerIntent()
    class SubtitleProgressChanged(val progress: Long) : VideoPlayerIntent()
    class VideoSizeChanged(val width: Int, val height: Int) : VideoPlayerIntent()
}

sealed class PlayerControllerCommand {
    object Next : PlayerControllerCommand()
    object Previous : PlayerControllerCommand()
    object Play : PlayerControllerCommand()
    object Pause : PlayerControllerCommand()
    object Prepare : PlayerControllerCommand()
    class ChangePlaybackSpeed(val speed: Float) : PlayerControllerCommand()
}