package me.pitok.settings.di

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.options.di.OptionsComponentBuilder

object SettingsComponentBuilder: ComponentBuilder<SettingsComponent>(){
    override fun initComponent(): SettingsComponent {
        return DaggerSettingsComponent.builder()
            .optionsComponent(OptionsComponentBuilder.getComponent())
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .build()
    }

}