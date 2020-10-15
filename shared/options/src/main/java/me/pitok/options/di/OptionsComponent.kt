package me.pitok.options.di

import dagger.Component
import me.pitok.dependencyinjection.shared.SharedScope
import me.pitok.options.datasource.PlayerOptionsReadType
import me.pitok.options.datasource.PlayerOptionsWriteType
import me.pitok.sharedpreferences.di.components.SharedPreferencesComponent

@SharedScope
@Component(
    modules = [
        OptionsModule::class,
    ],
    dependencies = [
        SharedPreferencesComponent::class
    ]
)
interface OptionsComponent{

    fun providePlayerOptionsReader(): PlayerOptionsReadType
    fun providePlayerOptionsWriter(): PlayerOptionsWriteType

}