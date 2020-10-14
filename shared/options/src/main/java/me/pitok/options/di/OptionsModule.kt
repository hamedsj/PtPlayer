package me.pitok.options.di

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.shared.SharedScope
import me.pitok.options.datasource.PlayerOptionsReadType
import me.pitok.options.datasource.PlayerOptionsReader

@Module
interface OptionsModule{

    @Binds
    @SharedScope
    fun providePlayerOptionsReader(
        playerOptionsReaderImpl: PlayerOptionsReader
    ): PlayerOptionsReadType

}