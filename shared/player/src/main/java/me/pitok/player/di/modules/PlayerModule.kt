package me.pitok.player.di.modules

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.dependencyinjection.shared.SharedScope
import me.pitok.player.di.IndictableSimpleExoPlayer

@Module
class PlayerModule {

    @Provides
    @SharedScope
    fun provideDefaultBandwidthMeter(@ApplicationContext context: Context): DefaultBandwidthMeter{
        return DefaultBandwidthMeter.Builder(context).build()
    }

    @Provides
    @SharedScope
    fun provideDefaultDataSourceFactory(@ApplicationContext context: Context,
                                        defaultBandwidthMeter : DefaultBandwidthMeter
    ): DefaultDataSourceFactory{
        return DefaultDataSourceFactory(context,
            Util.getUserAgent(context, "PtPlayer"),
            defaultBandwidthMeter)
    }

    @Provides
    @SharedScope
    fun provideDefaultHttpDataSourceFactory(
        @ApplicationContext context: Context
    ): DefaultHttpDataSourceFactory{
        return DefaultHttpDataSourceFactory(Util.getUserAgent(context, "PtPlayer"))
    }

    @Provides
    @SharedScope
    fun provideExoPlayer(@ApplicationContext context: Context): IndictableSimpleExoPlayer{
        return IndictableSimpleExoPlayer(context)
    }



}