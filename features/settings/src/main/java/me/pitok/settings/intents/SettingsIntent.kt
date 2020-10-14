package me.pitok.settings.intents

import me.pitok.mvi.MviIntent

sealed class SettingsIntent: MviIntent {
    object FetchSettedOptions: SettingsIntent()
}