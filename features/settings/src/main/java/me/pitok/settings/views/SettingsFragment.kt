package me.pitok.settings.views

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.design.entity.BottomSheetItemEntity
import me.pitok.design.views.ChooserBottomSheetView
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.mvi.MviView
import me.pitok.navigation.observeNavigation
import me.pitok.settings.R
import me.pitok.settings.di.SettingsComponentBuilder
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import me.pitok.settings.viewmodels.SettingsViewModel
import javax.inject.Inject

class SettingsFragment: Fragment(R.layout.fragment_settings), MviView<SettingsState> {

    companion object{
        const val CLICK_ANIMATION_DURATION = 100L
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @ApplicationContext
    @Inject
    lateinit var applicationContext: Context

    private val settingsViewModel: SettingsViewModel by viewModels { viewModelFactory }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        SettingsComponentBuilder.getComponent().inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.state.observe(viewLifecycleOwner, ::render)
        settingsViewModel.navigationObservable.observeNavigation(this)
        settingsBackIc.setOnClickListener(::onBackClickListener)
        settingsPlaybackSpeedClickable.setOnClickListener(::onPlaybackSpeedOptionClick)
        settingsSpeakerVolumeClickable.setOnClickListener(::onSpeakerVolumeOptionClick)
        settingsScreenOrientationClickable.setOnClickListener(::onScreenOrientationOptionClick)
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.FetchSettedOptions
            )
        }
    }

    private fun onPlaybackSpeedOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowPlaybackSpeedMenuIntent
            )
        }
    }

    private fun onSpeakerVolumeOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowSpeakerVolumeBottomSheetIntent
            )
        }
    }

    private fun onScreenOrientationOptionClick(view: View){
        lifecycleScope.launch {
            delay(CLICK_ANIMATION_DURATION)
            settingsViewModel.intents.send(
                SettingsIntent.ShowScreenOrientationBottomSheetIntent
            )
        }
    }

    private fun onBackClickListener(view: View){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.ExitFromSettings
            )
        }
    }

    override fun render(state: SettingsState) {
        when(state){
            is SettingsState.ShowSettedSettings -> {
                state.defaultPlaybackSpeed?.let {
                    settingsSettedDefaultPlaybackSpeed.text = it
                }
                state.defaultSpeakerVolume?.apply{
                    settingsSettedDefaultSpeakerVolume.text = this
                }?:apply {
                    settingsSettedDefaultSpeakerVolume.text = getString(R.string.device_volume)
                }
                state.defaultScreenOrientation?.apply{
                    settingsSettedDefaultScreenOrientation.text =
                        if (this == SettingsViewModel.LANDSCAPE_ORIENTATION)
                            getString(R.string.landscape)
                        else
                            getString(R.string.portrait)
                }?:apply {
                    settingsSettedDefaultScreenOrientation.text = getString(R.string.landscape)
                }
            }
            is SettingsState.ShowPlaybackSpeedMenu -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.25f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._0_25x,
                            { changePlayBackSpeed(0.25f) }
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.5f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._0_50x,
                            { changePlayBackSpeed(0.5f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 0.75f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._0_75x,
                            { changePlayBackSpeed(0.75f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._1x,
                            { changePlayBackSpeed(1f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.25f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._1_25x,
                            { changePlayBackSpeed(1.25f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.5f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._1_50x,
                            { changePlayBackSpeed(1.5f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 1.75f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._1_75x,
                            { changePlayBackSpeed(1.75f) }
                        ), BottomSheetItemEntity(
                            if (settingsViewModel.defaultPlaybackSpeed == 2f)
                                R.drawable.ic_check
                            else
                                null,
                            R.string._2x,
                            { changePlayBackSpeed(2f) }
                        )
                    )
                    show()
                }
            }
            is SettingsState.ShowSpeakerVolumeBottomSheet -> {
                SpeakerVolumeBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    primaryText = "Set"
                    secondaryText = "CANCEL"
                    defaultSpeakerVolume = settingsViewModel.defaultSpeakerVolume
                    onPrimaryClick = {speakerVolumetoSet ->
                        lifecycleScope.launch {
                            settingsViewModel.intents.send(
                                SettingsIntent.SetSpeakerVolume(speakerVolumetoSet)
                            )
                        }
                        dismiss()
                    }
                    onSecondaryClick = {
                        dismiss()
                    }
                    show()
                }
            }
            is SettingsState.ShowScreenOrientationBottomSheet -> {
                ChooserBottomSheetView(this@SettingsFragment.requireActivity()).apply {
                    sheetTitle = ""
                    sheetItems = listOf(
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultScreenOrientation ==
                                SettingsViewModel.LANDSCAPE_ORIENTATION)
                                R.drawable.ic_check
                            else
                                null,
                            R.string.landscape,
                            { changeScreenOrientation(SettingsViewModel.LANDSCAPE_ORIENTATION) }
                        ),
                        BottomSheetItemEntity(
                            if (settingsViewModel.defaultScreenOrientation ==
                                SettingsViewModel.PORTRAIT_ORIENTATION)
                                R.drawable.ic_check
                            else
                                null,
                            R.string.portrait,
                            { changeScreenOrientation(SettingsViewModel.PORTRAIT_ORIENTATION) }
                        )
                    )
                    show()
                }
            }
        }
    }

    private fun changeScreenOrientation(orientation: Int){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetScreenOrientation(orientation)
            )
        }
    }

    private fun changePlayBackSpeed(speed: Float){
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.SetPlaybackSpeed(speed)
            )
        }
    }


}