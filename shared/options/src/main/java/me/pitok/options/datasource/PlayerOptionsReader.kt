package me.pitok.options.datasource

import me.pitok.datasource.Readable
import me.pitok.options.Keys
import me.pitok.options.entity.PlayerOptionsEntity
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP
import me.pitok.sharedpreferences.typealiases.SpReader
import javax.inject.Inject

class PlayerOptionsReader @Inject constructor(
    @SettingsSP private val settingsReader: SpReader
): PlayerOptionsReadType {
    override suspend fun read(): PlayerOptionsEntity {
        return PlayerOptionsEntity(

            settingsReader.read(Keys.PLAYER_DEFAULT_SPEED_KEY).run run@{
                if (isNotEmpty()) this
                else "1.0"
            }.toFloat(),

            settingsReader.read(Keys.PLAYER_DEFAULT_SPEAKER_VOLUME_KEY).run run@{
                if (isNotEmpty()) this
                else "-1.0"
            }.toFloat().run {
                when {
                    this > 1f -> 1f
                    else -> this
                }
            },

            (settingsReader.read(Keys.PLAYER_DEFAULT_LAYOUT_ORIENTATION_KEY).run run@{
                if (isNotEmpty()) this
                else "1"
            } != "0")
            )
    }
}

typealias PlayerOptionsReadType = Readable.Suspendable<@JvmSuppressWildcards PlayerOptionsEntity>