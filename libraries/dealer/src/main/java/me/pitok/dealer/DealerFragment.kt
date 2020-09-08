package me.pitok.dealer

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class DealerFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes layout: Int) : super(layout)

    private val fragmentPlugins by lazy{ plugins() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentPlugins.forEach { plugin ->
            plugin.onAttach(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentPlugins.forEach { plugin ->
            plugin.onCreate(savedInstanceState)
        }
    }

    open fun plugins(): MutableList<FragmentDealer> = mutableListOf()
}