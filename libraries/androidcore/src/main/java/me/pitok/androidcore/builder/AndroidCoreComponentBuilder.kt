package me.pitok.androidcore.builder

import android.app.Application
import me.pitok.androidcore.components.AndroidCoreComponent
import me.pitok.androidcore.components.DaggerAndroidCoreComponent
import me.pitok.dependencyinjection.ComponentBuilder

object AndroidCoreComponentBuilder: ComponentBuilder<AndroidCoreComponent>() {
    lateinit var application: Application
    override fun initComponent(): AndroidCoreComponent {
        return DaggerAndroidCoreComponent
            .builder()
            .bindApplication(application = application)
            .build()
    }
}