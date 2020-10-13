package me.pitok.settings.di

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.settings.views.SettingsFragment

@FeatureScope
@Component(
    modules = [
        SettingsViewModelModule::class,
    ],
    dependencies = [
        AndroidCoreComponent::class
    ]
)
interface SettingsComponent {

    fun bindViewModel(): ViewModelFactory

    fun inject(videoListFragment: SettingsFragment)
}