package me.pitok.networking

sealed class CommonExceptions: Throwable() {
    object UnAuthenticatedException : CommonExceptions()
    object ConnectionException : CommonExceptions()
}