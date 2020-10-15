package me.pitok.settings.states

import me.pitok.mvi.MviState

sealed class SettingsState: MviState{
    class ShowSettedSettings(val defaultPlaybackSpeed: String? = null,
                             val defaultSpeakerVolume: String? = null): SettingsState()
    object ShowPlaybackSpeedMenu: SettingsState()
    class ShowSpeakerVolumeBottomSheet(val defaultSpeakerVolume: Int): SettingsState()
}