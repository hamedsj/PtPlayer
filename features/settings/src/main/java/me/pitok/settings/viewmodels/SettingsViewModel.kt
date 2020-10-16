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
import me.pitok.options.datasource.SubtitleOptionsReadType
import me.pitok.options.datasource.SubtitleOptionsWriteType
import me.pitok.options.entity.PlayerOptionsToWriteEntity
import me.pitok.options.entity.SubtitleOptionsToWriteEntity
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class SettingsViewModel @Inject constructor(
    private val playerOptionsReader: PlayerOptionsReadType,
    private val playerOptionsWriter: PlayerOptionsWriteType,
    private val subtitleOptionsReader: SubtitleOptionsReadType,
    private val subtitleOptionsWriter: SubtitleOptionsWriteType
) : ViewModel(), MviModel<SettingsState, SettingsIntent> {

    companion object{
        const val PORTRAIT_ORIENTATION = 0
        const val LANDSCAPE_ORIENTATION = 1
    }

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
    var defaultSpeakerVolume = -1
    var defaultScreenOrientation = LANDSCAPE_ORIENTATION
    var subtitleFontSize = 18

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
                        handleSetPlaybackSpeedIntent(intent)
                    }
                    is SettingsIntent.ExitFromSettings -> {
                        withContext(Dispatchers.Main){
                            pNavigationObservable.value = Navigate.Up
                        }
                    }
                    is SettingsIntent.ShowSpeakerVolumeBottomSheetIntent -> {
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowSpeakerVolumeBottomSheet
                            }
                        }
                    }
                    is SettingsIntent.ShowScreenOrientationBottomSheetIntent -> {
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowScreenOrientationBottomSheet
                            }
                        }
                    }
                    is SettingsIntent.ShowSubtitleFontSizeBottomSheetIntent -> {
                        withContext(Dispatchers.Main) {
                            pState.update {
                                SettingsState.ShowSubtitleFontSizeBottomSheet
                            }
                        }
                    }
                    is SettingsIntent.SetSpeakerVolume -> {
                        handleSetSpeakerVolumeIntent(intent)
                    }
                    is SettingsIntent.SetScreenOrientation -> {
                        handleSetScreenOrientationIntent(intent)
                    }
                    is SettingsIntent.SetSubtitleFontSize -> {
                        handleSetSubtitleFontSizeIntent(intent)
                    }
                }

            }
        }
    }

    private fun refreshSettedOptions() {
        jobRefreshSettedOptions = GlobalScope.launch(Dispatchers.IO){
            val settedPlayerOptions = playerOptionsReader.read()
            val settedSubtitleOptions = subtitleOptionsReader.read()
            defaultPlaybackSpeed = settedPlayerOptions.deafultSpeed
            defaultSpeakerVolume =
                if (settedPlayerOptions.defaultSpeakerVolume == -1f)
                    -1
                else
                    (settedPlayerOptions.defaultSpeakerVolume*100).toInt()
            defaultScreenOrientation =
                if (settedPlayerOptions.landscape) LANDSCAPE_ORIENTATION
                else PORTRAIT_ORIENTATION
            settedSubtitleOptions.fontSize?.let {
                subtitleFontSize = it
            }?:let {
                subtitleFontSize = 18
            }
            withContext(Dispatchers.Main) {
                pState.update {
                    SettingsState.ShowSettedSettings(
                        defaultPlaybackSpeed = "${defaultPlaybackSpeed}x",
                        defaultSpeakerVolume = defaultSpeakerVolume.let{
                            if (it == -1) null
                            else "$defaultSpeakerVolume%"
                        },
                        defaultScreenOrientation = defaultScreenOrientation,
                        subtitleFontSize = subtitleFontSize
                    )
                }
            }
        }
    }

    private suspend fun handleSetSpeakerVolumeIntent(intent: SettingsIntent.SetSpeakerVolume){
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

    private suspend fun handleSetPlaybackSpeedIntent(intent: SettingsIntent.SetPlaybackSpeed){
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

    private suspend fun handleSetScreenOrientationIntent(intent: SettingsIntent.SetScreenOrientation){
        playerOptionsWriter.write(
            PlayerOptionsToWriteEntity.DefaultLayoutOrientationOption(
                intent.screenOrientation == LANDSCAPE_ORIENTATION
            )
        )
        defaultScreenOrientation = intent.screenOrientation
        withContext(Dispatchers.Main) {
            pState.update {
                SettingsState.ShowSettedSettings(
                    defaultScreenOrientation = intent.screenOrientation
                )
            }
        }
    }

    private suspend fun handleSetSubtitleFontSizeIntent(intent: SettingsIntent.SetSubtitleFontSize){
        subtitleOptionsWriter.write(
            SubtitleOptionsToWriteEntity.FontSizeOption(intent.size)
        )
        subtitleFontSize = intent.size
        withContext(Dispatchers.Main) {
            pState.update {
                SettingsState.ShowSettedSettings(
                    subtitleFontSize = intent.size
                )
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