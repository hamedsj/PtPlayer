package me.pitok.settings.states

import me.pitok.mvi.MviState

sealed class SettingsState: MviState{
    class ShowSettedSettings(val defaultPlaybackSpeed: String = "",
                             val defaultSpeakerVolume: String = ""): SettingsState()
    object ShowPlaybackSpeedMenu: SettingsState()
}