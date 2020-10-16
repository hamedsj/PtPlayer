package me.pitok.options.datasource

import me.pitok.datasource.Writable
import me.pitok.options.Keys
import me.pitok.options.entity.SubtitleOptionsToWriteEntity
import me.pitok.sharedpreferences.StoreModel
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP
import me.pitok.sharedpreferences.typealiases.SpWriter
import javax.inject.Inject

class SubtitleOptionsWriter @Inject constructor(
    @SettingsSP private val settingsWriter: SpWriter
): SubtitleOptionsWriteType {

    override suspend fun write(input: SubtitleOptionsToWriteEntity) {

        when(input){
            is SubtitleOptionsToWriteEntity.FontSizeOption -> {
                settingsWriter.write(
                    StoreModel(Keys.SUBTITLE_FONT_SIZE_KEY,input.size.toString())
                )
            }
        }

    }
}

typealias SubtitleOptionsWriteType =
        Writable.Suspendable<@JvmSuppressWildcards SubtitleOptionsToWriteEntity>