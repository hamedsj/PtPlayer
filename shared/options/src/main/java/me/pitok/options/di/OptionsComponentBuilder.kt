package me.pitok.options.di

import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.sharedpreferences.di.builder.SharedPreferencesComponentBuilder

object OptionsComponentBuilder: ComponentBuilder<OptionsComponent>(){
    override fun initComponent(): OptionsComponent {
        return DaggerOptionsComponent.builder()
            .sharedPreferencesComponent(SharedPreferencesComponentBuilder.getComponent())
            .build()
    }
}