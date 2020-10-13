package me.pitok.settings.di

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder

object SettingsComponentBuilder: ComponentBuilder<SettingsComponent>(){
    override fun initComponent(): SettingsComponent {
        return DaggerSettingsComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .build()
    }

}