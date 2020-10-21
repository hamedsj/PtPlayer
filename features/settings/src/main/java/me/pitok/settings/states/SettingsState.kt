package me.pitok.settings.states

import me.pitok.mvi.MviState

sealed class SettingsState: MviState{
    class ShowSettedSettings(val defaultPlaybackSpeed: String? = null,
                             val defaultSpeakerVolume: String? = null,
                             val defaultScreenOrientation: Int? = null,
                             val subtitleFontSize: Int? = null,
                             val subtitleTextColor: Int? = null): SettingsState()
    object ShowPlaybackSpeedMenu: SettingsState()
    object ShowSpeakerVolumeBottomSheet: SettingsState()
    object ShowScreenOrientationBottomSheet: SettingsState()
    object ShowSubtitleFontSizeBottomSheet: SettingsState()
    object ShowSubtitleTextColorBottomSheet: SettingsState()
}