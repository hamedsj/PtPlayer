package me.pitok.player.di.builder

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.player.di.components.DaggerPlayerComponent
import me.pitok.player.di.components.PlayerComponent
import me.pitok.player.di.modules.PlayerModule


object PlayerComponentBuilder: ComponentBuilder<PlayerComponent>(){
    override fun initComponent(): PlayerComponent {
        return DaggerPlayerComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .playerModule(PlayerModule())
            .build()
    }
}