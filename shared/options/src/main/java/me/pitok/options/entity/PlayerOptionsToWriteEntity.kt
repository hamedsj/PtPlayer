package me.pitok.options.entity

sealed class PlayerOptionsToWriteEntity{
    class DefaultPlaybackSpeedOption(val deafultSpeed: Float): PlayerOptionsToWriteEntity()
    class DefaultSpeakerVolumeOption(val defaultSpeakerVolume: Float): PlayerOptionsToWriteEntity()
    class DefaultLayoutOrientationOption(val orientation: Int): PlayerOptionsToWriteEntity()
}