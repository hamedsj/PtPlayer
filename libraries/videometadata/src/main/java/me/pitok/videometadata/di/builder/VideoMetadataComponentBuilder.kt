package me.pitok.videometadata.di.builder

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.videometadata.di.components.DaggerVideoMetadataComponent
import me.pitok.videometadata.di.components.VideoMetadataComponent
import me.pitok.videometadata.di.modules.VideoMetadataModule

object VideoMetadataComponentBuilder: ComponentBuilder<VideoMetadataComponent>(){
    override fun initComponent(): VideoMetadataComponent {
        return DaggerVideoMetadataComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .videoMetadataModule(VideoMetadataModule())
            .build()
    }
}