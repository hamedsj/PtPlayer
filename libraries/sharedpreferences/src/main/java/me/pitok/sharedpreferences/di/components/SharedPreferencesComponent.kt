package me.pitok.sharedpreferences.di.components

import android.content.SharedPreferences
import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.library.LibraryScope
import me.pitok.sharedpreferences.di.modules.SharedPreferencesModule
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP
import me.pitok.sharedpreferences.typealiases.SpReader
import me.pitok.sharedpreferences.typealiases.SpWriter

@LibraryScope
@Component(modules = [SharedPreferencesModule::class], dependencies = [AndroidCoreComponent::class])
interface SharedPreferencesComponent {

    @SettingsSP
    fun provideSettingsSharedPreferences(): SharedPreferences

    @SettingsSP
    fun provideSettingsSharedPreferencesEditor(): SharedPreferences.Editor


    @SettingsSP
    fun provideSettingsReaderImpl(): SpReader

    @SettingsSP
    fun provideSettingsWriterImpl(): SpWriter

}