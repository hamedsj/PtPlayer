package me.pitok.subtitle.di

import dagger.Component
import me.pitok.dependencyinjection.library.LibraryScope
import me.pitok.subtitle.SubtitleReaderType

@LibraryScope
@Component(
    modules = [SubtitleDataSourceModule::class]
)
interface SubtitleComponent{
    fun getSubtitleReader(): SubtitleReaderType
}