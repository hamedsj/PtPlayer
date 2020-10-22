package me.pitok.videoplayer.di.builder

import me.pitok.androidcore.builder.AndroidCoreComponentBuilder
import me.pitok.dependencyinjection.ComponentBuilder
import me.pitok.options.di.OptionsComponentBuilder
import me.pitok.player.di.builder.PlayerComponentBuilder
import me.pitok.subtitle.di.SubtitleComponentBuilder
import me.pitok.videometadata.di.builder.VideoMetadataComponentBuilder
import me.pitok.videoplayer.di.components.DaggerVideoPlayerComponent
import me.pitok.videoplayer.di.components.VideoPlayerComponent


object VideoPlayerComponentBuilder: ComponentBuilder<VideoPlayerComponent>(){
    override fun initComponent(): VideoPlayerComponent {
        return DaggerVideoPlayerComponent.builder()
            .androidCoreComponent(AndroidCoreComponentBuilder.getComponent())
            .playerComponent(PlayerComponentBuilder.getComponent())
            .videoMetadataComponent(VideoMetadataComponentBuilder.getComponent())
            .subtitleComponent(SubtitleComponentBuilder.getComponent())
            .optionsComponent(OptionsComponentBuilder.getComponent())
            .build()
    }
}