package com.proyek.maganggsp.data.repository

import com.proyek.maganggsp.util.NetworkException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Extension function untuk mengkonversi throwable ke NetworkException
 */
fun Throwable.toNetworkException(): NetworkException {
    return when (this) {
        is ConnectException -> NetworkException.Connection
        is SocketTimeoutException -> NetworkException.Timeout
        is UnknownHostException -> NetworkException.UnknownHost
        is HttpException -> {
            when (code()) {
                in 500..599 -> NetworkException.Server(message())
                in 400..499 -> NetworkException.ClientError(message())
                else -> NetworkException.Unknown(message())
            }
        }
        else -> NetworkException.Unknown(message ?: "Unknown error occurred")
    }
}
