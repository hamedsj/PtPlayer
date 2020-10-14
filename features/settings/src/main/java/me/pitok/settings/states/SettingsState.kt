package me.pitok.settings.states

import me.pitok.mvi.MviState

sealed class SettingsState: MviState{
    data class ShowSettedSettings(
        val defaultPlaybackSpeed: String = ""
    ): SettingsState()
}