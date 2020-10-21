package me.pitok.options.datasource

import me.pitok.datasource.Readable
import me.pitok.options.Keys
import me.pitok.options.entity.SubtitleOptionsEntity
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP
import me.pitok.sharedpreferences.typealiases.SpReader
import javax.inject.Inject

class SubtitleOptionsReader @Inject constructor(
    @SettingsSP private val settingsReader: SpReader
): SubtitleOptionsReadType {
    override suspend fun read(): SubtitleOptionsEntity {
        return SubtitleOptionsEntity(

            settingsReader.read(Keys.SUBTITLE_FONT_SIZE_KEY).run run@{
                if (isNotEmpty()) this
                else "18"
            }.toInt(),
            settingsReader.read(Keys.SUBTITLE_FONT_COLOR_KEY).run run@{
                if (isNotEmpty()) this.toInt()
                else null
            },
            null
        )
    }
}

typealias SubtitleOptionsReadType = Readable.Suspendable<@JvmSuppressWildcards SubtitleOptionsEntity>