package me.pitok.sharedpreferences

data class StoreModel<T: Any> (val key: String, val value: T)