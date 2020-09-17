package me.pitok.videoplayer.intents

import me.pitok.mvi.MviIntent
import me.pitok.videoplayer.states.PlaybackState

sealed class VideoPlayerIntent(): MviIntent {
    class SetPlayBackState(val playbackState: PlaybackState): VideoPlayerIntent()
}