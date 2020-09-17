package me.pitok.videometadata.di.modules

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.library.LibraryScope
import me.pitok.videometadata.datasource.FolderVideosReadType
import me.pitok.videometadata.datasource.FolderVideosReader
import me.pitok.videometadata.datasource.VideoFoldersReadType
import me.pitok.videometadata.datasource.VideoFoldersReader

@Module
interface VideoMetadataDataSourceModule {

    @Binds
    @LibraryScope
    fun getVideoFolderReader(videoFoldersReader: VideoFoldersReader): VideoFoldersReadType

    @Binds
    @LibraryScope
    fun getFolderVideosReader(folderVideosReader: FolderVideosReader): FolderVideosReadType

}