package me.pitok.videometadata.di.modules

import android.content.Context
import android.media.MediaMetadataRetriever
import coil.ImageLoader
import coil.fetch.VideoFrameFileFetcher
import dagger.Module
import dagger.Provides
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.dependencyinjection.library.LibraryScope

@Module
class VideoMetadataModule {

    @Provides
    @LibraryScope
    fun provideCoilVideoImageLoader(@ApplicationContext context:Context): ImageLoader {
        return ImageLoader.Builder(context).componentRegistry{
            add(VideoFrameFileFetcher(context))
        }
            .build()
    }


    @Provides
    @LibraryScope
    fun provideMMDR(): MediaMetadataRetriever {
        return MediaMetadataRetriever()
    }


}