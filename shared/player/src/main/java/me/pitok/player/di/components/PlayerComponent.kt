package me.pitok.player.di.components

import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.Component
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.dependencyinjection.shared.SharedScope
import me.pitok.player.di.IndictableSimpleExoPlayer
import me.pitok.player.di.modules.PlayerModule

@SharedScope
@Component(
    modules = [
        PlayerModule::class,
    ],
    dependencies = [
        AndroidCoreComponent::class,
    ]
)
interface PlayerComponent{
    fun exposeDefaultBandwidthMeter(): DefaultBandwidthMeter

    fun exposeDefaultDataSourceFactory(): DefaultDataSourceFactory

    fun exposeExoPlayer(): IndictableSimpleExoPlayer
}