package me.pitok.videolist.di.modules

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.videolist.datasource.VideoFoldersReadType
import me.pitok.videolist.datasource.VideoFoldersReader

@Module
interface VideoListDataSourceModule {

    @Binds
    @FeatureScope
    fun getVideoFolderReader(videoFoldersReader: VideoFoldersReader): VideoFoldersReadType

}