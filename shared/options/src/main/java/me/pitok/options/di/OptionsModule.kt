package me.pitok.options.di

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.shared.SharedScope
import me.pitok.options.datasource.*

@Module
interface OptionsModule{

    @Binds
    @SharedScope
    fun providePlayerOptionsReader(
        playerOptionsReaderImpl: PlayerOptionsReader
    ): PlayerOptionsReadType

    @Binds
    @SharedScope
    fun providePlayerOptionsWriter(
        playerOptionsWriterImpl: PlayerOptionsWriter
    ): PlayerOptionsWriteType

    @Binds
    @SharedScope
    fun provideSubtitleOptionsReader(
        subtitleOptionsReaderImpl: SubtitleOptionsReader
    ): SubtitleOptionsReadType

    @Binds
    @SharedScope
    fun provideSubtitleOptionsWriter(
        subtitleOptionsWriterImpl: SubtitleOptionsWriter
    ): SubtitleOptionsWriteType


}