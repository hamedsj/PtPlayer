package me.pitok.lifecycle

import androidx.lifecycle.ViewModel
import javax.inject.Provider

typealias ViewModelProviders = Map<@JvmSuppressWildcards Class<out ViewModel>,
        @JvmSuppressWildcards Provider<ViewModel>>