package me.pitok.ptplayer.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import me.pitok.ptplayer.R

class MainActivity : AppCompatActivity() {

    companion object{
        const val NAV_HOST_TAG = "ptplayer_main_nav_host"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null){
            setupNavigation()
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation(){
        if (supportFragmentManager.findFragmentByTag(NAV_HOST_TAG) != null){
            val host = supportFragmentManager
                .findFragmentByTag(NAV_HOST_TAG) as NavHostFragment
            supportFragmentManager
                .beginTransaction()
                .attach(host)
                .setPrimaryNavigationFragment(host)
                .commit()
        }else{
            val host = NavHostFragment.create(R.navigation.navigation_app_graph)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.mainActivityFrameLayout, host, NAV_HOST_TAG)
                .setPrimaryNavigationFragment(host)
                .commit()
        }
    }
}