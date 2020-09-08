package me.pitok.coroutines.di.module

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import me.pitok.coroutines.Dispatcher
import me.pitok.dependencyinjection.library.LibraryScope

@Module
class DispatchersModule{

    @Provides
    @LibraryScope
    fun provideDispatcher(): Dispatcher {
        return Dispatcher(Dispatchers.Default,
            Dispatchers.IO,
            Dispatchers.Main)
    }

}