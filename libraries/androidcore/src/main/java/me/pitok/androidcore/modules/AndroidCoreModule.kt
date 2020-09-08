package me.pitok.androidcore.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import me.pitok.androidcore.qulifiers.ApplicationContext
import me.pitok.androidcore.scopes.AndroidCoreScope

@Module
class AndroidCoreModule{

    @Provides
    @AndroidCoreScope
    @ApplicationContext
    fun provideApplicationContext(application: Application): Context{
        return application
    }


}