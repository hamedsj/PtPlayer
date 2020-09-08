package me.pitok.lifecycle

import androidx.lifecycle.MutableLiveData

fun <T : Any> MutableLiveData<T>.update(block: T.() -> T) {
    value = block.invoke(requireNotNull(value))
}