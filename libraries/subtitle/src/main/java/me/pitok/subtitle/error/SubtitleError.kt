package me.pitok.subtitle.error

sealed class SubtitleError : Throwable() {
    object SubtitleFileNotFound: SubtitleError()
    object ReadingSubtitleFileError : SubtitleError()
}