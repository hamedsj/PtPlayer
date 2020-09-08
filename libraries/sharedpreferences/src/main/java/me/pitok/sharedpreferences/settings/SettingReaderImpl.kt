package me.pitok.sharedpreferences.settings

import android.content.SharedPreferences
import me.pitok.sharedpreferences.typealiases.SpReader
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP

class SettingReaderImpl constructor(@SettingsSP private val spSettings: SharedPreferences): SpReader{

    override fun read(input: String): String {
        return requireNotNull(spSettings.getString(input,""))
    }

}