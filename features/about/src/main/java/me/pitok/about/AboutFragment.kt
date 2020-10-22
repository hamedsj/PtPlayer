package me.pitok.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pitok.lifecycle.SingleLiveData
import me.pitok.navigation.Navigate
import me.pitok.navigation.observeNavigation

class AboutFragment: Fragment(R.layout.fragment_about) {

    companion object{
        const val CLICK_ANIMATION_DURATION = 100L
    }

    private val navigationObservable = SingleLiveData<Navigate>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationObservable.observeNavigation(this@AboutFragment)
        aboutBackIc.setOnClickListener(::onBackClickListener)
        aboutRepoBt.setOnClickListener(::onRepoClickListener)
    }
    private fun onBackClickListener(view: View){
        lifecycleScope.launch{
            delay(CLICK_ANIMATION_DURATION)
            withContext(Dispatchers.Main){
                navigationObservable.value = Navigate.Up
            }
        }
    }
    private fun onRepoClickListener(view: View){
        lifecycleScope.launch{
            delay(CLICK_ANIMATION_DURATION)
            withContext(Dispatchers.Main){
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://github.com/hamedsj/PtPlayer")
                    startActivity(this)
                }
            }
        }
    }
}