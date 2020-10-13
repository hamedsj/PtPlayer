package me.pitok.settings.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.lifecycle.ViewModelKey
import me.pitok.lifecycle.ViewModelProviders
import me.pitok.settings.viewmodels.SettingsViewModel

@Module
interface SettingsViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    @FeatureScope
    fun bindViewModel(viewModel: SettingsViewModel): ViewModel


    companion object {

        @Provides
        @FeatureScope
        fun provideViewModelFactory(viewModelProviders: ViewModelProviders): ViewModelFactory {
            return ViewModelFactory(viewModelProviders)
        }

    }

}