package me.pitok.settings.intents

import me.pitok.mvi.MviIntent

sealed class SettingsIntent: MviIntent {
    object FetchSettedOptions: SettingsIntent()
    object ShowPlaybackSpeedMenuIntent: SettingsIntent()
    object ShowSpeakerVolumeBottomSheetIntent: SettingsIntent()
    object ShowScreenOrientationBottomSheetIntent: SettingsIntent()
    object ExitFromSettings: SettingsIntent()
    class SetPlaybackSpeed(val playbackSpeed: Float): SettingsIntent()
    class SetSpeakerVolume(val speakerVolume: Int): SettingsIntent()
    class SetScreenOrientation(val screenOrientation: Int): SettingsIntent()
}