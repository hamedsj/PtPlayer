package me.pitok.videometadata.di.components

import android.media.MediaMetadataRetriever
import coil.ImageLoader
import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.library.LibraryScope
import me.pitok.videometadata.datasource.FolderVideosReadType
import me.pitok.videometadata.datasource.VideoFoldersReadType
import me.pitok.videometadata.di.modules.VideoMetadataDataSourceModule
import me.pitok.videometadata.di.modules.VideoMetadataModule

@LibraryScope
@Component(
    modules = [
        VideoMetadataModule::class,
        VideoMetadataDataSourceModule::class
    ],
    dependencies = [AndroidCoreComponent::class]
)
interface VideoMetadataComponent {
    fun exposeMMDR(): MediaMetadataRetriever

    fun exposeVideoFolderReader(): VideoFoldersReadType

    fun exposeFolderVideosReader(): FolderVideosReadType

    fun exposeCoilVideoImageLoader(): ImageLoader
}