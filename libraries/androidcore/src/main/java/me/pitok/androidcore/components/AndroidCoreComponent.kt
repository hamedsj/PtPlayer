package me.pitok.androidcore.components

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import me.pitok.androidcore.modules.AndroidCoreModule
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.androidcore.scopes.AndroidCoreScope

@AndroidCoreScope
@Component(modules = [AndroidCoreModule::class])
interface AndroidCoreComponent {

    @Component.Builder
    interface Builder{
        fun build(): AndroidCoreComponent

        @BindsInstance
        fun bindApplication(application: Application): Builder
    }

    fun exposeApplication(): Application

    @ApplicationContext
    fun exposeApplicationContext(): Context

}