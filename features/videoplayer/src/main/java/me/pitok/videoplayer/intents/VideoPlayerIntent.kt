package me.pitok.videoplayer.intents

import me.pitok.mvi.MviIntent
import me.pitok.options.entity.PlayerOptionsEntity
import me.pitok.options.entity.SubtitleOptionsEntity
import me.pitok.videoplayer.states.PlaybackState

sealed class VideoPlayerIntent(): MviIntent {
    object SetInitialConfigsIntent: VideoPlayerIntent()
    class SetPlayBackState(val playbackState: PlaybackState): VideoPlayerIntent()
    class SendCommand(val command: PlayerControllerCommmand): VideoPlayerIntent()
    class ShowOptions(val OptionsMenu: Int): VideoPlayerIntent()
    class LoadSubtitle(val path: String): VideoPlayerIntent()
    object RemoveSubtitle: VideoPlayerIntent()
    class SubtitleProgressChanged(val progress: Long): VideoPlayerIntent()
}

sealed class PlayerControllerCommmand{
    object Next: PlayerControllerCommmand()
    object Previous: PlayerControllerCommmand()
    object Play: PlayerControllerCommmand()
    object Pause: PlayerControllerCommmand()
    object Prepare: PlayerControllerCommmand()
    class ChangePlaybackSpeed(val spped: Float): PlayerControllerCommmand()
}