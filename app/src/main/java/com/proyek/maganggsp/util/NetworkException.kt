package com.proyek.maganggsp.util

sealed class NetworkException : Exception() {
    object Connection : NetworkException()
    object Timeout : NetworkException()
    object UnknownHost : NetworkException()
    data class Server(override val message: String) : NetworkException()
    data class ClientError(override val message: String) : NetworkException()
    data class Unknown(override val message: String) : NetworkException()
}
