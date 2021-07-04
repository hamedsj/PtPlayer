package me.pitok.videoplayer.states

sealed class SubtitleState {
    object Clear : SubtitleState()
    class Show(val subText: String) : SubtitleState()
    object SubtitleNotFoundError : SubtitleState()
    object SubtitleReadingError : SubtitleState()
}