package me.pitok.settings.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import me.pitok.lifecycle.SingleLiveData
import me.pitok.lifecycle.update
import me.pitok.mvi.MviModel
import me.pitok.navigation.Navigate
import me.pitok.options.datasource.PlayerOptionsReadType
import me.pitok.options.datasource.PlayerOptionsWriteType
import me.pitok.options.entity.PlayerOptionsToWriteEntity
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SettingsViewModel @Inject constructor(
    private val playerOptionsReader: PlayerOptionsReadType,
    private val playerOptionsWriter: PlayerOptionsWriteType
) : ViewModel(), MviModel<SettingsState, SettingsIntent> {

    override val intents: Channel<SettingsIntent> = Channel(Channel.UNLIMITED)
    private val pState = SingleLiveData<SettingsState>().apply {
        value = SettingsState.ShowSettedSettings()
    }
    override val state: LiveData<SettingsState>
        get() = pState

    private val pNavigationObservable = SingleLiveData<Navigate>()
    val navigationObservable: LiveData<Navigate> = pNavigationObservable

    private var jobRefreshSettedOptions : CoroutineContext? = null

    var defaultPlaybackSpeed = 1f
    private var defaultSpeakerVolume = -1

    init {
        handleIntents()
    }

    private fun handleIntents() {
        GlobalScope.launch(NonCancellable) {
            intents.consumeAsFlow().collect { intent ->
                when(intent){
                    is SettingsIntent.FetchSettedOptions -> {
                        refreshSettedOptions()
                    }
                    is SettingsIntent.ShowPlaybackSpeedMenuIntent -> {
                        withContext(Dispatchers.Main) {
                            pState.update { SettingsState.ShowPlaybackSpeedMenu }
                        }
                    }
                    is SettingsIntent.SetPlaybackSpeed -> {
                        playerOptionsWriter.write(
                            PlayerOptionsToWriteEntity.DefaultPlaybackSpeedOption(
                                intent.playbackSpeed
                            )
                        )
                        defaultPlaybackSpeed = intent.playbackSpeed
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowSettedSettings(
                                    defaultPlaybackSpeed = "${intent.playbackSpeed}x"
                                )
                            }
                        }
                    }
                    is SettingsIntent.ExitFromSettings -> {
                        withContext(Dispatchers.Main){
                            pNavigationObservable.value = Navigate.Up
                        }
                    }
                    is SettingsIntent.ShowSpeakerVolumeBottomSheetIntent -> {
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowSpeakerVolumeBottomSheet(defaultSpeakerVolume)
                            }
                        }
                    }
                    is SettingsIntent.SetSpeakerVolume -> {
                        playerOptionsWriter.write(
                            if (intent.speakerVolume in 0..100) {
                                PlayerOptionsToWriteEntity.DefaultSpeakerVolumeOption(
                                    intent.speakerVolume / 100f
                                )
                            }else{
                                PlayerOptionsToWriteEntity.DefaultSpeakerVolumeOption(
                                    -1f
                                )
                            }
                        )
                        defaultSpeakerVolume = intent.speakerVolume
                        withContext(Dispatchers.Main) {
                            pState.update {
                                intent.speakerVolume.let {
                                    if (it != -1){
                                        SettingsState.ShowSettedSettings(
                                            defaultSpeakerVolume = "$it%"
                                        )
                                    }else{
                                        SettingsState.ShowSettedSettings(
                                            defaultSpeakerVolume = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private fun refreshSettedOptions() {
        jobRefreshSettedOptions = GlobalScope.launch(Dispatchers.IO){
            val settedPlayerOptions = playerOptionsReader.read()
            defaultPlaybackSpeed = settedPlayerOptions.deafultSpeed
            withContext(Dispatchers.Main) {
                pState.update {
                    SettingsState.ShowSettedSettings(
                        defaultPlaybackSpeed = "${settedPlayerOptions.deafultSpeed}x",
                        defaultSpeakerVolume = settedPlayerOptions.defaultSpeakerVolume.let{
                            if (it == -1f) null
                            else "${(settedPlayerOptions.defaultSpeakerVolume*100).toInt()}%"
                        }
                    )
                }
            }
        }
    }

    /**
     *  cause viewmodelScope not working with injected viewModels
     *  we should use GlobalScope and then cancel them in [onCleared()]
     *
     */
    override fun onCleared() {
        jobRefreshSettedOptions?.cancel()
        super.onCleared()
    }
}