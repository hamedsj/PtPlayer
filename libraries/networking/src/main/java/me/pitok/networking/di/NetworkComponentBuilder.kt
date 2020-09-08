package me.pitok.networking.di

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.networking.di.components.DaggerNetworkComponent
import me.pitok.networking.di.components.NetworkComponent
import me.pitok.networking.di.modules.NetworkModule
import me.pitok.sharedpreferences.di.builder.SharedPreferencesComponentBuilder

object NetworkComponentBuilder: ComponentBuilder<NetworkComponent>() {
    override fun initComponent(): NetworkComponent {
        return DaggerNetworkComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .sharedPreferencesComponent(SharedPreferencesComponentBuilder.getComponent())
            .networkModule(NetworkModule())
            .build()
    }
}