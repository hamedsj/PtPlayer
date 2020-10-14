package me.pitok.settings.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import me.pitok.lifecycle.update
import me.pitok.mvi.MviModel
import me.pitok.options.datasource.PlayerOptionsReadType
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import javax.inject.Inject


class SettingsViewModel @Inject constructor(
    private val playerOptionsReader: PlayerOptionsReadType
) : ViewModel(), MviModel<SettingsState, SettingsIntent> {

    override val intents: Channel<SettingsIntent> = Channel(Channel.UNLIMITED)
    private val pState = MutableLiveData<SettingsState>().apply {
        value = SettingsState.ShowSettedSettings()
    }
    override val state: LiveData<SettingsState>
        get() = pState

    init {
        handleIntents()
    }

    private fun handleIntents() {
        GlobalScope.launch(NonCancellable) {
            intents.consumeAsFlow().collect { intent ->
                when(intent){
                    is SettingsIntent.FetchSettedOptions -> {
                        val settedPlayerOptions = playerOptionsReader.read()
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowSettedSettings(
                                    defaultPlaybackSpeed = "${settedPlayerOptions.deafultSpeed.toInt()}x"
                                )
                            }
                        }
                    }
                }

            }
        }
    }

}