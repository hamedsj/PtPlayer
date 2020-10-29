package me.pitok.options.datasource

import me.pitok.datasource.Writable
import me.pitok.options.Keys
import me.pitok.options.entity.PlayerOptionsToWriteEntity
import me.pitok.sharedpreferences.StoreModel
import me.pitok.sharedpreferences.di.qulifiers.SettingsSP
import me.pitok.sharedpreferences.typealiases.SpWriter
import javax.inject.Inject

class PlayerOptionsWriter @Inject constructor(
    @SettingsSP private val settingsWriter: SpWriter
): PlayerOptionsWriteType {

    override suspend fun write(input: PlayerOptionsToWriteEntity) {

        when(input){
            is PlayerOptionsToWriteEntity.DefaultPlaybackSpeedOption -> {
                settingsWriter.write(
                    StoreModel(Keys.PLAYER_DEFAULT_SPEED_KEY,input.deafultSpeed.toString())
                )
            }
            is PlayerOptionsToWriteEntity.DefaultSpeakerVolumeOption -> {
                settingsWriter.write(
                    StoreModel(
                        Keys.PLAYER_DEFAULT_SPEAKER_VOLUME_KEY,
                        input.defaultSpeakerVolume.toString()
                    )
                )
            }
            is PlayerOptionsToWriteEntity.DefaultLayoutOrientationOption -> {
                settingsWriter.write(
                    StoreModel(
                        Keys.PLAYER_DEFAULT_LAYOUT_ORIENTATION_KEY,
                        input.orientation.toString()
                    )
                )
            }
        }

    }
}

typealias PlayerOptionsWriteType =
        Writable.Suspendable<@JvmSuppressWildcards PlayerOptionsToWriteEntity>