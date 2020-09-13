package me.pitok.videolist.di.components

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.videolist.datasource.VideoFoldersReadType
import me.pitok.videolist.di.modules.VideoListDataSourceModule
import me.pitok.videolist.di.modules.VideoListViewModelModule
import me.pitok.videolist.views.VideoListFragment

@FeatureScope
@Component(
    modules = [
        VideoListViewModelModule::class,
        VideoListDataSourceModule::class,
    ],
    dependencies = [AndroidCoreComponent::class]
)
interface VideoListComponent {

    fun bindViewModel(): ViewModelFactory

    fun provideVideoFolderReader(): VideoFoldersReadType

    fun inject(videoListFragment: VideoListFragment)
}