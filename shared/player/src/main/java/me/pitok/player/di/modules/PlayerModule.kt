package me.pitok.player.di.modules

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.dependencyinjection.shared.SharedScope

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
    fun provideExoPlayer(@ApplicationContext context: Context): SimpleExoPlayer{
        return SimpleExoPlayer.Builder(context).build()
    }



}