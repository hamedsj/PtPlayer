package me.pitok.coroutines.di.component

import dagger.Component
import me.pitok.coroutines.Dispatcher
import me.pitok.coroutines.di.module.DispatchersModule
import me.pitok.dependencyinjection.library.LibraryScope

@LibraryScope
@Component(modules = [DispatchersModule::class])
interface CoroutinesComponent{

    fun exposeDispatcher(): Dispatcher
}