package me.pitok.videoplayer.intents

import me.pitok.mvi.MviIntent
import me.pitok.videoplayer.states.PlaybackState

sealed class VideoPlayerIntent(): MviIntent {
    class SetPlayBackState(val playbackState: PlaybackState): VideoPlayerIntent()
    class SendCommand(val command: PlayerControllerCommmand): VideoPlayerIntent()
    class ShowOptions(val OptionsMenu: Int): VideoPlayerIntent()
    class SubtitleProgressChanged(val progress: Long): VideoPlayerIntent()
}

sealed class PlayerControllerCommmand{
    object Next: PlayerControllerCommmand()
    object Previous: PlayerControllerCommmand()
    object Play: PlayerControllerCommmand()
    object Pause: PlayerControllerCommmand()
}