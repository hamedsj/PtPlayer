package me.pitok.coroutines

import kotlinx.coroutines.CoroutineDispatcher

data class Dispatcher(val default: CoroutineDispatcher,
                      val io: CoroutineDispatcher,
                      val main: CoroutineDispatcher)