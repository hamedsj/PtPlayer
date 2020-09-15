package me.pitok.videolist.di.components

import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.videolist.datasource.FolderVideosReadType
import me.pitok.videolist.datasource.VideoFoldersReadType
import me.pitok.videolist.di.modules.VideoListDataSourceModule
import me.pitok.videolist.di.modules.VideoListViewModelModule
import me.pitok.videolist.views.VideoListFragment
import me.pitok.videometadata.di.VideoMetadataComponent

@FeatureScope
@Component(
    modules = [
        VideoListViewModelModule::class,
        VideoListDataSourceModule::class,
    ],
    dependencies = [
        AndroidCoreComponent::class,
        VideoMetadataComponent::class
    ]
)
interface VideoListComponent {

    fun bindViewModel(): ViewModelFactory

    fun exposeVideoFolderReader(): VideoFoldersReadType

    fun exposeFolderVideosReader(): FolderVideosReadType

    fun inject(videoListFragment: VideoListFragment)
}