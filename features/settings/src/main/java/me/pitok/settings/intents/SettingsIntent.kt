package me.pitok.settings.intents

import me.pitok.mvi.MviIntent

sealed class SettingsIntent: MviIntent {
    object FetchSettedOptions: SettingsIntent()
    object ShowPlaybackSpeedMenuIntent: SettingsIntent()
    object ExitFromSettings: SettingsIntent()
    class SetPlaybackSpeed(val playbackSpeed: Float): SettingsIntent()
}