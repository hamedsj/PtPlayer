package me.pitok.subtitle.di

import me.pitok.dependencyinjection.ComponentBuilder


object SubtitleComponentBuilder: ComponentBuilder<SubtitleComponent>(){
    override fun initComponent(): SubtitleComponent {
        return DaggerSubtitleComponent.builder().build()
    }
}