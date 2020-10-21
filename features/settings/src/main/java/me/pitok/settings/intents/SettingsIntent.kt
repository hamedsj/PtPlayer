package me.pitok.settings.intents

import me.pitok.mvi.MviIntent

sealed class SettingsIntent: MviIntent {
    object FetchSettedOptions: SettingsIntent()
    object ShowPlaybackSpeedMenuIntent: SettingsIntent()
    object ShowSpeakerVolumeBottomSheetIntent: SettingsIntent()
    object ShowScreenOrientationBottomSheetIntent: SettingsIntent()
    object ShowSubtitleFontSizeBottomSheetIntent: SettingsIntent()
    object ShowSubtitleTextColorBottomSheetIntent: SettingsIntent()
    object ShowSubtitleHighlightColorBottomSheetIntent: SettingsIntent()
    object ExitFromSettings: SettingsIntent()
    class SetPlaybackSpeed(val playbackSpeed: Float): SettingsIntent()
    class SetSpeakerVolume(val speakerVolume: Int): SettingsIntent()
    class SetScreenOrientation(val screenOrientation: Int): SettingsIntent()
    class SetSubtitleFontSize(val size: Int): SettingsIntent()
    class SetSubtitleTextColor(val color: Int): SettingsIntent()
    class SetSubtitleHighlightColor(val color: Int): SettingsIntent()
}