package me.pitok.videometadata.di

import android.media.MediaMetadataRetriever
import coil.ImageLoader
import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.library.LibraryScope

@LibraryScope
@Component(
    modules = [VideoMetadataModule::class],
    dependencies = [AndroidCoreComponent::class]
)
interface VideoMetadataComponent {
    fun exposeMMDR(): MediaMetadataRetriever

    fun exposeCoilVideoImageLoader(): ImageLoader
}