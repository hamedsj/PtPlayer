package me.pitok.dealer

import android.content.Context
import android.os.Bundle

interface FragmentDealer {

    fun onAttach(context: Context) {}

    fun onCreate(savedInstanceState: Bundle?) {}
}