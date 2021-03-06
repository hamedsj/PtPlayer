package me.pitok.videolist.di.builder

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.videolist.di.components.DaggerVideoListComponent
import me.pitok.videolist.di.components.VideoListComponent
import me.pitok.videometadata.di.builder.VideoMetadataComponentBuilder

object VideoListComponentBuilder: ComponentBuilder<VideoListComponent>(){
    override fun initComponent(): VideoListComponent {
        return DaggerVideoListComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .videoMetadataComponent(VideoMetadataComponentBuilder.getComponent())
            .build()
    }

}