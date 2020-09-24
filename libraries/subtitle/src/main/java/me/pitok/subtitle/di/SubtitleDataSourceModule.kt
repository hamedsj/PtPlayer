package me.pitok.subtitle.di

import dagger.Binds
import dagger.Module
import me.pitok.dependencyinjection.library.LibraryScope
import me.pitok.subtitle.datasource.SubtitleReader
import me.pitok.subtitle.datasource.SubtitleReaderType

@Module
interface SubtitleDataSourceModule {

    @Binds
    @LibraryScope
    fun provideSubtitleReader(subtitleReader: SubtitleReader): SubtitleReaderType

}