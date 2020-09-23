package me.pitok.videoplayer.di.components

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.player.di.components.PlayerComponent
import me.pitok.subtitle.di.SubtitleComponent
import me.pitok.videometadata.di.components.VideoMetadataComponent
import me.pitok.videoplayer.di.modules.VideoPlayerViewModelModule
import me.pitok.videoplayer.views.VideoPlayerActivity

@FeatureScope
@Component(
    modules = [
        VideoPlayerViewModelModule::class,
    ],
    dependencies = [
        VideoMetadataComponent::class,
        AndroidCoreComponent::class,
        PlayerComponent::class,
        SubtitleComponent::class
    ]
)
interface VideoPlayerComponent {

    fun bindViewModel(): ViewModelFactory

    fun inject(videoPlayerActivity: VideoPlayerActivity)
}