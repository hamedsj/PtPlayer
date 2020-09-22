package me.pitok.subtitle

sealed class SubtitleError : Throwable() {
    object SubtitleFileNotFound: SubtitleError()
    object ReadingSubtitleFileError : SubtitleError()
}