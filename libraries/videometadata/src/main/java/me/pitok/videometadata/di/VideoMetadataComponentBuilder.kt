package me.pitok.videometadata.di

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder

object VideoMetadataComponentBuilder: ComponentBuilder<VideoMetadataComponent>(){
    override fun initComponent(): VideoMetadataComponent {
        return DaggerVideoMetadataComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .videoMetadataModule(VideoMetadataModule())
            .build()
    }

}