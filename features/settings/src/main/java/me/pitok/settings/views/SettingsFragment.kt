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
                settingsSettedDefaultPlaybackSpeed.text = state.defaultPlaybackSpeed
                settingsSettedDefaultSpeakerVolume.text = state.defaultSpeakerVolume
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