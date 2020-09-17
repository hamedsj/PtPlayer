package me.pitok.videoplayer.di.modules

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import me.pitok.dependencyinjection.feature.FeatureScope
import me.pitok.lifecycle.ViewModelFactory
import me.pitok.lifecycle.ViewModelKey
import me.pitok.lifecycle.ViewModelProviders
import me.pitok.videoplayer.viewmodels.VideoPlayerViewModel

@Module
interface VideoPlayerViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(VideoPlayerViewModel::class)
    @FeatureScope
    fun bindViewModel(viewModel: VideoPlayerViewModel): ViewModel


    companion object {

        @Provides
        @FeatureScope
        fun provideViewModelFactory(viewModelProviders: ViewModelProviders): ViewModelFactory {
            return ViewModelFactory(viewModelProviders)
        }

    }

}