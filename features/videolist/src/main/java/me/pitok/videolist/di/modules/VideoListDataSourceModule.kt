package me.pitok.videolist.di.modules

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.videolist.datasource.*

@Module
interface VideoListDataSourceModule {

    @Binds
    @FeatureScope
    fun getVideoFolderReader(videoFoldersReader: VideoFoldersReader): VideoFoldersReadType

    @Binds
    @FeatureScope
    fun getFolderVideosReader(folderVideosReader: FolderVideosReader): FolderVideosReadType

}