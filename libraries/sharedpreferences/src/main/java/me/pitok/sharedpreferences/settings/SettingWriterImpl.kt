package me.pitok.sharedpreferences.settings

import android.content.SharedPreferences
import me.pitok.sharedpreferences.StoreModel
import me.pitok.sharedpreferences.typealiases.SpWriter
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP

class SettingWriterImpl constructor(@SettingsSP private val settingsEditor: SharedPreferences.Editor): SpWriter{
    override fun write(input: StoreModel<String>) {
        settingsEditor.putString(input.key,input.value).run{
            commit()
        }
    }
}