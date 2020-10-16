package me.pitok.options.entity

sealed class SubtitleOptionsToWriteEntity{
    class FontSizeOption(val size: Int): SubtitleOptionsToWriteEntity()
    class FontColorOption(val color: Int): SubtitleOptionsToWriteEntity()
    class HighlightColorOption(val color: Int): SubtitleOptionsToWriteEntity()
}