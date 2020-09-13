package me.pitok.mvi

interface MviView<iState: MviState> {
    fun render(state: iState)
}