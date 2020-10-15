package me.pitok.settings.states

import me.pitok.mvi.MviState

sealed class SettingsState: MviState{
    class ShowSettedSettings(val defaultPlaybackSpeed: String = ""): SettingsState()
    object ShowPlaybackSpeedMenu: SettingsState()
}