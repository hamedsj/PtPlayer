package me.pitok.coroutines.di.builder

import me.pitok.coroutines.di.component.CoroutinesComponent
import me.pitok.coroutines.di.component.DaggerCoroutinesComponent
import me.pitok.coroutines.di.module.DispatchersModule
import me.pitok.dependencyinjection.ComponentBuilder

object CoroutinesComponentBuilder: ComponentBuilder<CoroutinesComponent>() {
    override fun initComponent(): CoroutinesComponent {
        return DaggerCoroutinesComponent
            .builder()
            .dispatchersModule(DispatchersModule())
            .build()
    }
}