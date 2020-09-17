package me.pitok.videolist.di.components

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.videolist.di.modules.VideoListViewModelModule
import me.pitok.videolist.views.VideoListFragment
import me.pitok.videometadata.di.components.VideoMetadataComponent

@FeatureScope
@Component(
    modules = [
        VideoListViewModelModule::class,
    ],
    dependencies = [
        AndroidCoreComponent::class,
        VideoMetadataComponent::class
    ]
)
interface VideoListComponent {

    fun bindViewModel(): ViewModelFactory

    fun inject(videoListFragment: VideoListFragment)
}