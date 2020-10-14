package me.pitok.settings.views

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.mvi.MviView
import me.pitok.settings.R
import me.pitok.settings.di.SettingsComponentBuilder
import me.pitok.settings.intents.SettingsIntent
import me.pitok.settings.states.SettingsState
import me.pitok.settings.viewmodels.SettingsViewModel
import javax.inject.Inject

class SettingsFragment: Fragment(R.layout.fragment_settings), MviView<SettingsState> {

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
        settingsBackIc.setOnClickListener(::onBackClickListener)
        lifecycleScope.launch {
            settingsViewModel.intents.send(
                SettingsIntent.FetchSettedOptions
            )
        }
    }

    private fun onBackClickListener(view: View){

    }

    override fun render(state: SettingsState) {
        when(state){
            is SettingsState.ShowSettedSettings -> {
                settingsSettedDefaultPlaybackSpeed.text = state.defaultPlaybackSpeed
            }
        }
    }


}