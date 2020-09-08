package me.pitok.dependencyinjection

abstract class ComponentBuilder<Component: Any> {

    private var component: Component? = null

    fun getComponent(): Component{
        if (component == null){
            component = initComponent()
        }
        return requireNotNull(component)
    }

    abstract fun initComponent(): Component

}