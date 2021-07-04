package me.pitok.sdkextentions

class SingleEvent<out T>(val value: T) {
    private var hasBeenHandled: Boolean = false
    fun ifNotHandled(runnable: (T) -> Unit) {
        if (this.hasBeenHandled.not()) {
            hasBeenHandled = true
            runnable.invoke(value)
        }
    }

    override fun toString(): String {
        return "SingleEvent(${hasBeenHandled},$value)"
    }
}

typealias EmptySingleEvent = SingleEvent<EmptyEntity>