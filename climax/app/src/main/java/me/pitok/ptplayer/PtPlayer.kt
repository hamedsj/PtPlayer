package me.pitok.ptplayer

import android.app.Application
import me.pitok.androidcore.builder.AndroidCoreComponentBuilder

class PtPlayer : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidCoreComponentBuilder.application = this
    }
}