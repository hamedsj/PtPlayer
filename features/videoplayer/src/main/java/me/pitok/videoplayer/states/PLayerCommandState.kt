package me.pitok.videoplayer.states

import com.google.android.exoplayer2.source.MediaSource

sealed class PLayerCommandState: VideoPlayerState() {
    object Pause: PLayerCommandState()
    object Start: PLayerCommandState()
    class Prepare(val mediaSource: MediaSource): PLayerCommandState()
    class SeekToPosition(val position : Long): PLayerCommandState()
    class ChangeSpeed(val speed: Float): PLayerCommandState()
}